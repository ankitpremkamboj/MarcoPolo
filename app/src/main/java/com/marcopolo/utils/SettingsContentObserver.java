package com.marcopolo.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import com.marcopolo.InterfaceListeners.InterfaceListener;


/**
 * Created by kamran on 6/4/17.
 */

public class SettingsContentObserver extends ContentObserver {
    int previousVolume;
    Context context;

    public SettingsContentObserver(Context c, Handler handler) {
        super(handler);
        context = c;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        int delta = previousVolume - currentVolume;

        if (delta > 0) {
            Log.d("Decreased", "dec");
            previousVolume = currentVolume;
            InterfaceListener.getOnControllingDeviceVolume();
        } else if (delta < 0) {
            Log.d("Increased", "Increased");
            previousVolume = currentVolume;
            InterfaceListener.getOnControllingDeviceVolume();
        }
    }
}