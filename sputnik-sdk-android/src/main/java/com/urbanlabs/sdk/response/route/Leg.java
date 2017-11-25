package com.urbanlabs.sdk.response.route;

import com.urbanlabs.sdk.dict.TUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by днс on 20.02.2015.
 */
public class Leg extends AbstractRoute {
    private List<Step> steps_ = new ArrayList<Step>();

    public Leg(JSONObject root) throws Exception {
        super(root);
        JSONArray steps = root.getJSONArray(TUtil.R_STEPS);
        for(int i = 0; i < steps.length(); ++i) {
            steps_.add(new Step(steps.getJSONObject(i)));
        }
    }

    public List<Step> getSteps() {
        return steps_;
    }
}
