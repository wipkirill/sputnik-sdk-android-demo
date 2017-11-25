package com.urbanlabs.sdk.response.json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kirill on 1/26/14.
 */
public abstract class JsonHandler extends ResponseHandler<JSONObject> {

    public JsonHandler(String[] reqFields) {
        super(reqFields);
    }

    @Override
    public void onResponse(JSONObject data) {
        if (validate(data))
            onResult(data);
        else
            onError("Validation error: " + error_);
    }


    public abstract void onResult(JSONObject jsonObject);

    @Override
    public abstract void onError(String message);
    /**
     *
     * @param data
     * @return
     */
    public boolean validate(JSONObject data) {
        if (data == null) {
            error_ = "Sputnik encountered some unknown error. Please try again a later";
            return false;
        }
        try {
            if((data.has("ok")) && (data.getBoolean("ok") == true)) {
                return true;
            }
            if (data.has("error") || (!data.has("response"))) {
                failed_ = true;
                if (data.has("error")) {
                    error_ = data.getString("error");
                    return false;
                }
                error_ = "Failed to find response section in json data";
                return false;
            }
            for(String key : reqFields_) {
                if(!data.has(key)) {
                    error_  = "Key "+key+" hasn't been found in output data";
                    failed_ = true;
                    return false;
                }
            }
        } catch (JSONException e) {
            error_ = e.getMessage();
            return false;
        }
        return true;
    }
}
