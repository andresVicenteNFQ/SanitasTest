package com.mycorp;

import java.io.IOException;

import com.ning.http.client.AsyncCompletionHandler;

/**
 * 
 * @author Andres.Vicente
 *
 * @param <T>
 */
public abstract class ZendeskAsyncCompletionHandler<T> extends AsyncCompletionHandler<T> {
    @Override
    public void onThrowable(Throwable t) {
        if (t instanceof IOException) {
            throw new ZendeskException(t);
        } else {
            super.onThrowable(t);
        }
    }
}