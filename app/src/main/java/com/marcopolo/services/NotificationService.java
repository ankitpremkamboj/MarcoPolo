package com.marcopolo.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.marcopolo.R;
import com.marcopolo.screens.SplashScreen;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;

/**
 * Created by Renjith Krishnan on 16-Aug-16.
 */
public class NotificationService extends Service {

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent != null) {
                if (intent.getAction().equals(AppConstants.STARTFOREGROUND_ACTION)) {
                    try {
                        showNotification();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (intent.getAction().equals(AppConstants.PAUSE_ACTION)) {
                    SharedPreferenceWriter.getInstance(NotificationService.this).writeBooleanValue(SPreferenceKey.IS_POWER_ON, false);
                    clearNotification();

                    showResumeNotification();

                    Intent BBintent = new Intent("my.action.string");
                    BBintent.putExtra("on", true);
                    sendBroadcast(BBintent);

                    try {
                        if (null != CMUVoiceRecognitionService.mPlayer) {
                            if (CMUVoiceRecognitionService.mPlayer.isPlaying()) {
                                CMUVoiceRecognitionService.mPlayer.stop();
                            }
                            // CMUVoiceRecognitionService.mPlayer.reset();
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                } else if (intent.getAction().equals(AppConstants.STOP_ACTION)) {
                    SharedPreferenceWriter.getInstance(NotificationService.this)
                            .writeBooleanValue(SPreferenceKey.IS_POWER_ON, !SharedPreferenceWriter
                                    .getInstance(NotificationService.this)
                                    .getBoolean(SPreferenceKey.IS_POWER_ON));

                    views.setTextViewText(R.id.stop_tv, SharedPreferenceWriter
                            .getInstance(NotificationService.this)
                            .getBoolean(SPreferenceKey.IS_POWER_ON) ? "STOP" : "RESUME");

                    if (SharedPreferenceWriter
                            .getInstance(NotificationService.this)
                            .getBoolean(SPreferenceKey.IS_POWER_ON)) showNotification();
                    else clearNotification();

                    Intent BBintent = new Intent("my.action.string");
                    BBintent.putExtra("on", true);
                    sendBroadcast(BBintent);
                } else if (intent.getAction().equals(AppConstants.STOPFOREGROUND_ACTION)) {
//                Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
                    stopForeground(true);
                    stopSelf();
                } else if (intent.getAction().equals(AppConstants.START_ACTION)) {
                    clearNotification();
                } else if (intent.getAction().equals(AppConstants.RESTART_ACTION)) {
                    showNotification();
                    Intent BBintent = new Intent("my.action.string");
                    BBintent.putExtra("on", true);
                    sendBroadcast(BBintent);
                } else if (intent.getAction().equals(AppConstants.RESUME_ACTION)) {
                    SharedPreferenceWriter.getInstance(NotificationService.this)
                            .writeBooleanValue(SPreferenceKey.IS_POWER_ON, !SharedPreferenceWriter
                                    .getInstance(NotificationService.this)
                                    .getBoolean(SPreferenceKey.IS_POWER_ON));
                    showNotification();
                    Intent BBintent = new Intent("my.action.string");
                    BBintent.putExtra("on", true);
                    sendBroadcast(BBintent);
                } else if (intent.getAction().equals(AppConstants.SETTINGS_SHOW)) {
                    if (SharedPreferenceWriter.getInstance(NotificationService.this)
                            .getBoolean(SPreferenceKey.IS_POWER_ON)) showNotification();
                } else if (intent.getAction().equals(AppConstants.SETTINGS_HIDE)) {
                    clearNotification();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    private RemoteViews views = null;
    Notification status;


    private void showNotification() {
        try {


            if (SharedPreferenceWriter
                    .getInstance(NotificationService.this)
                    .getBoolean(SPreferenceKey.PUSH_FIXED_NOTIFICATION_STATUS)) {
// Using RemoteViews to bind custom layouts into Notification
                views = new RemoteViews(getPackageName(), R.layout.layout_notification);

                Intent notificationIntent = new Intent(this, SplashScreen.class);
                notificationIntent.setAction(AppConstants.MAIN_ACTION);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Intent pauseIntent = new Intent(this, NotificationService.class);
                pauseIntent.setAction(AppConstants.PAUSE_ACTION);
                PendingIntent pPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
                views.setOnClickPendingIntent(R.id.pause_tv, pPauseIntent);
//
                Intent stopIntent = new Intent(this, NotificationService.class);
                stopIntent.setAction(AppConstants.STOP_ACTION);
                PendingIntent pStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);
                views.setOnClickPendingIntent(R.id.stop_tv, pStopIntent);

                views.setTextViewText(R.id.stop_tv, SharedPreferenceWriter
                        .getInstance(NotificationService.this)
                        .getBoolean(SPreferenceKey.IS_POWER_ON) ? "STOP" : "RESUME");

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setSmallIcon(R.drawable.launcher);
                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                builder.setCustomContentView(views);
                builder.setCustomBigContentView(views);
                startForeground(AppConstants.FOREGROUND_SERVICE, builder.build());
            }
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        } catch (NoSuchFieldError e) {
            e.printStackTrace();
        }
    }

    private RemoteViews Rviews = null;

    private void showResumeNotification() {
        try {


            if (SharedPreferenceWriter
                    .getInstance(NotificationService.this)
                    .getBoolean(SPreferenceKey.PUSH_FIXED_NOTIFICATION_STATUS)) {
                Rviews = new RemoteViews(getPackageName(), R.layout.layout_notification_resume);

                Intent notificationIntent = new Intent(this, SplashScreen.class);
                notificationIntent.setAction(AppConstants.MAIN_ACTION);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Intent pauseIntent = new Intent(this, NotificationService.class);
                pauseIntent.setAction(AppConstants.RESUME_ACTION);
                PendingIntent pPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
                Rviews.setOnClickPendingIntent(R.id.resume_tv, pPauseIntent);

                NotificationManager notificationManager = (NotificationManager) NotificationService.this
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setSmallIcon(R.drawable.launcher);
                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                builder.setCustomContentView(Rviews);
                builder.setCustomBigContentView(Rviews);

                notificationManager.notify(AppConstants.FOREGROUND_SERVICE, builder.build());
            }
        } catch (NoSuchFieldError noSuchFieldError) {
            noSuchFieldError.printStackTrace();
        }
    }

    private void clearNotification() {
        stopForeground(true);
    }
}