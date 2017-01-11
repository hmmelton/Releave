package com.hmmelton.releave.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.hmmelton.releave.R;
import com.hmmelton.releave.models.Restroom;

import java.util.List;
import java.util.Locale;

/**
 * Created by harrisonmelton on 1/9/17.
 * This is a helper file for dealing with a GoogleMap object.
 */

public class MapHelper {

    @SuppressWarnings("unused")
    private final String TAG = getClass().getSimpleName();

    private GoogleMap mMap;
    private DatabaseReference mDatabaseReference;
    private Context mContext;
    private LatLng mMapCenter;

    /**
     * Initializer
     * @param map GoogleMap instance
     */
    public MapHelper(GoogleMap map, DatabaseReference databaseReference, Context context) {
        this.mMap = map;
        this.mDatabaseReference = databaseReference;
        this.mContext = context;
    }

    /**
     * This method sets the map's minimum and maximum zoom scales.
     * @param min minimum zoom scale
     * @param max maximum zoom scale
     */
    public void setMinMaxZoom(float min, float max) {
        this.mMap.setMinZoomPreference(min);
        this.mMap.setMaxZoomPreference(max);
    }

    /**
     * This method sets up the GoogleMap instance with a zoom on the user's current location.
     */
    public void setInitialMapView() {
        LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = manager.getProviders(true);

        // Check if user has given permission to access location
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Cycle through location providers until one finds the user's current location
        Location location = null;
        for (int i = 0; i < providers.size(); i++) {
            location = manager.getLastKnownLocation(providers.get(i));
            if (location != null) {
                break;
            }
        }

        // User's current latitude and longitude
        double latitude;
        double longitude;

        if (location != null) {
            // Location was found - grab lat & long
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            // Get current latitude & longitude
            LatLng currentLocation = new LatLng(latitude, longitude);
            // Store center of map
            this.mMapCenter = new LatLng(latitude, longitude);

            // Move to location on map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            // 14x zoom in on location
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(latitude, longitude), 14.0f));
            this.setMapCameraIdleListener();

            // Display settings
            mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.setMyLocationEnabled(true);
        } else {
            // Alert the user that his/her location could not be found
            Toast.makeText(mContext, R.string.location_retrieval_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This method returns the current range of latitudes and longitudes displayed by the map.
     */
    private void updateMapCenter() {
        // Update global map center variable
        this.mMapCenter = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
    }

    /**
     * This method fetches restroom data from the database.
     * @param callback Callback used to return Restrooms pulled from database
     */
    private void getData(OnMapDataChangeListener callback) {
        // Reverse geocode to find country/state/city of restroom
        Geocoder geocoder = new Geocoder(mContext, Locale.US);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(mMapCenter.latitude, mMapCenter.longitude, 1);
        } catch (Exception e) {
            // Error getting addresses
            Toast.makeText(mContext, R.string.location_retrieval_error, Toast.LENGTH_SHORT).show();
        }

        if (addresses == null || addresses.size() < 1) {
            // No addresses
            Toast.makeText(mContext, R.string.location_retrieval_error, Toast.LENGTH_SHORT).show();
        } else {
            // There was an address, so get it
            Address address = addresses.get(0);
            String countryCode = address.getCountryCode();
            String province = address.getAdminArea();
            String locality = address.getLocality();
            String city = (locality != null ? locality : address.getSubLocality());
            // Query database for restrooms in current zip code
            Query visibleRestrooms = mDatabaseReference
                    .child("restrooms")
                    .child(countryCode)
                    .child(province)
                    .child(city)
                    .orderByChild("zip")
                    .equalTo(address.getPostalCode());

            // Fetch restrooms from database
            visibleRestrooms.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    // Convert value into a Restroom object and add to list
                    Restroom restroom = dataSnapshot.getValue(Restroom.class);
                    // Pass restroom to callback
                    callback.onDataChanged(restroom);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * This method sets the map's OnCameraIdleListener.
     */
    private void setMapCameraIdleListener() {
        mMap.setOnCameraIdleListener(() -> {
            this.updateMapCenter();
            this.getData(this::addRestroomToMap);
        });
    }

    /**
     * This method is used to update restroom icons on the map.
     * @param restroom New Restroom object to display on map
     */
    private void addRestroomToMap(Restroom restroom) {
        // Create a marker and add it to the map
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng currentLocation = new LatLng(restroom.lat, restroom.lng);
        markerOptions.position(currentLocation);
        markerOptions.title(String.format("%s", restroom.name));
        markerOptions.snippet(String.format("%s", restroom.address));
        this.mMap.addMarker(markerOptions);
    }
}
