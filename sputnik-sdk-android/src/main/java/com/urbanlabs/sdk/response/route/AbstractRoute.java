package com.urbanlabs.sdk.response.route;

import android.util.Log;
import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.util.LatLon;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by днс on 20.02.2015.
 */
public abstract class AbstractRoute {
    protected String distanceText_;
    protected long distanceVal_;
    protected String durationText_;
    protected long durationVal_;
    protected LatLon startLocation_;
    protected LatLon endLocation_;

    public AbstractRoute(JSONObject root) throws Exception {
        readDistance(root.getJSONObject(TUtil.R_DISTANCE));
        readDuration(root.getJSONObject(TUtil.R_DURATION));
        startLocation_ = readLoc(root.getJSONArray(TUtil.R_START_LOC));
        endLocation_ = readLoc(root.getJSONArray(TUtil.R_END_LOC));
    }

    /**
     *
     * @param root
     * @throws Exception
     */
    protected void readDistance(JSONObject root) throws Exception{
        distanceText_ = TUtil.getString(TUtil.TEXT, root);
        distanceVal_ = TUtil.getLong(TUtil.VALUE, root);
    }

    /**
     *
     * @param root
     * @throws Exception
     */
    protected void readDuration(JSONObject root) throws Exception{
        durationText_ = TUtil.getString(TUtil.TEXT, root);
        durationVal_ = TUtil.getLong(TUtil.VALUE, root);
    }
    /**
     *
     * @param root
     * @throws Exception
     */
    protected LatLon readLoc(JSONArray root) throws Exception{
        double[] coord = TUtil.readDoubleArray(root);
        if(coord.length == 2) {
            return new LatLon(coord[0], coord[1]);
        } else {
            Log.e("[AbstractRoute]", "Failed to read location");
        }
        return null;
    }
}
