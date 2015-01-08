package uk.co.ordnancesurvey.api;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.simple.JSONArray;

import javax.servlet.http.*;

import uk.co.ordnancesurvey.api.appengine.AppEngineMemCacheProvider;
import uk.co.ordnancesurvey.elevation.Configuration;
import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.ElevationServiceProvider;
import uk.co.ordnancesurvey.elevation.SpatialReference;
import uk.co.ordnancesurvey.elevation.Strategy;

public class ElevationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ElevationServlet.class.getName());

    private static final ElevationService sElevationService;
    private static final Gson mGson = new GsonBuilder().setPrettyPrinting().create();

    static {
        AppEngineMemCacheProvider appEngineMemCacheProvider = new AppEngineMemCacheProvider();
        Configuration configuration = new Configuration.Builder()
                .setStrategy(Strategy.MAX_PERFORMANCE)
                .setPrimaryCache(appEngineMemCacheProvider)
                .setSecondaryCache(appEngineMemCacheProvider)
                .terrain50DataUrl("https://github.com/snodnipper/terrain50-java/raw/master/data/")
                .build();
        sElevationService = ElevationServiceProvider.getInstance(configuration);
    }

    public static void main(String args[]) {
    }

    private static class JsonDataV1 {
        public static String export(String elevation) {
            Map<String, Object> json = new LinkedHashMap<String, Object>();
            json.put("elevation", elevation);
            return mGson.toJson(json);
        }
        public static String exportValues(String[] elevationValues) {
            Map<String, Object> json = new LinkedHashMap<String, Object>();
            JSONArray jsonArray = new JSONArray();
            for (String value : elevationValues) {
                jsonArray.add(value);
            }
            json.put("results", jsonArray);
            return mGson.toJson(json);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (req.getParameterMap().containsKey("easting")
                && req.getParameterMap().containsKey("northing")) {
            String easting = req.getParameter("easting");
            String northing = req.getParameter("northing");
            String elevation = sElevationService.getElevation(SpatialReference.EPSG_27700, easting,
                    northing);

            String json = JsonDataV1.export(elevation);
            resp.setContentType("application/json");
            resp.getWriter().println(json);
        } else if (req.getParameterMap().containsKey("latitude")
                && req.getParameterMap().containsKey("longitude")) {
            String latitude = req.getParameter("latitude");
            String longitude = req.getParameter("longitude");
            String elevation = sElevationService.getElevation(latitude, longitude);

            String json = JsonDataV1.export(elevation);
            resp.setContentType("application/json");
            resp.getWriter().println(json);
        } else if (req.getParameterMap().containsKey("locations")) {
            String spatialReference = SpatialReference.EPSG_4326;

            if (req.getParameterMap().containsKey("srid")) {
                spatialReference = req.getParameter("srid");
            }

            String input = req.getParameter("locations");
            String[] locations = input.split("\\|");
            String[] results = new String[locations.length];

            Pattern pattern = Pattern.compile("^(.*)[,](.*)$");
            for (int i = 0; i < locations.length; i++) {
                String location = locations[i];
                    Matcher matcher = pattern.matcher(location);
                    if (!matcher.find()) {
                        LOGGER.log(Level.INFO, "Ignoring: " + location);
                        throw new IllegalArgumentException("unsupported: " + location);
                    }

                    String result;
                    if (spatialReference.equals(SpatialReference.EPSG_4326)) {
                        double x = Double.parseDouble(matcher.group(2));
                        double y = Double.parseDouble(matcher.group(1));
                        result = sElevationService.getElevation(spatialReference, x, y);

                    } else {
                        double x = Double.parseDouble(matcher.group(1));
                        double y = Double.parseDouble(matcher.group(2));
                        result = sElevationService.getElevation(spatialReference, x, y);
                    }
                    results[i] = result;
            }
            String json = JsonDataV1.exportValues(results);
            resp.setContentType("application/json");
            resp.getWriter().println(json);
        }
    }
}
