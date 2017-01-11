package com.hmmelton.releave.helpers;

import com.hmmelton.releave.models.Restroom;

/**
 * Created by harrisonmelton on 1/9/17.
 * This is an interface used as a callback for a change in map data.
 */

interface OnMapDataChangeListener {

    /**
     * This method is used to return a list of Restrooms.
     * @param restroom Restroom object for map to deal with
     */
    void onDataChanged(Restroom restroom);
}
