package com.hmmelton.releave;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(16.0f);

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = manager.getProviders(true);

        // Check if user has given permission to access location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
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

            // Add a marker at the current location and move the camera
            LatLng currentLocation = new LatLng(latitude, longitude);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.title("You are here");
            Marker locationMarker = mMap.addMarker(markerOptions);
            locationMarker.showInfoWindow();
            // Move to location on map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            // 14x zoom in on location
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(latitude, longitude), 14.0f));

            // Display settings
            mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true); // Idk why this doesn't work
            mMap.getUiSettings().setZoomControlsEnabled(true);
        } else {

        }
    }
}
