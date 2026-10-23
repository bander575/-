package com.bandartv.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Android TV shell for the bundled Arabic Bandar TV UI.
 * The Javascript bridge is enabled only on the bundled local page.
 */
public class MainActivity extends Activity {
    private WebView tvView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        tvView = new WebView(this);
        tvView.setBackgroundColor(Color.rgb(6, 19, 34));
        tvView.getSettings().setJavaScriptEnabled(true);
        tvView.getSettings().setDomStorageEnabled(true);
        tvView.getSettings().setAllowFileAccess(true);
        tvView.getSettings().setAllowContentAccess(false);
        tvView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        tvView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Never navigate this trusted WebView to a remotely hosted page.
                return !request.getUrl().toString().startsWith("file:///android_asset/");
            }
        });
        tvView.setWebChromeClient(new WebChromeClient());
        tvView.addJavascriptInterface(new TvBridge(), "BandarAndroid");
        tvView.setFocusable(true);
        tvView.setFocusableInTouchMode(true);
        setContentView(tvView);
        tvView.loadUrl("file:///android_asset/index.html");
        tvView.requestFocus();
    }

    private final class TvBridge {
        @JavascriptInterface
        public boolean launchPackage(String packageName) {
            if (packageName == null || !packageName.matches("[A-Za-z][A-Za-z0-9_.]{1,120}")) return false;
            try {
                Intent intent = getPackageManager().getLeanbackLaunchIntentForPackage(packageName);
                if (intent == null) intent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent == null) return false;
                final Intent launch = intent;
                runOnUiThread(() -> {
                    try { startActivity(launch); }
                    catch (Exception ignored) { }
                });
                return true;
            } catch (Exception ignored) { return false; }
        }

        @JavascriptInterface
        public void openWebsite(String url) {
            if (url == null) return;
            final Uri parsed;
            try { parsed = Uri.parse(url); }
            catch (Exception error) { return; }
            if (!"https".equalsIgnoreCase(parsed.getScheme())) return;
            runOnUiThread(() -> {
                try { startActivity(new Intent(Intent.ACTION_VIEW, parsed)); }
                catch (Exception ignored) { }
            });
        }

        @JavascriptInterface
        public void openWifiSettings() {
            runOnUiThread(() -> {
                try { startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); }
                catch (Exception ignored) { }
            });
        }
    }

    @Override
    public void onBackPressed() {
        // Let the UI handle back first rather than exiting on remote Back.
        if (tvView != null) {
            tvView.evaluateJavascript("document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',bubbles:true}))", null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (tvView != null) {
            tvView.removeJavascriptInterface("BandarAndroid");
            tvView.destroy();
            tvView = null;
        }
        super.onDestroy();
    }
}
