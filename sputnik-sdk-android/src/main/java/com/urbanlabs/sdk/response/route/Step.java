package com.urbanlabs.sdk.response.route;

import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.util.LatLon;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by днс on 20.02.2015.
 */
public class Step extends AbstractRoute {
    private String wayIdText_;
    private Long wayId_;
    private String instruction_;
    private List<LatLon> polyline_ = new ArrayList<LatLon>();

    public Step(JSONObject stepRoot) throws Exception{
        super(stepRoot);
        readWay(stepRoot.getJSONObject(TUtil.R_WAY));
        instruction_ = TUtil.getString(TUtil.R_INSTRUCTION, stepRoot);
        readPolyline(stepRoot.getJSONArray(TUtil.R_POLYLINE));
    }

    private void readWay(JSONObject root) throws Exception {
        wayIdText_ = TUtil.getString(TUtil.TEXT, root);
        wayId_ = TUtil.getLong(TUtil.VALUE, root);
    }

    private void readPolyline(JSONArray line) throws Exception {
        for(int i = 0; i < line.length(); ++i) {
            LatLon loc = readLoc(line.getJSONArray(i));;
            if(loc != null)
                polyline_.add(loc);
        }
    }

    public String getWayIdText() {
        return wayIdText_;
    }

    public Long getWayId() {
        return wayId_;
    }

    public String getInstruction() {
        return instruction_;
    }

    public List<LatLon> getPolyline() {
        return polyline_;
    }
}
