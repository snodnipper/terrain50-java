package uk.co.ordnancesurvey.elevation.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.gis.BngTools;
import uk.co.ordnancesurvey.gis.Point;

public class ElevationServiceImpl implements ElevationService {

    private final CacheManager mCacheManager = new CacheManager();
    private final File mCacheDirectory;

    public ElevationServiceImpl() {
        mCacheDirectory = new File(System.getProperty("java.io.tmpdir"));
    }

    public String getElevation(double eastings, double northings) {
        return getElevation(String.valueOf(Math.round(eastings)),
                String.valueOf(Math.round(northings)));
    }

    /**
     * @param gridReference e.g. TQ 9123 9678
     * @return a String value of the altitude
     */
    public String getElevation(String gridReference) {
        Point point = BngTools.parseGridReference(gridReference);
        return getElevation(point.getX(), point.getY());
    }

    public String getElevation(String easting, String northing) {
        return mCacheManager.getElevation(easting, northing);
    }


    /**
     * See: https://en.wikipedia.org/wiki/Esri_grid
     */
    private static class EsriAsciiGrid {
        private static final int HEADER = 5;

        private static String getValue(String easting, String northing,
                                       File zippedAsciiGrid) throws IOException {
            return getValue(easting, northing, getAsciiGrid(zippedAsciiGrid));
        }

        private static String getValue(String easting, String northing, String asciiGrid) {

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

            try {
                final Enumeration<? extends ZipEntry> entries = file.entries();
                while (entries.hasMoreElements()) {
                    final ZipEntry entry = entries.nextElement();

                    String filename = entry.getName();

                    Pattern pattern = Pattern.compile(".*[.]asc$");
                    Matcher matcher = pattern.matcher(filename);
                    if (matcher.find()) {
                        System.out.println("Found it: " + filename);

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
                            e.printStackTrace();
                        } finally {
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
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
}

