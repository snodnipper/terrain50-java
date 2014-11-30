package uk.co.ordnancesurvey.api;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.*;

import uk.co.ordnancesurvey.api.appengine.AppEngineMemCacheProvider;
import uk.co.ordnancesurvey.elevation.Configuration;
import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.ElevationServiceProvider;
import uk.co.ordnancesurvey.elevation.SpatialReference;

public class ElevationServlet extends HttpServlet {

    private static final ElevationService sElevationService;
    private static final Gson mGson = new GsonBuilder().setPrettyPrinting().create();

    static {
        AppEngineMemCacheProvider appEngineMemCacheProvider = new AppEngineMemCacheProvider();
        Configuration configuration = new Configuration.Builder()
                .concurrentFileRequests(50)
                .setPrimaryCache(appEngineMemCacheProvider)
                .setSecondaryCache(appEngineMemCacheProvider)
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
        }
    }
}
