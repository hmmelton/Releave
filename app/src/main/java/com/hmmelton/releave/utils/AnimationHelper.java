package com.hmmelton.releave.utils;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by harrisonmelton on 1/3/17.
 * This class is a helper used to
 */

public class AnimationHelper {

    /**
     * This method slides a view onto the screen from the bottom.
     * @param view View to be animated
     */
    public static void slideFromBottom(View view) {
        ObjectAnimator anim = ObjectAnimator
                .ofFloat(view, View.TRANSLATION_Y, 0, -1 * view.getHeight());
        anim.setDuration(500);
        anim.start();
    }

    /**
     * This method slides a view onto the screen from the bottom.
     * @param view View to be animated
     */
    public static void slideToBottom(View view) {
        ObjectAnimator anim = ObjectAnimator
                .ofFloat(view, View.TRANSLATION_Y, -1 * view.getHeight(), 0);
        anim.setDuration(500);
        anim.start();
    }

}
