package com.urbanlabs.sdk.response.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kirill on 1/26/14.
 */
public class JsonData extends ResponseData<JSONObject> {
    public JsonData(JSONObject obj) {
        super(obj);
    }

    public JSONArray getJSONArray(String field) throws JSONException {
        return object_.getJSONArray(field);
    }
}
