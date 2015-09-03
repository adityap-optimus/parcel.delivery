package com.aditya.hackerearth.porterapp.helper.network;

/**
 * Created by adityapratap on 29/08/2015.
 */
public class APIUtils {

    public static final String API_BASE_URL = "https://porter.0x10.info/api/parcel";

    public static final String API_PARAM_TYPE = "?type=json";
    public static final String API_PARAM_QUERY = "&query=";
    public static final String API_QUERY_PARCEL = "list_parcel";
    public static final String API_QUERY_HITS = "api_hits";

    public static String getUrl(String queryType) {
        StringBuilder builder = new StringBuilder();
        builder.append(API_BASE_URL).append(API_PARAM_TYPE).append(API_PARAM_QUERY).append(queryType);
        return builder.toString();
    }

}
