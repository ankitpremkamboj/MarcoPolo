package com.morcopolo.fragments.Fragmentss;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.marcopolo.InterfaceListeners.InterfaceListener;
import com.marcopolo.InterfaceListeners.Interfaces.CallingHomeUI;
import com.marcopolo.InterfaceListeners.Interfaces.OnControllingDeviceVolume;
import com.marcopolo.InterfaceListeners.Interfaces.OnSettingServiceTime;
import com.marcopolo.InterfaceListeners.Interfaces.onErrorReceived;
import com.marcopolo.InterfaceListeners.Interfaces.onPowerButtonInitialised;
import com.marcopolo.R;
import com.marcopolo.main.MarcoPoloApplication;
import com.marcopolo.services.CMUVoiceRecognitionService;
import com.marcopolo.services.NotificationService;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;
import com.marcopolo.utils.CenteredImageSpan;
import com.marcopolo.utils.UtilityFunctions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/*import com.startapp.android.publish.adsCommon.StartAppAd;*/


/**
 * Created by kamran on 3/3/17.
 */


public class HomeFragment extends Fragment implements View.OnClickListener, onPowerButtonInitialised, onErrorReceived, CallingHomeUI/*, RadialTimePickerDialog.OnTimeSetListener*/, OnSettingServiceTime, OnControllingDeviceVolume {


    //private StartAppAd startAppAd = new StartAppAd(getActivity());
    private String Scheduled_Option_From = "Scheduled_Option_From";
    private String Scheduled_Option_To = "Scheduled_Option_To";
    public static final int AUDIO_RECORDER = 101;
   // SeekBar _senstivity, _volume;
    DiscreteSeekBar _volume,_senstivity;
    ImageView iv_enable_beats;

    Activity activity;
    private int RESULT_OK;

    RadioGroup radioGroup;
    RadioButton radio1, radio2;

    View rootView;
    TextView volume_indicator_text, sensitivtyValue_indicator_text;
    private TextView mFromDateTextView, mToDateTextView;
    private RadialTimePickerDialog timePickerDialog;
    private boolean isFromTimeCliked = false;
    private String Scheduled_option = "";
    ImageView edit_time_image, edit_image_setphrase, mark_sign_sensitivity, recogniser_mark, mark_sign1;

    RelativeLayout rl_RootLayout, schedule_layout;
    private AudioManager audioManager;
    private Timer timer;

    ProgressDialog progressDialog = null;
    IntentFilter mIntentFilter;

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();
        View rootView = inflater.inflate(R.layout.homescreen, container, false);
        this.rootView = rootView;

        InterfaceListener.setPowertButtonInitialised(this);
        InterfaceListener.setOnErrorReceived(this);
        InterfaceListener.setCallingHomeUI(this);
        InterfaceListener.setmOnSettingServiceTime(this);
        InterfaceListener.setOnControllingDeviceVolume(this);

        initViews(rootView);

        setTime();

        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(AppConstants.ALARM_TIME_OUT);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        InitAudioService();


