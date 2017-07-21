package com.morcopolo.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.gc.materialdesign.views.ButtonFlat;
import com.marcopolo.InterfaceListeners.InterfaceListener;
import com.marcopolo.InterfaceListeners.Interfaces.OnControllingDeviceVolume;
import com.marcopolo.InterfaceListeners.Interfaces.isSchedulerRunning;
import com.marcopolo.R;
import com.marcopolo.main.MarcoPoloApplication;
import com.marcopolo.services.NotificationService;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;
import com.marcopolo.utils.Constants;
import com.marcopolo.utils.UtilityFunctions;
import com.marcopolo.utils.WebviewActivity;
import com.morcopolo.fragments.Fragmentss.DiscreteSeekBar;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class SettingFragment extends Fragment implements isSchedulerRunning, OnClickListener, OnControllingDeviceVolume {

    private View rootView;
    private String Scheduled_Option_From = "Scheduled_Option_From";
    private String Scheduled_Option_To = "Scheduled_Option_To";
    private String Scheduled_option;
    private RadialTimePickerDialog timePickerDialog = null;
    private boolean isFromTimeCliked = false;
    private int lastSensitivity;
    private Activity activity;
    com.marcopolo.views.Slider _volume;
    DiscreteSeekBar _volume1;

    ToggleButton _pushNotification, _silent;
    ImageView _share;

    RelativeLayout txt_not_working_layout, have_recommendation_layout;
    private TextView volume_indicator_text;

    private TextView mFromDateTextView, mToDateTextView;

    ImageView mark_sign_volume, listen_image, switch_mark, push_mark, override_mark, fixednotification_mark;
    private RelativeLayout share_layout;
    ProgressDialog progressDialog = null;
    private Timer timer;
    AudioManager audioManager;
    private RadioButton radio1, radio2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.setting_fragment, container, false);

        activity = getActivity();
        initView();

        SetHeader(rootView);


        InterfaceListener.setOnSchedulerShowing(this);
        InterfaceListener.setOnControllingDeviceVolume(this);
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        InitialisingAudioService();
    }

    private void InitialisingAudioService() {

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        _volume1.setMax(maxVolume);
        _volume1.setProgress(curVolume);


        try {

            volume_indicator_text.setText(curVolume + "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final Handler handler = new Handler();


        _volume1.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
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


    private void ShareText(String shareBody) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_friends)));
    }

    private void ShareTextEmail(String emailAddress) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        email.putExtra(Intent.EXTRA_SUBJECT, "Please do this!");
        email.putExtra(Intent.EXTRA_TEXT, "");
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }

    private void initView() {


        try {
            /**
             *View to be find from Layout false alarm
             */

            _volume = (com.marcopolo.views.Slider) rootView.findViewById(R.id.sliderNumberSettings);
            _volume1 = (DiscreteSeekBar) rootView.findViewById(R.id.sliderNumberSettings1);

            volume_indicator_text = (TextView) rootView.findViewById(R.id.volume_indicator_text);
            radio1 = (RadioButton) rootView.findViewById(R.id.radio1);
            radio2 = (RadioButton) rootView.findViewById(R.id.radio2);
            _pushNotification = (ToggleButton) rootView.findViewById(R.id.toggle_Notification);
            _silent = (ToggleButton) rootView.findViewById(R.id.toggle_Silent);
            _share = (ImageView) rootView.findViewById(R.id.iv_share);
            _share = (ImageView) rootView.findViewById(R.id.iv_share);
            share_layout = (RelativeLayout) rootView.findViewById(R.id.share_layout);
            share_layout.setOnClickListener(this);


            txt_not_working_layout = (RelativeLayout) rootView.findViewById(R.id.txt_not_working_layout);
            have_recommendation_layout = (RelativeLayout) rootView.findViewById(R.id.have_recommendation_layout);
            mark_sign_volume = (ImageView) rootView.findViewById(R.id.mark_sign_volume);

            mFromDateTextView = (TextView) rootView.findViewById(R.id.tv_From);
            mToDateTextView = (TextView) rootView.findViewById(R.id.tv_To);


            listen_image = (ImageView) rootView.findViewById(R.id.listen_image);
            switch_mark = (ImageView) rootView.findViewById(R.id.switch_mark);
            override_mark = (ImageView) rootView.findViewById(R.id.override_mark);

            push_mark = (ImageView) rootView.findViewById(R.id.push_mark);
            fixednotification_mark = (ImageView) rootView.findViewById(R.id.fixednotification_mark);

            fixednotification_mark.setOnClickListener(this);

            mark_sign_volume.setOnClickListener(this);
            push_mark.setOnClickListener(this);


            mFromDateTextView.setOnClickListener(this);

            mToDateTextView.setOnClickListener(this);

            listen_image.setOnClickListener(this);
            switch_mark.setOnClickListener(this);
            override_mark.setOnClickListener(this);


            if (SharedPreferenceWriter.getInstance(getActivity()).getBoolean(SPreferenceKey.OVERRIDE_SILENT_STATUS)) {
                _silent.setChecked(true);
            } else {
                _silent.setChecked(false);
            }

            if (SharedPreferenceWriter.getInstance(getActivity()).getBoolean(SPreferenceKey.PUSH_NOTIFICATION_STATUS)) {
                _pushNotification.setChecked(true);
            } else {
                _pushNotification.setChecked(false);
            }

            _silent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferenceWriter.getInstance(getActivity()).writeBooleanValue(SPreferenceKey.OVERRIDE_SILENT_STATUS, b);
                }
            });


            _pushNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferenceWriter.getInstance(getActivity()).writeBooleanValue(SPreferenceKey.PUSH_NOTIFICATION_STATUS, b);
                }
            });


            _share.setOnClickListener(this);


            have_recommendation_layout.setOnClickListener(this);

            txt_not_working_layout.setOnClickListener(this);

            radio2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  MarcoPoloApplication.getInstance().stopVRService();
                    // MarcoPoloApplication.getInstance().startGVService();


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
                    //MarcoPoloApplication.getInstance().stopGVService();
                    //MarcoPoloApplication.getInstance().startVRService();


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


        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            rootView.findViewById(R.id.tv_Close).setOnClickListener(this);
            rootView.findViewById(R.id.toggle_Silent).setOnClickListener(this);
            _pushNotification.setOnClickListener(this);
            rootView.findViewById(R.id.iv_share).setOnClickListener(this);
            rootView.findViewById(R.id.toggle_FixedNotification).setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {

            lastSensitivity = SharedPreferenceWriter.getInstance(getActivity()).getInt(SPreferenceKey.SENSITIVITY_VALUE_STATUS);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            setValues();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTime();


    }

    private void setValues() {
        if (SharedPreferenceWriter.getInstance(getActivity()).getBoolean(SPreferenceKey.PUSH_NOTIFICATION_STATUS)) {
            (_pushNotification).setChecked(true);
        } else {
            (_pushNotification).setChecked(false);
        }

        if (SharedPreferenceWriter.getInstance(getActivity()).getBoolean(SPreferenceKey.PUSH_FIXED_NOTIFICATION_STATUS)) {
            ((ToggleButton) rootView.findViewById(R.id.toggle_FixedNotification)).setChecked(true);
        } else {
            ((ToggleButton) rootView.findViewById(R.id.toggle_FixedNotification)).setChecked(false);
        }

        if (SharedPreferenceWriter.getInstance(getActivity()).getBoolean(SPreferenceKey.OVERRIDE_SILENT_STATUS)) {
            ((ToggleButton) rootView.findViewById(R.id.toggle_Silent)).setChecked(true);
        } else {
            ((ToggleButton) rootView.findViewById(R.id.toggle_Silent)).setChecked(false);
        }

        if (SharedPreferenceWriter.getInstance(getActivity()).getInt(SPreferenceKey.VOLUME_STATUS) == 0) {
            SharedPreferenceWriter.getInstance(getActivity()).writeIntValue(SPreferenceKey.VOLUME_STATUS, 70);

        } else {

        }
//  ficode

    }

    private void setTime() {
        try {
            if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.FROM_TIME).toString().length() > 0
                    && SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.TO_TIME).toString().length() > 0) {

                NumberFormat format = new DecimalFormat("00");
                if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.FROM_TIME).toString().length() > 0) {
                    String from[] = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.FROM_TIME).split("-");
                    if (from[2].equalsIgnoreCase("0"))
                        ((TextView) rootView.findViewById(R.id.tv_From)).setText(format.format(Integer.parseInt(from[0])) + ":" + format.format(Integer.parseInt(from[1])) + " " + "AM");
                    else
                        ((TextView) rootView.findViewById(R.id.tv_From)).setText(format.format(Integer.parseInt(from[0])) + ":" + format.format(Integer.parseInt(from[1])) + " " + "PM");

                }
                if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.TO_TIME).toString().length() > 0) {
                    String to[] = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.TO_TIME).split("-");
                    if (to[2].equalsIgnoreCase("0"))
                        ((TextView) rootView.findViewById(R.id.tv_To)).setText(format.format(Integer.parseInt(to[0])) + ":" + format.format(Integer.parseInt(to[1])) + " " + "AM");
                    else
                        ((TextView) rootView.findViewById(R.id.tv_To)).setText(format.format(Integer.parseInt(to[0])) + ":" + format.format(Integer.parseInt(to[1])) + " " + "PM");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean isScheduleTimerShowing() {
        try {
            if (null != timePickerDialog && timePickerDialog.isVisible()) {
                timePickerDialog.dismiss();
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.push_mark:

                String toast_push = activity.getResources().getString(R.string.toast_push_notification);
                UtilityFunctions.showLongToast(activity, toast_push);

                break;

            case R.id.fixednotification_mark:

                UtilityFunctions.showLongToast(activity, "TOAST");

                break;

            case R.id.listen_image:

                String toast_listen = activity.getResources().getString(R.string.toast_only_listen);
                UtilityFunctions.showLongToast(activity, "" + toast_listen);

                break;

            case R.id.switch_mark:

                String toast_switch = activity.getResources().getString(R.string.toast_switch_recogniser);
                UtilityFunctions.showLongToast(activity, "" + toast_switch);

                break;

            case R.id.override_mark:

                String toast_override_silent = activity.getResources().getString(R.string.toast_override_silent);
                UtilityFunctions.showLongToast(activity, "" + toast_override_silent);

                break;

            case R.id.mark_sign_volume:

                String toast_volume = activity.getResources().getString(R.string.toast_volume);
                UtilityFunctions.showLongToast(activity, "" + toast_volume);

                break;

            case R.id.have_recommendation_layout:

                String emailAddress = "myrecommendation@gofindmarco.com";
                ShareTextEmail(emailAddress);

                break;
            case R.id.txt_not_working_layout:

                if (UtilityFunctions.isNetworkAvailable(activity)) {
                    try {
                        String url = "http://gofindmarco.com/support.html";
                        Intent i = new Intent(activity, WebviewActivity.class);
                        i.putExtra(Constants.LOAD_URL, url);
                        activity.startActivity(i);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(activity, "Internet Not connected", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.share_layout:
                String text = "I use this app to find my lost phone by shouting MARCO! and it shouts back POLO!\n\nCheck it out: gofindmarco.com";
                ShareText(text);
                break;


            case R.id.tv_Close:
                InterfaceListener.getOnPageNavigation(Constants.CURRENT_ITEM_ZERO);
                break;

            case R.id.toggle_Notification:
                SharedPreferenceWriter.getInstance(getActivity()).writeBooleanValue(SPreferenceKey.PUSH_NOTIFICATION_STATUS, ((ToggleButton) v).isChecked());
                break;

            case R.id.toggle_FixedNotification:
                SharedPreferenceWriter.getInstance(getActivity()).writeBooleanValue(SPreferenceKey.PUSH_FIXED_NOTIFICATION_STATUS, ((ToggleButton) v).isChecked());
                Intent serviceIntent = new Intent(getActivity(), NotificationService.class);
                serviceIntent.setAction(((ToggleButton) v).isChecked() ? AppConstants.SETTINGS_SHOW : AppConstants.SETTINGS_HIDE);
                getActivity().startService(serviceIntent);
                break;

            case R.id.toggle_Silent:
                SharedPreferenceWriter.getInstance(getActivity()).writeBooleanValue(SPreferenceKey.OVERRIDE_SILENT_STATUS, ((ToggleButton) v).isChecked());
                break;

            case R.id.tv_NotWorkingTap:

                break;
            case R.id.tv_From:
                openFromTimeDialog();
                break;

            case R.id.tv_To:
                openToTimeDialog();
                break;
            case R.id.rl_Container:
                UtilityFunctions.hideSoftKeyboard(getActivity());
                break;

            default:
                break;
        }
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
                        //timePickerDialog.setDoneText("Set End Time");
                    }
                }
            });


            timeDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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


            timeDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (lastSensitivity != SharedPreferenceWriter.getInstance(getActivity()).getInt(SPreferenceKey.SENSITIVITY_VALUE_STATUS)) {
            MarcoPoloApplication.getInstance().restartVRService();
        }
    }


    private void SetHeader(View rootView) {
        TextView header_text = (TextView) rootView.findViewById(R.id.header_text);
        String headerText = activity.getResources().getString(R.string.settings);
        header_text.setText(headerText);
    }


    public void showAlertDilaog() {

        final Dialog errorDialog = new Dialog(getActivity(), R.style.ThemeDialogCustom);
        errorDialog.setContentView(R.layout.message_suggestion);
        errorDialog.setCancelable(true);
        errorDialog.setTitle("Offline Voice Recognition");

        ((ButtonFlat) errorDialog.findViewById(R.id.btn_Cancel)).setBackgroundColor(Color.parseColor("#5d5d5d"));
        ((ButtonFlat) errorDialog.findViewById(R.id.btn_Settings)).setBackgroundColor(Color.parseColor("#f47752"));
        final String tittle = "<font>If your phone does not respond between the times " + "<font color = #000000><b>" + getFromTime() + "</b></font>" + " to " + "<font color = #000000><b>" + getToTime() + "</b></font>" + " when you shout " + "<font color = #eda503><b>" + SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).toUpperCase() + "</b></font>, then please check the following:</font>";
        String detailText = "<font><p>Google Speech Recognition is required to use Marco Polo. To install Offline Speech Recognition</font>,<font color =#000000> <b>tap Settings > Offline Speech Recognition > All > </b></font> Then install English (US)";
        String detailText2 = "<b><font color = #000000>*Note:</b></font> On some devices it may take up to 5 minutes for Android to recognise your voice for the first time, please be patient";

        ((TextView) errorDialog.findViewById(R.id.tv_DetailedText)).setText(Html.fromHtml(detailText));
        ((TextView) errorDialog.findViewById(R.id.tv_TitleHeading)).setText(Html.fromHtml(tittle));
        ((TextView) errorDialog.findViewById(R.id.tv_Last)).setText(Html.fromHtml(detailText2));


        errorDialog.findViewById(R.id.btn_Settings).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                errorDialog.dismiss();

                openSpeechRecognitionSettings();
            }
        });
        errorDialog.findViewById(R.id.btn_Cancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != errorDialog)
                    errorDialog.dismiss();
            }
        });
        errorDialog.findViewById(R.id.tvEmail).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                errorDialog.dismiss();
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setType("plain/text");
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"help@gofindmarco.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Not Responding");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Text");

                getActivity().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        });


        errorDialog.show();
    }

    /**
     * Open speech recognition settings activity
     *
     * @return true in case activity was launched, false otherwise
     **/
    public boolean openSpeechRecognitionSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean started = false;
        ComponentName[] components = new ComponentName[]{
                new ComponentName("com.google.android.voicesearch", "com.google.android.voicesearch.VoiceSearchPreferences"),
                new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.voicesearch.VoiceSearchPreferences"),
                new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.velvet.ui.settings.VoiceSearchPreferences")
        };
        for (ComponentName componentName : components) {
            try {
                intent.setComponent(componentName);
                startActivity(intent);
                started = true;
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return started;
    }

    private String getToTime() {
        StringBuilder builder = new StringBuilder();
        NumberFormat format = new DecimalFormat("00");
        String to[] = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.TO_TIME).split("-");
        if (Integer.parseInt(to[2]) == 0) {
            builder.append(format.format(Integer.parseInt(to[0])) + ":" + format.format(Integer.parseInt(to[1])) + " " + "AM");
        } else {
            builder.append(format.format(Integer.parseInt(to[0])) + ":" + format.format(Integer.parseInt(to[1])) + " " + "PM");
        }
        return builder.toString();

    }

    private String getFromTime() {
        StringBuilder builder = new StringBuilder();
        NumberFormat format = new DecimalFormat("00");
        String from[] = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.FROM_TIME).split("-");
        if (Integer.parseInt(from[2]) == 0) {
            builder.append(format.format(Integer.parseInt(from[0])) + ":" + format.format(Integer.parseInt(from[1])) + " " + "AM");
        } else {
            builder.append(format.format(Integer.parseInt(from[0])) + ":" + format.format(Integer.parseInt(from[1])) + " " + "PM");
        }
        return builder.toString();
    }


    // add condition for starting svervice after 24 hour in end service

    TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        String meridiem;

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            try {
                String valueTime = "" + (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay) + "-" + minute + "-" + (hourOfDay > 12 ? 1 : 0);
                SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(isFromTimeCliked ?
                        SPreferenceKey.FROM_TIME : SPreferenceKey.TO_TIME, valueTime);
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

    @Override
    public void onControllingVolume() {
        InitialisingAudioService();
    }
}
