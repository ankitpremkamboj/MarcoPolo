package com.marcopolo.audiowave;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.AdView;
import com.marcopolo.InterfaceListeners.InterfaceListener;
import com.marcopolo.InterfaceListeners.Interfaces.OnSendingAudioPath;
import com.marcopolo.R;
import com.marcopolo.Tabs.TabActivity;
import com.marcopolo.main.MarcoPoloApplication;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;
import com.marcopolo.utils.Constants;
import com.marcopolo.utils.FontUtility;


import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_OK;

public class NewRecorder extends Fragment implements OnClickListener, OnSendingAudioPath {
    boolean recording, isplayAudio;
    private ImageView iv_Record, iv_playRecording;
    //private TextView tv_Done;
    private int count;
    private boolean isRecorded_ResponseDone;
    private MediaRecorder myAudioRecorder;
    private String outputFile = "";
    private Timer timer;
    private TimerTask tt;
    MediaPlayer mPlayer = null;
    Activity activity;
    Button btn_Skip;
    View rootView;
    Button select_mp3;


    ImageView iv_beats_gif;


    //2

    long seconds = 0;
    long millis = 0;
    int mCount = 0;

    String isMp3orRecording = "";
    String mp3Path = "";


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        activity = getActivity();


        rootView = inflater.inflate(R.layout.audio_recorder, container, false);


        iv_beats_gif = (ImageView) rootView.findViewById(R.id.iv_beats_gif);


        InterfaceListener.setmOnSendingAudioPath(this);

