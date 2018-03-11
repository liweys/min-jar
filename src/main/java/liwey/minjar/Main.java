package liwey.minjar;

import java.io.File;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            usage();
            return;
        }

        JarMinimizer parser = new JarMinimizer();
        parser.parse(args[0]);
        String dest = args[1].replace(".jar",".min.jar");
        parser.shrink(args[1], dest);
        File srcFile = new File(args[1]);
        File destFile = new File(dest);
        System.out.println("Shrunk size from " + srcFile.length() + " to " + destFile.length());
    }

    private static void usage(){
        System.out.println("Usage: java -cp . liwey.minjar.Main verboseLogFilePath srcShadedJarFilePath");
    }
}

