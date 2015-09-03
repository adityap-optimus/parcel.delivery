package com.aditya.hackerearth.porterapp.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by adityapratap on 29/08/2015.
 */
public class ParcelAPIResponse implements Serializable {

    public Parcel[] parcels;

    public ParcelAPIResponse(Parcel[] parcels) {
        this.parcels = parcels;
    }

    public class Parcel implements Serializable {
        public String name, image, type, weight, phone, color, link;
        public long date;
        public String price;
        public int quantity;
        public Location live_location;

        public Parcel(String name, String image, String type, String weight, String phone, String color, String link, long date, String price, int quantity, Location live_location) {
            this.name = name;
            this.image = image;
            this.type = type;
            this.weight = weight;
            this.phone = phone;
            this.color = color;
            this.link = link;
            this.date = date;
            this.price = price;
            this.quantity = quantity;
            this.live_location = live_location;
        }
    }

    public class Location implements Serializable {
        public double latitude, longitude;

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
