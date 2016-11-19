package com.hmmelton.releave;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import butterknife.BindString;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UploadActivity extends AppCompatActivity {

    @BindString(R.string.pm) protected String PM;
    @BindString(R.string.am) protected String AM;

    // region Views
    @BindViews({R.id.weekday_start_hours, R.id.weekday_end_hours, R.id.weekend_start_hours,
            R.id.weekend_end_hours}) protected List<EditText> mHoursInputs;
    @OnClick({R.id.weekday_start_am, R.id.weekday_end_am, R.id.weekend_start_am,
            R.id.weekend_end_am}) protected void onAmButtonClick(TextView tv) {
        if (tv.getText().equals(AM)) {
            tv.setText(PM);
        } else {
            tv.setText(AM);
        }
    }
    // region

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ButterKnife.bind(this);
    }
}
