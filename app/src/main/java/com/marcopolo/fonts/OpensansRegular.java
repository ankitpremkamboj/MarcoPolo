package com.marcopolo.fonts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.marcopolo.utils.AppConstants;

/**
 * Created by kamran on 10/3/17.
 */

public class OpensansRegular extends android.support.v7.widget.AppCompatTextView {

    public OpensansRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public OpensansRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OpensansRegular(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), AppConstants.OPEN_SANS_REGULAR);
        setTypeface(tf ,1);

    }

}