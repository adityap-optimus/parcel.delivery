package com.aditya.hackerearth.porterapp.model;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by adityapratap on 29/08/2015.
 */
public class ParcelUtils {

    public static class NameComparator implements Comparator<ParcelAPIResponse.Parcel> {
        public int compare(ParcelAPIResponse.Parcel c1, ParcelAPIResponse.Parcel c2) {
            return c1.name.compareTo(c2.name);
        }
    }

    public static class PriceComparator implements Comparator<ParcelAPIResponse.Parcel> {
        public int compare(ParcelAPIResponse.Parcel c1, ParcelAPIResponse.Parcel c2) {
            double price1 = Double.parseDouble(c1.price.replace(",",""));
            double price2 = Double.parseDouble(c2.price.replace(",",""));
            return Double.compare(price1,price2);
        }
    }

    public static class WeightComparator implements Comparator<ParcelAPIResponse.Parcel> {
        public int compare(ParcelAPIResponse.Parcel c1, ParcelAPIResponse.Parcel c2) {
            return c1.weight.compareTo(c2.weight);
        }
    }

    public static String getDate(long date){
        Date cDate = new Date(date * 1000L);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        return format.format(cDate);
    }
}
