package com.urbanlabs.sdk.query;

import com.urbanlabs.sdk.callback.AbstractCallback;
import com.urbanlabs.sdk.response.AbstractResponse;

/**
 * Created by kirill on 17.02.15.
 */
public abstract class AbstractQuery<C extends AbstractCallback> {
    public abstract void find(C callback);
}
