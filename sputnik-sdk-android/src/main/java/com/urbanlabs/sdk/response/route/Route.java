package com.urbanlabs.sdk.response.route;

import com.urbanlabs.sdk.dict.TUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by днс on 20.02.2015.
 */
public class Route extends AbstractRoute {
    private List<Leg> legs_ = new ArrayList<Leg>();

    public Route(JSONObject root) throws Exception {
        super(root);
        JSONArray legs = root.getJSONArray(TUtil.R_LEGS);
        for(int i = 0; i < legs.length(); ++i) {
            legs_.add(new Leg(legs.getJSONObject(i)));
        }
    }

    public List<Leg> getLegs() {
        return legs_;
    }
}
