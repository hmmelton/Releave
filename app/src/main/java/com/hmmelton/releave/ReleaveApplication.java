package com.hmmelton.releave;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by harrisonmelton on 11/5/16.
 */

public class ReleaveApplication extends Application {

    private static ReleaveApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        instance = this;
    }

    /**
     * This method is used to access global context.
     * @return
     */
    public static ReleaveApplication getInstance() {
        return instance;
    }

}
