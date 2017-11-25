package com.urbanlabs.sdk.util;

/**
 * Created by kirill on 2/7/14.
 */
public class StringUtil {
    /**
     *
     * @param s
     * @return
     */
    public static String toUpperFirst(String s) {
        return s.substring(0,1).toUpperCase()+s.substring(1, s.length());
    }
}
