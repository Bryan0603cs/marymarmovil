package com.marymar.mobile.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
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
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.setSupportZoom(false)
                settings.useWideViewPort = false
                settings.loadWithOverviewMode = false

                isFocusable = true
                isFocusableInTouchMode = true
                isClickable = true
                isLongClickable = false
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = WebView.OVER_SCROLL_NEVER

                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                        parent?.requestDisallowInterceptTouchEvent(true)
                        requestFocus()
                        post {
                            loadUrl("javascript:window.onHostTouch && window.onHostTouch();")
                        }
                    }
                    false
                }

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
                            handler.post { onHeightChanged(height) }
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
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.removeAllViews()
            webView.destroy()
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
                let interactionLockUntil = 0;

                function reportHeight(value) {
                    const safeHeight = Math.ceil(value);
                    if (Math.abs(safeHeight - lastReportedHeight) > 2) {
                        lastReportedHeight = safeHeight;
                        AndroidBridge.onHeightChanged(safeHeight);
                    }
                }

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
                    const frames = visibleFrames();

                    return frames
                        .slice()
                        .sort((a, b) => {
                            const ra = a.getBoundingClientRect();
                            const rb = b.getBoundingClientRect();
                            return (rb.width * rb.height) - (ra.width * ra.height);
                        })
                        .find(frame => {
                            const rect = frame.getBoundingClientRect();
                            const title = (frame.getAttribute('title') || '').toLowerCase();
                            return title.includes('challenge') ||
                                   title.includes('desaf') ||
                                   rect.height >= 280 ||
                                   rect.width >= 320;
                        }) || null;
                }

                function fitCollapsed() {
                    const wrapper = document.getElementById('captcha-wrapper');
                    const anchorShell = document.getElementById('captcha-anchor-shell');
                    if (!wrapper || !anchorShell) return;

                    const availableWidth = Math.max(wrapper.clientWidth - 6, 1);
                    const scale = Math.min(1, availableWidth / 304);
                    const collapsedHeight = Math.max(Math.ceil((78 * scale) + 10), 88);

                    anchorShell.style.transform = 'scale(' + scale + ')';
                    anchorShell.style.transformOrigin = 'top center';
                    anchorShell.style.width = '304px';

                    visibleFrames().forEach(frame => {
                        frame.style.transform = '';
                        frame.style.transformOrigin = '';
                        frame.style.margin = '';
                        frame.style.display = '';
                    });

                    wrapper.style.minHeight = collapsedHeight + 'px';
                    reportHeight(collapsedHeight);
                }

                function fitWaitingChallenge() {
                    const wrapper = document.getElementById('captcha-wrapper');
                    if (!wrapper) return;

                    wrapper.style.minHeight = '430px';
                    reportHeight(430);
                }

                function fitExpanded(frame) {
                    const wrapper = document.getElementById('captcha-wrapper');
                    if (!wrapper || !frame) return;

                    const rect = frame.getBoundingClientRect();
                    const rawWidth = Math.max(frame.offsetWidth || 0, rect.width || 0, 320);
                    const rawHeight = Math.max(frame.offsetHeight || 0, rect.height || 0, 420);
                    const availableWidth = Math.max(wrapper.clientWidth - 10, 1);
                    const scale = Math.min(1, availableWidth / rawWidth);
                    const expandedHeight = Math.ceil((rawHeight * scale) + 20);

                    frame.style.transform = 'scale(' + scale + ')';
                    frame.style.transformOrigin = 'top center';
                    frame.style.margin = '0 auto';
                    frame.style.display = 'block';

                    wrapper.style.minHeight = expandedHeight + 'px';
                    reportHeight(expandedHeight);
                }

                function adjustLayout() {
                    const openChallenge = challengeFrame();

                    if (openChallenge) {
                        fitExpanded(openChallenge);
                        return;
                    }

                    const now = Date.now();
                    if (now < interactionLockUntil) {
                        fitWaitingChallenge();
                        return;
                    }

                    fitCollapsed();
                }

                function onCaptchaSuccess(token) {
                    interactionLockUntil = 0;
                    AndroidBridge.onCaptchaSuccess(token);
                    setTimeout(adjustLayout, 80);
                    setTimeout(adjustLayout, 220);
                }

                function onCaptchaExpired() {
                    interactionLockUntil = 0;
                    AndroidBridge.onCaptchaExpired();
                    setTimeout(adjustLayout, 80);
                    setTimeout(adjustLayout, 220);
                }

                function onCaptchaError(message) {
                    interactionLockUntil = 0;
                    AndroidBridge.onCaptchaError(message || 'Error al cargar reCAPTCHA');
                    setTimeout(adjustLayout, 80);
                    setTimeout(adjustLayout, 220);
                }

                function renderRecaptcha() {
                    if (widgetRendered || typeof grecaptcha === 'undefined') {
                        adjustLayout();
                        return;
                    }

                    try {
                        grecaptcha.render('captcha-anchor', {
                            sitekey: '$siteKey',
                            theme: 'light',
                            size: 'normal',
                            callback: onCaptchaSuccess,
                            'expired-callback': onCaptchaExpired,
                            'error-callback': function() {
                                onCaptchaError('No fue posible cargar reCAPTCHA');
                            }
                        });

                        widgetRendered = true;
                        setTimeout(adjustLayout, 120);
                        setTimeout(adjustLayout, 260);
                    } catch (error) {
                        onCaptchaError(
                            error && error.message
                                ? error.message
                                : 'No fue posible inicializar el captcha'
                        );
                    }
                }

                function onRecaptchaLoaded() {
                    renderRecaptcha();
                }

                window.onHostTouch = function() {
                    interactionLockUntil = Date.now() + 1400;
                    setTimeout(adjustLayout, 20);
                    setTimeout(adjustLayout, 160);
                    setTimeout(adjustLayout, 420);
                };

                window.addEventListener('resize', function() {
                    setTimeout(adjustLayout, 80);
                    setTimeout(adjustLayout, 220);
                });

                document.addEventListener('DOMContentLoaded', function() {
                    const observer = new MutationObserver(function() {
                        setTimeout(adjustLayout, 50);
                        setTimeout(adjustLayout, 180);
                        setTimeout(adjustLayout, 320);
                    });

                    observer.observe(document.documentElement, {
                        childList: true,
                        subtree: true,
                        attributes: true
                    });

                    setTimeout(adjustLayout, 60);
                    setInterval(adjustLayout, 500);
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
                    min-height: 88px;
                }

                #captcha-wrapper {
                    width: 100%;
                    min-height: 88px;
                    display: flex;
                    justify-content: center;
                    align-items: flex-start;
                    overflow: visible;
                    padding: 2px 0;
                }

                #captcha-anchor-shell {
                    width: 304px;
                    max-width: 304px;
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