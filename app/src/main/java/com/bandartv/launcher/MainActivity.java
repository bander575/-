package com.bandartv.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bandar TV: a selectable Android HOME launcher, not a static picture.
 * The bundled local page is the only origin given access to the Android bridge.
 * The user must explicitly choose Bandar TV as Home when their firmware permits it.
 */
public class MainActivity extends Activity {
    private WebView tvView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
                // A remote web page must never run with the privileged native bridge.
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

    private String makeIcon(Drawable icon) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            icon.setBounds(0, 0, 80, 80);
            icon.draw(canvas);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes);
            return "data:image/png;base64," + Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP);
        } catch (Exception err) {
            return "";
        }
    }

    private JSONArray queryInstalledApps() {
        JSONArray result = new JSONArray();
        PackageManager pm = getPackageManager();
        List<ResolveInfo> candidates = new ArrayList<>();
        Intent tvIntent = new Intent(Intent.ACTION_MAIN);
        tvIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        Intent standardIntent = new Intent(Intent.ACTION_MAIN);
        standardIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        candidates.addAll(pm.queryIntentActivities(tvIntent, 0));
        candidates.addAll(pm.queryIntentActivities(standardIntent, 0));
        Collections.sort(candidates, Comparator.comparing(i -> i.loadLabel(pm).toString(), String.CASE_INSENSITIVE_ORDER));
        Set<String> added = new HashSet<>();
        for (ResolveInfo info : candidates) {
            if (info.activityInfo == null) continue;
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !added.add(pkg)) continue;
            try {
                JSONObject app = new JSONObject();
                app.put("id", pkg);
                app.put("pkg", pkg);
                app.put("name", info.loadLabel(pm).toString());
                app.put("iconUrl", makeIcon(info.loadIcon(pm)));
                app.put("css", "native");
                app.put("cat", "tools");
                result.put(app);
            } catch (Exception ignored) { }
        }
        return result;
    }

    private final class TvBridge {
        @JavascriptInterface
        public String getInstalledApps() {
            return queryInstalledApps().toString();
        }

        @JavascriptInterface
        public boolean launchPackage(String packageName) {
            if (packageName == null || !packageName.matches("[A-Za-z][A-Za-z0-9_.]{1,120}")) return false;
            try {
                Intent launch = getPackageManager().getLeanbackLaunchIntentForPackage(packageName);
                if (launch == null) launch = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launch == null) return false;
                final Intent target = launch;
                runOnUiThread(() -> {
                    try { startActivity(target); } catch (Exception ignored) { }
                });
                return true;
            } catch (Exception ignored) { return false; }
        }

        @JavascriptInterface
        public void openWebsite(String url) {
            if (url == null) return;
            final Uri uri;
            try { uri = Uri.parse(url); } catch (Exception err) { return; }
            if (!"https".equalsIgnoreCase(uri.getScheme())) return;
            runOnUiThread(() -> {
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) { }
            });
        }

        @JavascriptInterface
        public void openWifiSettings() {
            runOnUiThread(() -> {
                try { startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); } catch (Exception ignored) { }
            });
        }

        /**
         * Opens Android's own HOME selection UI. Never silently replaces the
         * current launcher or disables the vendor launcher.
         */
        @JavascriptInterface
        public void chooseAsHome() {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        RoleManager manager = getSystemService(RoleManager.class);
                        if (manager != null && manager.isRoleAvailable(RoleManager.ROLE_HOME)
                            && !manager.isRoleHeld(RoleManager.ROLE_HOME)) {
                            startActivityForResult(manager.createRequestRoleIntent(RoleManager.ROLE_HOME), 5);
                            return;
                        }
                    } catch (Exception ignored) { }
                }
                try {
                    startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
                } catch (Exception ignored) {
                    android.widget.Toast.makeText(MainActivity.this,
                        "اختيار الشاشة الرئيسية غير متاح عبر إعدادات هذا التلفزيون.",
                        android.widget.Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tvView != null) {
            tvView.evaluateJavascript("if(window.refreshInstalledApps){window.refreshInstalledApps()}", null);
        }
    }

    @Override
    public void onBackPressed() {
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
