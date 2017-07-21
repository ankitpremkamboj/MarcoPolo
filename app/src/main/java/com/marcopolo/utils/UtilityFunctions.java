package com.marcopolo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.marcopolo.R;

import java.net.InetAddress;

public class UtilityFunctions {
    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }

    }

    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static void showLongToast(Activity activity, String message) {
        //Toast.makeText(activity, "" + message, Toast.LENGTH_LONG).show();

        String title = "<font color=#359af6>" + message + "</font>";


        //String dummy=activity.getResources().getString(R.string.sample_string);

        AlertDialog.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth);
        } else {
            builder = new AlertDialog.Builder(activity, R.style.YourAlertDialogTheme);
        }
        //builder.setTitle(Html.fromHtml(title));
        builder.setMessage(Html.fromHtml(message));


        builder.setPositiveButton(activity.getResources().getString(R.string.got_it), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialog.cancel();
                    }
                }
        );

        //builder.show();
        AlertDialog alert = builder.create();
        alert.show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#359af6"));
        }

    }
}