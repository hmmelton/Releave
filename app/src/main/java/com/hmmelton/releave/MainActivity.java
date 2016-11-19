package com.hmmelton.releave;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    @SuppressWarnings("unused")
    private final String TAG = this.getClass().getSimpleName();

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    // OnClick handler for FloatingActionButton
    @OnClick(R.id.fab) void onFabClick() {
        startActivity(new Intent(MainActivity.this, UploadActivity.class));
    }

    // OnClick for profile image in AppBar
    @OnClick(R.id.profile_button) void onProfileClick() {
        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
    }

    @BindString(R.string.no_location) String mNoLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ButterKnife.bind(this);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Set zoom preferences
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

            // Get current latitude & longitude
            LatLng currentLocation = new LatLng(latitude, longitude);



            /** For future reference - adding a marker: */
            // MarkerOptions markerOptions = new MarkerOptions();
            // markerOptions.position(currentLocation);
            // markerOptions.title("You are here");
            // Marker locationMarker = mMap.addMarker(markerOptions);
            // locationMarker.showInfoWindow();

            // Move to location on map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            // 14x zoom in on location
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(latitude, longitude), 14.0f));

            // Display settings
            mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.setMyLocationEnabled(true);
        } else {
            // Alert the user that his/her location could not be found
            Toast.makeText(this, mNoLocation, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed: " + connectionResult.getErrorMessage());
    }
}
