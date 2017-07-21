package com.marcopolo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marcopolo.R;

public class WebviewActivity extends Activity {
    private WebView webview;
    private static final String TAG = "Main";
    private ProgressDialog progressBar;
    ImageView back_button;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.webview);

        SetHeader();

        webview = (WebView) findViewById(R.id.webView);
        back_button = (ImageView)findViewById(R.id.back_button);
        back_button.setVisibility(View.VISIBLE);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        progressBar = ProgressDialog.show(WebviewActivity.this, "", "Loading...");

        webview.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest url) {
              //  Log.i(TAG, "Processing webview url click...");
                view.loadUrl(url.getUrl().toString());
                return true;
            }

            public void onPageFinished(WebView view, String url) {
              //  Log.i(TAG, "Finished loading URL: " + url);
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
               // Log.e(TAG, "Error: " + description);
                //Toast.makeText(WebviewActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(description);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                alertDialog.show();
            }
        });





        String url = getIntent().getStringExtra(Constants.LOAD_URL);
        webview.loadUrl(url);
    }


    private void SetHeader() {
        TextView header_text = (TextView) findViewById(R.id.header_text);
        String headerText = getResources().getString(R.string.support_center);
        header_text.setText(headerText);
    }
}