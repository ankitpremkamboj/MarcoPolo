package com.marcopolo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.marcopolo.main.MarcoPoloApplication;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            MarcoPoloApplication.getInstance().startVRService();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

}
