package com.urbanlabs.sdk.callback;

import com.urbanlabs.sdk.SputnikException;

/**
 * Created by kirill on 10.02.15.
 */
public interface BasicCallback {
    void done(SputnikException e);
}
