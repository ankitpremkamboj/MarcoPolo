package com.marcopolo.Tabs;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.marcopolo.Adapters.TabsPagerAdapter;
import com.marcopolo.InterfaceListeners.InterfaceListener;
import com.marcopolo.InterfaceListeners.Interfaces.OnLoadingInterstitialAd;
import com.marcopolo.InterfaceListeners.Interfaces.navigateToPage;
import com.marcopolo.InterfaceListeners.Interfaces.onFetchingMp3Files;
import com.marcopolo.R;
import com.marcopolo.callbacks.MyDialogCloseListener;
import com.marcopolo.main.MarcoPoloApplication;
import com.marcopolo.receiver.FalseAlarmPopUpReceiver;
import com.marcopolo.services.CMUVoiceRecognitionService;
import com.marcopolo.services.NotificationService;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;
import com.marcopolo.utils.Constants;
import com.marcopolo.utils.SettingsContentObserver;
import com.morcopolo.fragments.FalseAlarmFragment;
import com.morcopolo.fragments.Fragmentss.SlidingTabLayout;
import com.morcopolo.fragments.Fragmentss.TabViewPager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kamran on 3/3/17.
 */

public class TabActivity extends FragmentActivity implements ActionBar.TabListener, navigateToPage, onFetchingMp3Files, OnLoadingInterstitialAd, MyDialogCloseListener {
    public TabViewPager viewPager;
    private TabsPagerAdapter tabsPagerAdapter;
    //private ActionBar actionBar;
    private String[] tabNames = {"Home", "Edit", "Voice/Mp3", "Setting"};
    private int[] icons = {R.drawable.home_selector, R.drawable.phone_selector, R.drawable.microphone_selector, R.drawable.settings_selector};
    //private String[] tabs = {"Home", "Edit", "Voice/Mp3", "Setting"};
    public static Activity activitys;
    private AdView mAdView;
    SlidingTabLayout slidingTabLayout;
    Context mContext;
    private BroadcastReceiver mReceiver;

    private static final int REQ_CODE_PICK_SOUNDFILE = 567;
    private InterstitialAd mInterstitialAd;

    private InterstitialAd mInterstitialAd_replywith;
    AdRequest adRequestInter, adRequestInter_replywith;
    //public static boolean isInPencilMode;

    public static boolean isOnHome = true;
    private Activity activity;
    private SettingsContentObserver mSettingsContentObserver;

    //public static boolean isServiceRunning ;

    public void SetAdapterViewPager() {
        viewPager.setAdapter(tabsPagerAdapter);
    }

    FalseAlarmPopUpReceiver falseAlarmPopUp = new FalseAlarmPopUpReceiver();
    IntentFilter mIntentFilter;


    public static FragmentManager fragmentManager;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab);

        mContext = this;
        activity = this;
        activitys = TabActivity.this;

        fragmentManager = getSupportFragmentManager();

        if (Integer.valueOf(Build.VERSION.SDK_INT) < 21) {
            super.setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
            //Theme.Holo.Light.NoActionBar
        } else {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
          //  window.setStatusBarColor(getResources().getColor(R.color.sliding_selectedtabs));
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.sliding_selectedtabs));

        }

        viewPager = (TabViewPager) findViewById(R.id.pager);
        tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager(), tabNames, tabNames.length, icons, TabActivity.this);
        viewPager.setAdapter(tabsPagerAdapter);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);

        slidingTabLayout.setSelectedIndicatorColors(R.color.color_transparent);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);
        viewPager.setPagingEnabled(false);


        InterfaceListener.setOnPageNavigation(this);
        InterfaceListener.setmOnFetchingMp3Files(this);
        InterfaceListener.setOnLoadingInterstitialAd(this);

        loadAds();

        initViews();

        LoadInterstitialAd();
        LoadInterstitialAd_ReplyWith();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.truiton.broadcast.string");


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    InterfaceListener.getButtonInitialised();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000);


        try {
            mSettingsContentObserver = new SettingsContentObserver(this, new Handler());
            getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            slidingTabLayout.setViewPager(viewPager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDialogLayout(String heading) {
        try {


            /**
             * If App is Enabled
             */
            if (SharedPreferenceWriter.getInstance(activity).getBoolean(SPreferenceKey.IS_POWER_ON)) {
                /**
                 * If It is on home screen.
                 */
                if (isOnHome) {
                    FalseAlarmFragment falseAlarmFragment = new FalseAlarmFragment().newInstance(heading, this);
                    falseAlarmFragment.show(getSupportFragmentManager(), "FalseAlarmFragment");
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }


    private void LoadInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        adRequestInter = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequestInter);
    }


    private void LoadInterstitialAd_ReplyWith() {
        mInterstitialAd_replywith = new InterstitialAd(this);
        mInterstitialAd_replywith.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id_reply_with));
        adRequestInter_replywith = new AdRequest.Builder().build();

        mInterstitialAd_replywith.loadAd(adRequestInter_replywith);
    }


    private void initViews() {

        MarcoPoloApplication.getInstance().setInHomeScreen(true);

        try {

            Log.e("text", "register");
            registerReceiver(receiverForError, new IntentFilter("Send_Error_Message"));
            Log.e("text", "register1");
        } catch (Exception e) {
            e.printStackTrace();
        }


        IntentFilter intentFilter = new IntentFilter("my.action.string");
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                InterfaceListener.getButtonInitialised();
                Log.e("text", "register_receive");
            }
        };

        //registering our receiver
        try {
            TabActivity.this.registerReceiver(mReceiver, intentFilter);
            Log.e("text", "register_intent");
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (handler != null) {
                    handler.removeCallbacks(animateViewPager);
                }
                handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
            }
        }).start();
    }


    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
