package com.urbanlabs.sdk.response;
import com.urbanlabs.sdk.dict.TUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Response for nearestneighbor function
 */
public class GraphNearest extends AbstractResponse {
    private String id_;
    private double lat_;
    private double lon_;


    public GraphNearest(JSONObject data) throws JSONException {
        JSONObject result = data.getJSONObject(TUtil.RESPONSE);
        id_ = String.valueOf(result.getLong(TUtil.ID));
        lat_ = result.getDouble(TUtil.LAT);
        lon_ = result.getDouble(TUtil.LON);
    }

    public String id() {
        return id_;
    }

    public double lat() {
        return lat_;
    }

    public double lon() {
        return lon_;
    }
    @Override
    public String toString() {
        return id_ +" "+ String.valueOf(lat_) + " "+String.valueOf(lon_);
    }
}
