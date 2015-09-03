package com.aditya.hackerearth.porterapp.helper.network;

import android.content.Context;
import android.text.TextUtils;

import com.aditya.hackerearth.porterapp.BuildConfig;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;


/**
 * Created by adityapratap on 29/08/2015.
 */
public class NetworkFunctions {

    private static final String TAG = NetworkFunctions.class.getSimpleName();
    private static VolleyNetworkSingleton networkSingleton;

    /**
     * Method converts the response text to java object whose class is taken as parameter
     *
     * @param context  of the calling activity
     * @param query    formed by Url + parameter-value pair in plain text
     * @param tClass   class for the return type
     * @param listener instance of listener interface implemented by calling activity
     * @param <T>      generic type for the object
     */
    public static <T> void getGsonObjectFromQuery(final Context context, final boolean shouldShowProgressbar, String query,
                                                  Class<T> tClass, final RequestListener<T> listener) {
        if (BuildConfig.DEBUG) {
            VolleyLog.DEBUG = true;
        }

        if (TextUtils.isEmpty(query)) {
            listener.OnRequestFailed(getErrorTag(new VolleyError("URL is null",
                    new NullPointerException())), shouldShowProgressbar);
            return;
        }

        networkSingleton = VolleyNetworkSingleton.getNetworkInstance(context);
        GsonObjectRequest<T> gsonObjectRequest = new GsonObjectRequest<>(Request.Method.GET, query,
                tClass, null, new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                listener.OnRequestSucessful(response, shouldShowProgressbar);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.OnRequestFailed(getErrorTag(error), shouldShowProgressbar);
                    }
                });

        gsonObjectRequest.setTag(TAG);
        gsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        networkSingleton.getRequestQueue().add(gsonObjectRequest);
        listener.OnRequestStarted(query, shouldShowProgressbar);
    }


    /**
     * Method returns the Network error Tag depending upon the volley error instance
     *
     * @param error instance of VolleyError encountered on request failure
     * @return Network error Tag
     */
    private static String getErrorTag(VolleyError error) {
        if (BuildConfig.DEBUG) {
            error.printStackTrace();
        }
        return error.getMessage();
    }


}