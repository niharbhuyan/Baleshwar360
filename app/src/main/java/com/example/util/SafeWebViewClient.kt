package com.example.util

import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi

/**
 * A safe WebViewClient that intercepts Chromium renderer process crashes (code -1)
 * and prevents aw_browser_terminator.cc from terminating the host Android application.
 */
open class SafeWebViewClient(
    private val onCrashFallback: (() -> Unit)? = null
) : WebViewClient() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        val didCrash = detail?.didCrash() ?: true
        Log.e("SafeWebViewClient", "Interception of chromium renderer crash (didCrash=$didCrash). Preventing host app termination.")

        try {
            view?.let { webView ->
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.destroy()
            }
        } catch (e: Throwable) {
            Log.w("SafeWebViewClient", "Error cleaning up crashed WebView: ${e.message}")
        }

        try {
            onCrashFallback?.invoke()
        } catch (e: Throwable) {
            Log.w("SafeWebViewClient", "Error during onCrashFallback: ${e.message}")
        }

        // Returning true informs Android system that the host app handled the renderer termination
        // and MUST NOT be killed with aw_browser_terminator exit code.
        return true
    }
}
