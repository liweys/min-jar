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

    /**
     * Remove classes which are not used.
     *
     * @param verboseFilePath Full path name of the output file of verbose:class.
     * @param destJarPath     Full path name of the destination minimized jar file.
     * @throws IOException File exceptions.
     */
    public void shrink(String verboseFilePath, String destJarPath) throws IOException {
        parseVerboseClassInfo(verboseFilePath);
        System.out.println(usedClasses.size() + " classes are used in the following jars:");
        usedJars.forEach(x -> System.out.println(x));

        findDuplicateEntries();
        System.out.println("Following files will be merged:");
        duplicateEntries.forEach(x -> System.out.println(x));

        addUsedEntriesInAllJars(destJarPath);
        System.out.println("\nGenerated minimized jar file " + destJarPath);
    }

    /**
     * Parse verbose information and generate the used classes and jars list.
     *
     * @param verboseFilePath Full path name of the output file of verbose:class.
     * @throws IOException File exceptions.
     */
    private void parseVerboseClassInfo(String verboseFilePath) throws IOException {
        usedJars.clear();
        usedClasses.clear();
        List<String> lines = Files.readAllLines(Paths.get(verboseFilePath));
        lines.forEach(line -> {
            int pos = line.indexOf("from file");
            if (pos < 0) return;
            String jarName = line.substring(pos + 11, line.length() - 1);
            if (jarName.contains("/jre") || jarName.contains("\\jre") || !jarName.endsWith(".jar"))
                return;
            usedJars.add(jarName);
            String className = line.substring(8, pos - 1);
            usedClasses.add(className.replace('.', '/') + ".class");
        });
    }

    /**
     * Find entries whose name occurred more than once in all jars.
     *
     * @throws IOException File exceptions.
     */
    private void findDuplicateEntries() throws IOException {
        TreeSet<String> entries = new TreeSet<>();
        for (String usedJar : usedJars) {
            ZipFile zipFile = new ZipFile(usedJar);
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
     * @param destJarPath     Full path name of the destination minimized jar file.
     * @throws IOException File exceptions.
     */
    private void addUsedEntriesInAllJars(String destJarPath) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destJarPath), Charset.forName("UTF8"));
        zipOut.setLevel(9);
        zipOut.setMethod(ZipOutputStream.DEFLATED);
        for (String usedJar : usedJars) {
            try {
                addUsedClassesInJar(usedJar, zipOut);
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
     * @param usedJar  Source jar file path name.
     * @param zipOut The destination zip file's out stream.
     * @throws IOException
     */
    private void addUsedClassesInJar(String usedJar, ZipOutputStream zipOut) throws IOException {
        ZipFile zipFile = new ZipFile(usedJar);
        Enumeration enumeration = zipFile.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry inEntry = (ZipEntry) enumeration.nextElement();
            if (inEntry.isDirectory()) continue;

            String path = inEntry.getName();
            //remove unused classes
            if (path.endsWith(".class") && !usedClasses.contains(path)) {
                continue;
            }

            InputStream zipIn = zipFile.getInputStream(inEntry);

            if (!duplicateEntries.contains(path)) {
                ZipEntry outEntry = new ZipEntry(path);
                zipOut.putNextEntry(outEntry);
                byte[] data = new byte[1024];
                int length;
                while ((length = zipIn.read(data)) > 0) {
                    zipOut.write(data, 0, length);
                }
            } else {
                //merge entries (expected to be text files, otherwise will be broken)
                String text = read(zipIn, (int) inEntry.getSize());
                if (!mergedEntries.containsKey(path))
                    mergedEntries.put(path, text);
                else
                    mergedEntries.put(path, mergedEntries.get(path) + "\n" + text);
            }
            zipIn.close();
        }
    }

    /**
     * Append merged entries (expected to be text files).
     *
     * @param item
     * @param zipOut
     * @throws IOException
     */
    private void appendMergedEntry(Map.Entry<String, String> item, ZipOutputStream zipOut) throws IOException {
        ZipEntry outEntry = new ZipEntry(item.getKey());
        zipOut.putNextEntry(outEntry);
        zipOut.write(item.getValue().getBytes());
        outEntry.setSize(item.getValue().getBytes().length);
        zipOut.closeEntry();
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
        in.read(buf);
        return new String(buf);
    }
}
