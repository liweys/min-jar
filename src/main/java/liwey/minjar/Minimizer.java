package liwey.minjar;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Minimizer {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            usage();
            return;
        }

        Minimizer minimizer = new Minimizer();
        minimizer.shrink(args[0], args[1]);
    }

    private static void usage() {
        System.out.println("Usage: java -jar min-jar.jar verboseOutputPath destJarPath\nE.g.:  java -jar min-jar.jar d:/temp/verbose.txt d:/temp/dest-min.jar");
    }

    /* used jars and classes */
    private TreeSet<String> usedJars = new TreeSet<>();
    private TreeSet<String> usedClasses = new TreeSet<>();

    /* entries whose name occurred more than once will be merged (expected to be text files) */
    private TreeSet<String> duplicateEntries = new TreeSet<>();
    private HashMap<String, String> mergedEntries = new HashMap<>();

    private String dest;

    /**
     * Remove classes which are not used.
     *
     * @param verboseFilePath Full path name of the output file of verbose:class.
     * @param destJarPath     Full path name of the destination minimized jar file.
     * @throws IOException File exceptions.
     */
    public void shrink(String verboseFilePath, String destJarPath) throws IOException {
        this.dest = destJarPath;
        parseVerbose(Files.readAllLines(Paths.get(verboseFilePath)));
        System.out.println(usedClasses.size() + " classes are used in the following jars:");
        usedJars.forEach(x -> System.out.println(x));

        findDuplicates();
        System.out.println("Following files will be merged:");
        duplicateEntries.forEach(x -> System.out.println(x));

        shrinkJars();
        System.out.println("\nGenerated minimized jar file " + destJarPath);
    }

    /**
     * Parse verbose information and generate the used classes and jars list.
     *
     * @param lines
     * @throws IOException
     */
    private void parseVerbose(List<String> lines) throws IOException {
        usedJars.clear();
        usedClasses.clear();
        lines.forEach(line -> {
            int p1 = line.indexOf("from file");
            if (p1 < 0) return;
            String jarName = line.substring(p1 + 11, line.length() - 1);
            if (jarName.contains("/jre") || jarName.contains("\\jre") || !jarName.endsWith(".jar"))
                return;
            usedJars.add(jarName);
            String className = line.substring(8, p1 - 1);
            usedClasses.add(className.replace('.', '/') + ".class");
        });
    }

    /**
     * Find entries whose name occurred more than once.
     *
     * @throws IOException
     */
    private void findDuplicates() throws IOException {
        TreeSet<String> entries = new TreeSet<>();
        for (String src : usedJars) {
            ZipFile zipFile = new ZipFile(src);
            Enumeration enumeration = zipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry inEntry = (ZipEntry) enumeration.nextElement();
                if (inEntry.isDirectory()) continue;
                String name = inEntry.getName();
                if (!entries.contains(name))
                    entries.add(name);
                else
                    duplicateEntries.add(name);
            }
        }
    }

    /**
     * Shrink all jars in the class path and being used.
     *
     * @throws IOException
     */
    private void shrinkJars() throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(dest), Charset.forName("UTF8"));
        zipOut.setLevel(9);
        zipOut.setMethod(ZipOutputStream.DEFLATED);
        for (String src : usedJars) {
            try {
                shrinkJar(src, zipOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<String, String> entry : mergedEntries.entrySet()) {
            appendMergedEntry(entry, zipOut);
        }

        zipOut.flush();
        zipOut.close();
    }

    /**
     * Shrink one jar.
     *
     * @param src  Source jar file path name.
     * @param zout The destination zip file's out stream.
     * @throws IOException
     */
    private void shrinkJar(String src, ZipOutputStream zout) throws IOException {
        ZipFile zipFile = new ZipFile(src);
        Enumeration enumeration = zipFile.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry inEntry = (ZipEntry) enumeration.nextElement();
            if (inEntry.isDirectory()) continue;

            String path = inEntry.getName();
            //remove unused classes
            if (path.endsWith(".class") && !usedClasses.contains(path)) {
                continue;
            }

            InputStream zin = zipFile.getInputStream(inEntry);

            if (!duplicateEntries.contains(path)) {
                ZipEntry outEntry = new ZipEntry(path);
                zout.putNextEntry(outEntry);
                byte[] data = new byte[1024];
                int length;
                while ((length = zin.read(data)) > 0) {
                    zout.write(data, 0, length);
                }
            } else {
                //merge entries (expected to be text files, otherwise will be broken)
                String text = read(zin, (int) inEntry.getSize());
                if (!mergedEntries.containsKey(path))
                    mergedEntries.put(path, text);
                else
                    mergedEntries.put(path, mergedEntries.get(path) + "\n" + text);
            }
            zin.close();
        }
    }

    /**
     * Append merged entries (expected to be text files).
     *
     * @param item
     * @param zout
     * @throws IOException
     */
    private void appendMergedEntry(Map.Entry<String, String> item, ZipOutputStream zout) throws IOException {
        ZipEntry outEntry = new ZipEntry(item.getKey());
        zout.putNextEntry(outEntry);
        zout.write(item.getValue().getBytes());
        outEntry.setSize(item.getValue().getBytes().length);
        zout.closeEntry();
    }

    /**
     * Read contents of a text entry.
     *
     * @param in   Input entry.
     * @param size Size of the entry.
     * @return Text content.
     * @throws IOException
     */
    private static String read(InputStream in, int size) throws IOException {
        byte[] buf = new byte[size];
        int r = in.read(buf);
        return new String(buf);
    }
}
