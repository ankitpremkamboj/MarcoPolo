package com.marcopolo.services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.marcopolo.R;
import com.marcopolo.main.MarcoPoloApplication;
import com.marcopolo.screens.SplashScreen;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;
import com.marcopolo.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class CMUVoiceRecognitionService extends Service implements RecognitionListener, TextToSpeech.OnInitListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String PHONE_SEARCH = "phones";
    private static final String MENU_SEARCH = "menu";

    /* Keyword we are looking for to activate menu */
    private static String KEYPHRASE = "marco";

    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private ActivityManager mActivityManager;
    private int originalVolume;
    private AsyncTask<Void, Void, Exception> asyncTask;
    //   public static MediaPlayer mPlayer = null;
    public static MediaPlayer mPlayer = new MediaPlayer();

    private Handler handler = new Handler();
    private int ANIM_VIEWPAGER_DELAY = 1000;
    int value = 1;
    boolean first = true;

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // FlurryAgent.onStartSession(this, Constants.FLURRY_KEY);
        try {


            FlurryAgent.init(this, Constants.FLURRY_KEY);
            if (!TextUtils.isEmpty(SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.KEYPHRASE))) {
                KEYPHRASE = SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.KEYPHRASE).toLowerCase(Locale.getDefault());
            } else {
                KEYPHRASE = AppConstants.DEFAULT_INPUT_PHRASE.toLowerCase(Locale.getDefault());
            }
            tts = new TextToSpeech(this, this);
            tts.setOnUtteranceProgressListener(utteranceProgressListener);
/*
            if (SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getBoolean(SPreferenceKey.IS_POWER_ON)) {
                boolean foregroud = new ForegroundCheckTask().execute(CMUVoiceRecognitionService.this).get();
                if (!foregroud){
                    if (!isBetween()){
                        Intent serviceIntent = new Intent(CMUVoiceRecognitionService.this, NotificationService.class);
                        serviceIntent.setAction(AppConstants.PAUSE_ACTION);
                        startService(serviceIntent);
                        MarcoPoloApplication.getInstance().stopVRService();
                        MarcoPoloApplication.getInstance().stopWakeUpService();
                    }
                }
            }*/


        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        try {
            startVRService();
        } catch (Exception e) {
            e.printStackTrace();
        }
