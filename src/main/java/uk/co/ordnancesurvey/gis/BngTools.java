package uk.co.ordnancesurvey.gis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BngTools {

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

    public static Point parseGridReference(String gridRefIn) {
        Pattern pattern = Pattern.compile("^(\\w\\w)(\\d{0,10})$");
        Matcher matcher = pattern.matcher(gridRefIn.toUpperCase().replace(" ",""));
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
