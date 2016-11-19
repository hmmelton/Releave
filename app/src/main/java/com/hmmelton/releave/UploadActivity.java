package com.hmmelton.releave;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hmmelton.releave.models.Restroom;

import java.util.List;
import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UploadActivity extends AppCompatActivity {

    @BindString(R.string.pm) protected String PM;
    @BindString(R.string.am) protected String AM;
    @BindString(R.string.location_retrieval_error) protected String LOCATION_ERROR;

    private DatabaseReference mDatabase;

    private LatLng mRestroomLocation;
    private String mRestroomName;
    private String mRestroomAddress;

    private final int PLACE_PICKER_REQUEST = 1;

    @BindViews({R.id.weekday_start_hours, R.id.weekday_end_hours, R.id.weekend_start_hours,
            R.id.weekend_end_hours}) protected List<EditText> mHoursInputs;
    @BindView(R.id.key_code_switch)
    protected Switch mLocked;
    // Toggle the text of the AM/PM buttons when pressed
    @OnClick({R.id.weekday_start_am, R.id.weekday_end_am, R.id.weekend_start_am,
            R.id.weekend_end_am}) protected void onAmButtonClick(TextView tv) {
        if (tv.getText().equals(AM)) {
            tv.setText(PM);
        } else {
            tv.setText(AM);
        }
    }

    @OnClick(R.id.location_button)
    protected void onLocationClick() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(UploadActivity.this), UploadActivity.this.PLACE_PICKER_REQUEST);
        } catch (Exception e) {
            Log.e("UploadActivity", e.getMessage());
        }
    }

    @OnClick(R.id.upload_submit)
    protected void onSubmit() {
        // Reverse geocode to find country/state/city of restroom
        Geocoder geocoder = new Geocoder(UploadActivity.this, Locale.US);
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
            boolean locked = this.mLocked.isChecked();
            // Create restroom object
            Restroom restroom = new Restroom(this.mRestroomLocation.latitude, this.mRestroomLocation.longitude,
                    this.mRestroomName, this.mRestroomAddress, locked);

            // Get country/state/city
            Address address = addresses.get(0);
            String country = address.getCountryCode();
            String province = address.getAdminArea();
            String city = address.getSubLocality();

            if (country != null && province != null && city != null) {
                // Upload new restroom to database
                mDatabase.child("restrooms")
                        .child(country)
                        .child(province)
                        .child(city)
                        .setValue(restroom);
            } else {
                Log.e("UploadActivity", String.format(Locale.US, "country: %s, state: %s, city: %s", country, province, city));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ButterKnife.bind(this);

        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

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
}
