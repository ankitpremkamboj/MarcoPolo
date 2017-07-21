package com.morcopolo.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.marcopolo.InterfaceListeners.InterfaceListener;
import com.marcopolo.InterfaceListeners.Interfaces.OnSendingAudioPath;
import com.marcopolo.R;
import com.marcopolo.Tabs.TabActivity;
import com.marcopolo.main.MarcoPoloApplication;
import com.marcopolo.sharedpreference.SPreferenceKey;
import com.marcopolo.sharedpreference.SharedPreferenceWriter;
import com.marcopolo.utils.AppConstants;
import com.marcopolo.utils.Constants;
import com.marcopolo.utils.FontTypeface;
import com.marcopolo.utils.FontUtility;
import com.marcopolo.utils.UtilityFunctions;
import com.marcopolo.views.ButtonRectangle;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

/*import com.startapp.android.publish.adsCommon.StartAppAd;*/

public class SetPhraseFragment extends Fragment implements OnClickListener, TextWatcher, OnEditorActionListener, OnSendingAudioPath {
    private boolean userClickedAd;
    /*  private StartAppAd startAppAd;*/
    private View rootView;
    private Animation zoomIn, zoomOut, anim_RightToLeft;
    private boolean isFirstWordSet = false;
    private ImageView iv_Recoding, img_recodring;
    private String missingWord;
    private ProgressDialog progressDialogInbox;
    private Handler handler = new Handler();
    private String lastKeyPrase;
    private CustomAdapter customAdapter;
    private CustomAdapterBeta customAdapterBeta;
    private ArrayList<String> suggestionList;
    private ArrayList<String> suggestionListBeta;
    private int selectedItem = -1;
    ListView listView;
    ImageView cross_image, back_button;
    Activity activity;
    EditText etMarco;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_phrase_fragment, container, false);
        activity = getActivity();

        //InterfaceListener.setmOnSendingAudioPath(this);
        initViews();
        SetHeader(rootView);
        return rootView;
    }


    private void SetHeader(View rootView) {
        TextView header_text = (TextView) rootView.findViewById(R.id.header_text);
        String headerText = activity.getResources().getString(R.string.phrase_fragment_header_one);
        header_text.setText(headerText);

        Button btn_Skip = (Button) rootView.findViewById(R.id.btn_Skip);
        btn_Skip.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        /*startAppAd = new StartAppAd(activity);*/
        super.onAttach(activity);
    }

    private void initViews() {
        try {
            //loadAds();
            zoomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_in);
            zoomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_out);
            anim_RightToLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_righttoleft);
            rootView.findViewById(R.id.iv_Close).setOnClickListener(this);
            rootView.findViewById(R.id.btn_Save).setOnClickListener(this);
            rootView.findViewById(R.id.btn_Skip).setOnClickListener(this);
            rootView.findViewById(R.id.iv_Recoding).setOnClickListener(this);
            rootView.findViewById(R.id.iv_mp3).setOnClickListener(this);
            back_button = (ImageView) rootView.findViewById(R.id.back_button);
            back_button.setOnClickListener(this);

            ((ListView) rootView.findViewById(R.id.lv_SuggestionList)).setOnItemClickListener(itemClickListener);

            etMarco = ((EditText) rootView.findViewById(R.id.et_Marco));
            etMarco.addTextChangedListener(this);
            try {
                etMarco.setSelection(etMarco.getText().toString().trim().length());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //int color = R.color.red_color;

            cross_image = (ImageView) rootView.findViewById(R.id.cross_image);
            cross_image.setOnClickListener(this);

            etMarco.setOnEditorActionListener(this);
            ((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).setAnimation(zoomIn);
            ((TextView) rootView.findViewById(R.id.header_text)).setAnimation(anim_RightToLeft);
            ((TextView) rootView.findViewById(R.id.header_text)).setTypeface(FontTypeface.getTypeface(getActivity(), AppConstants.RBOTO_REGULAR));
            ((TextView) rootView.findViewById(R.id.tv_Recommendation)).setTypeface(FontTypeface.getTypeface(getActivity(), AppConstants.RBOTO_REGULAR));
            ((TextView) rootView.findViewById(R.id.tv_Instruction)).setTypeface(FontTypeface.getTypeface(getActivity(), AppConstants.RBOTO_Light));

            FontUtility.getInstance().setTypePhase(getActivity(), (TextView) rootView.findViewById(R.id.tv_Result), AppConstants.TOMATO_ROUND_CONDENSED);
            setHintOnEditText();
            lastKeyPrase = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE);
            setSuggestionList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSuggestionList() {
        suggestionList = new ArrayList<>();
        suggestionList.add("Marco");
        suggestionList.add("Where are you phone?");
        suggestionList.add("Get back in my pocket");
        suggestionList.add("Did the couch get you again?");
        suggestionList.add("Who's your daddy?");
        suggestionList.add("Macaroni and cheese");
        suggestionList.add("OK Marco");

        customAdapter = new CustomAdapter();
        customAdapter.initViews(suggestionList, selectedItem);
        ((ListView) rootView.findViewById(R.id.lv_SuggestionList)).setAdapter(customAdapter);
    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            etMarco.setText(suggestionList.get(position).replace("?", ""));
            try {
                etMarco.setSelection(etMarco.getText().toString().length());
            } catch (Exception e) {
                e.printStackTrace();
            }
            customAdapter.initViews(suggestionList, position);
            customAdapter.notifyDataSetChanged();
        }
    };

    @SuppressLint("NewApi")
    private void setHintOnEditText() {
        try {
            if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).isEmpty()) {

                ((FloatLabeledEditText) rootView.findViewById(R.id.floatableView)).setHint(AppConstants.DEFAULT_INPUT_PHRASE.toUpperCase(Locale.getDefault()));
            } else {
                ((FloatLabeledEditText) rootView.findViewById(R.id.floatableView)).setHint(SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).toUpperCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHintOnEditText_Reply() {
        try {
            if (SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.REPLY_KEYPHRASE).isEmpty()) {

                ((FloatLabeledEditText) rootView.findViewById(R.id.floatableView)).setHint(AppConstants.DEFAULT_OUTPUT_PHRASE.toUpperCase(Locale.getDefault()));
            } else {
                ((FloatLabeledEditText) rootView.findViewById(R.id.floatableView)).setHint(SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.REPLY_KEYPHRASE).toUpperCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (isFirstWordSet) {
            if (s.length() == 1) {

                /**
                 * Button is getting visible which we are controlling functionality on header done button.
                 */
                if (!((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).isShown()) {
                    ((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).setVisibility(View.GONE);
                    //((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).setVisibility(View.VISIBLE);
                    ((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).startAnimation(zoomIn);
                }
            } else if (s.length() == 0) {
                if (((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).isShown()) {
                    ((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).startAnimation(zoomOut);
                    zoomOut.setAnimationListener(animationListner);
                }

            }
        }
        if (isFirstWordSet && s.length() == 0) {
            rootView.findViewById(R.id.iv_Recoding).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.recording_layout).setVisibility(View.VISIBLE);//Added full layout

        } else {
            rootView.findViewById(R.id.iv_Recoding).setVisibility(View.GONE);
            rootView.findViewById(R.id.recording_layout).setVisibility(View.GONE);//Added full layout

        }
    }

    @Override
    public void onDestroy() {
        /*if (mAdView != null) {
            mAdView.destroy();
        }*/
        // TODO Auto-generated method stub
        super.onDestroy();
        if (!lastKeyPrase.equalsIgnoreCase(SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE))) {
            SharedPreferenceWriter.getInstance(getActivity()).writeIntValue(SPreferenceKey.SENSITIVITY_VALUE_STATUS, countSyllables(SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE)));
            MarcoPoloApplication.getInstance().restartVRService();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

        if (s.toString().length() > 0) {
            if (!cross_image.isShown())
                cross_image.setVisibility(View.VISIBLE);
        } else {
            cross_image.setVisibility(View.GONE);
        }

    }

    private AnimationListener animationListner = new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            ((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).setVisibility(View.GONE);
        }
    };

    @Override
    public void OnSendingAudio(Uri uri) {
        Toast.makeText(activity, "Ur" + uri, Toast.LENGTH_SHORT).show();

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
                    isFirstWordSet = true;
                    UtilityFunctions.hideSoftKeyboard(getActivity());
                    //((TabActivity) getActivity()).setNewPhrase();
                    InterfaceListener.getCallingHomeUI();
                    setReplyWord();

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

    public void setReplyWord() {


        UtilityFunctions.hideSoftKeyboard(getActivity());
        ((EditText) rootView.findViewById(R.id.et_Marco)).setImeOptions(EditorInfo.IME_ACTION_DONE);
        ((EditText) rootView.findViewById(R.id.et_Marco)).setText("");
        rootView.findViewById(R.id.iv_Recoding).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.iv_mp3).setVisibility(View.VISIBLE);
        ((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).setVisibility(View.GONE);
        ((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).setText("Done");
        back_button.setVisibility(View.VISIBLE);
        //((FloatLabeledEditText) rootView.findViewById(R.id.floatableView)).setHint("POLO");
        setHintOnEditText_Reply();
        ((TextView) rootView.findViewById(R.id.header_text)).setText("Reply with");
        ((TextView) rootView.findViewById(R.id.header_text)).startAnimation(anim_RightToLeft);
        ((Button) rootView.findViewById(R.id.btn_Skip)).setText("Done");
        ((ListView) rootView.findViewById(R.id.lv_SuggestionList)).setVisibility(View.GONE);
        ((ListView) rootView.findViewById(R.id.lv_SuggestionListBeta)).setVisibility(View.VISIBLE);
        ((TextView) rootView.findViewById(R.id.tv_Recommendation)).setVisibility(View.GONE);
        ((TextView) rootView.findViewById(R.id.tv_Instruction)).setVisibility(View.GONE);


        suggestionListBeta = new ArrayList<>();
        suggestionListBeta.add("Polo");
        suggestionListBeta.add("I'm over here");
        suggestionListBeta.add("Roger that");
        suggestionListBeta.add("I need backup");
        suggestionListBeta.add("I'm your daddy");

        customAdapterBeta = new CustomAdapterBeta();
        customAdapterBeta.initView(suggestionListBeta, selectedItem);
        ((ListView) rootView.findViewById(R.id.lv_SuggestionListBeta)).setAdapter(customAdapterBeta);

        ((ListView) rootView.findViewById(R.id.lv_SuggestionListBeta)).setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((EditText) rootView.findViewById(R.id.et_Marco)).setText(suggestionListBeta.get(i).replace("?", ""));
                customAdapterBeta.initView(suggestionListBeta, i);
                customAdapterBeta.notifyDataSetChanged();


            }
        });

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
        dialog.setOnAcceptButtonClickListener(new OnClickListener() {

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

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:

                isFirstWordSet = false;

                ((EditText) rootView.findViewById(R.id.et_Marco)).setImeOptions(EditorInfo.IME_ACTION_NEXT);
                ((EditText) rootView.findViewById(R.id.et_Marco)).setText("");
                rootView.findViewById(R.id.iv_Recoding).setVisibility(View.GONE);
                rootView.findViewById(R.id.iv_mp3).setVisibility(View.GONE);
                ((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).setVisibility(View.GONE);
                ((ButtonRectangle) rootView.findViewById(R.id.btn_Save)).setText("Next");
                back_button.setVisibility(View.GONE);
                ((TextView) rootView.findViewById(R.id.header_text)).setText("When I Say");
                ((TextView) rootView.findViewById(R.id.header_text)).startAnimation(anim_RightToLeft);
                ((Button) rootView.findViewById(R.id.btn_Skip)).setText("Next");
                ((ListView) rootView.findViewById(R.id.lv_SuggestionList)).setVisibility(View.VISIBLE);
                ((ListView) rootView.findViewById(R.id.lv_SuggestionListBeta)).setVisibility(View.GONE);
                ((TextView) rootView.findViewById(R.id.tv_Recommendation)).setVisibility(View.VISIBLE);
                ((TextView) rootView.findViewById(R.id.tv_Instruction)).setVisibility(View.VISIBLE);

                setHintOnEditText();
                lastKeyPrase = SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE);
                setSuggestionList();

                break;
            case R.id.cross_image:

                ((EditText) rootView.findViewById(R.id.et_Marco)).setText("");

                break;


            case R.id.btn_Save:
                saveMarcoPhrase();
                break;

            case R.id.iv_Close:
                InterfaceListener.getOnPageNavigation(Constants.CURRENT_ITEM_ZERO);

                break;

            case R.id.btn_Skip:

                if (!isFirstWordSet && ((EditText) rootView.findViewById(R.id.et_Marco)).getText().toString().isEmpty()) {
                    // Move to Next Screen
                    isFirstWordSet = true;
                    setReplyWord();

                } else {
                    saveMarcoPhrase();
                }

                break;
            case R.id.iv_Recoding:

                InterfaceListener.getOnPageNavigation(Constants.CURRENT_ITEM_TWO);
                try {
                    getActivity().overridePendingTransition(R.anim.slide_up, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.iv_mp3:
                InterfaceListener.getOnPageNavigation(Constants.CURRENT_ITEM_TWO);
                try {
                    getActivity().overridePendingTransition(R.anim.slide_up, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;


            default:
                break;
        }
    }

    @SuppressLint("NewApi")
    private void saveMarcoPhrase() {

        if (!isFirstWordSet) { // For Setting Main Word
            if (getWord().isEmpty()) {
                isFirstWordSet = true;
                setReplyWord();

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
            isFirstWordSet = false;
            if (!getWord().isEmpty())
                SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(SPreferenceKey.REPLY_KEYPHRASE, getWord());
            SharedPreferenceWriter.getInstance(getActivity()).writeStringValue(SPreferenceKey.RESPONSE_TYPE, AppConstants.TEXT);
            ((LinearLayout) rootView.findViewById(R.id.ll_FieldContainer)).setVisibility(View.GONE);
            String text = "<font color=#ffffff>Call out </font><font color=#eda503>" + SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.KEYPHRASE).toUpperCase(Locale.getDefault()) + "!</font><font color=#ffffff> and your Phone will reply with </font><font color=#D97245>" + SharedPreferenceWriter.getInstance(getActivity()).getString(SPreferenceKey.REPLY_KEYPHRASE).toUpperCase(Locale.getDefault()) + "!";
            ((TextView) rootView.findViewById(R.id.tv_Result)).setText(Html.fromHtml(text));
            //setNewPhrase(activity);
            InterfaceListener.getCallingHomeUI();


            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    ((EditText) rootView.findViewById(R.id.et_Marco)).setText("");
                    InterfaceListener.getOnPageNavigation(Constants.CURRENT_ITEM_ZERO);
                    ((TabActivity) getActivity()).SetAdapterViewPager();
                    /*startAppAd.showAd();*/

                    MarcoPoloApplication.getInstance().restartVRService();
                }
            }, 3000);


            back_button.setVisibility(View.GONE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InterfaceListener.getOnLoadingInterstitialAd();
                }
            }, 2000);
            ((Button) rootView.findViewById(R.id.btn_Skip)).setVisibility(View.GONE);



        }

    }

    /**
     * Not in use..as we are updating on home.
     */


    public String getWord() {
        return ((EditText) rootView.findViewById(R.id.et_Marco)).getText().toString().trim();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
            saveMarcoPhrase();
        }
        return false;
    }

    private class CustomAdapter extends BaseAdapter {
        private ArrayList<String> suggestionList;
        int selectedItem;

        @Override
        public int getCount() {
            return suggestionList.size();
        }

        public void initViews(ArrayList<String> suggestionList, int selectedItem) {
            this.suggestionList = suggestionList;
            this.selectedItem = selectedItem;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_suggestion_list, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_Suggestions = (TextView) convertView.findViewById(R.id.tv_Suggestion);
                viewHolder.mainLayout = (RelativeLayout) convertView.findViewById(R.id.mainLayout);
                viewHolder.tv_Dot = (TextView) convertView.findViewById(R.id.tvCheck);
                viewHolder.tv_Dot.setTypeface(FontTypeface.getTypeface(getActivity(), AppConstants.FONT_AWESOME));
                viewHolder.tv_Dot.setText(getResources().getString(R.string.tick));
                viewHolder.tv_Suggestions.setTypeface(FontTypeface.getTypeface(getActivity(), AppConstants.RBOTO_Light));
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            viewHolder.tv_Suggestions.setText(suggestionList.get(position));
            if (selectedItem >= 0 && selectedItem == position) {
                viewHolder.mainLayout.setBackgroundResource(R.drawable.selected_background_list_itme);
                viewHolder.tv_Dot.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mainLayout.setBackgroundResource(R.drawable.default_backround_list_item);
                viewHolder.tv_Dot.setVisibility(View.GONE);

            }
            return convertView;
        }

    }

    private class CustomAdapterBeta extends BaseAdapter {
        private ArrayList<String> suggestionList;
        int selectedItem;

        @Override
        public int getCount() {
            return suggestionList.size();
        }

        public void initView(ArrayList<String> suggestionList, int selectedItem) {
            this.suggestionList = suggestionList;
            this.selectedItem = selectedItem;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_suggestion_list, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_Suggestions = (TextView) convertView.findViewById(R.id.tv_Suggestion);
                viewHolder.mainLayout = (RelativeLayout) convertView.findViewById(R.id.mainLayout);
                viewHolder.tv_Dot = (TextView) convertView.findViewById(R.id.tvCheck);
                viewHolder.tv_Dot.setTypeface(FontTypeface.getTypeface(getActivity(), AppConstants.FONT_AWESOME));
                viewHolder.tv_Dot.setText(getResources().getString(R.string.tick));
                viewHolder.tv_Suggestions.setTypeface(FontTypeface.getTypeface(getActivity(), AppConstants.RBOTO_Light));
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            viewHolder.tv_Suggestions.setText(suggestionList.get(position));
            if (selectedItem >= 0 && selectedItem == position) {
                viewHolder.mainLayout.setBackgroundResource(R.drawable.selected_background_list_itme);
                viewHolder.tv_Dot.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mainLayout.setBackgroundResource(R.drawable.default_backround_list_item);
                viewHolder.tv_Dot.setVisibility(View.GONE);

            }
            return convertView;
        }

    }

    static class ViewHolder {
        TextView tv_Suggestions, tv_Dot;
        RelativeLayout mainLayout;
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
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
