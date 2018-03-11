package liwey.minjar;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JarMinimizer {
    private TreeSet<String> usedClasses = new TreeSet<>();

    void parse(String verboseOutFileName) throws IOException {
        usedClasses.clear();
        List<String> lines = Files.readAllLines(Paths.get(verboseOutFileName));
        //[Loaded java.lang.Object from C:\Program Files\Java\jdk1.8.0_144\jre\lib\rt.jar]
        lines.forEach(line -> {
            int p1 = line.indexOf("from file");
            if (p1 < 0) return;
            String jarName = line.substring(p1 + 11, line.length() - 1);
            if (jarName.contains("jdk") || jarName.contains("jre"))
                return;
            String className = line.substring(8, p1 - 1);
            usedClasses.add(className);
        });
        System.out.println(usedClasses.size() + " classes are used:");
        usedClasses.forEach(x->System.out.println(x));
    }

    void shrink(String src, String dest) throws IOException {
        System.out.println();
        System.out.println("generating minimized jar...");

        ZipInputStream zin = new ZipInputStream(new FileInputStream(src), Charset.forName("UTF8"));
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(dest), Charset.forName("UTF8"));
        zout.setLevel(9);
        zout.setMethod(ZipOutputStream.DEFLATED);
        while (true) {
            ZipEntry entry = zin.getNextEntry();
            if (entry == null)
                break;
            if (entry.isDirectory()) continue;

            String path = entry.getName();

            if(path.endsWith(".class")) {
                String className = path.replace('/', '.').substring(0, path.lastIndexOf('.'));
                if (!usedClasses.contains(className))
                    continue;
            }

            ZipEntry t = new ZipEntry(path);
            t.setSize(entry.getSize());
            t.setCrc(entry.getCrc());
            zout.putNextEntry(t);
            pipe(zin, zout);
            zout.closeEntry();
        }

        zout.flush();
        zout.finish();
        zout.flush();
        zout.close();

        System.out.println("generated minimized jar file " +  dest);
    }

    private void pipe(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[2048];
        for (; ; ) {
            int r = in.read(buf);
            if (r == -1)
                break;
            out.write(buf, 0, r);
        }
    }
}
