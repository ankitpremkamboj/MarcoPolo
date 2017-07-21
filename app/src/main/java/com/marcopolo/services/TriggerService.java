package com.marcopolo.services;

import java.util.List;

import com.marcopolo.main.MarcoPoloApplication;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class TriggerService extends Service{

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		//registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		startTimer();
		return START_STICKY;
	}
	
	private void startTimer() {

		new CountDownTimer(50000,5000) {
			
			@Override
			public void onTick(long arg0) {
				if (getTopActivityName()) {
					MarcoPoloApplication.getInstance().stopVRService();

				}else {
					if (!MarcoPoloApplication.getInstance().isRestricted()) {
						MarcoPoloApplication.getInstance().startVRService();

					}
				}
			}
			
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				
			}
		}.start();
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			 boolean value =	getTopActivityName();
			 Log.d("Value", value+"");
				//MarcoPoloApplication.getInstance().startVRService();
				Log.e("ScreenOOFF", "Trigger Value is = ");
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				getTopActivityName();

				//MarcoPoloApplication.getInstance().stopVRService();

				Log.e("Screen On", "Trigger Value is = " + " " + "");
			}
		}
	};
	
	public synchronized boolean getTopActivityName() {

		try {
			Log.d("", "");
			ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
			ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
			String activityOnTop = ar.topActivity.getPackageName();
			Log.d("PackageName", activityOnTop);

			PackageInfo packageInfo = null;
			try {
				packageInfo = getPackageManager().getPackageInfo(activityOnTop, PackageManager.GET_PERMISSIONS);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			String[] requestedPermissions = packageInfo.requestedPermissions;
			if (!packageInfo.packageName.equalsIgnoreCase("com.google.android.googlequicksearchbox")) {
				if (!packageInfo.packageName.equalsIgnoreCase("com.safewords")) {
					if (requestedPermissions != null) {
						for (int i = 0; i < requestedPermissions.length; i++) {
							if (requestedPermissions[i].contains("android.permission.RECORD_AUDIO")) {
								return true;
							}
							Log.d("Pemissions", requestedPermissions[i]);
						}
					} 
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != receiver)
			unregisterReceiver(receiver);
	}
}
