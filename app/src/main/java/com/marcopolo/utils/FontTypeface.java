package com.marcopolo.utils;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;

public class FontTypeface {

	private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();

    public static Typeface getTypeface(Context context , String name) {
        Typeface tf = fontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), name);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }

}