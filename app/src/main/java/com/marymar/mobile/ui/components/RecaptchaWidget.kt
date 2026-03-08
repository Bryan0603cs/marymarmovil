package com.marymar.mobile.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
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
    onError: (String) -> Unit
) {
    key(reloadNonce) {
        InternalRecaptchaWebView(
            modifier = modifier,
            siteKey = RECAPTCHA_SITE_KEY,
            onTokenReceived = onTokenReceived,
            onExpired = onExpired,
            onError = onError
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
    onError: (String) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val handler = Handler(Looper.getMainLooper())

            WebView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                webChromeClient = WebChromeClient()

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.allowFileAccess = false
                settings.allowContentAccess = false

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
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            <script src="https://www.google.com/recaptcha/api.js?hl=es" async defer></script>
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    background: transparent;
                    overflow: hidden;
                    font-family: Arial, sans-serif;
                }

                body {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    min-height: 86px;
                }

                .wrapper {
                    width: 100%;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
            </style>

            <script>
                function onCaptchaSuccess(token) {
                    AndroidBridge.onCaptchaSuccess(token);
                }

                function onCaptchaExpired() {
                    AndroidBridge.onCaptchaExpired();
                }

                function onCaptchaError() {
                    AndroidBridge.onCaptchaError("Error al cargar reCAPTCHA");
                }
            </script>
        </head>
        <body>
            <div class="wrapper">
                <div
                    class="g-recaptcha"
                    data-sitekey="$siteKey"
                    data-callback="onCaptchaSuccess"
                    data-expired-callback="onCaptchaExpired"
                    data-error-callback="onCaptchaError">
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()
}