        initViews(rootView);
        SetHeader(rootView);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Glide.with(activity).load(R.drawable.beats).placeholder(R.drawable.beats).into(iv_beats_gif);
            }
        }, 500);


        return rootView;
    }

    private void initViews(View rootView) {
        try {

            MarcoPoloApplication.getInstance().stopVRService();
            File dir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "Marco Polo");
            if (dir.exists()) {
                dir.delete();
            }
            dir.mkdir();
            outputFile = new File(dir, "audio.mp4").getAbsolutePath();
            rootView.findViewById(R.id.rl_playRecording).setOnClickListener(this);

            btn_Skip = (Button) rootView.findViewById(R.id.btn_Skip);
            btn_Skip.setVisibility(View.GONE);
            btn_Skip.setText(activity.getResources().getString(R.string.done_label));
            btn_Skip.setOnClickListener(this);

            select_mp3 = (Button) rootView.findViewById(R.id.select_mp3);
            select_mp3.setOnClickListener(this);


            iv_Record = (ImageView) rootView.findViewById(R.id.iv_Record);
            iv_playRecording = (ImageView) rootView.findViewById(R.id.iv_playRecording);
            //tv_Done = (TextView) rootView.findViewById(R.id.iv_Done);
            iv_Record.setOnClickListener(this);
            FontUtility.getInstance().setTypePhase(activity, ((TextView) rootView.findViewById(R.id.tv_Timer)), AppConstants.GOTHAM_ROUNDED_MEDIUM);
            rootView.findViewById(R.id.rl_Close).setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        FlurryAgent.onStartSession(activity, Constants.FLURRY_KEY);
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        FlurryAgent.onEndSession(activity);
    }

    public void startRecordAudio() {
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);
        try {
            //releaseRecorder();

            myAudioRecorder.prepare();
            myAudioRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playAudio() {
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(outputFile);
            mPlayer.prepare();
            mPlayer.start();
            startTimer();
            mPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    stopAudio();
                    iv_playRecording.setImageResource(R.drawable.play_full);
                    isplayAudio = false;
                    iv_Record.setEnabled(true);
                    stopTimer();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Glide.with(activity).load(R.drawable.enable_beats).placeholder(R.drawable.enable_beats).into(iv_beats_gif);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 500);

                }
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void stopAudio() {
        try {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                stopTimer();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public void startTimer() {
        try {
            ((TextView) rootView.findViewById(R.id.tv_Timer)).setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.tv_Timer)).setText("00:00");
            timer = new Timer();
            tt = new TimerTask() {

                @Override
                public void run() {
                    timerMethod();

                }
            };
            timer.scheduleAtFixedRate(tt, 1, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stopTimer() {
        mCount = 0;
        seconds = 0;

        if (timer != null)
            timer.cancel();

        if (tt != null)
            tt.cancel();
        count = 1;
        ((TextView) rootView.findViewById(R.id.tv_Timer)).setVisibility(View.INVISIBLE);
    }


    private void timerMethod() {
        try {
            getActivity().runOnUiThread(generate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable generate = new Runnable() {
        @Override
        public void run() {

            DecimalFormat formatter = new DecimalFormat("00");
            //formatter.format(seconds);

            ((TextView) rootView.findViewById(R.id.tv_Timer)).setText(formatter.format(seconds) + ":" + formatter.format(mCount));
            mCount++;
            if (mCount == 10) {
                mCount = 0;
                seconds++;
            }
        }
    };




    private void SetHeader(View rootView) {
        TextView header_text = (TextView) rootView.findViewById(R.id.header_text);
        String headerText = activity.getResources().getString(R.string.custom_response);
        header_text.setText(headerText);
        //header_text.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_Skip:
                isRecorded_ResponseDone = true;

                String text = "";
                if (isMp3orRecording == Constants.isRecording) {
                    text = "<font color=#ffffff>Call out </font><font color=#eda503>" + SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).toUpperCase(Locale.getDefault()) + "!</font><font color=#ffffff> and your Phone will reply with </font><font color=#D97245>" + "(recording)" + "!";
                    SharedPreferenceWriter.getInstance(activity).writeStringValue(SPreferenceKey.RESPONSE_TYPE, AppConstants.AUDIO);
                } else {
                    if (mp3Path.trim().length() > 0) {
                        text = "<font color=#ffffff>Call out </font><font color=#eda503>" + SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).toUpperCase(Locale.getDefault()) + "!</font><font color=#ffffff> and your Phone will reply with </font><font color=#D97245>" + mp3Path + "!";
                    }
                    SharedPreferenceWriter.getInstance(activity).writeStringValue(SPreferenceKey.RESPONSE_TYPE, AppConstants.MP3);
                }


                SharedPreferenceWriter.getInstance(activity).writeStringValue(SPreferenceKey.AUDIO_FILE_PATH, outputFile);
                SharedPreferenceWriter.getInstance(activity).writeStringValue(SPreferenceKey.MP3_PATH, mp3Path);


                final TextView resultText = ((TextView) rootView.findViewById(R.id.tv_Result));
                resultText.setVisibility(View.VISIBLE);
                resultText.setText(Html.fromHtml(text));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resultText.setVisibility(View.GONE);
                        InterfaceListener.getOnPageNavigation(Constants.CURRENT_ITEM_ZERO);
                        //finishActivty();
                        try {
                            ((TabActivity) getActivity()).SetAdapterViewPager();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 3000);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InterfaceListener.getOnLoadingInterstitialAd();
                    }
                }, 2000);
                ((Button) rootView.findViewById(R.id.btn_Skip)).setVisibility(View.GONE);

                break;

            case R.id.rl_Close:
                finishActivty();
                break;

            case R.id.iv_Record:

                try {
                    if (!recording) {
                        recording = true;
                        iv_Record.setImageResource(R.drawable.stop_red);
                        iv_playRecording.setImageResource(R.drawable.play);
                        iv_playRecording.setEnabled(false);
                        iv_playRecording.setVisibility(View.INVISIBLE);
                        btn_Skip.setVisibility(View.GONE);
                        ((TextView) rootView.findViewById(R.id.tv_Recoding)).setText("RECORDING");
                        startTimer();
                        startRecordAudio();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(activity).load(R.raw.enable_power_animation).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.enable_beats).into(iv_beats_gif);
                            }
                        }, 50);


                    } else {
                        recording = false;
                        iv_Record.setImageResource(R.drawable.stop_image);
                        iv_playRecording.setImageResource(R.drawable.play_full);
                        btn_Skip.setVisibility(View.VISIBLE);
                        iv_playRecording.setVisibility(View.VISIBLE);
                        iv_playRecording.setEnabled(true);
                        ((TextView) rootView.findViewById(R.id.tv_Recoding)).setText("RECORD YOUR CUSTOM RESPONSE");
                        stopTimer();
                        releaseRecorder();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(activity).load(R.drawable.enable_beats).placeholder(R.drawable.enable_beats).into(iv_beats_gif);
                            }
                        }, 50);


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case R.id.select_mp3:

                InterfaceListener.getmOnFetchingMp3Files();
                break;

            case R.id.rl_playRecording:

                if (!isplayAudio) {
                    isplayAudio = true;
                    //	releaseRecorder();
                    iv_playRecording.setVisibility(View.VISIBLE);
                    iv_Record.setImageResource(R.drawable.stop_image);
                    iv_Record.setEnabled(false);
                    iv_playRecording.setImageResource(R.drawable.ic_action_pause);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            playAudio();

                        }
                    }, 500);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(activity).load(R.raw.enable_power_animation).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.enable_beats).into(iv_beats_gif);
                        }
                    }, 10);


                } else {
                    iv_playRecording.setVisibility(View.VISIBLE);
                    isplayAudio = false;
                    iv_playRecording.setImageResource(R.drawable.play);
                    iv_Record.setEnabled(true);
                    iv_playRecording.setEnabled(false);
                    stopAudio();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Glide.with(activity).load(R.drawable.enable_beats).placeholder(R.drawable.enable_beats).into(iv_beats_gif);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 10);

                }

                break;
            default:
                break;
        }
    }

    public synchronized void releaseRecorder() {
        try {
            if (null != myAudioRecorder) {
                recording = false;
                myAudioRecorder.stop();
                myAudioRecorder.reset();
                //myAudioRecorder.release();
                myAudioRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isMp3orRecording = Constants.isRecording;
    }

    private void finishActivty() {
        Intent intent = new Intent();
        if (isRecorded_ResponseDone)
            intent.putExtra(SPreferenceKey.RESPONSE_TYPE, "recording");

        activity.setResult(RESULT_OK, intent);
        activity.overridePendingTransition(R.anim.slide_down, R.anim.slide_down);
        //activity.finish();

        InterfaceListener.getOnPageNavigation(Constants.CURRENT_ITEM_ZERO);

    }

    @Override
    public void onPause() {
        super.onPause();

        stopAudio();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        MarcoPoloApplication.getInstance().startVRService();
        releaseRecorder();
        if (myAudioRecorder != null)
            myAudioRecorder.release();
        stopAudio();
    }

    private AdView mAdView;


    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {

                // Return the remote address
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();

                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    public String getRealPathFromURI(Context context, Uri contentUri) {

        Cursor cursor = null;
        String filePath = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {

                filePath = getPath(activity, contentUri);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            return filePath;
        } else {
            try {
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    /**
     * You will received Audio URI to Play Recording
     */
    @Override
    public void OnSendingAudio(Uri uri) {

        mp3Path = getFileName(uri);
        isMp3orRecording = Constants.isMP3;

        btn_Skip.setVisibility(View.VISIBLE);
        outputFile = getRealPathFromURI(getActivity(), uri);

        if (outputFile != null && outputFile.length() > 0)
            Log.d("SomeOutPutelse", outputFile);


        if (!isplayAudio) {
            isplayAudio = true;

            //	releaseRecorder();
            iv_playRecording.setVisibility(View.VISIBLE);
            iv_Record.setImageResource(R.drawable.stop_image);
            iv_Record.setEnabled(false);
            iv_playRecording.setImageResource(R.drawable.ic_action_pause);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    playAudio();

                }
            }, 500);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Glide.with(activity).load(R.raw.enable_power_animation).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.enable_beats).into(iv_beats_gif);
                }
            }, 50);


        } else {
            iv_playRecording.setVisibility(View.VISIBLE);
            isplayAudio = false;
            iv_playRecording.setImageResource(R.drawable.play);
            iv_Record.setEnabled(true);
            iv_playRecording.setEnabled(false);
            stopAudio();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Glide.with(activity).load(R.drawable.enable_beats).placeholder(R.drawable.enable_beats).into(iv_beats_gif);
                }
            }, 500);

        }
    }

}