// TODO Auto-generated method stub

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
// TODO Auto-generated method stub

    }


    private void loadAds() {

        try {
            mAdView = (AdView) findViewById(R.id.ad_view);
            AdRequest adRequest = new AdRequest.Builder()
                    //                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("BC66CD84AE30139003DB301E2AB6525F")
                    .build();

            mAdView.loadAd(adRequest);
            // Load ads into Interstitial Ads
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        //startAppAd.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        registerReceiver(falseAlarmPopUp, mIntentFilter);
        tabsPagerAdapter.notifyDataSetChanged();


    }

    @Override
    public void onPause() {

        if (mAdView != null) {
            mAdView.pause();
        }

        if (!isBetween()) {
            Intent serviceIntent = new Intent(this, NotificationService.class);
            serviceIntent.setAction(AppConstants.PAUSE_ACTION);
            startService(serviceIntent);
            MarcoPoloApplication.getInstance().stopVRService();
            MarcoPoloApplication.getInstance().stopWakeUpService();
        }


        super.onPause();
    }


    @Override
    public void onDestroy() {

        //  isServiceRunning = false;

        super.onDestroy();
        try {
            MarcoPoloApplication.getInstance().setInHomeScreen(false);
            if (!MarcoPoloApplication.getInstance().isValidTimeStamp()) {
                MarcoPoloApplication.getInstance().stopVRService();
            }

            try {
                FlurryAgent.onStartSession(activity);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mAdView != null) {
                mAdView.destroy();
            }

            if (receiverForError != null) {
                unregisterReceiver(receiverForError);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        try {
            getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    BroadcastReceiver receiverForError = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {


            try {
                if (intent.getAction().equals("Send_Error_Message")) {
                    Log.d("start", intent.getAction() + "122");
                } else {
                    Log.d("start", intent.getAction() + "333");
                }
                Log.d("start", intent.getData() + "");
                final String text;
                if (intent != null && intent.getStringExtra("message") != null) {
                    text = intent.getStringExtra("message");

                    Log.e("text", "" + text);

                    if (text.equalsIgnoreCase(Constants.ERROR_MESSAGE)) {
                        InterfaceListener.getOnErrorReceived(text, "#D97245");
                    } else {
                        if (text.equalsIgnoreCase(Constants.MESSAGE_PREPARE_RECOGNISER)) {

                            InterfaceListener.getOnErrorReceived(text, "#71E7EF");

                        } else {

                            try {
                                InterfaceListener.getOnErrorReceived(text, "#71E7EF");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //}
        }
    };


    /**
     * This Method will help navigate the fragment through view pagwer
     *
     * @param currentPage We will pass current page to which we have to navigate
     */
    @Override
    public void OnPageNavigation(int currentPage) {
        viewPager.setCurrentItem(currentPage);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_POWER) {
            try {
                if (null != CMUVoiceRecognitionService.mPlayer) {
                    if (CMUVoiceRecognitionService.mPlayer.isPlaying()) {
                        CMUVoiceRecognitionService.mPlayer.stop();
                        CMUVoiceRecognitionService.mPlayer.reset();
                    }

                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            try {
                if (InterfaceListener.OnSchedulerRunning()) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onBackPressed() {

        try {
            if (viewPager.getCurrentItem() > 0)
                viewPager.setCurrentItem(Constants.CURRENT_ITEM_ZERO);
            else {
                try {
                    LoadInterstital();
                    if (!isBetween()) {
                        Intent serviceIntent = new Intent(this, NotificationService.class);
                        serviceIntent.setAction(AppConstants.PAUSE_ACTION);
                        startService(serviceIntent);
                        MarcoPoloApplication.getInstance().stopVRService();
                        MarcoPoloApplication.getInstance().stopWakeUpService();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //finish();

    }


    private void LoadInterstital() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mInterstitialAd != null) {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 200);
    }

    private void LoadInterstital_ReplyWith() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mInterstitialAd_replywith != null) {
                        if (mInterstitialAd_replywith.isLoaded()) {
                            mInterstitialAd_replywith.show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 200);
    }


    public void setNewPhrase() {
        if (SharedPreferenceWriter.getInstance(TabActivity.this).getString(SPreferenceKey.RESPONSE_TYPE).contains(AppConstants.TEXT)) {
            String marcoText = SharedPreferenceWriter.getInstance(TabActivity.this).getString(SPreferenceKey.KEYPHRASE).isEmpty() ? AppConstants.DEFAULT_INPUT_PHRASE.toUpperCase(Locale.getDefault()) : SharedPreferenceWriter.getInstance(TabActivity.this).getString(SPreferenceKey.KEYPHRASE);
            String poloText = SharedPreferenceWriter.getInstance(TabActivity.this).getString(SPreferenceKey.REPLY_KEYPHRASE).isEmpty() ? AppConstants.DEFAULT_OUTPUT_PHRASE.toUpperCase(Locale.getDefault()) : SharedPreferenceWriter.getInstance(TabActivity.this).getString(SPreferenceKey.REPLY_KEYPHRASE);
            ((TextView) findViewById(R.id.tv_DetailText)).setText(Html.fromHtml("<font color=#ffffff>Call out </font><font color=#eda503>" + marcoText.toUpperCase(Locale.getDefault()) + "!</font><font color=#ffffff> and<br>your Phone will reply with </font><font color=#D97245>" + poloText.toUpperCase(Locale.getDefault()) + "!"));

        } else {
            String marcoText = SharedPreferenceWriter.getInstance(TabActivity.this).getString(SPreferenceKey.KEYPHRASE).isEmpty() ? AppConstants.DEFAULT_INPUT_PHRASE.toUpperCase(Locale.getDefault()) : SharedPreferenceWriter.getInstance(TabActivity.this).getString(SPreferenceKey.KEYPHRASE).toUpperCase(Locale.getDefault());
            ((TextView) findViewById(R.id.tv_DetailText)).setText(Html.fromHtml("<font color=#ffffff>Call out </font><font color=#eda503>" + marcoText + "!</font><font color=#ffffff> and<br>your Phone will reply with </font><font color=#D97245>" + "(recording)"));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK) {
                if ((data != null) && (data.getData() != null)) {

                    // Now you can use that Uri to get the file path, or upload it, ...
                    Uri audioFileUri = data.getData();
                    InterfaceListener.getmOnSendingAudioPath(audioFileUri);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnFetchingMp3() {
        Intent intent;
        intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_audio_file_title)), REQ_CODE_PICK_SOUNDFILE);
    }

    @Override
    public void onLoadingBigAds() {
        //mInterstitialAd.loadAd(adRequestInter);
        //LoadInterstital();
        try {
            LoadInterstital_ReplyWith();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onStart() {
        try {
            // FlurryAgent.onStartSession(this, Constants.FLURRY_KEY);
            FlurryAgent.init(this, Constants.FLURRY_KEY);

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();

        // your code
    }

    public void onStop() {
        super.onStop();
        try {
            FlurryAgent.onEndSession(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            unregisterReceiver(falseAlarmPopUp);
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // your code
    }

    /**
     * Declaring Time for animation of View Pager
     */
    private int ANIM_VIEWPAGER_DELAY = 14000;
    /** Declaring Time for animation of View Pager */
//    private int CHAT_DELAY =  300000;
    /**
     * declare Handler for Animating View Pager
     */
    private Handler handler = new Handler();

    /**
     * creating Runnable Interface for View Pager Animator
     */
    private Runnable animateViewPager = new Runnable() {
        public void run() {
            try {
                if (!SharedPreferenceWriter.getInstance(TabActivity.this).getBoolean(SPreferenceKey.IS_APP_RESPONDED)) {
                    showDialogLayout("No Response?");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void handleDialogClose() {
        InterfaceListener.getCallingHomeUI();
        InterfaceListener.getmOnSettingServiceTime();

    }


    private boolean isBetween() {
        String[] fromTime = SharedPreferenceWriter.getInstance(this).getString(SPreferenceKey.FROM_TIME).split("-");
        String[] toTime = SharedPreferenceWriter.getInstance(this).getString(SPreferenceKey.TO_TIME).split("-");

        int from = getTimeString(fromTime[0], fromTime[1], fromTime[2]);//2300;
        int to = getTimeString(toTime[0], toTime[1], toTime[2]); //800;
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int t = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE);
        boolean isBetween = to > from && t >= from && t <= to || to < from && (t >= from || t <= to);
        Log.e("isBetween", "" + isBetween);
        return isBetween;
    }

    private int getTimeString(String hour, String min, String ampm) {
        NumberFormat numberFormat = new DecimalFormat("00");
        StringBuilder builder = new StringBuilder();
        if (Integer.parseInt(ampm) == 0) {
            builder.append(numberFormat.format(Integer.parseInt(hour)) + numberFormat.format(Integer.parseInt(min)));
        } else {
            builder.append(numberFormat.format(12 + Integer.parseInt(hour)) + numberFormat.format(Integer.parseInt(min)));
        }
        return Integer.parseInt(builder.toString());
    }


}