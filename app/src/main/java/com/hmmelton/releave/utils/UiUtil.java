package com.hmmelton.releave.utils;

import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by harrisonmelton on 1/11/17.
 * This is a helper file for minor UI tweaks.
 */

public class UiUtil {
    /**
     * This method sets the font of the ActionBar title.
     * @param view TextView whose custom font is to be set
     * @param resource String representation of .ttf resource file used to set font.
     */
    public static void setCustomFont(TextView view, String resource) {
        Typeface customFont = Typeface.createFromAsset(view.getContext().getAssets(), resource);
        view.setTypeface(customFont);

    }
}
