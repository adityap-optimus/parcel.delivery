package com.aditya.hackerearth.porterapp.helper.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by adityapratap on 29/08/2015.
 */
public class VolleyNetworkSingleton {

    private static VolleyNetworkSingleton networkInstance;
    private RequestQueue requestQueue;
    private Context volleyInstanceContext;

    private static final String TAG = VolleyNetworkSingleton.class.getSimpleName();

    private VolleyNetworkSingleton(Context context) {
        volleyInstanceContext = context;
    }

    public static synchronized VolleyNetworkSingleton getNetworkInstance(Context context) {
        if (networkInstance == null)
            networkInstance = new VolleyNetworkSingleton(context);
        return networkInstance;
    }

    /**
     * This method can be used to define custom cache size and HurlStack
     *
     * @return
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(volleyInstanceContext);
        }
        return requestQueue;
    }
}
