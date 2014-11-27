package uk.co.ordnancesurvey.elevation.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class NetworkManager {

    private static String mTerrain50DataEndpoint;

    public NetworkManager(String terrain50DataEndpoint) {
        mTerrain50DataEndpoint = terrain50DataEndpoint;
    }

    /**
     * Uses the first two characters as a subdirectory - very internals
     */
    public static byte[] download(File file) throws IOException {
        String url = mTerrain50DataEndpoint + file.getName().substring(0, 2) + "/" + file.getName();
        return download2(url);
    }

    private static byte[] download2(final String urlString)
            throws IOException {
        byte[] result;
        BufferedInputStream in = null;
        ByteArrayOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new ByteArrayOutputStream();

            final byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
            result = fout.toByteArray();
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
        return result;
    }
}
