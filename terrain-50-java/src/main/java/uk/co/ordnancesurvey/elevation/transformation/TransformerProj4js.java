package uk.co.ordnancesurvey.elevation.transformation;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import hello.HelloTerrain50;

public abstract class TransformerProj4js implements Transformer {

    private static final Logger LOGGER = Logger.getLogger(TransformerProj4js.class.getName());
    private static final String SCRIPT = getProj4Js();

    private final String mSrid;
    private final String mSridDefinition;
    private final String mJavaScriptDefinition;

    /**
     * @param srid e.g. 'EPSG:27700'
     * @param sridDefinition '+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +datum=OSGB36 +units=m +no_defs'
     */
    public TransformerProj4js(String srid, String sridDefinition) {
        mSrid = srid;
        mSridDefinition = sridDefinition;
        mJavaScriptDefinition = "proj4.defs('" + mSrid + "', '" + sridDefinition + "');";
    }

    @Override
    public abstract boolean validFor(double latitude, double longitude);

    @Override
    public String getSpatialReference() {
        return mSrid;
    }

    @Override
    public double[] transform(double latitude, double longitude) {
        Context context = Context.enter();
        try {
            ScriptableObject scope = context.initStandardObjects();

            // load proj4js script
            context.evaluateString(scope, SCRIPT, "script", 1, null);

            // add the transformation string
            context.evaluateString(scope, mJavaScriptDefinition, "sourceName", 0, null);

            Function fct = (Function)scope.get("proj4", scope);

            NativeArray nativeArray = new NativeArray(new Object[]{longitude, latitude});

            Object result = fct.call(
                    context, scope, scope, new Object[] {mSrid, nativeArray});
            return convert(result);
        } finally {
            Context.exit();
        }
    }

    private static double[] convert(Object result) {
        NativeArray nativeArray = (NativeArray) result;
        Object x = nativeArray.get(0);
        Object y = nativeArray.get(1);
        return new double[]{(Double)x, (Double)y};
    }

    private static String getProj4Js() {
        StringBuffer sb = new StringBuffer();

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(HelloTerrain50.class.getResourceAsStream("/proj4.js"),
                            "UTF-8"));
            for (int c = br.read(); c != -1; c = br.read()) sb.append((char)c);
        } catch (Exception lazy) {
            LOGGER.log(Level.SEVERE, "error reading proj4.js", lazy);
        }
        return sb.toString();
    }
}
