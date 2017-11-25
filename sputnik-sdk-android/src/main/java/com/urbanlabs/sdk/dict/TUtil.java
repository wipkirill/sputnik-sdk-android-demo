package com.urbanlabs.sdk.dict;

import android.util.Log;
import com.urbanlabs.sdk.response.SearchResult;
import com.urbanlabs.sdk.util.Bounds;
import com.urbanlabs.sdk.util.LatLon;
import com.urbanlabs.sdk.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A tag util class works with OSM tags.
 */
public class TUtil {
    public static final String LTAG = "[TAGUTIL]";
    public static final String RESPONSE = "response";
    public static final String MAP_FEATURES = "map_feature";
    public static final String MAP_FILE = "mapfile";
    public static final String AREA_NAME = "area_name";
    public static final String COUNTRY_CODES = "country_codes";
    public static final String COMMENT = "comment";
    public static final String MAP_NAME = "mapname";
    public static final String MAP_TYPE = "maptype";
    public static final String ID = "id";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String BBOX = "bbox";

    public static final String SOURCE = "source";
    public static final String QUERY = "q";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String NEAR = "near";
    public static final String TAGS_VALS = "tagsvals";
    public static final String IDS = "ids";
    public static final String TAGS = "tags";
    public static final String TAG = "tag";

    public static final String WAYPOINTS = "waypoints";
    public static final String ZOOM = "zoom";

    // routing
    public static final String TRAVELMODE = "travelmode";
    public static final String METRIC = "metric";
    public static final String BBOX2 = "boundingBox";
    public static final String BBOX_NORTH_WEST = "nw";
    public static final String BBOX_SOUTH_EAST = "se";
    public static final String TEXT = "text";
    public static final String VALUE = "value";
    public static final String R_DISTANCE = "distance";
    public static final String R_DURATION = "duration";
    public static final String R_START_LOC = "start_location";
    public static final String R_END_LOC = "end_location";
    public static final String R_WAY = "wayid";
    public static final String R_INSTRUCTION = "instruction";
    public static final String R_POLYLINE = "polyline";
    public static final String R_STEPS = "steps";
    public static final String R_LEGS = "legs";
    public static final String R_ROUTES = "routes";


    // address
    public static final String[] ADDR_TAGS = {"addr:street",
            "addr:housenumber",
            "addr:suburb",
            "addr:city",
            "addr:country",
            "addr:zip",
            "addr:postcode"};
    public static final String[] ADDR_FIRST = {"addr:street", "addr:housenumber"};
    public static final String[] ADDR_SECOND = {"addr:suburb",
            "addr:city",
            "addr:country",
            "addr:zip",
            "addr:postcode"};
    public static final String OSM_NAME = "name";
    public static final String OSM_TYPE = "osmtype";
    public static final String OSM_NODE = "node";
    public static final String OSM_WAY = "way";
    public static final String OSM_REL = "rel";
    public static final String POINTS = "points";
    public static final String DECODE = "decode";


    /**
     * @param obj
     * @return
     */
    public static boolean hasTags(JSONObject obj) {
        if (obj != null)
            return obj.has(TAGS);
        else
            Log.e(LTAG, "Object is null");
        return false;
    }

    /**
     * @param tag
     * @param obj
     * @return
     */
    public static boolean hasProp(String tag, JSONObject obj) {
        if (obj != null)
            return obj.has(tag);
        else
            Log.e(LTAG, "Object is null");
        return false;
    }

    /**
     * @param name
     * @param obj
     * @return
     * @throws JSONException
     */
    public static String getString(String name, JSONObject obj) throws JSONException {
        if (obj != null && obj.has(name))
            return obj.getString(name);
        Log.e(LTAG, "Object is null or field " + name + " does not exist");
        return null;
    }

    /**
     * @param name
     * @param obj
     * @return
     * @throws JSONException
     */
    public static Long getLong(String name, JSONObject obj) throws JSONException {
        if (obj != null && obj.has(name))
            return obj.getLong(name);
        Log.e(LTAG, "Object is null or field " + name + " does not exist");
        return -1L;
    }

