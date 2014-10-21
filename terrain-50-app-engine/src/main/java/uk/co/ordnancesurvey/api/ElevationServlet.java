package uk.co.ordnancesurvey.api;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.*;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.impl.appengine.ElevationServiceImpl;

public class ElevationServlet extends HttpServlet {

    private static ElevationService sElevationService = new ElevationServiceImpl();
    private static Gson mGson = new GsonBuilder().setPrettyPrinting().create();

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
            String elevation = sElevationService.getElevation(easting, northing);

            String json = JsonDataV1.export(elevation);
            resp.setContentType("application/json");
            resp.getWriter().println(json);
        }
    }
}
