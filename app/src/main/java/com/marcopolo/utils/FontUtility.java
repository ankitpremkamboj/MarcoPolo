package com.marcopolo.utils;


import android.content.Context;
import android.widget.TextView;

public class FontUtility {

	public static FontUtility utils = null;

	public static FontUtility getInstance() {
		if (utils == null) {
			utils = new FontUtility();
		}
		return utils;
	}

	public void setTypePhase(Context ctx, TextView tv , String fontFamily_Name) {
		try {
			tv.setTypeface(FontTypeface.getTypeface(ctx, fontFamily_Name));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}


	
	
	
	
	
}