    /**
     * @param obj
     * @return
     * @throws JSONException
     */
    public static double[] readDoubleArray(JSONArray obj) throws JSONException {
        double[] arr = new double[obj.length()];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = obj.getDouble(i);
        }
        return arr;
    }

    /**
     * @param name
     * @param obj
     * @return
     * @throws JSONException
     */
    public static JSONArray getArray(String name, JSONObject obj) throws JSONException {
        if (obj != null && obj.has(name))
            return obj.getJSONArray(name);
        Log.e(LTAG, "Object is null or field " + name + " does not exist");
        return null;
    }

    /**
     * @param obj
     * @return
     * @throws JSONException
     */
    public static Long getId(JSONObject obj) throws JSONException {
        if (obj != null)
            if (obj.has(ID))
                return obj.getLong(ID);
        return -1l;
    }

    /**
     * @param name
     * @param obj
     * @return
     * @throws Exception
     */
    public static Double getDouble(String name, JSONObject obj) throws Exception {
        if (obj != null)
            return obj.getDouble(name);
        return -1.0;
    }

    /**
     * @param obj
     * @return
     * @throws JSONException
     */
    public static JSONObject getTags(JSONObject obj) throws JSONException {
        if (hasTags(obj))
            return obj.getJSONObject(TAGS);
        return null;
    }

    /**
     * @param data
     * @return
     */
    public static boolean hasAddress(SearchResult data) {
        if (data != null)
            for (String addrTag : ADDR_TAGS)
                if (data.getTag(addrTag) != null)
                    return true;
        return false;
    }

    /**
     * @param obj
     * @return
     */
    public static boolean hasCoords(JSONObject obj) {
        return obj != null && obj.has(LAT) && obj.has(LON);
    }

    /**
     * @param data
     * @return
     * @throws JSONException
     */
    public static String extractAddress(SearchResult data) throws Exception {
        List<String> addr = new ArrayList<String>();
        for (String line : ADDR_FIRST) {
            String val = data.getTag(line);
            if (val != null)
                addr.add(StringUtil.toUpperFirst(val));
        }

        for (String line : ADDR_SECOND) {
            String val = data.getTag(line);
            if (val != null)
                addr.add(StringUtil.toUpperFirst(val));
        }

        if (addr.size() > 0) {
            StringBuilder joined = new StringBuilder(addr.get(0));
            for (int i = 1; i < addr.size(); ++i)
                joined.append(", " + addr.get(i));
            return joined.toString();
        }
        return null;
    }

    /**
     * @return
     * @throws JSONException
     */
    public static Set<String> getStringSet(String key, JSONObject object) throws JSONException {
        Set<String> sFeatures = new HashSet<>();
        if (object.has(key)) {
            if(isJSONArray(key, object)) {
                JSONArray features = object.getJSONArray(key);
                for (int i = 0; i < features.length(); ++i)
                    sFeatures.add(features.getString(i));
            } else {
                sFeatures.add(object.getString(key));
            }
        } else {
            Log.e(LTAG, "failed to find key " + key + ", " + object.toString());
        }

        return sFeatures;
    }

    /**
     *
     * @param key
     * @param object
     * @return
     */
    public static boolean isJSONArray(String key, JSONObject object) {
        try {
            JSONArray arr = object.getJSONArray(key);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * @param cTags
     * @return
     */
    public static Bounds getBounds(JSONObject cTags) throws JSONException {
        if (cTags.has(BBOX)) {
            String[] coords = cTags.getString(BBOX).split(" ", -1);
            double lLat = Double.valueOf(coords[0]);
            double lLon = Double.valueOf(coords[1]);
            double rLat = Double.valueOf(coords[2]);
            double rLon = Double.valueOf(coords[3]);
            return new Bounds(new double[][]{{lLat, rLat}, {lLon, rLon}});
        } else {
            Log.e(LTAG, "failed to find bbox property " + cTags.toString());
        }
        return null;
    }

    /**
     * @param data
     * @return
     * @throws Exception
     */
    public static LatLon getLatLon(JSONObject data) throws Exception {
        if (hasProp(TUtil.LAT, data) && hasProp(TUtil.LON, data)) {
            double lat = getDouble(TUtil.LAT, data);
            double lon = getDouble(TUtil.LON, data);
            return new LatLon(lat, lon);
        } else {
            Log.e(LTAG, "Failed to find lat, lon property " + data.toString());
            return null;
        }
    }
}
