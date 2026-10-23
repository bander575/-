package com.bandartv.launcher

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
 private lateinit var web: WebView
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  web = WebView(this)
  web.settings.javaScriptEnabled = true
  web.settings.domStorageEnabled = true
  web.webViewClient = WebViewClient()
  setContentView(web)
  web.loadUrl("file:///android_asset/index.html")
 }
 @Deprecated("Deprecated in Java") override fun onBackPressed() {
  web.evaluateJavascript("document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape'}))", null)
 }
 override fun onDestroy(){web.destroy();super.onDestroy()}
}
