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
        InternalRecaptchaWebView(
            modifier = modifier,
            siteKey = RECAPTCHA_SITE_KEY,
            onTokenReceived = onTokenReceived,
            onExpired = onExpired,
            onError = onError,
            onHeightChanged = onHeightChanged
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun InternalRecaptchaWebView(
    modifier: Modifier,
    siteKey: String,
    onTokenReceived: (String) -> Unit,
    onExpired: () -> Unit,
    onError: (String) -> Unit,
    onHeightChanged: (Int) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val handler = Handler(Looper.getMainLooper())

            WebView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.allowFileAccess = false
                settings.allowContentAccess = false

                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = WebView.OVER_SCROLL_NEVER

                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onCaptchaSuccess(token: String) {
                            handler.post { onTokenReceived(token) }
                        }

                        @JavascriptInterface
                        fun onCaptchaExpired() {
                            handler.post { onExpired() }
                        }

                        @JavascriptInterface
                        fun onCaptchaError(message: String?) {
                            handler.post {
                                onError(message ?: "No fue posible cargar el captcha")
                            }
                        }

                        @JavascriptInterface
                        fun onHeightChanged(height: Int) {
                            handler.post {
                                onHeightChanged(height)
                            }
                        }
                    },
                    "AndroidBridge"
                )

                loadDataWithBaseURL(
                    RECAPTCHA_BASE_URL,
                    buildRecaptchaHtml(siteKey),
                    "text/html",
                    "utf-8",
                    null
                )
            }
        }
    )
}

private fun buildRecaptchaHtml(siteKey: String): String {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="utf-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>

            <script>
                let widgetRendered = false;
                let lastReportedHeight = 0;

                function onCaptchaSuccess(token) {
                    AndroidBridge.onCaptchaSuccess(token);
                    setTimeout(reportHeight, 120);
                }

                function onCaptchaExpired() {
                    AndroidBridge.onCaptchaExpired();
                    setTimeout(reportHeight, 120);
                }

                function onCaptchaError() {
                    AndroidBridge.onCaptchaError("Error al cargar reCAPTCHA");
                    setTimeout(reportHeight, 120);
                }

                function fitCaptcha() {
                    const wrapper = document.getElementById('captcha-wrapper');
                    const checkboxHost = document.getElementById('captcha-checkbox-host');

                    if (!wrapper || !checkboxHost) return;

                    const availableWidth = Math.max(wrapper.clientWidth - 8, 1);
                    const scale = Math.min(1, availableWidth / 304);
                    const scaledHeight = Math.max(78 * scale, 64);

                    checkboxHost.style.transform = 'scale(' + scale + ')';
                    checkboxHost.style.transformOrigin = 'top center';
                    wrapper.style.minHeight = scaledHeight + 'px';

                    setTimeout(reportHeight, 80);
                }

                function renderRecaptcha() {
                    if (widgetRendered || typeof grecaptcha === 'undefined') {
                        fitCaptcha();
                        return;
                    }

                    try {
                        grecaptcha.render('captcha-checkbox-host', {
                            'sitekey': '$siteKey',
                            'callback': onCaptchaSuccess,
                            'expired-callback': onCaptchaExpired,
                            'error-callback': onCaptchaError
                        });

                        widgetRendered = true;
                        setTimeout(fitCaptcha, 80);
                        setTimeout(reportHeight, 150);
                    } catch (error) {
                        AndroidBridge.onCaptchaError(
                            error && error.message
                                ? error.message
                                : 'No fue posible inicializar el captcha'
                        );
                    }
                }

                function getDynamicHeight() {
                    const body = document.body;
                    const html = document.documentElement;

                    let maxHeight = Math.max(
                        body ? body.scrollHeight : 0,
                        body ? body.offsetHeight : 0,
                        html ? html.scrollHeight : 0,
                        html ? html.offsetHeight : 0,
                        86
                    );

                    const iframes = document.querySelectorAll('iframe');
                    iframes.forEach(frame => {
                        const rect = frame.getBoundingClientRect();
                        const bottom = rect.bottom + 12;
                        if (bottom > maxHeight) {
                            maxHeight = bottom;
                        }
                    });

                    return Math.ceil(maxHeight);
                }

                function reportHeight() {
                    const newHeight = getDynamicHeight();

                    if (Math.abs(newHeight - lastReportedHeight) > 6) {
                        lastReportedHeight = newHeight;
                        AndroidBridge.onHeightChanged(newHeight);
                    }
                }

                function onRecaptchaLoaded() {
                    renderRecaptcha();
                }

                window.addEventListener('resize', function() {
                    fitCaptcha();
                    setTimeout(reportHeight, 100);
                });

                document.addEventListener('DOMContentLoaded', function() {
                    setTimeout(fitCaptcha, 40);
                    setTimeout(reportHeight, 120);

                    const observer = new MutationObserver(function() {
                        setTimeout(reportHeight, 60);
                        setTimeout(reportHeight, 180);
                    });

                    observer.observe(document.documentElement, {
                        childList: true,
                        subtree: true,
                        attributes: true
                    });

                    setInterval(reportHeight, 500);
                });
            </script>

            <script
                src="https://www.google.com/recaptcha/api.js?hl=es&onload=onRecaptchaLoaded&render=explicit"
                async
                defer>
            </script>

            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    background: transparent;
                    overflow-x: hidden;
                    overflow-y: visible;
                    font-family: Arial, sans-serif;
                }

                body {
                    display: flex;
                    justify-content: center;
                    align-items: flex-start;
                    min-height: 86px;
                }

                #captcha-wrapper {
                    width: 100%;
                    display: flex;
                    justify-content: center;
                    align-items: flex-start;
                    padding-top: 2px;
                    padding-bottom: 2px;
                    overflow: visible;
                    min-height: 86px;
                    position: relative;
                }

                #captcha-checkbox-host {
                    width: 304px;
                    transform-origin: top center;
                }
            </style>
        </head>

        <body>
            <div id="captcha-wrapper">
                <div id="captcha-checkbox-host"></div>
            </div>
        </body>
        </html>
    """.trimIndent()
}