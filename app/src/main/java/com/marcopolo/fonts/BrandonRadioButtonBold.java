package com.marcopolo.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.TextView;

import com.marcopolo.utils.AppConstants;

/**
 * Created by ranjana on 18/10/16.
 */

public class BrandonRadioButtonBold extends android.support.v7.widget.AppCompatRadioButton {
    public BrandonRadioButtonBold(Context context) {
        super(context);
        init();
    }

    public BrandonRadioButtonBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BrandonRadioButtonBold(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                AppConstants.BRANDON_BOLD);
        setTypeface(tf);
    }

}
