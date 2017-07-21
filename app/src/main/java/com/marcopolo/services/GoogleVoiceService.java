package com.marcopolo.services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.marcopolo.R;
import com.marcopolo.main.MarcoPoloApplication;
import com.marcopolo.screens.SplashScreen;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class GoogleVoiceService extends Service implements Runnable, TextToSpeech.OnInitListener {

    private boolean isVoiceMatch;
    private Timer iAudioTimer = null;
    private Handler handler = new Handler();
    private AudioManager amanager;
    private TimerTask tt;
    private Thread thread = null;
    private TextToSpeech tts;
    private boolean serviceDestroyd, isCriticalSectionRaise;
    private SpeechRecognizer speech;
    private MyRecognitionListener listener;
    private MediaPlayer mediaPlayer;
    private int originalVolume;
    private AudioTrack audioTrack = null;
    private short[] audioData;
    private int bufferSizeInBytes;
    private MediaPlayer mp = null;

    /*
     * private int countPrintLog = 0;
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tts = new TextToSpeech(this, this);
        amanager = (AudioManager) getSystemService(AUDIO_SERVICE);
        startThread();
        initializeAudioRecorder();
        return START_STICKY;
    }

    public void startThread() {
        thread = new Thread(this);
        thread.start();
    }

    public void offSound() {
        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    public void onSound() {

        amanager.setStreamMute(AudioManager.STREAM_ALARM, false);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }

    private void initializeAudioRecorder() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "Marco Polo");
        File file = new File(dir, "audio.pcm");

        // File file = new File(Environment.getExternalStorageDirectory(),
        // "test.pcm");

        int shortSizeInBytes = Short.SIZE / Byte.SIZE;

        bufferSizeInBytes = (int) (file.length() / shortSizeInBytes);
        audioData = new short[bufferSizeInBytes];

        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int i = 0;
            while (dataInputStream.available() > 0) {
                audioData[i] = dataInputStream.readShort();
                i++;
            }

            dataInputStream.close();

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 11025,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (null != iAudioTimer) {
            iAudioTimer.cancel();
            iAudioTimer = null;
        }
        if (tt != null) {

            tt.cancel();
            tt = null;
        }
        if (thread != null) {
            thread.interrupt();
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack = null;
        }

        serviceDestroyd = true;
    }

    public void run() {
        try {
            iAudioTimer = new Timer();
            tt = new TimerTask() {
                @Override
                public void run() {
                    if (!isVoiceMatch && !serviceDestroyd && !isCriticalSectionRaise) {

                        if (!amanager.isMusicActive()) {
                            isCriticalSectionRaise = true;
                            raise();
                        }
                    }
                }
            };
            iAudioTimer.schedule(tt, 0, 1 * 2000);
        } catch (Exception e) {
            Log.e("[AudioRecorder]: ", "run(): ", e);

        }
    }


    public void raise() {
        try {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    offSound();
                    speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                    listener = new MyRecognitionListener();
                    speech.setRecognitionListener(listener);
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getPackageName());

                    intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);
                    intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);
                    intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000);
                    intent.putExtra("android.speech.extra.DICTATION_MODE", true);
                    intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
                    intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true);

                    speech.startListening(intent);

                    Log.i("", "Calling the Recognise");

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            //onSound();
            Log.i("", "onError");
            new Handler().postAtTime(new Runnable() {

                @Override
                public void run() {
                    Log.e("", "soundOnnnnn");
                    isCriticalSectionRaise = false;
                    onSound();
                }
            }, 500);
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.e("", "onReadyForSpeech");
            new Handler().postAtTime(new Runnable() {

                @Override
                public void run() {
                    Log.e("", "soundOnnnnn");
                    //isCriticalSectionRaise = false;
                    onSound();
                }
            }, 1000);
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            tg.startTone(ToneGenerator.TONE_PROP_BEEP);
        }

        @Override
        public void onResults(Bundle results) {
            isCriticalSectionRaise = false;
            if (MarcoPoloApplication.getInstance().isResourceFree()) {

                ArrayList<String> strlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                for (int i = 0; i < strlist.size(); i++) {
                    Log.d("Speech", "result=" + strlist.get(i));
                    if (strlist.get(i).toLowerCase().equalsIgnoreCase(SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.KEYPHRASE).toLowerCase()) || strlist.get(i).toLowerCase().contains(SharedPreferenceWriter.getInstance(getApplicationContext()).getString(SPreferenceKey.KEYPHRASE).toLowerCase())) {
                        isVoiceMatch = true;
                        try {
                            boolean foregroud = new ForegroundCheckTask().execute(getApplicationContext()).get();
                            if (foregroud) palyResponse();
                            else {
                                if (isBetween()) palyResponse();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (SharedPreferenceWriter.getInstance(getBaseContext()).getBoolean(SPreferenceKey.PUSH_NOTIFICATION_STATUS)) {
                            showNotification(getBaseContext(), SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getString(SPreferenceKey.REPLY_KEYPHRASE), null);
                        }
                    }
                }

            } else {
                MarcoPoloApplication.getInstance().startWakeUpService();
                MarcoPoloApplication.getInstance().stopVRService();
            }
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

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
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.launcher)
                // .setLargeIcon(BitmapFactory.decodeResource(res,
                // R.drawable.some_big_img))
                // .setTicker(res.getString(R.string.your_ticker))
                .setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(context.getString(R.string.app_name)).setContentText(message);
        Notification notification = builder.build();
        notificationManager.notify((int) System.currentTimeMillis(), notification);

    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void palyResponse() {
        try {
            checkOverideSilentStatus();
            if (SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getString(SPreferenceKey.RESPONSE_TYPE).isEmpty() || SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getString(SPreferenceKey.RESPONSE_TYPE).equalsIgnoreCase(AppConstants.TEXT)) {
                HashMap<String, String> myHashStream = new HashMap<String, String>();
                myHashStream.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "1");

                if (SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getString(SPreferenceKey.REPLY_KEYPHRASE).isEmpty()) {
                    tts.speak("polo", TextToSpeech.QUEUE_ADD, myHashStream);
                } else {
                    tts.speak(SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getString(SPreferenceKey.REPLY_KEYPHRASE), TextToSpeech.QUEUE_ADD, myHashStream);
                }
            } else { // Play Recorded Audio
                // playAudio();
                playRecord();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void playRecord() {
        try {
            final int volume_level = amanager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mp = new MediaPlayer();
            if (new File("/sdcard/Marcopolo/rcdvoice_nxt.amr").exists()) {
                mp.setDataSource("/sdcard/Marcopolo/rcdvoice_nxt.amr");
            } else {
                AssetFileDescriptor descriptor;
                descriptor = getAssets().openFd("filename.mp3");
                mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
            }
            mp.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    amanager.setStreamVolume(AudioManager.STREAM_MUSIC, volume_level, 0);
                    isVoiceMatch = false;
                }
            });
            mp.prepare();
            mp.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkOverideSilentStatus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //originalVolume = Integer.parseInt(SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getString(SPreferenceKey.ORIGINAL_VOLUME_STATUS));
        if (originalVolume == 0) {// Silent
            if (SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getBoolean(SPreferenceKey.OVERRIDE_SILENT_STATUS)) {
                int volumeStatus = ((int) (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * ((float) SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getInt(SPreferenceKey.VOLUME_STATUS) / 100)));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeStatus, 0);
            }
        } else {
            int volumeStatus = ((int) (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * ((float) SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getInt(SPreferenceKey.VOLUME_STATUS) / 100)));
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeStatus, 0);
        }
    }

    @Override
    public void onInit(int arg0) {
        if (arg0 == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            // tts.setPitch(5); // set pitch level

            // tts.setSpeechRate(2); // set speech speed rate

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            }

        }
    }

    public class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
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
            return false;
        }
    }

    private boolean isBetween() {
        String[] fromTime = SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getString(SPreferenceKey.FROM_TIME).split("-");
        String[] toTime = SharedPreferenceWriter.getInstance(GoogleVoiceService.this).getString(SPreferenceKey.TO_TIME).split("-");

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
