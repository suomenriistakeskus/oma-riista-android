package fi.riista.mobile.activity;

import static fi.riista.mobile.ExternalUrls.getHunterMagazineUrl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import fi.riista.mobile.R;
import fi.riista.mobile.utils.AppPreferences;

public class MagazineActivity extends BaseActivity {
    static final String EXTRA_URL = "extra_url";

    private WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String url = intent.getStringExtra(MagazineActivity.EXTRA_URL);

        mWebView = new WebView(this);
        // Web reader requires JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportMultipleWindows(false);
        mWebView.getSettings().setSupportZoom(false);

        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        setTitle(R.string.more_show_magazine);
        this.setContentView(mWebView);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    static Intent getLaunchIntent(final Context context) {
        final Intent intent = new Intent(context, MagazineActivity.class);
        intent.putExtra(EXTRA_URL, getHunterMagazineUrl(AppPreferences.getLanguageCodeSetting(context)));
        return intent;
    }
}
