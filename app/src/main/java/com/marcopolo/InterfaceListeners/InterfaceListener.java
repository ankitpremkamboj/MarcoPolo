package com.marcopolo.InterfaceListeners;

import android.net.Uri;

import com.marcopolo.InterfaceListeners.Interfaces.CallingHomeUI;
import com.marcopolo.InterfaceListeners.Interfaces.OnActivatingFirstTab;
import com.marcopolo.InterfaceListeners.Interfaces.OnControllingDeviceVolume;
import com.marcopolo.InterfaceListeners.Interfaces.OnLoadingInterstitialAd;
import com.marcopolo.InterfaceListeners.Interfaces.OnSendingAudioPath;
import com.marcopolo.InterfaceListeners.Interfaces.OnSettingServiceTime;
import com.marcopolo.InterfaceListeners.Interfaces.isSchedulerRunning;
import com.marcopolo.InterfaceListeners.Interfaces.navigateToPage;
import com.marcopolo.InterfaceListeners.Interfaces.onErrorReceived;
import com.marcopolo.InterfaceListeners.Interfaces.onFetchingMp3Files;
import com.marcopolo.InterfaceListeners.Interfaces.onPowerButtonInitialised;

/**
 * Created by kamran on 8/3/17.
 */

public class InterfaceListener {

    private static onPowerButtonInitialised buttonInitialised;
    private static CallingHomeUI callingHomeUI;
    private static OnLoadingInterstitialAd onLoadingInterstitialAd;

    public static void getOnControllingDeviceVolume() {
        try {
            onControllingDeviceVolume.onControllingVolume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setOnControllingDeviceVolume(OnControllingDeviceVolume onControllingDeviceVolume) {
        InterfaceListener.onControllingDeviceVolume = onControllingDeviceVolume;
    }

    private static OnControllingDeviceVolume onControllingDeviceVolume;

    public static void getmOnActivatingTab(int whichTabSelected) {
        try {
            mOnActivatingTab.onActivatingTab(whichTabSelected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setmOnActivatingTab(OnActivatingFirstTab mOnActivatingTab) {
        try {
            InterfaceListener.mOnActivatingTab = mOnActivatingTab;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static OnActivatingFirstTab mOnActivatingTab;


    public static void getOnLoadingInterstitialAd() {
        onLoadingInterstitialAd.onLoadingBigAds();
    }

    public static void setOnLoadingInterstitialAd(OnLoadingInterstitialAd onLoadingInterstitialAd) {
        InterfaceListener.onLoadingInterstitialAd = onLoadingInterstitialAd;
    }

    public static void getCallingHomeUI() {
        callingHomeUI.CallingHomeUIViews();
    }

    public static void setCallingHomeUI(CallingHomeUI callingHomeUI) {
        InterfaceListener.callingHomeUI = callingHomeUI;
    }

    private static isSchedulerRunning isSchedulerRunning;

    private static onErrorReceived onErrorReceived;

    private static navigateToPage onPageNavigation;

    private static onFetchingMp3Files mOnFetchingMp3Files;

    public static void getmOnSettingServiceTime() {
        mOnSettingServiceTime.OnSettingTimeText();
    }

    public static void setmOnSettingServiceTime(OnSettingServiceTime mOnSettingServiceTime) {
        InterfaceListener.mOnSettingServiceTime = mOnSettingServiceTime;
    }

    private static OnSettingServiceTime mOnSettingServiceTime;

    public static void getmOnSendingAudioPath(Uri uri) {
        mOnSendingAudioPath.OnSendingAudio(uri);
    }

    public static void setmOnSendingAudioPath(OnSendingAudioPath mOnSendingAudioPath) {
        InterfaceListener.mOnSendingAudioPath = mOnSendingAudioPath;
    }

    private static OnSendingAudioPath mOnSendingAudioPath;

    public static void getmOnFetchingMp3Files() {
        mOnFetchingMp3Files.OnFetchingMp3();
    }

    public static void setmOnFetchingMp3Files(onFetchingMp3Files mOnFetchingMp3Files) {
        InterfaceListener.mOnFetchingMp3Files = mOnFetchingMp3Files;
    }

    public static void getButtonInitialised() {
        try {
            buttonInitialised.onPowerInitialised();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setPowertButtonInitialised(onPowerButtonInitialised buttonInitialised) {
        InterfaceListener.buttonInitialised = buttonInitialised;
    }

    ///
    public static void getOnErrorReceived(String text, String color) {
        onErrorReceived.OnErrorReceive(text, color);
    }

    public static void setOnErrorReceived(onErrorReceived onErrorReceived) {
        InterfaceListener.onErrorReceived = onErrorReceived;
    }


    //
    public static void getOnPageNavigation(int currentItem) {
        onPageNavigation.OnPageNavigation(currentItem);
    }

    public static void setOnPageNavigation(navigateToPage pageNavigation) {
        InterfaceListener.onPageNavigation = pageNavigation;
    }

    ///

    public static boolean OnSchedulerRunning() {
        return isSchedulerRunning.isScheduleTimerShowing();
    }

    public static void setOnSchedulerShowing(isSchedulerRunning isSchedulerRunning1) {
        InterfaceListener.isSchedulerRunning = isSchedulerRunning1;
    }


}
