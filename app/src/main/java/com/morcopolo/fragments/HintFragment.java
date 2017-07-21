package com.morcopolo.fragments;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.marcopolo.InterfaceListeners.InterfaceListener;
import com.marcopolo.R;
import com.marcopolo.callbacks.HintDialogCloseListener;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;
import com.marcopolo.utils.UtilityFunctions;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created by Renjith on 16-09-2016.
 */
public class HintFragment extends DialogFragment implements TextView.OnEditorActionListener {

    private TextView mCloseTextView;
    private static HintDialogCloseListener mHintDialogCloseListener;
    private String previousValue = "";
    private ProgressDialog progressDialogInbox;
    private Handler handler = new Handler();
    private String missingWord;
    private Animation anim_RightToLeft;
    private View rootView;

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static HintFragment newInstance(HintDialogCloseListener mHintDialogCloseListener) {
        HintFragment f = new HintFragment();
        HintFragment.mHintDialogCloseListener = mHintDialogCloseListener;
        f.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ThemeDialogCustom);
        return f;
    }

    private TextView mHint1TextView, mHint2TextView, mHint3TextView, mHint4TextView, mHint5TextView, mHint6TextView, mHint7TextView;
    private EditText mSelectedTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_false_alarm_hint, container, false);
        anim_RightToLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_righttoleft);
        mCloseTextView = (TextView) rootView.findViewById(R.id.bt_close);
        mSelectedTextView = (EditText) rootView.findViewById(R.id.et_Marco);

        if (mSelectedTextView.getText().toString().length() > 0) {
            mSelectedTextView.setSelection(mSelectedTextView.getText().toString().length());
        }

        mHint1TextView = (TextView) rootView.findViewById(R.id.selected_tv_1);
        mHint2TextView = (TextView) rootView.findViewById(R.id.selected_tv_2);
        mHint3TextView = (TextView) rootView.findViewById(R.id.selected_tv_3);
        mHint4TextView = (TextView) rootView.findViewById(R.id.selected_tv_4);
        mHint5TextView = (TextView) rootView.findViewById(R.id.selected_tv_5);
        mHint6TextView = (TextView) rootView.findViewById(R.id.selected_tv_6);
        mHint7TextView = (TextView) rootView.findViewById(R.id.selected_tv_7);


        mSelectedTextView.setOnEditorActionListener(this);

        mSelectedTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).isEmpty()) {
            previousValue = "MACARONI AND CHEESE";
        } else {
            previousValue = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).toUpperCase();
        }
        mSelectedTextView.setHint(previousValue);
        ((FloatLabeledEditText) rootView.findViewById(R.id.floatableView)).setHint(previousValue);
        mCloseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "woke", Toast.LENGTH_SHORT).show();

                if (mCloseTextView.getText().toString().equalsIgnoreCase("DONE")) {
                    String valueString = mSelectedTextView.getText().toString();
                    if (valueString.length() > 0 && !previousValue.equalsIgnoreCase(previousValue)) {
                        SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(SPreferenceKey.KEYPHRASE, valueString);
                    }
                    saveMarcoPhrase();
                }
            }
        });

        mHint1TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingPhrase(((TextView) v).getText().toString());
            }
        });

        mHint2TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingPhrase(((TextView) v).getText().toString());
            }
        });

        mHint3TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingPhrase(((TextView) v).getText().toString());
            }
        });

        mHint4TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingPhrase(((TextView) v).getText().toString());
            }
        });

        mHint5TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingPhrase(((TextView) v).getText().toString());
            }
        });

        mHint6TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingPhrase(((TextView) v).getText().toString());
            }
        });

        mHint7TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingPhrase(((TextView) v).getText().toString());
            }
        });


        return rootView;
    }

    private void settingPhrase(String valueString) {
        mSelectedTextView.setText(valueString.replace("?", ""));
        SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(SPreferenceKey.KEYPHRASE, valueString.replace("?", ""));
        try {
            mSelectedTextView.setSelection(valueString.replace("?", "").length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFisrstWordSet = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHintDialogCloseListener.handleDialogClose();
    }


    @SuppressLint("NewApi")
    private void saveMarcoPhrase() {
        if (!isFisrstWordSet) { // For Setting Main Word
            if (getWord().isEmpty()) {
                isFisrstWordSet = true;
//                setReplyWord();
                dismiss();
            } else {
                try {
                    CheckWordInDict checkWord = new CheckWordInDict();
                    checkWord.execute(getWord());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            UtilityFunctions.hideSoftKeyboard(getActivity());
            isFisrstWordSet = false;
            if (!getWord().isEmpty())
                SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(SPreferenceKey.REPLY_KEYPHRASE, getWord());
            SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(SPreferenceKey.RESPONSE_TYPE, AppConstants.TEXT);
            ((LinearLayout) rootView.findViewById(R.id.ll_FieldContainer)).setVisibility(View.GONE);
            String text = "<font color=#000000>Call out </font><font color=#eda503>" + SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).toUpperCase(Locale.getDefault()) + "!</font><font color=#000000> and your Phone will reply with </font><font color=#D97245>" + SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.REPLY_KEYPHRASE).toUpperCase(Locale.getDefault()) + "!";
            ((TextView) rootView.findViewById(R.id.tv_Result)).setText(Html.fromHtml(text));
            //((HomeScreen) getActivity()).setNewPhrase();

            InterfaceListener.getCallingHomeUI();

            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    dismiss();
                }
            }, 3000);
            mSelectedTextView.setVisibility(View.GONE);
        }

    }

    public boolean isWordsExist(String keyWord) {
        try {
            InputStream in;
            BufferedReader reader;
            String line;
            String[] match;
            in = getActivity().getAssets().open("file_dictionary.txt");
            reader = new BufferedReader(new InputStreamReader(in));
            if (reader != null) {
                while ((line = reader.readLine()) != null) {
                    match = line.split("\t");
                    if (match[0].equalsIgnoreCase(keyWord)) {
                        return true;
                    }
                }
                in.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void wordMissingDialog() {

        final com.gc.materialdesign.widgets.Dialog dialog = new com.gc.materialdesign.widgets.Dialog(getActivity(), "Sorry", "\"" + missingWord + "\" is not supported right now. Please use another word");
        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Click accept button", 1).show();
                if (null != dialog) {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void showProgressDialog(String message) {
        try {
            if ((progressDialogInbox == null) || (!progressDialogInbox.isShowing())) {
                progressDialogInbox = new ProgressDialog(getActivity());
                progressDialogInbox.setMessage(message);
                progressDialogInbox.setIndeterminate(false);
                progressDialogInbox.setCancelable(false);
                progressDialogInbox.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialogInbox.setIndeterminate(true);
                progressDialogInbox.setCanceledOnTouchOutside(false);
                progressDialogInbox.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class CheckWordInDict extends AsyncTask<String, Void, Boolean> {
        String sentance = "";

        @Override
        protected void onPreExecute() {
            showProgressDialog("Updating...");
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... str) {
            sentance = str[0];
            if (checkSentance(str[0])) {
                return true;
            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            try {
                if (progressDialogInbox != null)
                    progressDialogInbox.dismiss();
                if (result) {
                    SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(SPreferenceKey.KEYPHRASE, getWord());
                    isFisrstWordSet = true;
                    UtilityFunctions.hideSoftKeyboard(getActivity());
                    //((HomeScreen) getActivity()).setNewPhrase();
                    InterfaceListener.getCallingHomeUI();
//                    setReplyWord();
                    dismiss();
                } else {
                    wordMissingDialog();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            super.onPostExecute(result);
        }
    }

    public boolean checkSentance(String keyWord) {
        try {
            String[] sentance = keyWord.split(" ");
            for (int i = 0; i < sentance.length; i++) {
                keyWord = sentance[i];
                if (isWordsExist(keyWord)) {

                    continue;
                } else {
                    missingWord = sentance[i];
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getWord() {
        return mSelectedTextView.getText().toString().trim();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
            saveMarcoPhrase();
        }
        return false;
    }

    public void setReplyWord() {
        UtilityFunctions.hideSoftKeyboard(getActivity());
        mSelectedTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mSelectedTextView.setText("");
        rootView.findViewById(R.id.ll_FieldContainer_2).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.iv_Recoding).setVisibility(View.GONE);
//        mCloseTextView.setVisibility(View.GONE);
        mCloseTextView.setText("Done");
        ((FloatLabeledEditText) rootView.findViewById(R.id.floatableView)).setHint("POLO");
        ((TextView) rootView.findViewById(R.id.tv_WhenISay)).setText("Reply with");
        ((TextView) rootView.findViewById(R.id.tv_WhenISay)).startAnimation(anim_RightToLeft);

    }
}
