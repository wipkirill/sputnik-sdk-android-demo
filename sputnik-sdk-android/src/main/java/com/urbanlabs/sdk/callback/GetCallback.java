package com.urbanlabs.sdk.callback;

import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.response.AbstractResponse;

/**
 * Created by kirill on 12.02.15.
 */
public abstract class GetCallback<T extends AbstractResponse> extends AbstractCallback<T> {

    public abstract void done(T t, SputnikException e);

    @Override
    void internalDone(T t, SputnikException e) {
        done(t, e);
    }

}