package uk.co.ordnancesurvey.elevation;

    import java.io.BufferedInputStream;
    import java.io.BufferedReader;
    import java.io.File;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.net.URL;
    import java.util.Enumeration;
    import java.util.LinkedHashMap;
    import java.util.Map;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;
    import java.util.zip.ZipEntry;
    import java.util.zip.ZipFile;

public class ElevationServiceImpl {

    private final CacheManager mCacheManager = new CacheManager();
    private final File mCacheDirectory;

    public ElevationServiceImpl() {
        mCacheDirectory = new File(System.getProperty("java.io.tmpdir"));
    }

    public String getAltitude(double eastings, double northings) {
        // TODO: add functionality
        throw new UnsupportedOperationException("Please add this functionality");
    }

    /**
     * @param gridReference e.g. TQ 9123 9678
     * @return a String value of the altitude
     */
    public String getAltitude(String gridReference) {
        Point point = BngTools.parseGridReference(gridReference);
        return getAltitude(point.getX(), point.getY());
    }

    public String getAltitude(String easting, String northing) {
        // TODO: validate easting and northing values
        return mCacheManager.getAltitude(easting, northing);
    }

    private interface AltitudeProvider {
        String getAltitude(String easting, String northing);
    }

    private class CacheManager implements AltitudeProvider {

        private static final int MAX_CACHE_SIZE = 100;
        Map<String, String> mMap = new MaxSizeHashMap<String, String>(MAX_CACHE_SIZE);
        AltitudeProvider mFileManager = new FileManager();

        public String getAltitude(String easting, String northing) {
            String key = easting + northing;
            if (mMap.containsKey(key)) {
                return mMap.get(key);
            }
            return mFileManager.getAltitude(easting, northing);
        }

        public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
            private final int mMaxSize;

            public MaxSizeHashMap(int maxSize) {
                mMaxSize = maxSize;
            }

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > mMaxSize;
            }
        }
    }

    private class FileManager implements AltitudeProvider {

        NetworkManager mNetworkManager = new NetworkManager(mCacheDirectory);

        @Override
        public String getAltitude(String easting, String northing) {
            // TODO: validate easting and northing values
            String filePath = getFilename(easting, northing);
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    mNetworkManager.download(file);
                }
                return EsriAsciiGrid.getValue(easting, northing, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return String.valueOf(Float.MIN_VALUE);
        }

        private String getFilename(String easting, String northing) {
            String gridRef = BngTools.toGridReference(1, Double.parseDouble(easting),
                    Double.parseDouble(northing)).replaceAll(" ", "");
            String characters = gridRef.substring(0, 2).toLowerCase();
            String x = gridRef.substring(2, 3);
            String y = gridRef.substring(3, 4);
            String name = characters + x + y +  "_OST50GRID_20130611";
            return mCacheDirectory + File.separator + name + ".zip";
        }
    }

    private static class NetworkManager {

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
         * @throws IOException
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

    private static class BngTools {

        private static final String[] NATGRID_LETTERS = {"VWXYZ", "QRSTU", "LMNOP", "FGHJK",
                "ABCDE"};

        /**
         * Return a String containing a National Grid Reference containing two letters and an even
         * number of digits (e.g. SK35)
         * @param digits Number of digits to use for eastings and northings. For example, SK35
         *               contains one digit of eastings and northings.
         * @return OS Grid Reference, as a String
         */
        public static String toGridReference(int digits, double easting, double northing)
        {
            int e = (int) easting;
            int n = (int) northing;
            if (digits < 0) {
                return e + "," + n;
            }
            // We can actually handle negative E and N in the lettered case, but that's more effort.
            if (e < 0 || n < 0) { return null; }

            String ret = "";

            // The following code doesn't correctly handle e<0 or n<0 due to problems with / and %.
            int big = 500000;
            int small = big / 5;
            int firstdig = small / 10;

            int es = e / big;
            int ns = n / big;
            e = e % big;
            n = n % big;
            // move to the S square
            es += 2;
            ns += 1;
            if (es > 4 || ns > 4) { return null; }
            ret = ret + NATGRID_LETTERS[ns].charAt(es);

            es = e / small;
            ns = n / small;
            e = e % small;
            n = n % small;
            ret = ret + NATGRID_LETTERS[ns].charAt(es);

            // Only add spaces if there are digits too. This lets us have "zero-figure" grid
            // references, e.g. "SK"
            if (digits > 0) {
                ret += ' ';

                for (int dig = firstdig, i = 0; dig != 0 && i < digits; i++, dig /= 10) {
                    ret += (e / dig % 10);
                }

                ret += ' ';

                for (int dig = firstdig, i = 0; dig != 0 && i < digits; i++, dig /= 10) {
                    ret += (n / dig % 10);
                }
            }

            return ret;
        }


        private static Point parseGridReference(String gridRefIn) {
            Pattern pattern = Pattern.compile("^(\\w\\w)(\\d{0,10})$");
            Matcher matcher = pattern.matcher(gridRefIn.toUpperCase());
            int iIndex = 7;

            boolean probableGridReference = matcher.matches();

            if (probableGridReference) {
                String characters = matcher.group(1);
                String numbers = matcher.group(2);
                if (numbers.length() % 2 != 0) {
                    numbers += "0";
                }
                // TODO: tidy!!!
                gridRefIn = characters + numbers;

                // get numeric values of letter references, mapping A->0, B->1, C->2, etc:
                int l1 = Character.codePointAt(characters, 0) - Character.codePointAt("A", 0);
                int l2 = Character.codePointAt(characters, 1) - Character.codePointAt("A", 0);

                // shuffle down letters after 'I' since 'I' is not used in grid:
                if (l1 > iIndex) {
                    l1--;
                }
                if (l2 > iIndex) {
                    l2--;
                }

                // convert grid letters into 100km-square indexes from false origin
                // (grid square SV):
                int es = ((l1 - 2) % 5) * 5 + (l2 % 5);
                int ns = (int) ((19 - Math.floor(l1 / 5) * 5) - Math.floor(l2 / 5));
                if (es < 0 || es > 6 || ns < 0 || ns > 12) {
                    return null;
                }

                String e = String.valueOf(es);
                String n = String.valueOf(ns);

                // append numeric part of references to grid index:
                e += numbers.substring(0, numbers.length() / 2);
                n += numbers.substring(numbers.length() / 2);

                // normalise to 1m grid, rounding up to centre of grid square:
                int offset = 0;

                switch (String.valueOf(numbers).length()) {
                    case 0: offset = 50000; break;
                    case 2: offset = 5000; break;
                    case 4: offset = 500; break;
                    case 6: offset = 50; break;
                    case 8: offset = 5; break;
                    case 10: offset = 0; break;
                    default: return null;
                }

                // normalise to 1m grid, rounding up to centre of grid square:
                e += offset;
                n += offset;

                double easting = Double.valueOf(e);
                double northing = Double.valueOf(n);
                return new Point(easting, northing);
            }
            return null;
        }

    }

    private static class Point {

        private final double mX;
        private final double mY;

        public Point(double x, double y) {
            mX = x;
            mY = y;
        }

        public double getX() {
            return mX;
        }

        public double getY() {
            return mY;
        }
    }
}

