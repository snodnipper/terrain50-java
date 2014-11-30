package uk.co.ordnancesurvey.elevation.provider.epsg27700.gis;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Consider renaming DataAdapter or something
 * See: https://en.wikipedia.org/wiki/Esri_grid
 */
public class EsriAsciiGrid {
    private static final Logger LOGGER = Logger.getLogger(EsriAsciiGrid.class.getName());

    private static final int HEADER = 5;

    public static String getValue(String easting, String northing,
                                   File zippedAsciiGrid) throws IOException {
        return getValue(easting, northing, getAsciiGrid(zippedAsciiGrid));
    }

    public static String getValue(String easting, String northing, String asciiGrid) {

        if (asciiGrid == null || asciiGrid.isEmpty()) {
            return "";
        }

        int myLineNumber  =
                (Integer.parseInt(northing.substring(Math.max(0, northing.length() - 4),
                        northing.length())) / 50);
        int lineNumber = 200 - myLineNumber + HEADER;

        int wordNumber = Integer.parseInt(easting.substring(Math.max(0, easting.length() - 4),
                easting.length())) / 50;

        String[] lines = asciiGrid.split("\n");
        String line = lines[lineNumber - 1];

        String[] words = line.split(" ");

        String word = words[wordNumber];
        return word;
    }

    public static String getAsciiGrid(final byte[] input) throws IOException {

        LOGGER.log(Level.INFO, "Size: " + input.length + " bytes.");

        String result = "";
        ByteArrayInputStream bis = new ByteArrayInputStream(input);

        ZipInputStream stream = new ZipInputStream(bis);

        // create a buffer to improve copy performance later.
        byte[] buffer = new byte[2048];

        try {

            // now iterate through each item in the stream. The get next
            // entry call will return a ZipEntry for each file in the
            // stream
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                String filename = entry.getName();
                Pattern pattern = Pattern.compile(".*[.]asc$");
                Matcher matcher = pattern.matcher(filename);
                if (!matcher.find()) {
                    LOGGER.log(Level.INFO, "Ignoring: " + filename);
                    continue;
                }

                String s = String.format("entry: %s len %d added %TD",
                        entry.getName(), entry.getSize(),
                        new Date(entry.getTime()));
                LOGGER.log(Level.INFO, s);

                // Once we get the entry from the stream, the stream is
                // positioned read to read the raw data, and we keep
                // reading until read returns 0 or less.
                ByteArrayOutputStream output = null;
                try {
                    output = new ByteArrayOutputStream();
                    int len = 0;
                    while ((len = stream.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                    result = output.toString();
                    break;
                } finally {
                    // we must always close the output file
                    if (output != null) output.close();
                }
            }
        } finally {
            // we must always close the zip file.
            stream.close();
        }
        LOGGER.log(Level.INFO, "ASCII size: " + result.getBytes().length + " bytes");
        return result;
    }

    /**
     * @param zipFile containing asc file
     * @return ASCII grid file
     * @throws java.io.IOException
     */
    private static String getAsciiGrid(File zipFile) throws IOException {

        if (!zipFile.exists()) {
            throw new IOException("cannot find file: " + zipFile.getPath());
        }
        final ZipFile file = new ZipFile(zipFile);

        LOGGER.log(Level.INFO, "Using: " + zipFile.getAbsolutePath());

        try {
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();

                String filename = entry.getName();

                Pattern pattern = Pattern.compile(".*[.]asc$");
                Matcher matcher = pattern.matcher(filename);
                if (matcher.find()) {
                    LOGGER.log(Level.INFO, "Found it: " + filename);

                    InputStream inputStream = file.getInputStream(entry);
                    BufferedInputStream bis = new BufferedInputStream(inputStream);

                    //
                    BufferedReader br = null;
                    StringBuilder sb = new StringBuilder();

                    String line;
                    try {

                        br = new BufferedReader(new InputStreamReader(bis));
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                            sb.append(System.getProperty("line.separator"));
                        }

                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "error building string from zip entry", e);
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e) {
                                LOGGER.log(Level.SEVERE, "error closing zip buffered reader", e);
                            }
                        }
                    }
                    return sb.toString();
                } else {
                    continue;
                }
            }
        } finally {
            file.close();
        }
        return "";
    }
}
