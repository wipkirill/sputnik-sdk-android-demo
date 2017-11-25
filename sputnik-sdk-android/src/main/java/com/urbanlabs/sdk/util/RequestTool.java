package com.urbanlabs.sdk.util;

import android.content.Context;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

/**
 * Created by kirill on 1/25/14.
 */
public class RequestTool {
    private static Context context_;
    // Global request queue for Volley
    private static RequestQueue requestQueue_;
    private static DefaultRetryPolicy policy_;
    private static final int REQUEST_TIMEOUT = 60000;
    private static final int REQUEST_RETRIES = 0;

    /**
     * RequestTool
     */
    private RequestTool() {
        ;
    }

    /**
     *  Initialize
     * @param c
     */
    public static void init(Context c) {
        if(context_ == null) {
            policy_ = new DefaultRetryPolicy(REQUEST_TIMEOUT, REQUEST_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            context_ = c;
        }
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public static RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (requestQueue_ == null) {
            requestQueue_ = Volley.newRequestQueue(context_);
        }
        return requestQueue_;
    }
    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public static <T> void add(Request<T> req, String tag) {
        // set the default tag if tag is empty
        //req.setTag(tag);
        req.setRetryPolicy(policy_);
        VolleyLog.d("Adding request to queue: %s", req.getUrl());
        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     */
    public static <T> void add(Request<T> req) {
        // set the default tag if tag is empty
        //req.setTag("Volley");
        req.setRetryPolicy(policy_);
        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public static void cancelPendingRequests(String tag) {
        if (requestQueue_ != null) {
            requestQueue_.cancelAll(tag);
        }
    }
}
