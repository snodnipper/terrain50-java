package uk.co.ordnancesurvey.elevation.android;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

class NetworkManager {

    private static final String ENDPOINT =
            "https://github.com/snodnipper/terrain50-java/raw/master/data/";

    private final File mTempDirectory;

    public NetworkManager(File downloadFile) {
        mTempDirectory = downloadFile;
    }

    /**
     * Uses the first two characters as a subdirectory - very impl
     */
    public static void download(File file) throws IOException {
        String url = ENDPOINT + file.getName().substring(0, 2) + "/" + file.getName();
        download2(file, url);
    }

    private static void download2(final File file, final String urlString)
            throws IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new FileOutputStream(file);

            final byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
    }
}
