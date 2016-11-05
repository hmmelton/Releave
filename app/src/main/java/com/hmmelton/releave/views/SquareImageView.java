package com.hmmelton.releave.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.hmmelton.releave.R;

/**
 * Created by harrisonmelton on 11/4/16.
 * This is a custom ImageView that is forced into a square shape.  It's default side length
 * measurement is its width; the "byWidth" attribute can be set in it's xml declaration to measure
 * by height instead.
 */

public class SquareImageView extends ImageView {

    private boolean byWidth;

    public SquareImageView(Context context) {
        super(context);
        byWidth = true;
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttributes(context, attrs, 0, 0);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttributes(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressWarnings("all")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (byWidth)
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        else
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (byWidth)
            super.onSizeChanged(w, w, oldw, oldh);
        else
            super.onSizeChanged(h, h, oldw, oldh);
    }

    private void getAttributes(Context context, AttributeSet attrs, int defStyleAttr,
                               int defStyleRes) {
        TypedArray array = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.SquareImageView, defStyleAttr,
                        defStyleRes);
        try {
            byWidth = array.getBoolean(R.styleable.SquareImageView_byWidth, true);
        } finally {
            array.recycle();
        }
    }
}
