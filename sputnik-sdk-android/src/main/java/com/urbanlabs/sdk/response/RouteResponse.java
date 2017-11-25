package com.urbanlabs.sdk.response;

import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.response.route.Route;
import com.urbanlabs.sdk.util.Bounds;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirill on 18.02.15.
 */
public class RouteResponse extends AbstractResponse {
    private Bounds bounds_;
    private List<Route> routes_ = new ArrayList<Route>();

    public RouteResponse(JSONObject data) throws Exception {
        JSONObject result = data.getJSONObject(TUtil.RESPONSE);
        JSONArray routes = result.getJSONArray(TUtil.R_ROUTES);
        for(int i = 0; i < routes.length(); ++i) {
            routes_.add(new Route(routes.getJSONObject(i)));
        }
        bounds_ = Bounds.readJson(result.getJSONObject(TUtil.BBOX2));
    }

    public Bounds getBounds() {
        return bounds_;
    }

    public List<Route> getRoutes() {
        return routes_;
    }
}
