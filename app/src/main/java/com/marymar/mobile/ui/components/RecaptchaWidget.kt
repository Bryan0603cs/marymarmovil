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

private const val RECAPTCHA_BASE_URL = "https://appassets.androidplatform.net/"
private const val RECAPTCHA_SITE_KEY = "PON_AQUI_TU_SITE_KEY_WEB_CHECKBOX"

@Composable
fun RecaptchaWidget(
    modifier: Modifier = Modifier,
    reloadNonce: Int = 0,
    onTokenReceived: (String) -> Unit,
    onExpired: () -> Unit = {},
    onError: (String) -> Unit = {},
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
                webView.evaluateJavascript("window.__recalcRecaptchaHeight && window.__recalcRecaptchaHeight();", null)
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
            postDelayed({ evaluateJavascript("window.__recalcRecaptchaHeight && window.__recalcRecaptchaHeight();", null) }, 250)
            postDelayed({ evaluateJavascript("window.__recalcRecaptchaHeight && window.__recalcRecaptchaHeight();", null) }, 900)
            postDelayed({ evaluateJavascript("window.__recalcRecaptchaHeight && window.__recalcRecaptchaHeight();", null) }, 1600)
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
    src="https://www.google.com/recaptcha/enterprise.js?onload=onloadCallback&render=explicit"
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
      font-family: Arial, sans-serif;
    }

    body {
      display: flex;
      justify-content: center;
      align-items: flex-start;
    }

    #viewport {
      width: 100%;
      display: flex;
      justify-content: center;
      align-items: flex-start;
      overflow: hidden;
      padding: 0;
      margin: 0;
    }

    #shell {
      width: 304px;
      transform-origin: top center;
      will-change: transform;
    }

    #recaptcha-container {
      width: 304px;
      min-height: 78px;
      display: block;
    }
  </style>
</head>
<body>
  <div id="viewport">
    <div id="shell">
      <div id="recaptcha-container"></div>
    </div>
  </div>

  <script>
    const BASE_WIDTH = 304;
    const BASE_HEIGHT = 78;
    let widgetId = null;

    function postHeight(px) {
      if (window.AndroidBridge && Number.isFinite(px)) {
        window.AndroidBridge.onHeightChanged(String(Math.ceil(px)));
      }
    }

    function availableWidth() {
      const width = document.documentElement.clientWidth || window.innerWidth || BASE_WIDTH;
      return Math.max(240, width - 12);
    }

    function scaleShell() {
      const shell = document.getElementById('shell');
      const scale = Math.min(1, availableWidth() / BASE_WIDTH);
      shell.style.transform = 'scale(' + scale + ')';
      shell.dataset.scale = String(scale);
      recalcHeight();
    }

    function recalcHeight() {
      const shell = document.getElementById('shell');
      const rect = shell.getBoundingClientRect();
      let height = rect && rect.height ? rect.height : 0;

      if (!height || height < 70) {
        const scale = Number(shell.dataset.scale || '1');
        height = BASE_HEIGHT * scale;
      }

      postHeight(height + 10);
    }

    function installObservers() {
      window.addEventListener('resize', function() {
        scaleShell();
      });

      if (window.ResizeObserver) {
        const observer = new ResizeObserver(function() {
          recalcHeight();
        });
        observer.observe(document.body);
        observer.observe(document.getElementById('viewport'));
        observer.observe(document.getElementById('shell'));
      }

      if (window.MutationObserver) {
        const mutationObserver = new MutationObserver(function() {
          recalcHeight();
        });
        mutationObserver.observe(document.body, {
          childList: true,
          subtree: true,
          attributes: true
        });
      }

      setInterval(recalcHeight, 700);
    }

    function renderWidget() {
      if (typeof grecaptcha === 'undefined' || !grecaptcha.enterprise) {
        setTimeout(renderWidget, 250);
        return;
      }

      widgetId = grecaptcha.enterprise.render('recaptcha-container', {
        sitekey: '$siteKey',
        theme: 'light',
        callback: function(token) {
          recalcHeight();
          if (window.AndroidBridge) {
            window.AndroidBridge.onToken(token);
          }
        },
        'expired-callback': function() {
          recalcHeight();
          if (window.AndroidBridge) {
            window.AndroidBridge.onExpired();
          }
        },
        'error-callback': function() {
          recalcHeight();
          if (window.AndroidBridge) {
            window.AndroidBridge.onError('No fue posible cargar el reCAPTCHA');
          }
        }
      });

      setTimeout(recalcHeight, 250);
      setTimeout(recalcHeight, 900);
      setTimeout(recalcHeight, 1600);
    }

    function onloadCallback() {
      scaleShell();
      renderWidget();
      installObservers();
    }

    window.__recalcRecaptchaHeight = recalcHeight;
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