        if (MarcoPoloApplication.getInstance().isValidTimeStamp()) {
            setServiceTimeText(rootView);
        } else {
            setErrorOutofRange();
        }
    }

    private void InitAudioService() {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        _volume = (DiscreteSeekBar) rootView.findViewById(R.id.sliderNumber1);
        _volume.setMax(maxVolume);
        _volume.setProgress(curVolume);
        try {
            volume_indicator_text.setText(curVolume + "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final Handler handler = new Handler();

        _volume.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, final int value, boolean fromUser) {

                try {
                    volume_indicator_text.setText(value + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    handler.removeCallbacksAndMessages(null);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
                        }
                    }, 250);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                SharedPreferenceWriter.getInstance(activity).writeIntValue("volume", value);

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
    }


    private void initViews(final View rootView) {
        try {

            radio1 = (RadioButton) rootView.findViewById(R.id.radio1);
            radio2 = (RadioButton) rootView.findViewById(R.id.radio2);

            radioGroup = (RadioGroup) rootView.findViewById(R.id.recogniser_radio);
            _senstivity = (DiscreteSeekBar) rootView.findViewById(R.id.sensitivtyValue1);

            edit_time_image = (ImageView) rootView.findViewById(R.id.edit_time_image);
            mark_sign_sensitivity = (ImageView) rootView.findViewById(R.id.mark_sign_volume);
            recogniser_mark = (ImageView) rootView.findViewById(R.id.recogniser_mark);
            mark_sign1 = (ImageView) rootView.findViewById(R.id.mark_sign1);
            rl_RootLayout = (RelativeLayout) rootView.findViewById(R.id.rl_RootLayout);
            schedule_layout = (RelativeLayout) rootView.findViewById(R.id.schedule_layout);
            schedule_layout.setOnClickListener(this);

            ((TextView) rootView.findViewById(R.id.tv_DetailText)).setOnClickListener(this);

            edit_image_setphrase = (ImageView) rootView.findViewById(R.id.edit_image_setphrase);

            edit_time_image.setOnClickListener(this);
            edit_image_setphrase.setOnClickListener(this);
            mark_sign_sensitivity.setOnClickListener(this);
            recogniser_mark.setOnClickListener(this);
            mark_sign1.setOnClickListener(this);

            volume_indicator_text = (TextView) rootView.findViewById(R.id.volume_indicator_text);
            sensitivtyValue_indicator_text = (TextView) rootView.findViewById(R.id.sensitivtyValue_indicator_text);

            _senstivity.setProgress(SharedPreferenceWriter.getInstance(getActivity()).getInt(SPreferenceKey.SENSITIVITY_VALUE_STATUS));

            mFromDateTextView = (TextView) rootView.findViewById(R.id.tv_From);
            //mFromDateTextView.setOnClickListener(this);
            mToDateTextView = (TextView) rootView.findViewById(R.id.tv_To);

            radio2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    // user is typing: reset already started timer (if existing)
                    if (timer != null) {
                        timer.cancel();
                    }


                    timer = new Timer();

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // do your actual work here
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        progressDialog = ProgressDialog.show(activity, "", "Changing to Google......");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    // MarcoPoloApplication.getInstance().stopVRService();
                                    // MarcoPoloApplication.getInstance().startGVService();
                                       MarcoPoloApplication.getInstance().restartVRService();
                                }
                            });
                        }
                    }, 1000);


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                progressDialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 2000);

                }
            });
            radio1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // user is typing: reset already started timer (if existing)
                    if (timer != null) {
                        timer.cancel();
                    }


                    timer = new Timer();

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // do your actual work here
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        progressDialog = ProgressDialog.show(activity, "", "Changing to CMU...");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    MarcoPoloApplication.getInstance().restartVRService();

                                    //MarcoPoloApplication.getInstance().stopGVService();
                                    //MarcoPoloApplication.getInstance().startVRService();

                                }
                            });
                        }
                    }, 1000);


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                progressDialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 2000);


                }
            });
            _senstivity.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {


                    // user is typing: reset already started timer (if existing)
                    if (timer != null) {
                        timer.cancel();
                    }


                    timer = new Timer();

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // do your actual work here
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        progressDialog = ProgressDialog.show(activity, "", "Updating...");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    MarcoPoloApplication.getInstance().restartVRService();
                                }
                            });
                        }
                    }, 1000);


                    // final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Updating...");

                    try {
                        sensitivtyValue_indicator_text.setText(value + "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SharedPreferenceWriter.getInstance(getActivity()).writeIntValue(SPreferenceKey.SENSITIVITY_VALUE_STATUS, value);


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                progressDialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 2000);
                }

                @Override
                public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

                }
            });


            rootView.findViewById(R.id.tv_ProwerBtn).setOnClickListener(this);

            rootView.findViewById(R.id.tv_Enabled).setOnClickListener(this);
            iv_enable_beats = (ImageView) rootView.findViewById(R.id.iv_enable_beats);


            setNewPhrase(rootView);
            setServiceTimeText(rootView);
            try {
                showLollipopPermissionDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }


            SetHeader(rootView);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void setServiceTimeText(View rootView) {
        String[] fromTime = SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.FROM_TIME).split("-");
        String[] toTime = SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.TO_TIME).split("-");
        ((TextView) rootView.findViewById(R.id.tv_Scedule_Time_Text)).setText(Html.fromHtml("<font color=#71E7EF>Marco Polo will only respond between</font>"));
        ((TextView) rootView.findViewById(R.id.tv_Scedule_Time_Text1)).setText(Html.fromHtml("<font color=#ffffff>" + getTimeString(fromTime[0], fromTime[1], fromTime[2]) + "</font>" + "<font color=#ffffff> - </font>" + "<font color=#ffffff>" + getTimeString(toTime[0], toTime[1], toTime[2]) + "</font>"));
    }


    private void setErrorOutofRange() {

        ((TextView) rootView.findViewById(R.id.tv_Scedule_Time_Text)).setText(Html.fromHtml("<font color=#71E7EF> Marco Polo is currently <font color=#990012>not</font> listening in the background now to save battery</font>"));

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setTime() {

        if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.FROM_TIME).toString().length() > 0
                && SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.TO_TIME).toString().length() > 0) {

            NumberFormat format = new DecimalFormat("00");
            if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.FROM_TIME).toString().length() > 0) {
                String from[] = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.FROM_TIME).split("-");
                if (from[2].equalsIgnoreCase("0"))
                    mFromDateTextView.setText(format.format(Integer.parseInt(from[0])) + ":" + format.format(Integer.parseInt(from[1])) + " " + "AM");
                else
                    mFromDateTextView.setText(format.format(Integer.parseInt(from[0])) + ":" + format.format(Integer.parseInt(from[1])) + " " + "PM");
            }

            if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.TO_TIME).toString().length() > 0) {
                String to[] = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.TO_TIME).split("-");
                if (to[2].equalsIgnoreCase("0"))
                    mToDateTextView.setText(format.format(Integer.parseInt(to[0])) + ":" + format.format(Integer.parseInt(to[1])) + " " + "AM");
                else
                    mToDateTextView.setText(format.format(Integer.parseInt(to[0])) + ":" + format.format(Integer.parseInt(to[1])) + " " + "PM");

            }
        }

    }


    private String getTimeString(String hour, String min, String ampm) {
        NumberFormat numberFormat = new DecimalFormat("00");
        StringBuilder builder = new StringBuilder();
        if (Integer.parseInt(ampm) == 0) {
            builder.append(numberFormat.format(Integer.parseInt(hour)) + ":" + numberFormat.format(Integer.parseInt(min)) + " " + "AM");
        } else {
            builder.append(numberFormat.format(Integer.parseInt(hour)) + ":" + numberFormat.format(Integer.parseInt(min)) + " " + "PM");
        }
        return builder.toString();
    }

    private void setPowerButton(View rootView) {
         if (!SharedPreferenceWriter.getInstance(activity).getBoolean(SPreferenceKey.IS_POWER_ON)) {
            ((ImageView) rootView.findViewById(R.id.tv_ProwerBtn)).setImageResource(R.drawable.disabled);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Glide.with(activity).load(R.drawable.enable_beats).placeholder(R.drawable.enable_beats).into(iv_enable_beats);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 300);


            ((TextView) rootView.findViewById(R.id.tv_Enabled)).setText("DISABLED");

            ((TextView) rootView.findViewById(R.id.tv_Error)).setVisibility(View.VISIBLE);
            (((TextView) rootView.findViewById(R.id.tv_Error))).setText("Tap the button below to Enable");
            try {
                if (null != CMUVoiceRecognitionService.mPlayer) {
                    if (CMUVoiceRecognitionService.mPlayer.isPlaying()) {
                        CMUVoiceRecognitionService.mPlayer.stop();
                    }
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            MarcoPoloApplication.getInstance().stopVRService();
            Intent serviceIntent = new Intent(activity, NotificationService.class);
            serviceIntent.setAction(AppConstants.START_ACTION);
            activity.startService(serviceIntent);
        } else {
            ((ImageView) rootView.findViewById(R.id.tv_ProwerBtn)).setImageResource(R.drawable.enable_power);
            ((TextView) rootView.findViewById(R.id.tv_Enabled)).setText("ENABLED");


            (((TextView) rootView.findViewById(R.id.tv_Error))).setVisibility(View.VISIBLE);
            MarcoPoloApplication.getInstance().restartVRService();
            Intent serviceIntent = new Intent(activity, NotificationService.class);
            serviceIntent.setAction(AppConstants.STARTFOREGROUND_ACTION);
            activity.startService(serviceIntent);
        }

    }

    @SuppressLint("NewApi")
    private void showLollipopPermissionDialog() {
        try {
            //noinspection ResourceType
            if (android.os.Build.VERSION.SDK_INT >= 21 && !((UsageStatsManager) activity.getSystemService("usagestats"))
                    .queryEvents(System.currentTimeMillis() - 0x5265c00L, System.currentTimeMillis()).hasNextEvent()) {
                android.app.AlertDialog.Builder builder2 = new android.app.AlertDialog.Builder(activity);
                builder2.setTitle("Lollipop Recent Apps");
                builder2.setMessage("So as to not conflict with other apps, Marco Polo needs to know what app is currently running on your phone." +
                        " In Lollipop, you must give explicit permission. On the following page, please allow Marco Polo this information");
                builder2.setPositiveButton("Launch Settings", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                        try {
                            Intent intent = new Intent("android.settings.USAGE_ACCESS_SETTINGS");
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                });
                builder2.setCancelable(true);
                builder2.show();
            }

        } catch (Exception e) {
            Toast.makeText(activity, "Please open settings, your model can't be forced to open it", Toast.LENGTH_LONG).show();
        }
    }


    private void SetHeader(View rootView) {
        TextView header_text = (TextView) rootView.findViewById(R.id.header_text);
        String headerText = activity.getResources().getString(R.string.home_header_text);
        header_text.setText(headerText);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.edit_image_setphrase:

                break;
            case R.id.tv_DetailText:
                InterfaceListener.getmOnActivatingTab(1);
                break;
            case R.id.schedule_layout:
                InterfaceListener.getmOnActivatingTab(3);
                break;
            case R.id.mark_sign_volume:

                String toast_sensitivity = activity.getResources().getString(R.string.toast_sensitivity);
                UtilityFunctions.showLongToast(activity, "" + toast_sensitivity);

                break;

            case R.id.recogniser_mark:

                String toast_switch_recogniser = activity.getResources().getString(R.string.toast_switch_recogniser);
                UtilityFunctions.showLongToast(activity, "" + toast_switch_recogniser);

                break;
            case R.id.mark_sign1:

                String toast_volume = activity.getResources().getString(R.string.toast_volume);
                UtilityFunctions.showLongToast(activity, "" + toast_volume);

                break;

            case R.id.tv_ProwerBtn:
                setEnabledDisabled(rootView);
                break;

            case R.id.tv_Enabled:
                setEnabledDisabled(rootView);
                break;

            case R.id.tv_From:
                openFromTimeDialog();
                break;

            case R.id.tv_To:
                openToTimeDialog();
                break;
            case R.id.edit_time_image:
                //openFromTimeDialog();
                break;
            default:
                break;
        }
    }

    private void setEnabledDisabled(View rootView) {
        if (SharedPreferenceWriter.getInstance(activity).getBoolean(SPreferenceKey.IS_POWER_ON)) {
            ((ImageView) rootView.findViewById(R.id.tv_ProwerBtn)).setImageResource(R.drawable.disabled);
            ((TextView) rootView.findViewById(R.id.tv_Enabled)).setText("DISABLED");
            //iv_enable_beats.setBackgroundResource(R.drawable.beats);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Glide.with(activity).load(R.drawable.enable_beats).placeholder(R.drawable.enable_beats).into(iv_enable_beats);
                }
            }, 300);

            ((TextView) rootView.findViewById(R.id.tv_Error)).setVisibility(View.VISIBLE);
            (((TextView) rootView.findViewById(R.id.tv_Error))).setText("Tap the button below to Enable");
            MarcoPoloApplication.getInstance().stopVRService();
            SharedPreferenceWriter.getInstance(activity).writeBooleanValue(SPreferenceKey.IS_POWER_ON, false);
            Intent serviceIntent = new Intent(activity, NotificationService.class);
            serviceIntent.setAction(AppConstants.START_ACTION);
            activity.startService(serviceIntent);
        } else {
            SharedPreferenceWriter.getInstance(activity).writeBooleanValue(SPreferenceKey.IS_POWER_ON, true);
            ((ImageView) rootView.findViewById(R.id.tv_ProwerBtn)).setImageResource(R.drawable.enable_power);


            ((TextView) rootView.findViewById(R.id.tv_Enabled)).setText("ENABLED");
            ((TextView) rootView.findViewById(R.id.tv_Error)).setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.tv_Error)).setText(Html.fromHtml("<font color=#71E7EF>" + "Preparing the recognizer..." + "</font>"));
            MarcoPoloApplication.getInstance().startVRService();
            Intent serviceIntent = new Intent(activity, NotificationService.class);
            serviceIntent.setAction(AppConstants.
                    STARTFOREGROUND_ACTION);
            activity.startService(serviceIntent);
        }
    }


    private void SetSpannableText(Spanned textToSet1, Spanned textToSet2, TextView textView) {
        try {
            SpannableStringBuilder ssb = new SpannableStringBuilder(TextUtils.concat(textToSet1, textToSet2));
            Bitmap smiley = BitmapFactory.decodeResource(getResources(), R.drawable.edit);
            ssb.setSpan(new CenteredImageSpan(activity, R.drawable.edit), ssb.length() - 1, ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            textView.setText(ssb, TextView.BufferType.SPANNABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setNewPhrase(View rootView) {

        if (SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.RESPONSE_TYPE).contains(AppConstants.TEXT)) {
            String marcoText = SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.KEYPHRASE).isEmpty() ? AppConstants.DEFAULT_INPUT_PHRASE.toUpperCase(Locale.getDefault()) : SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.KEYPHRASE);
            String poloText = SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.REPLY_KEYPHRASE).isEmpty() ? AppConstants.DEFAULT_OUTPUT_PHRASE.toUpperCase(Locale.getDefault()) : SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.REPLY_KEYPHRASE);
            TextView textView = ((TextView) rootView.findViewById(R.id.tv_DetailText));
            Spanned textToSet1 = Html.fromHtml("<font color=#71E7EF>Call out </font><font color=#FFFFFF>" + marcoText.toUpperCase(Locale.getDefault()) + "!</font><font color=#71E7EF> and your Phone </font>");
            Spanned textToSet2 = Html.fromHtml("<font color=#71E7EF>will reply with </font><font color=#FFFFFF>" + poloText.toUpperCase(Locale.getDefault()) + "!&nbsp;&nbsp;&nbsp;");


            SetSpannableText(textToSet1, textToSet2, textView);


        } else if (SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.RESPONSE_TYPE).contains(AppConstants.AUDIO)) {
            String marcoText = SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.KEYPHRASE).isEmpty() ? AppConstants.DEFAULT_INPUT_PHRASE.toUpperCase(Locale.getDefault()) : SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.KEYPHRASE).toUpperCase(Locale.getDefault());

            TextView textView = ((TextView) rootView.findViewById(R.id.tv_DetailText));
            Spanned textToSet1 = Html.fromHtml("<font color=#71E7EF>Call out </font><font color=#FFFFFF>" + marcoText + "!</font><font color=#71E7EF> and your Phone </font>");
            Spanned textToSet2 = Html.fromHtml("<font color=#71E7EF>will reply with </font><font color=#FFFFFF>" + "(recording)&nbsp;&nbsp;&nbsp;");
            SetSpannableText(textToSet1, textToSet2, textView);


        } else {
            String marcoText = SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.KEYPHRASE).isEmpty() ? AppConstants.DEFAULT_INPUT_PHRASE.toUpperCase(Locale.getDefault()) : SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.KEYPHRASE).toUpperCase(Locale.getDefault());
            String FileName = SharedPreferenceWriter.getInstance(activity).getString(SPreferenceKey.MP3_PATH);

            TextView textView = ((TextView) rootView.findViewById(R.id.tv_DetailText));
            Spanned textToSet1 = Html.fromHtml("<font color=#71E7EF>Call out </font><font color=#FFFFFF>" + marcoText + "!</font><font color=#71E7EF> and your Phone </font>");
            Spanned textToSet2 = Html.fromHtml("<font color=#71E7EF>will reply with </font><font color=#FFFFFF>" + FileName + "&nbsp;&nbsp;&nbsp;");
            SetSpannableText(textToSet1, textToSet2, textView);


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK && requestCode == AUDIO_RECORDER && data != null && data.getStringExtra(SPreferenceKey.RESPONSE_TYPE).equalsIgnoreCase("recording")) {
                setNewPhrase(rootView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPowerInitialised() {
        setPowerButton(rootView);
    }

    @Override
    public void OnErrorReceive(final String text, final String color) {


        if (text.contains("Ready")) {

            //TabActivity.isServiceRunning = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        try {
                            Glide.with(activity).load(R.raw.enable_power_animation).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.enable_beats).into(iv_enable_beats);
                            // ((TextView) rootView.findViewById(R.id.tv_Error)).setText(Html.fromHtml("<font color=" + color + ">" + text + "</font>"));

                            if (!MarcoPoloApplication.getInstance().isValidTimeStamp()) {
                                ((TextView) rootView.findViewById(R.id.tv_Error)).setText(Html.fromHtml("<font color=" + color + ">" + "Ready... not listening in background" + "</font>"));

                            } else {
                                ((TextView) rootView.findViewById(R.id.tv_Error)).setText(Html.fromHtml("<font color=" + color + ">" + "Ready..." + "</font>"));

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 300);

        } else {
            Glide.with(activity).load(R.raw.enable_power_animation).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.enable_beats).into(iv_enable_beats);

            if (!MarcoPoloApplication.getInstance().isValidTimeStamp()) {
                ((TextView) rootView.findViewById(R.id.tv_Error)).setText(Html.fromHtml("<font color=" + color + ">" + "Ready... not listening in background" + "</font>"));

            } else {
                ((TextView) rootView.findViewById(R.id.tv_Error)).setText(Html.fromHtml("<font color=" + color + ">" + "Ready..." + "</font>"));

            }

        }


    }

    @Override
    public void CallingHomeUIViews() {
        setNewPhrase(rootView);
    }


    private void openToTimeDialog() {
        setFromTimeClicked(false);
        Scheduled_option = Scheduled_Option_To;
        if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.TO_TIME).toString().length() > 0) {
            String from[] = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.TO_TIME).split("-");
            showTimeScheduledDialog(Integer.parseInt(from[0]), Integer.parseInt(from[1]), Integer.parseInt(from[2]));
        } else {
            showTimeScheduledDialog();
        }
    }

    private void openFromTimeDialog() {
        setFromTimeClicked(true);
        Scheduled_option = Scheduled_Option_From;
        if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.FROM_TIME).toString().length() > 0) {
            String from[] = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.FROM_TIME).split("-");
            showTimeScheduledDialog(Integer.parseInt(from[0]), Integer.parseInt(from[1]), Integer.parseInt(from[2]));
        } else {
            showTimeScheduledDialog();
        }
    }

    private void setFromTimeClicked(boolean b) {
        isFromTimeCliked = b;
    }

    public void showTimeScheduledDialog(int hour, int min, int ampm) {
        try {

            final TimePickerDialog timeDialog = new TimePickerDialog(activity, R.style.MyDialogThemeTimePicker, timePickerListener, ampm == 0 ? hour : 12 + hour, min, false);

            timeDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    // This is hiding the "Cancel" button:
                    timeDialog.getButton(Dialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                    if (isFromTimeCliked) {
                        timeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Set Start Time");
                    } else {
                        timeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Set End Time");
                    }
                }
            });


            timeDialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        String meridiem;

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            try {
                String valueTime = "" + (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay) + "-" + minute + "-" + (hourOfDay > 12 ? 1 : 0);
                SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(isFromTimeCliked ? SPreferenceKey.FROM_TIME : SPreferenceKey.TO_TIME, valueTime);


                setTime();
                if (isFromTimeCliked) {
                    isFromTimeCliked = false;
                    openToTimeDialog();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    public void showTimeScheduledDialog() {
        try {


            final Calendar c = Calendar.getInstance();
            int hour, minute;
            hour = c.get(Calendar.HOUR);
            minute = c.get(Calendar.MINUTE);
            final TimePickerDialog timeDialog = new TimePickerDialog(activity, R.style.MyDialogThemeTimePicker, timePickerListener, hour, minute, false);


            timeDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    // This is hiding the "Cancel" button:
                    timeDialog.getButton(Dialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                    if (isFromTimeCliked) {
                        timeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Set Start Time");
                    } else {
                        timeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Set End Time");
                    }
                }
            });


            if (isFromTimeCliked) {
                //timePickerDialog.setDoneText("Set Start time");
            } else {
                //timePickerDialog.setDoneText("Set End Time");
            }

            timeDialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void OnSettingTimeText() {
        setServiceTimeText(rootView);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Glide.clear(iv_enable_beats);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onControllingVolume() {
        InitAudioService();
    }


}
