package com.marcopolo.fonts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.marcopolo.utils.AppConstants;

import static java.security.AccessController.getContext;

/**
 * Created by kamran on 10/3/17.
 */

@SuppressLint("AppCompatCustomView")
public class OpensansSemibold extends TextView {

    public OpensansSemibold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public OpensansSemibold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OpensansSemibold(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), AppConstants.OPEN_SANS_SEMIBOLD);
        setTypeface(tf ,1);

    }

}