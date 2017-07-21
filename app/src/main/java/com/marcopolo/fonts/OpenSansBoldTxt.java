package com.marcopolo.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ranjana on 18/10/16.
 */

public class OpenSansBoldTxt extends android.support.v7.widget.AppCompatTextView {
    public OpenSansBoldTxt(Context context) {
        super(context);
        init();
    }

    public OpenSansBoldTxt(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OpenSansBoldTxt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "OpenSans-Bold.ttf");
        setTypeface(tf);
    }

}
