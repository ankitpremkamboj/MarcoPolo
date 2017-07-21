package com.marcopolo.receiver;

import com.marcopolo.main.MarcoPoloApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class StartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "Service Hitted", Toast.LENGTH_LONG).show();
        try {
            MarcoPoloApplication.getInstance().startVRService();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

}