//


        return START_STICKY;
    }

    private void startVRService() {

        asyncTask = new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Intent intent = new Intent();
                    intent.putExtra("message", Constants.MESSAGE_PREPARE_RECOGNISER);
                    intent.setAction("Send_Error_Message");
                    sendBroadcast(intent);

                    if (MarcoPoloApplication.getInstance().isMyServiceRunning(CMUVoiceRecognitionService.class)) {
                        Assets assets = new Assets(CMUVoiceRecognitionService.this);
                        File assetDir = assets.syncAssets();
                        try {
                            setupRecognizer(assetDir);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                try {
                    if (result != null) {

                    } else {
                        switchSearch(KWS_SEARCH);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void setupRecognizer(File assetsDir) throws Exception {
        try {

            float thrashHoldValue = 0;
            float sensitivity = SharedPreferenceWriter.getInstance(getApplicationContext()).getInt(SPreferenceKey.SENSITIVITY_VALUE_STATUS);

            sensitivity = (int) ((sensitivity * (7) / 100) + 1);
            // The recognizer can be configured to perform multiple searches
            // of different kind and switch_on between them

            File modelsDir = new File(assetsDir, "models");
            // String chekTrashHold[] = KEYPHRASE.trim().split(" ");
            try {
                thrashHoldValue = getThrashold(sensitivity);
            } catch (Exception e) {
                e.printStackTrace();
                thrashHoldValue = 0;
            }

            recognizer = defaultSetup().setAcousticModel(new File(modelsDir, "hmm/en-us-ptm")).setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                    .setKeywordThreshold(thrashHoldValue).setBoolean("-allphone_ci", true).getRecognizer();
            recognizer.addListener(this);

            // Create keyword-activation search.
            recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);//Crashing

            // Create grammar-based search for selection between demos
            File menuGrammar = new File(modelsDir, "grammar/menu.gram");
            recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

            // Create grammar-based search for digit recognition
            File digitsGrammar = new File(modelsDir, "grammar/digits.gram");
            recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

            // Create language model search
            File languageModel = new File(modelsDir, "lm/weather.dmp");
            recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);

            // Phonetic search
            File phoneticModel = new File(modelsDir, "phone/en-phone.dmp");
            recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
            Intent intent = new Intent();
            intent.putExtra("message", "Ready...");
            intent.setAction("Send_Error_Message");
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent();
            intent.putExtra("message", Constants.ERROR_MESSAGE);
            intent.setAction("Send_Error_Message");
            sendBroadcast(intent);
        }
    }

    private void switchSearch(String searchName) {

        try {
            Log.e("Searchname", "" + searchName);
            Log.e("KWS_SEARCH", "" + KWS_SEARCH);

            recognizer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (searchName.equals(KWS_SEARCH))
                recognizer.startListening(searchName);
            else
                recognizer.startListening(searchName, 10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    @Override
    public void onError(Exception arg0) {
        try {
            Intent intent = new Intent();
            intent.putExtra("message", Constants.ERROR_MESSAGE);
            intent.setAction("Send_Error_Message");
            sendBroadcast(intent);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        if (MarcoPoloApplication.getInstance().isResourceFree()) {
            String text = hypothesis.getHypstr();
            Log.e("textinhyper__", "" + text);
            if (text.equals(KEYPHRASE)) {
                switchSearch(KWS_SEARCH);
                try {
                    if (SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getBoolean(SPreferenceKey.IS_POWER_ON)) {
                        boolean foregroud = new ForegroundCheckTask().execute(CMUVoiceRecognitionService.this).get();
                        if (foregroud) {
                            playResponse();

                        } else {
                            if (isBetween()) {
                                playResponse();
                            } else {
                                Intent serviceIntent = new Intent(CMUVoiceRecognitionService.this, NotificationService.class);
                                serviceIntent.setAction(AppConstants.PAUSE_ACTION);
                                startService(serviceIntent);
                                MarcoPoloApplication.getInstance().stopVRService();
                                MarcoPoloApplication.getInstance().stopWakeUpService();

                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            MarcoPoloApplication.getInstance().startWakeUpService();
            MarcoPoloApplication.getInstance().stopVRService();
        }
    }

    /**
     * This callback is called when we stop the recognizer.
     */

    @Override
    public void onResult(Hypothesis arg0) {
    }


    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    public void onStop() {
        // TODO Auto-generated method stub
        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        relaseResources();

    }

    private void relaseResources() {
        if (asyncTask != null) {
            asyncTask.cancel(true);
            asyncTask = null;
        }
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
            recognizer = null;
        }
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }

        try {
            if (null != mPlayer) {
                mPlayer.stop();
                mPlayer.reset();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void playResponse() {
        try {
            if (((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                if (SharedPreferenceWriter.getInstance(getBaseContext()).getBoolean(SPreferenceKey.PUSH_NOTIFICATION_STATUS)) {
                    showNotification(getBaseContext(), SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.REPLY_KEYPHRASE), null);
                }
                checkOverideSilentStatus();
                if (SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.RESPONSE_TYPE).isEmpty()
                        || SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.RESPONSE_TYPE).equalsIgnoreCase(AppConstants.TEXT)) {
                    final HashMap<String, String> myHashStream = new HashMap<String, String>();
                    myHashStream.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "1");
                    SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).writeBooleanValue(SPreferenceKey.IS_APP_RESPONDED, true);
                    if (SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.REPLY_KEYPHRASE).isEmpty()) {
                        tts.speak("polo", TextToSpeech.QUEUE_ADD, myHashStream);
                    } else {
                        tts.speak(SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.REPLY_KEYPHRASE), TextToSpeech.QUEUE_ADD, myHashStream);

                        final Timer timer2 = new Timer();
                        timer2.schedule(new TimerTask() {
                            public void run() {
                                //  tts.speak(SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.REPLY_KEYPHRASE), TextToSpeech.QUEUE_ADD, myHashStream);
                                value++;
                                timer2.cancel(); //this will cancel the timer of the system
                            }
                        }, 7000);
                        if (value >= 4) {
                            if (first) {
                                falseAlarm();
                                first = false;
                            }

                            // tts.stop();
                            value = 1;
                        }
                    }
                } else {

                    try {
                        //mPlayer = new MediaPlayer();
                        if (mPlayer != null) {
                            if (mPlayer.isPlaying()) {
                                stopAudio();
                                playAudio();
                            } else {
                                playAudio();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void falseAlarm() {

        String mBroadcastStringAction = "com.truiton.broadcast.string";
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(mBroadcastStringAction);
        broadcastIntent.putExtra("Data", "Broadcast Data");
        sendBroadcast(broadcastIntent);
    }

    private void checkOverideSilentStatus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (originalVolume == 0 && SharedPreferenceWriter.getInstance(getApplicationContext()).getBoolean(SPreferenceKey.OVERRIDE_SILENT_STATUS)) {// Silent
            int volumeStatus = ((int) (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * ((float) SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getInt(SPreferenceKey.VOLUME_STATUS) / 100)));
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeStatus, 0);
        } else if (originalVolume != 0) {
            int volumeStatus = ((int) (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * ((float) SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getInt(SPreferenceKey.VOLUME_STATUS) / 100)));
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeStatus, 0);

        }

    }

    public void playAudio() {

        try {
            //mPlayer = new MediaPlayer();
            mPlayer.setDataSource(SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.AUDIO_FILE_PATH));
            mPlayer.prepare();
            mPlayer.start();

            mPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                    stopAudio();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopAudio() {
        try {


            if (null != mPlayer) {
                mPlayer.stop();
                mPlayer.reset();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public float getThrashold(float sensitivity) {
        float thrashHoldValue = 0;

        if (sensitivity == 1)
            thrashHoldValue = 1e-10f;
        else if (sensitivity == 2)
            thrashHoldValue = 1e-10f;
        else if (sensitivity == 3)
            thrashHoldValue = 1e-15f;
        else if (sensitivity == 4)
            thrashHoldValue = 1e-20f;
        else if (sensitivity == 5)
            thrashHoldValue = 1e-30f;
        else if (sensitivity == 6)
            thrashHoldValue = 1e-35f;
        else if (sensitivity == 7)
            thrashHoldValue = 1e-40f;
        else if (sensitivity >= 8)
            thrashHoldValue = 1e-45f;

        return thrashHoldValue;

    }

    @Override
    public void onInit(int arg0) {
//        try {
//            if (arg0 == TextToSpeech.SUCCESS) {
//                int result = tts.setLanguage(Locale.US);
//                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                   // Log.e("TTS", "Language is not supported");
//                }
//            }
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }

    }

    public void showNotification(Context context, String message, Bundle bundle) {

        Builder builder = new Builder(context);
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent notificationIntent = null;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationManager.cancel(MyConstant.CALL_NOTIFICATION_ID);
        notificationIntent = new Intent(context, SplashScreen.class);
        notificationIntent.putExtra("Notification_Data", message);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.launcher)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(context.getString(R.string.app_name)).setContentText(message.toUpperCase(Locale.getDefault()));
        Notification notification = builder.build();
        notificationManager.notify(0, notification);

    }

    //incedo
    @SuppressLint("NewApi")
    UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            //  Log.d("Uttrance Done", "uttrance done");
        }

        @Override
        public void onError(String utteranceId) {
            //Log.d("Uttrance Done", "uttrance done");
        }

        @Override
        public void onDone(String utteranceId) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        }
    };


    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {

            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            try {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
                if (appProcesses == null) {
                    return false;
                }
                final String packageName = context.getPackageName();
                for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                    if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                        return true;
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private boolean isBetween() {
        String[] fromTime = SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.FROM_TIME).split("-");
        String[] toTime = SharedPreferenceWriter.getInstance(CMUVoiceRecognitionService.this).getString(SPreferenceKey.TO_TIME).split("-");

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
