package com.aditya.hackerearth.porterapp.helper.network;

/**
 * Created by adityapratap on 29/08/2015.
 */
public interface RequestListener<T> {

    /**
     * Method is called on the completion of request is successful
     *
     * @param requestResponse the requestResponse for the desired class,
     *                        null if request fails
     */
    public void OnRequestSucessful(T requestResponse, boolean shouldShowProgressBar);

    /**
     * Method is called when request fails
     * @param ErrorTag
     */
    public void OnRequestFailed(String ErrorTag, boolean shouldShowProgressBar);

    /**
     * Method is called when the request is being executed
     *
     * @param TAG unique request Tag
     */
    public void OnRequestStarted(String TAG, boolean shouldShowProgressBar);
}
