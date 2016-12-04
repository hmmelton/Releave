package com.hmmelton.releave;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hmmelton.releave.models.Restroom;

import java.util.List;
import java.util.Locale;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    @SuppressWarnings("unused")
    private final String TAG = this.getClass().getSimpleName();

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mDatabase;

    private boolean isLocked;

    private LatLng mRestroomLocation;
    private String mRestroomName, mRestroomAddress;

    private final int PLACE_PICKER_REQUEST = 1;

    @BindString(R.string.location_retrieval_error) protected String LOCATION_ERROR;
    @BindString(R.string.upload) protected String UPLOAD;
    @BindString(R.string.submit) protected String SUBMIT;
    @BindString(android.R.string.cancel) protected String CANCEL;

    // OnClick handler for FloatingActionButton
    @OnClick(R.id.fab) void onFabClick() {
        // Prepare dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_restroom_dialog, null);
        dialogBuilder.setView(dialogView);

        // Grab views and set listeners
        final Switch isLocked = (Switch) dialogView.findViewById(R.id.is_locked);
        final ImageView mapIcon = (ImageView) dialogView.findViewById(R.id.gps_select);
        mapIcon.setOnClickListener(view -> MainActivity.this.onLocationClick());
        isLocked.setOnCheckedChangeListener((compoundButton, b) -> MainActivity.this.isLocked = b);

        // Show
        AlertDialog dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
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

        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.isLocked = false;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                this.mRestroomLocation = place.getLatLng();
                this.mRestroomName = place.getName().toString();
                this.mRestroomAddress = place.getAddress().toString();
            }
        }
    }

    /**
     * This method is called when a new restroom is being submitted.
     */
    protected void onSubmit() {
        // Reverse geocode to find country/state/city of restroom
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.US);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(this.mRestroomLocation.latitude, this.mRestroomLocation.longitude, 1);
        } catch (Exception e) {
            // Error getting addresses
            Toast.makeText(this, this.LOCATION_ERROR, Toast.LENGTH_SHORT).show();
        }

        if (addresses == null || addresses.size() < 1) {
            // No addresses
            Toast.makeText(this, this.LOCATION_ERROR, Toast.LENGTH_SHORT).show();
        } else {
            // Get country/state/city
            Address address = addresses.get(0);
            String country = address.getCountryCode();
            String province = address.getAdminArea();
            String locality = address.getLocality();
            String city = (locality != null ? locality : address.getSubLocality());
            Log.e(TAG, String.format(Locale.US, "%s -> %s", locality, address.getSubLocality()));
            String thoroughfare = mRestroomAddress.substring(0, mRestroomAddress.indexOf(","));
            String zip = address.getPostalCode();

            if (country != null && province != null && city != null && zip != null
                    && !thoroughfare.equals("")) {
                // Create restroom object
                Restroom restroom = new Restroom(this.mRestroomLocation.latitude,
                        this.mRestroomLocation.longitude, this.mRestroomName,
                        thoroughfare, this.isLocked, city, province, zip);
                // Upload new restroom to database
                mDatabase.child("restrooms")
                        .child(country)
                        .child(province)
                        .child(city)
                        .child(thoroughfare)
                        .setValue(restroom);
            } else {
                Log.e(TAG,
                        String.format(Locale.US, "country: %s, state: %s, city: %s",
                                country, province, city));
            }
        }
    }

    /**
     * This method is called when the GPS button on the dialog is pressed.
     */
    protected void onLocationClick() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(MainActivity.this),
                    MainActivity.this.PLACE_PICKER_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
