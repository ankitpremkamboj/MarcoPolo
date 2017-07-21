package com.marcopolo.screens;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.marcopolo.R;
import com.marcopolo.Tabs.TabActivity;
import com.marcopolo.receiver.EndServiceReceiver;
import com.marcopolo.receiver.StartServiceReceiver;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;
import com.marcopolo.utils.CalendarBean;
import com.marcopolo.utils.CalendarUtils;

import java.util.Calendar;

public class SplashScreen extends Activity {
    private Handler handler = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AudioManager audioManager;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        setContentView(R.layout.splash_screen);
        initViews();

    }

    public void initViews() {

        try {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (Integer.valueOf(Build.VERSION.SDK_INT) < 21) {
                super.setTheme(android.R.style.Theme_Light_NoTitleBar);
            } else {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                //window.setStatusBarColor(getResources().getColor(R.color.color_home_screen_bg));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(ContextCompat.getColor(this, R.color.color_home_screen_bg));
                }
            }
            handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    startActivity(new Intent(SplashScreen.this, TabActivity.class));
                    //startActivity(new Intent(SplashScreen.this, HomeScreen.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }, 3000);

            if (SharedPreferenceWriter.getInstance(SplashScreen.this).getString(SPreferenceKey.FROM_TIME).equalsIgnoreCase("") ||
                    SharedPreferenceWriter.getInstance(SplashScreen.this).getString(SPreferenceKey.TO_TIME).equalsIgnoreCase("")) {
                SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.FROM_TIME, "6-00-0");
                SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.TO_TIME, "11-30-1");
                setFromTimeForService(6, 00, 0);
                setToTimeForService(11, 30, 1);
            }

            if (!SharedPreferenceWriter.getInstance(SplashScreen.this).getBoolean(SPreferenceKey.IS_APP_ALREADY_LAUNCHED)) {// first
                setPrefernceValues();
            }

            SharedPreferenceWriter.getInstance(SplashScreen.this).writeBooleanValue(SPreferenceKey.IS_APP_RESPONDED, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setPrefernceValues() {
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeBooleanValue(SPreferenceKey.IS_APP_ALREADY_LAUNCHED, true);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeBooleanValue(SPreferenceKey.PUSH_FIXED_NOTIFICATION_STATUS, true);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeBooleanValue(SPreferenceKey.OVERRIDE_SILENT_STATUS, true);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeBooleanValue(SPreferenceKey.OVERRIDE_GOOGLE_STATUS, true);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeBooleanValue(SPreferenceKey.PUSH_NOTIFICATION_STATUS, true);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeIntValue(SPreferenceKey.VOLUME_STATUS, 70);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.FROM_TIME, AppConstants.DEFAULT_FROM_TIME);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.TO_TIME, AppConstants.DEFAULT_TO_TIME);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeBooleanValue(SPreferenceKey.IS_POWER_ON, true);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.RESPONSE_TYPE, AppConstants.TEXT);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.KEYPHRASE, AppConstants.DEFAULT_INPUT_PHRASE);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.REPLY_KEYPHRASE, AppConstants.DEFAULT_OUTPUT_PHRASE);
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeIntValue(SPreferenceKey.SENSITIVITY_VALUE_STATUS,
                countSyllables(SharedPreferenceWriter.getInstance(SplashScreen.this).getString(SPreferenceKey.KEYPHRASE)));

        Log.e("Sens", SharedPreferenceWriter.getInstance(SplashScreen.this).getInt(SPreferenceKey.SENSITIVITY_VALUE_STATUS) + "");

        try {
            SetVolumeto70Percent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void SetVolumeto70Percent() {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        float cursorVolumeto70Percent = ((0.7f) * maxVolume);
        int cursorVolumeto70PercentInteger = Math.round(cursorVolumeto70Percent);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, cursorVolumeto70PercentInteger, 0);
    }

    private void setToTimeForService(int hour, int min, int ampm) {
        Calendar toTimeStamp = Calendar.getInstance();
        toTimeStamp.set(Calendar.HOUR_OF_DAY, hour);
        toTimeStamp.set(Calendar.MINUTE, min);
        toTimeStamp.set(Calendar.SECOND, 0);
        if (CalendarUtils.getInstance().isLessThanCurrentTime(toTimeStamp)) {
            toTimeStamp.add(Calendar.DATE, 1); // Next Day
        }
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.TO_TIME_IN_MILLISECONDS, String.valueOf(toTimeStamp.getTimeInMillis()));

        AlarmManager alarmMgr = (AlarmManager) SplashScreen.this.getSystemService(Context.ALARM_SERVICE);

        Intent endService = new Intent(SplashScreen.this, EndServiceReceiver.class);
        PendingIntent endServiceInetent = PendingIntent.getBroadcast(SplashScreen.this, 0, endService, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, toTimeStamp.getTimeInMillis(), AlarmManager.INTERVAL_DAY, endServiceInetent);

        // Validate From Time Stamp
        if (!SharedPreferenceWriter.getInstance(SplashScreen.this).getString(SPreferenceKey.FROM_TIME_IN_MILLISECOND).isEmpty()) {
            Calendar fromTimeStamp = Calendar.getInstance();
            fromTimeStamp.setTimeInMillis(Long.parseLong(SharedPreferenceWriter.getInstance(SplashScreen.this).getString(SPreferenceKey.FROM_TIME_IN_MILLISECOND)));
            if (CalendarUtils.getInstance().isLessThanCurrentTime(fromTimeStamp)) {
                fromTimeStamp.add(Calendar.DATE, 1);
            }
            SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.FROM_TIME_IN_MILLISECOND, String.valueOf(fromTimeStamp.getTimeInMillis()));

            Intent startService = new Intent(SplashScreen.this, StartServiceReceiver.class);
            PendingIntent startServiceInatent = PendingIntent.getBroadcast(SplashScreen.this, 0, startService, 0);
            alarmMgr.cancel(startServiceInatent);
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, fromTimeStamp.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startServiceInatent);
        }

    }

    // add condition for starting svervice after 24 hour in end service
    private void setFromTimeForService(int hour, int min, int ampm) {
        // Validate From Time
        Calendar fromTime = Calendar.getInstance();
        fromTime.set(Calendar.HOUR_OF_DAY, hour);
        fromTime.set(Calendar.MINUTE, min);
        fromTime.set(Calendar.SECOND, 0);
        if (CalendarUtils.getInstance().isLessThanCurrentTime(fromTime)) {
            fromTime.add(Calendar.DATE, 1);
        }
        SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.FROM_TIME_IN_MILLISECOND, String.valueOf(fromTime.getTimeInMillis()));
        Intent startService = new Intent(SplashScreen.this, StartServiceReceiver.class);
        PendingIntent startServiceInatent = PendingIntent.getBroadcast(SplashScreen.this, 0, startService, 0);
        AlarmManager alarmMgr = (AlarmManager) SplashScreen.this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + fromTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startServiceInatent);

        // Validating To Time
        if (!SharedPreferenceWriter.getInstance(SplashScreen.this).getString(SPreferenceKey.TO_TIME_IN_MILLISECONDS).isEmpty()) {
            Calendar toTimeStamp = Calendar.getInstance();
            toTimeStamp.setTimeInMillis(Long.parseLong(SharedPreferenceWriter.getInstance(SplashScreen.this).getString(SPreferenceKey.TO_TIME_IN_MILLISECONDS)));
            CalendarBean calendarBean = CalendarUtils.getInstance().getCalendarData(toTimeStamp);
            Calendar toTime = Calendar.getInstance();
            toTime.set(Calendar.HOUR_OF_DAY, calendarBean._hour);
            toTime.set(Calendar.MINUTE, calendarBean._minute);
            toTime.set(Calendar.SECOND, 0);
            if (CalendarUtils.getInstance().is_A_LessThan_B(toTime, fromTime)) {
                toTime.add(Calendar.DATE, 1);
            }
            SharedPreferenceWriter.getInstance(SplashScreen.this).writeStringValue(SPreferenceKey.TO_TIME_IN_MILLISECONDS, String.valueOf(toTime.getTimeInMillis()));
            Intent endService = new Intent(SplashScreen.this, EndServiceReceiver.class);
            PendingIntent endServiceInetent = PendingIntent.getBroadcast(SplashScreen.this, 0, endService, 0);
            alarmMgr.cancel(endServiceInetent);
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + toTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, endServiceInetent);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    public int countSyllables(String word) {
        int syl = 0;
        boolean vowel = false;
        int length = word.length();

        // check each word for vowels (don't count more than one vowel in a row)
        for (int i = 0; i < length; i++) {
            if (isVowel(word.charAt(i)) && (vowel == false)) {
                vowel = true;
                syl++;
            } else if (isVowel(word.charAt(i)) && (vowel == true)) {
                vowel = true;
            } else {
                vowel = false;
            }
        }

        char tempChar = word.charAt(word.length() - 1);
        // check for 'e' at the end, as long as not a word w/ one syllable
        if (((tempChar == 'e') || (tempChar == 'E')) && (syl != 1)) {
            syl--;
        }
        return (syl * 100 / 4);
    }

    // check if a char is a vowel (count y)
    public static boolean isVowel(char c) {
        if ((c == 'a') || (c == 'A')) {
            return true;
        } else if ((c == 'e') || (c == 'E')) {
            return true;
        } else if ((c == 'i') || (c == 'I')) {
            return true;
        } else if ((c == 'o') || (c == 'O')) {
            return true;
        } else if ((c == 'u') || (c == 'U')) {
            return true;
        } else if ((c == 'y') || (c == 'Y')) {
            return true;
        } else {
            return false;
        }
    }
}