package com.urbanlabs.sdk.util;

import org.apache.http.message.BasicNameValuePair;

/**
 * Created by kirill on 21.02.15.
 */
public class KV extends BasicNameValuePair {
    public KV(String name, String value) {
        super(name, value);
    }
}
