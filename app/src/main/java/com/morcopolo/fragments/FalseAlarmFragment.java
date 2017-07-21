package com.morcopolo.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.marcopolo.R;
import com.marcopolo.Tabs.TabActivity;
import com.marcopolo.callbacks.HintDialogCloseListener;
import com.marcopolo.callbacks.MyDialogCloseListener;
import com.marcopolo.main.MarcoPoloApplication;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.morcopolo.fragments.Fragmentss.DiscreteSeekBarFalseAlarm;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Renjith on 16-09-2016.
 */
public class FalseAlarmFragment extends DialogFragment implements HintDialogCloseListener {
    private TextView mCloseTextView;
    private ImageView cross_icon_image;
    private TextView mHeadTextView;
    private TextView mHintTextView;
    private String lastKeyPrase;
    private int lastSensitivity;
    // private SliderPopUp mSensitivitySlider;
    private DiscreteSeekBarFalseAlarm mSensitivitySlider;
    private static MyDialogCloseListener myDialogCloseListener;
    private TextView mFromDateTextView, mToDateTextView;
    private String Scheduled_Option_From = "Scheduled_Option_From";
    private String Scheduled_Option_To = "Scheduled_Option_To";
    private String Scheduled_option;
    private boolean isFromTimeCliked = false;
    private boolean isTimeCliked = false;
    private View rootView;
    private TextView sensitity_txt;

    private Timer timer;
    ProgressDialog progressDialog = null;
    private Activity activity;

    private RadioButton radio1, radio2;

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static FalseAlarmFragment newInstance(String nameHead, MyDialogCloseListener myDialogCloseListener) {
        FalseAlarmFragment f = new FalseAlarmFragment();
        FalseAlarmFragment.myDialogCloseListener = myDialogCloseListener;
        f.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ThemeDialogCustom);
        //f.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullBleedTheme_Dialog);
        Bundle args = new Bundle();
        args.putString("nameHead", nameHead);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_false_alarm, container, false);

        activity = getActivity();
        String strText = getArguments().getString("nameHead");

        mCloseTextView = (TextView) rootView.findViewById(R.id.bt_close);
        cross_icon_image = (ImageView) rootView.findViewById(R.id.cross_icon_image);
        sensitity_txt = (TextView) rootView.findViewById(R.id.sensitity_txt);
        mHeadTextView = (TextView) rootView.findViewById(R.id.tv_head);
        mHintTextView = (TextView) rootView.findViewById(R.id.tv_hint);
        radio1 = (RadioButton) rootView.findViewById(R.id.radio1);
        radio2 = (RadioButton) rootView.findViewById(R.id.radio2);
        mSensitivitySlider = (DiscreteSeekBarFalseAlarm) rootView.findViewById(R.id.sensitivtyValue);

        mHeadTextView.setText(strText);
        if (strText.equalsIgnoreCase("False Alarm?")) {
            sensitity_txt.setText(" Decrease Sensitivity:");
        } /*else {
            mHeadTextView.setText(strText);
        }*/

        mFromDateTextView = (TextView) rootView.findViewById(R.id.tv_From);
        mToDateTextView = (TextView) rootView.findViewById(R.id.tv_To);

        mCloseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                try {
                    getActivity().getFragmentManager().popBackStack();
                    ((TabActivity) getActivity()).SetAdapterViewPager();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }
        });

        cross_icon_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                getActivity().getFragmentManager().popBackStack();
            }
        });

        mFromDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFromTimeDialog();
            }
        });

        mToDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openToTimeDialog();
            }
        });

        mHintTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HintFragment hintFragment = new HintFragment().newInstance(FalseAlarmFragment.this);
                hintFragment.show(getFragmentManager(), "HintFragment");
            }
        });

        //  int stvalue=SharedPreferenceWriter.getInstance(getActivity()).getInt(SPreferenceKey.SENSITIVITY_VALUE_STATUS);

        mSensitivitySlider.setProgress(SharedPreferenceWriter.getInstance(getActivity()).getInt(SPreferenceKey.SENSITIVITY_VALUE_STATUS));

        mSensitivitySlider.setOnProgressChangeListener(new DiscreteSeekBarFalseAlarm.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBarFalseAlarm seekBar, int value, boolean fromUser) {

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
            public void onStartTrackingTouch(DiscreteSeekBarFalseAlarm seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBarFalseAlarm seekBar) {

            }
        });


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


        lastKeyPrase = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE);
        lastSensitivity = SharedPreferenceWriter.getInstance(getActivity()).getInt(SPreferenceKey.SENSITIVITY_VALUE_STATUS);
        setTime();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).isEmpty()) {
            mHintTextView.setText("MACARONI AND CHEESE");
        } else {
            mHintTextView.setText(SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).toUpperCase());
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        try {

            if (!lastKeyPrase.equalsIgnoreCase(SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE))) {
                SharedPreferenceWriter.getInstance(getActivity()).writeIntValue(SPreferenceKey.SENSITIVITY_VALUE_STATUS,
                        countSyllables(SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE)));
                MarcoPoloApplication.getInstance().restartVRService();
            } else if (lastSensitivity != SharedPreferenceWriter.getInstance(getActivity()).getInt(SPreferenceKey.SENSITIVITY_VALUE_STATUS)) {
                MarcoPoloApplication.getInstance().restartVRService();
            } else if (isTimeCliked) {
                MarcoPoloApplication.getInstance().restartVRService();
            }

            myDialogCloseListener.handleDialogClose();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        super.onDestroy();
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
        return (syl * 100 / 7);
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

    @Override
    public void handleDialogClose() {
        if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).isEmpty()) {
            mHintTextView.setText("MACARONI AND CHEESE");
        } else {
            mHintTextView.setText(SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).toUpperCase());
        }
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

    /**
     * OPEN TIME
     */
    private void openToTimeDialog() {
        getDialog().hide();
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
        getDialog().hide();
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

            final TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), R.style.MyDialogThemeTimePicker, timePickerListener, ampm == 0 ? hour : 12 + hour, min, false);

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


    TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        String meridiem;

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            try {

                String valueTime = "" + (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay) + "-" + minute + "-" + (hourOfDay > 12 ? 1 : 0);
                SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(isFromTimeCliked ?SPreferenceKey.FROM_TIME : SPreferenceKey.TO_TIME, valueTime);
                setTime();
                if (isFromTimeCliked) {
                    isFromTimeCliked = false;
                    openToTimeDialog();
                } else {
                    getDialog().show();
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
            final TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), R.style.MyDialogThemeTimePicker, timePickerListener, hour, minute, false);


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

}