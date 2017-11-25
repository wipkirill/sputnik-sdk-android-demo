package com.urbanlabs.sdk.response;

import com.urbanlabs.sdk.dict.TUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response for getloadedgraphs function
 */
public class GraphList extends AbstractResponse {
    private List<String> graphs_;
    public GraphList(JSONObject data) throws JSONException {
        JSONArray graphs = data.getJSONArray(TUtil.RESPONSE);
        graphs_ = new ArrayList<String>();
        for (int i = 0; i < graphs.length(); ++i) {
            graphs_.add(graphs.getString(i));
        }
    }

    public List<String> getGraphs() {
        return graphs_;
    }
}
