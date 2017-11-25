package com.urbanlabs.sdk.response.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kirill on 1/26/14.
 */
public class ResponseData<T> {
    protected T object_;
    public ResponseData(T obj) {
        object_ = obj;
    }
}
