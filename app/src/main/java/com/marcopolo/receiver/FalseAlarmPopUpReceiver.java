package com.marcopolo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.marcopolo.Tabs.TabActivity;
import com.marcopolo.callbacks.MyDialogCloseListener;
import com.marcopolo.services.NotificationService;
import com.marcopolo.utils.AppConstants;
import com.morcopolo.fragments.FalseAlarmFragment;

/**
 * Created by ankit on 4/19/2017.
 */

public class FalseAlarmPopUpReceiver extends BroadcastReceiver implements MyDialogCloseListener {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals("com.truiton.broadcast.string")) {

                FalseAlarmFragment falseAlarmFragment = new FalseAlarmFragment().newInstance("False Alarm?", this);
                falseAlarmFragment.show(TabActivity.fragmentManager, "FalseAlarmFragment");

            }

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleDialogClose() {

    }
}
