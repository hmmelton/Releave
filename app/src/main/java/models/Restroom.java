package models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harrisonmelton on 11/3/16.
 * This is a model class that holds the information for 1 restroom.
 */
public class Restroom {

    double lat, lng, rating;
    String name, address, hours;

    public Restroom() {}

    public Restroom(double lat, double lng, double rating, String name, String address,
                    String hours) {
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.name = name;
        this.address = address;
        this.hours = hours;
    }

    public Restroom(JSONObject restroom) {
        try {
            this.lat = restroom.getDouble("lat");
            this.lng = restroom.getDouble("lng");
            this.rating = restroom.getDouble("rating");
            this.name = restroom.getString("name");
            this.address = restroom.getString("address");
            this.hours = restroom.getString("hours");
        } catch (JSONException e) {
            // TODO: handle error
        }
    }
}
