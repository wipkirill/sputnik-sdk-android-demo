package com.urbanlabs.sdk.response.json;

import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;


/**
 * Created by kirill on 1/26/14.
 */
public abstract class ResponseHandler<T> implements Response.Listener<T>, Response.ErrorListener {
    protected boolean failed_ = false;
    protected String error_;
    protected String[] reqFields_;

    public ResponseHandler(String[] reqFields) {
        reqFields_ = reqFields;
    }

    public abstract void onResponse(T data);

    /**
     * @param volleyError
     */
    public final void onErrorResponse(VolleyError volleyError) {
        Log.e("[VOLLEY]", volleyError.toString());
        error_ = volleyError.toString();
        failed_ = true;
        this.onError(error_);
    }

    /**
     * @return
     */
    public boolean isFailed() {
        return failed_;
    }

    /**
     *
     * @param data
     * @return
     */
    public abstract boolean validate(T data);

    /**
     * @param data
     */
    public abstract void onResult(T data);

    /**
     * @param message
     */
    public abstract void onError(String message);
}
