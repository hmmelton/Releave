package com.hmmelton.releave;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hmmelton.releave.helpers.MapHelper;
import com.hmmelton.releave.models.Restroom;
import com.hmmelton.releave.utils.AnimationUtil;
import com.hmmelton.releave.utils.UiUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener {

    @SuppressWarnings("unused")
    private final String TAG = this.getClass().getSimpleName();

    private MapHelper mMapHelper;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mDatabase;

    private boolean isLocked;

    private LatLng mRestroomLocation;
    private String mRestroomName, mRestroomAddress;

    private final int PLACE_PICKER_REQUEST = 1;

    // region String Resources
    @BindString(R.string.location_retrieval_error) protected String LOCATION_ERROR;
    @BindString(R.string.upload) protected String UPLOAD;
    @BindString(R.string.submit) protected String SUBMIT;
    @BindString(android.R.string.cancel) protected String CANCEL;
    @BindString(R.string.no_location) String mNoLocation;
    // endregion

    // region Views
    @BindView(R.id.main_footer) protected LinearLayout mFooter;
    @BindView(R.id.fab) protected FloatingActionButton mFab;
    @BindView(R.id.main_content) protected CoordinatorLayout mContent;
    @BindView(R.id.toolbar) protected Toolbar mToolbar;
    @BindView(R.id.toolbar_title) protected TextView mTitle;
    // endregion

    // region OnClick Handlers
    // OnClick handler for locked button
    @OnClick(R.id.locked_yes_button) protected void onYesClick() {
        this.isLocked = true;
        AnimationUtil.slideToBottom(mFooter);
        mFab.setVisibility(View.VISIBLE);
        this.onSubmit();
    }
    // OnClick handler for not locked button
    @OnClick(R.id.locked_no_button) protected void onNoClick() {
        this.isLocked = false;
        AnimationUtil.slideToBottom(mFooter);
        mFab.setVisibility(View.VISIBLE);
        this.onSubmit();
    }
    // OnClick handler for FloatingActionButton
    @OnClick(R.id.fab) void onFabClick(View view) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        builder.setLatLngBounds(mMapHelper.getMapBounds());
        try {
            startActivityForResult(builder.build(MainActivity.this),
                    MainActivity.this.PLACE_PICKER_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        UiUtil.setCustomFont(mTitle, "fonts/Rubik-Medium.ttf");
        FacebookSdk.sdkInitialize(getApplicationContext());

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.isLocked = false;
        this.setLayoutListeners();
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
        // Set map's listeners here, because they reference UI
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);
        mMapHelper = new MapHelper(googleMap, mDatabase, this);
        // Set zoom preferences
        mMapHelper.setMinMaxZoom(10.0f, 16.0f);
        mMapHelper.setInitialMapView();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mFab.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mFab.setVisibility(View.GONE);
        return false;
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

                mFab.setVisibility(View.GONE);
                AnimationUtil.slideFromBottom(mFooter);
            } else {
                // TODO: Handle this -- Alert dialog?
                Log.e(TAG, String.format("Result code was: %s", resultCode));
            }
        } else
            Log.e(TAG, String.format("Request code was: %s", requestCode));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle based on item id
        switch (item.getItemId()) {
            case R.id.menu_log_out:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method logs the user out of the application
     */
    private void logOut() {
        // Log out of Firebase
        FirebaseAuth.getInstance().signOut();
        // Log out of Facebook
        LoginManager.getInstance().logOut();
        startActivity(new Intent(this, LoginActivity.class));
        // Override for smooth transition
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_out_left);
    }

    /**
     * This method sets GlobalLayoutListeners for views who need to be measured
     */
    private void setLayoutListeners() {
        mFooter.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove listener based on current device's SDK version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mFooter.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mFooter.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                // Hide footer off bottom of screen
                CoordinatorLayout.LayoutParams params =
                        (CoordinatorLayout.LayoutParams) mFooter.getLayoutParams();
                params.setMargins(0, 0, 0, -1 * mFooter.getHeight());
                mFooter.setLayoutParams(params);
            }
        });
    }

    /**
     * This method notifies the user that the bathroom they created has been uploaded to the
     * database, and gives them the option to undo their action.
     * @param location Keys to the location of the bathroom in the database
     */
    private void showConfirmationSnackbar(Map<String, String> location) {
        // Display Snackbar with undo action to user
        Snackbar
                .make(mContent, R.string.bathroom_uploaded, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, view -> mDatabase.child("restrooms")
                        .child(location.get("country"))
                        .child(location.get("province"))
                        .child(location.get("city"))
                        .child(location.get("thoroughfare"))
                        .removeValue()
                        .addOnCompleteListener(task -> {
                            // Removal over
                            if (task.isSuccessful()) {
                                // Removal was successful
                                Snackbar
                                        .make(mContent, R.string.undo_upload,
                                                Snackbar.LENGTH_SHORT)
                                        .show();
                            } else {
                                // Removal encountered an error
                                Snackbar
                                        .make(mContent, R.string.undo_upload_fail,
                                                Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }))
                .setActionTextColor(Color.WHITE)
                .show();
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
                        .setValue(restroom)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Database upload was successful

                                // Create map with data leading to uploaded item
                                Map<String, String> param = new HashMap<>();
                                param.put("country", country);
                                param.put("province", province);
                                param.put("city", city);
                                param.put("thoroughfare", thoroughfare);
                                this.showConfirmationSnackbar(param);
                            } else {
                                // Database upload was not successful
                                Toast.makeText(this, R.string.database_error_toast,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Log.e(TAG,
                        String.format(Locale.US, "country: %s, state: %s, city: %s",
                                country, province, city));
            }
        }
    }
}