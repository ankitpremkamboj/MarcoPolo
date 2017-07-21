package com.marcopolo.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ranjana on 18/10/16.
 */

public class OpenSansLightTxt extends android.support.v7.widget.AppCompatTextView {
    public OpenSansLightTxt(Context context) {
        super(context);
        init();
    }

    public OpenSansLightTxt(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OpenSansLightTxt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "OpenSans-Light.ttf");
        setTypeface(tf);
    }

}
