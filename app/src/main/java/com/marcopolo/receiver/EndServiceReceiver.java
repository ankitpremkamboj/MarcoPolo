package com.marcopolo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.marcopolo.main.MarcoPoloApplication;

public class EndServiceReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            this.context = context;
            //  Toast.makeText(context, "Service Canceled", Toast.LENGTH_LONG).show();
            MarcoPoloApplication.getInstance().stopVRService();
            MarcoPoloApplication.getInstance().updateToAndFromTime();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
