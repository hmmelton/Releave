package com.hmmelton.releave.models;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harrisonmelton on 11/3/16.
 * This is a model class that holds the information for 1 restroom.
 */
public class Restroom {

    public double lat, lng;
    public String name, address, city, state, zip, uploader;
    public boolean locked;

    public Restroom(double lat, double lng, String name, String address,
                    boolean locked, String city, String state, String zip) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.address = address;
        this.locked = locked;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.uploader = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // Used by Firebase
    public Restroom() {}

    // TODO: Do I even need this?
    public Restroom(JSONObject restroom) {
        try {
            this.lat = restroom.getDouble("lat");
            this.lng = restroom.getDouble("lng");
            this.name = restroom.getString("name");
            this.address = restroom.getString("address");
            this.city = restroom.getString("city");
            this.state = restroom.getString("state");
            this.zip = restroom.getString("zip");
            this.locked = restroom.getBoolean("locked");
            this.uploader = restroom.getString("uploader");
        } catch (JSONException e) {
            // TODO: handle error
        }
    }
}
