package com.hmmelton.releave.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.hmmelton.releave.R;
import com.hmmelton.releave.ReleaveApplication;

/**
 * Created by harrisonmelton on 1/12/17.
 * This class
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    @Override
    public View getInfoWindow(Marker marker) {
        View window =
                View.inflate(ReleaveApplication.getInstance().getApplicationContext(),
                        R.layout.custom_info_window, null);

        // Grab views from layout
        TextView title = (TextView) window.findViewById(R.id.info_window_title);
        TextView address = (TextView) window.findViewById(R.id.info_window_address);
        ImageView locked = (ImageView) window.findViewById(R.id.info_window_locked_icon);

        // Fill in information
        title.setText(marker.getTitle());
        address.setText(marker.getSnippet());
        // Hide lock icon if not locked
        if (!(boolean) marker.getTag()) {
            locked.setVisibility(View.INVISIBLE);
        }

        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
