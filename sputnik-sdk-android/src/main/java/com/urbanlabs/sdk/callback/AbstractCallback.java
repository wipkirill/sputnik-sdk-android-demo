package com.urbanlabs.sdk.callback;

import com.urbanlabs.sdk.SputnikException;

/**
 * Created by kirill on 10.02.15.
 */
public abstract class AbstractCallback<T> {
    abstract void internalDone(T paramT, SputnikException e);
}
