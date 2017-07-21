package com.marcopolo.main;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.marcopolo.R;
import com.marcopolo.services.CMUVoiceRecognitionService;
import com.marcopolo.services.GoogleVoiceService;
import com.marcopolo.services.WakeUpService;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.CalendarUtils;
import com.marcopolo.utils.Constants;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import io.fabric.sdk.android.Fabric;

@ReportsCrashes(mailTo = "crash@gofindmarco.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.app_name)
public class MarcoPoloApplication extends Application {
    private static MarcoPoloApplication marcoApp;

    private boolean isInHomeScreen = false;

    public boolean isInHomeScreen() {
        return isInHomeScreen;
    }

    public void setInHomeScreen(boolean isInHomeScreen) {
        this.isInHomeScreen = isInHomeScreen;
    }

    public static MarcoPoloApplication getInstance() {
        try {
            if (null != marcoApp) {
                return marcoApp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String topPackageName = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        marcoApp = (MarcoPoloApplication) getApplicationContext();
        FlurryAgent.init(this, Constants.FLURRY_KEY);
        FlurryAgent.setLogEnabled(true);
        ACRA.init(MarcoPoloApplication.this);
    }

    public void startVRService() {
        try {
            if (SharedPreferenceWriter.getInstance(getApplicationContext()).getBoolean(SPreferenceKey.IS_POWER_ON)) {
                if (isValidTimeStamp() || isInHomeScreen()) {
                    if (!isMyServiceRunning(CMUVoiceRecognitionService.class)) {
                        Intent intent = new Intent(MarcoPoloApplication.getInstance(), CMUVoiceRecognitionService.class);
                        startService(intent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGVService() {
        try {
            if (SharedPreferenceWriter.getInstance(getApplicationContext()).getBoolean(SPreferenceKey.IS_POWER_ON)) {
                if (isValidTimeStamp() || isInHomeScreen()) {
                    if (!isMyServiceRunning(GoogleVoiceService.class)) {
                        Intent intent = new Intent(MarcoPoloApplication.getInstance(), GoogleVoiceService.class);
                        startService(intent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopGVService() {
        try {
            if (isMyServiceRunning(GoogleVoiceService.class)) {
                Intent intent = new Intent(MarcoPoloApplication.getInstance(), GoogleVoiceService.class);
                stopService(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void restartGVService() {

        try {
            if (SharedPreferenceWriter.getInstance(getApplicationContext()).getBoolean(SPreferenceKey.IS_POWER_ON)) {
                if (isValidTimeStamp() || isInHomeScreen()) {
                    stopGVService();
                    startGVService();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void stopVRService() {
        try {
            // if (isValidTimeStamp() &&
            // SharedPreferenceWriter.getInstance(getApplicationContext()).getBoolean(SPreferenceKey.IS_POWER_ON))
            // {
            if (isMyServiceRunning(CMUVoiceRecognitionService.class)) {
                Intent intent = new Intent(MarcoPoloApplication.getInstance(), CMUVoiceRecognitionService.class);
                stopService(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restartVRService() {

        try {
            if (SharedPreferenceWriter.getInstance(getApplicationContext()).getBoolean(SPreferenceKey.IS_POWER_ON)) {
                if (isValidTimeStamp() || isInHomeScreen()) {
                    stopVRService();
                    startVRService();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startWakeUpService() {
        try {
            if (!isMyServiceRunning(WakeUpService.class)) {
                Intent intent = new Intent(MarcoPoloApplication.getInstance(), WakeUpService.class);
                startService(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopWakeUpService() {
        if (isMyServiceRunning(WakeUpService.class)) {
            Intent intent = new Intent(MarcoPoloApplication.getInstance(), WakeUpService.class);
            stopService(intent);
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isResourceFree() {
        try {
            ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//			PackageInfo packageInfo = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //noinspection ResourceType
                UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
                long time = System.currentTimeMillis();
                List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
                if (stats != null) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                    for (UsageStats usageStats : stats) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
                        topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        SharedPreferenceWriter.getInstance(getApplicationContext()).writeStringValue(SPreferenceKey.LAST_PACKAGE_NAME, topPackageName);
                    }
                }
            } else {
                topPackageName = ((ActivityManager.RunningTaskInfo) mActivityManager.getRunningTasks(1).get(0)).topActivity.getPackageName();
                SharedPreferenceWriter.getInstance(getApplicationContext()).writeStringValue(SPreferenceKey.LAST_PACKAGE_NAME, topPackageName);
            }
            PackageInfo packageInfo = getPackageManager().getPackageInfo(SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.LAST_PACKAGE_NAME)
                    , PackageManager.GET_PERMISSIONS);

//            new ComponentName(mActivityClass.getPackage().getName(), mActivityClass.getName());
//            new ComponentName(getInstrumentation().getTargetContext(), mActivityClass);
            String[] requestedPermissions = packageInfo.requestedPermissions;
            if (packageInfo.packageName.equalsIgnoreCase("com.google.android.googlequicksearchbox") || packageInfo.packageName.equalsIgnoreCase("com.marcopolo")) {
                return true;
            } else if (null != requestedPermissions) {
                for (int i = 0; i < requestedPermissions.length; i++) {
                    if (requestedPermissions[i].contains("android.permission.RECORD_AUDIO")) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
        }
        return true;

    }

    public boolean isValidTimeStamp1() {
        if (!SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.FROM_TIME_IN_MILLISECOND).isEmpty() && !SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.TO_TIME_IN_MILLISECONDS).isEmpty()) {
            Calendar fromTimeStamp = Calendar.getInstance();
            fromTimeStamp.setTimeInMillis(Long.parseLong(SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.FROM_TIME_IN_MILLISECOND)));
            Calendar toTimeStamp = Calendar.getInstance();
            toTimeStamp.setTimeInMillis(Long.parseLong(SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.TO_TIME_IN_MILLISECONDS)));

            // Validation-- If FromTime < Current Time < To Time return True
            if (CalendarUtils.getInstance().isLessThanCurrentTime(fromTimeStamp) && !CalendarUtils.getInstance().isLessThanCurrentTime(toTimeStamp)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidTimeStamp() {
        try {
            String startTime = "", endTime = "", currentTime;
            String currentDate, nextDate;

            String startTimeArry[] = SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.FROM_TIME).split("-");
            String endTimeArray[] = SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.TO_TIME).split("-");

            if (startTimeArry != null && startTimeArry.length >= 3) {
                if (startTimeArry[2].equalsIgnoreCase("0")) {
                    startTime = startTimeArry[0] + ":" + startTimeArry[1] + " " + "AM";
                } else {
                    startTime = startTimeArry[0] + ":" + startTimeArry[1] + " " + "PM";
                }

            }
            if (endTimeArray != null && endTimeArray.length >= 3) {
                if (endTimeArray[2].equalsIgnoreCase("0")) {
                    endTime = endTimeArray[0] + ":" + endTimeArray[1] + " " + "AM";
                } else {
                    endTime = endTimeArray[0] + ":" + endTimeArray[1] + " " + "PM";
                }
            }

            Date mToday = new Date();

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
            String curTime = sdf.format(mToday);
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            Date userDate = sdf.parse(curTime);

            if (end.before(start)) {
                Calendar mCal = Calendar.getInstance();
                mCal.setTime(end);
                mCal.add(Calendar.DAY_OF_YEAR, 1);
                end.setTime(mCal.getTimeInMillis());
            }
            if (userDate.after(start) && userDate.before(end)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateToAndFromTime() {
        long interval = 24 * 60 * 60 * 1000;
        long toTime = Long.parseLong(SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.TO_TIME_IN_MILLISECONDS));
        long fromTime = Long.parseLong(SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.FROM_TIME_IN_MILLISECOND));
        SharedPreferenceWriter.getInstance(getApplicationContext()).writeStringValue(SPreferenceKey.FROM_TIME_IN_MILLISECOND, String.valueOf(fromTime + interval));
        SharedPreferenceWriter.getInstance(getApplicationContext()).writeStringValue(SPreferenceKey.TO_TIME_IN_MILLISECONDS, String.valueOf(toTime + interval));
    }
}