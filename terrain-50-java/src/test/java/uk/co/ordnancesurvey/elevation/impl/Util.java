package uk.co.ordnancesurvey.elevation.impl;

import java.io.File;

public class Util {

    public static void clearDownloadDirectory() {
        File directory = new File(System.getProperty("java.io.tmpdir") +
                File.separator + "os-elevation-cache");

        // delete downloaded files within top level directory
        File[] files = directory.listFiles();
        if(files != null) {
            for(File f: files) {
                if(f.isFile()) {
                    f.delete();
                }
            }
        }
    }
}
