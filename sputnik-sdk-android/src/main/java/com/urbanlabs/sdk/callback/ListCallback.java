package com.urbanlabs.sdk.callback;

import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.response.AbstractResponse;

import java.util.List;

/**
 * Created by kirill on 19.02.15.
 */
public abstract class ListCallback<T extends AbstractResponse> extends AbstractCallback<List<T>> {
    public abstract void done(List<T> list, SputnikException e);

    @Override
    void internalDone(List<T> list, SputnikException e) {
        done(list, e);
    }
}
