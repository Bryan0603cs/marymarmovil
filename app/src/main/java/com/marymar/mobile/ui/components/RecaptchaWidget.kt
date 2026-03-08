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
            <meta
                name="viewport"
                content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
            />

            <script>
                let widgetRendered = false;
                let lastReportedHeight = 0;

                function visibleFrames() {
                    return Array.from(document.querySelectorAll('iframe')).filter(frame => {
                        const rect = frame.getBoundingClientRect();
                        const style = window.getComputedStyle(frame);
                        return rect.width > 0 &&
                               rect.height > 0 &&
                               style.display !== 'none' &&
                               style.visibility !== 'hidden' &&
                               style.opacity !== '0';
                    });
                }

                function challengeFrame() {
                    return visibleFrames().find(frame => {
                        const rect = frame.getBoundingClientRect();
                        const title = (frame.getAttribute('title') || '').toLowerCase();
                        return title.includes('challenge') ||
                               title.includes('desaf') ||
                               rect.height > 260;
                    });
                }

                function reportHeight(value) {
                    const safeHeight = Math.ceil(value);
                    if (Math.abs(safeHeight - lastReportedHeight) > 3) {
                        lastReportedHeight = safeHeight;
                        AndroidBridge.onHeightChanged(safeHeight);
                    }
                }

                function fitCollapsed() {
                    const wrapper = document.getElementById('captcha-wrapper');
                    const anchorShell = document.getElementById('captcha-anchor-shell');
                    if (!wrapper || !anchorShell) return;

                    const availableWidth = Math.max(wrapper.clientWidth - 4, 1);
                    const scale = Math.min(1.0, availableWidth / 304);

                    anchorShell.style.transform = 'scale(' + scale + ')';
                    anchorShell.style.transformOrigin = 'top center';
                    wrapper.style.minHeight = Math.ceil((78 * scale) + 10) + 'px';

                    visibleFrames().forEach(frame => {
                        frame.style.transform = '';
                        frame.style.transformOrigin = '';
                        frame.style.margin = '';
                        frame.style.display = '';
                    });

                    reportHeight(96);
                }

                function fitExpanded(frame) {
                    const wrapper = document.getElementById('captcha-wrapper');
                    if (!wrapper || !frame) return;

                    const rect = frame.getBoundingClientRect();
                    const rawWidth = Math.max(frame.offsetWidth || 0, rect.width || 0, 302);
                    const rawHeight = Math.max(frame.offsetHeight || 0, rect.height || 0, 420);
                    const availableWidth = Math.max(wrapper.clientWidth - 12, 1);

                    const scale = Math.min(0.94, availableWidth / rawWidth);

                    frame.style.transform = 'scale(' + scale + ')';
                    frame.style.transformOrigin = 'top center';
                    frame.style.display = 'block';
                    frame.style.margin = '0 auto';

                    const finalHeight = Math.ceil((rawHeight * scale) + 26);
                    wrapper.style.minHeight = finalHeight + 'px';
                    reportHeight(finalHeight);
                }

                function adjustLayout() {
                    const openChallenge = challengeFrame();
                    if (openChallenge) {
                        fitExpanded(openChallenge);
                    } else {
                        fitCollapsed();
                    }
                }

                function onCaptchaSuccess(token) {
                    AndroidBridge.onCaptchaSuccess(token);
                    setTimeout(adjustLayout, 80);
                    setTimeout(adjustLayout, 240);
                }

                function onCaptchaExpired() {
                    AndroidBridge.onCaptchaExpired();
                    setTimeout(adjustLayout, 80);
                    setTimeout(adjustLayout, 240);
                }

                function onCaptchaError() {
                    AndroidBridge.onCaptchaError("Error al cargar reCAPTCHA");
                    setTimeout(adjustLayout, 80);
                    setTimeout(adjustLayout, 240);
                }

                function renderRecaptcha() {
                    if (widgetRendered || typeof grecaptcha === 'undefined') {
                        adjustLayout();
                        return;
                    }

                    try {
                        grecaptcha.render('captcha-anchor', {
                            'sitekey': '$siteKey',
                            'callback': onCaptchaSuccess,
                            'expired-callback': onCaptchaExpired,
                            'error-callback': onCaptchaError
                        });

                        widgetRendered = true;
                        setTimeout(adjustLayout, 100);
                        setTimeout(adjustLayout, 250);
                    } catch (error) {
                        AndroidBridge.onCaptchaError(
                            error && error.message
                                ? error.message
                                : 'No fue posible inicializar el captcha'
                        );
                    }
                }

                function onRecaptchaLoaded() {
                    renderRecaptcha();
                }

                window.addEventListener('resize', function() {
                    setTimeout(adjustLayout, 80);
                    setTimeout(adjustLayout, 220);
                });

                document.addEventListener('DOMContentLoaded', function() {
                    setTimeout(adjustLayout, 80);

                    const observer = new MutationObserver(function() {
                        setTimeout(adjustLayout, 70);
                        setTimeout(adjustLayout, 180);
                        setTimeout(adjustLayout, 320);
                    });

                    observer.observe(document.documentElement, {
                        childList: true,
                        subtree: true,
                        attributes: true
                    });

                    setInterval(adjustLayout, 450);
                });
            </script>

            <script
                src="https://www.google.com/recaptcha/api.js?hl=es&onload=onRecaptchaLoaded&render=explicit"
                async
                defer>
            </script>

            <style>
                * {
                    box-sizing: border-box;
                }

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
                    min-height: 96px;
                }

                #captcha-wrapper {
                    width: 100%;
                    min-height: 96px;
                    display: flex;
                    justify-content: center;
                    align-items: flex-start;
                    overflow: visible;
                    padding: 2px 0;
                }

                #captcha-anchor-shell {
                    width: 304px;
                    transform-origin: top center;
                }

                #captcha-anchor {
                    width: 304px;
                }
            </style>
        </head>

        <body>
            <div id="captcha-wrapper">
                <div id="captcha-anchor-shell">
                    <div id="captcha-anchor"></div>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()
}