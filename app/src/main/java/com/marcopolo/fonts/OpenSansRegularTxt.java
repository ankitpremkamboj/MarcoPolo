package com.marcopolo.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.marcopolo.utils.AppConstants;

/**
 * Created by ranjana on 18/10/16.
 */

public class OpenSansRegularTxt extends android.support.v7.widget.AppCompatTextView {
    public OpenSansRegularTxt(Context context) {
        super(context);
        init();
    }

    public OpenSansRegularTxt(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OpenSansRegularTxt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
       /* Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "OpenSans-Regular.ttf");*/

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), AppConstants.OPEN_SANS_REGULAR);
        //  setTypeface(tf ,1);
        setTypeface(tf);
    }

}
