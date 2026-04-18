package com.marymar.mobile.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val RECAPTCHA_SITE_KEY = "6LcVgIIsAAAAABxim5wib-BL635DRqpJF7I0slj2"
private const val RECAPTCHA_BASE_URL = "https://appassets.androidplatform.net/"

@Composable
fun RecaptchaWidget(
    modifier: Modifier = Modifier,
    reloadNonce: Int,
    onTokenReceived: (String) -> Unit,
    onExpired: () -> Unit,
    onError: (String) -> Unit,
    onHeightChanged: (Int) -> Unit = {}
) {
    key(reloadNonce) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    setupRecaptchaWebView(
                        onTokenReceived = onTokenReceived,
                        onExpired = onExpired,
                        onError = onError,
                        onHeightChanged = onHeightChanged
                    )
                }
            },
            update = { webView ->
                webView.loadUrl("javascript:window.__syncHeight && window.__syncHeight();")
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.removeAllViews()
                webView.destroy()
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setupRecaptchaWebView(
    onTokenReceived: (String) -> Unit,
    onExpired: () -> Unit,
    onError: (String) -> Unit,
    onHeightChanged: (Int) -> Unit
) {
    setBackgroundColor(AndroidColor.TRANSPARENT)
    overScrollMode = WebView.OVER_SCROLL_NEVER
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false

    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.databaseEnabled = true
    settings.loadsImagesAutomatically = true
    settings.allowFileAccess = false
    settings.allowContentAccess = false
    settings.cacheMode = WebSettings.LOAD_NO_CACHE
    settings.useWideViewPort = true
    settings.loadWithOverviewMode = true
    settings.displayZoomControls = false
    settings.builtInZoomControls = false
    settings.setSupportZoom(false)
    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

    addJavascriptInterface(
        RecaptchaBridge(
            onTokenReceived = onTokenReceived,
            onExpired = onExpired,
            onError = onError,
            onHeightChanged = onHeightChanged
        ),
        "AndroidBridge"
    )

    webChromeClient = WebChromeClient()

    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            postDelayed({ evaluateJavascript("window.__syncHeight && window.__syncHeight();", null) }, 250)
            postDelayed({ evaluateJavascript("window.__syncHeight && window.__syncHeight();", null) }, 900)
        }
    }

    loadDataWithBaseURL(
        RECAPTCHA_BASE_URL,
        buildRecaptchaHtml(RECAPTCHA_SITE_KEY),
        "text/html",
        "utf-8",
        null
    )
}

private fun buildRecaptchaHtml(siteKey: String): String = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <meta
    name="viewport"
    content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
  />
  <script
    src="https://www.google.com/recaptcha/api.js?onload=onloadCallback&render=explicit"
    async
    defer
  ></script>
  <style>
    html, body {
      margin: 0;
      padding: 0;
      width: 100%;
      background: transparent;
      overflow: hidden;
    }

    body {
      display: flex;
      justify-content: center;
      align-items: flex-start;
    }

    #outer {
      width: 100%;
      display: flex;
      justify-content: center;
      align-items: flex-start;
      overflow: hidden;
    }

    #recaptcha-container {
      min-height: 78px;
    }
  </style>
</head>
<body>
  <div id="outer">
    <div id="recaptcha-container"></div>
  </div>

  <script>
    let widgetId = null;

    function reportHeight(px) {
      if (window.AndroidBridge && Number.isFinite(px)) {
        window.AndroidBridge.onHeightChanged(String(Math.ceil(px)));
      }
    }

    function syncHeight() {
      const outer = document.getElementById('outer');
      const rect = outer.getBoundingClientRect();
      let h = rect && rect.height ? rect.height : 82;

      if (!h || h < 78) h = 82;

      reportHeight(h + 4);
    }

    function onloadCallback() {
      if (typeof grecaptcha === 'undefined') {
        setTimeout(onloadCallback, 250);
        return;
      }

      widgetId = grecaptcha.render('recaptcha-container', {
        sitekey: '$siteKey',
        theme: 'light',
        callback: function(token) {
          syncHeight();
          if (window.AndroidBridge) window.AndroidBridge.onToken(token);
        },
        'expired-callback': function() {
          syncHeight();
          if (window.AndroidBridge) window.AndroidBridge.onExpired();
        },
        'error-callback': function() {
          syncHeight();
          if (window.AndroidBridge) {
            window.AndroidBridge.onError('No fue posible cargar el reCAPTCHA');
          }
        }
      });

      setTimeout(syncHeight, 250);
      setTimeout(syncHeight, 900);
      setInterval(syncHeight, 700);
    }

    window.__syncHeight = syncHeight;
  </script>
</body>
</html>
""".trimIndent()

private class RecaptchaBridge(
    private val onTokenReceived: (String) -> Unit,
    private val onExpired: () -> Unit,
    private val onError: (String) -> Unit,
    private val onHeightChanged: (Int) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onToken(token: String) {
        mainHandler.post { onTokenReceived(token) }
    }

    @JavascriptInterface
    fun onExpired() {
        mainHandler.post { onExpired() }
    }

    @JavascriptInterface
    fun onError(message: String) {
        mainHandler.post { onError(message) }
    }

    @JavascriptInterface
    fun onHeightChanged(height: String) {
        val parsed = height.toIntOrNull() ?: return
        mainHandler.post { onHeightChanged(parsed) }
    }
}