package com.marcopolo.services;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.marcopolo.main.MarcoPoloApplication;

public class WakeUpService extends Service {

	private ActivityManager mActivityManager;
	private Timer timer;
	private String topPackageName;
	private TimerTask doAsynchronousTask = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		startServiceCheckTimer();
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		return START_STICKY;
	}

	private void startServiceCheckTimer() {

		final Handler handler = new Handler();
		timer = new Timer();
		doAsynchronousTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {

							if (MarcoPoloApplication.getInstance().isResourceFree()) {
								Log.d("ResourceFree", "ResourceFree");
								MarcoPoloApplication.getInstance().startVRService();
								MarcoPoloApplication.getInstance().stopWakeUpService();
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
			}
		};
		timer.schedule(doAsynchronousTask, 0, 3000);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
		if (doAsynchronousTask != null) {
			doAsynchronousTask.cancel();
		}
	}

}
