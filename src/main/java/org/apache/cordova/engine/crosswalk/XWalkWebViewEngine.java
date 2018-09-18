/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package org.apache.cordova.engine.crosswalk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;

import org.apache.cordova.CordovaBridge;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.ICordovaCookieManager;
import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginManager;
import org.xwalk.core.XWalkActivityDelegate;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;

import java.util.Map;

/**
 * Glue class between CordovaWebView (main Cordova logic) and XWalkCordovaView (the actual View).
 */
public class XWalkWebViewEngine implements CordovaWebViewEngine {

    public static final String TAG = "XWalkWebViewEngine";
    public static final String XWALK_USER_AGENT = "xwalkUserAgent";
    public static final String XWALK_Z_ORDER_ON_TOP = "xwalkZOrderOnTop";

    protected final XWalkCordovaView webView;
    protected XWalkCordovaCookieManager cookieManager;
    protected CordovaBridge bridge;
    protected CordovaWebViewEngine.Client client;
    protected CordovaWebView parentWebView;
    protected CordovaInterface cordova;
    protected PluginManager pluginManager;
    protected CordovaResourceApi resourceApi;
    protected NativeToJsMessageQueue nativeToJsMessageQueue;
    protected XWalkActivityDelegate activityDelegate;
    protected String startUrl;
    protected CordovaPreferences preferences;
    protected int loadUrlCacheMode = 0;

    /** Used when created via reflection. */
    public XWalkWebViewEngine(Context context, CordovaPreferences preferences) {
        Log.i("WebViewSDK","XWalkWebViewEngine 111");
        this.preferences = preferences;
        Runnable cancelCommand = new Runnable() {
            @Override
            public void run() {
                //modified by fyb
                Context context = cordova.getActivity();
                if (context instanceof Activity){
                    ((Activity) context).finish();
                } else{
                    Log.i("WebViewSDK", "context is not Activity");
                }
            }
        };
        Runnable completeCommand = new Runnable() {
            @Override
            public void run() {
                cookieManager = new XWalkCordovaCookieManager();
                Log.i("WebViewSDK","completeCommand");
                initWebViewSettings();
                exposeJsInterface(webView, bridge);

                // Send the massage of xwalk's ready to plugin.
                if (pluginManager != null) {
                    pluginManager.postMessage("onXWalkReady", this);
                }

                if (startUrl != null) {
                    webView.load(startUrl, null);
                }
            }
        };
        activityDelegate = new XWalkActivityDelegate((Activity) context, cancelCommand, completeCommand);

        Log.i("WebViewSDK","XWalkWebViewEngine 2222");
        webView = new XWalkCordovaView(context, preferences);
    }

    // Use two-phase init so that the control will work with XML layouts.

    @Override
    public void init(CordovaWebView parentWebView, CordovaInterface cordova, CordovaWebViewEngine.Client client,
                     CordovaResourceApi resourceApi, PluginManager pluginManager,
                     NativeToJsMessageQueue nativeToJsMessageQueue) {
        if (this.cordova != null) {
            throw new IllegalStateException();
        }
        this.parentWebView = parentWebView;
        this.cordova = cordova;
        this.client = client;
        this.resourceApi = resourceApi;
        this.pluginManager = pluginManager;
        this.nativeToJsMessageQueue = nativeToJsMessageQueue;

        CordovaPlugin activityDelegatePlugin = new CordovaPlugin() {
            @Override
            public void onResume(boolean multitasking) {
                activityDelegate.onResume();
            }
        };
        pluginManager.addService(new PluginEntry("XWalkActivityDelegate", activityDelegatePlugin));

        webView.init(this);

        nativeToJsMessageQueue.addBridgeMode(new NativeToJsMessageQueue.OnlineEventsBridgeMode(
                new NativeToJsMessageQueue.OnlineEventsBridgeMode.OnlineEventsBridgeModeDelegate() {
            @Override
            public void setNetworkAvailable(boolean value) {
                webView.setNetworkAvailable(value);
            }
            @Override
            public void runOnUiThread(Runnable r) {
                XWalkWebViewEngine.this.cordova.getActivity().runOnUiThread(r);
            }
        }));
        bridge = new CordovaBridge(pluginManager, nativeToJsMessageQueue);
    }

    @Override
    public CordovaWebView getCordovaWebView() {
        return parentWebView;
    }

    @Override
    public View getView() {
        return webView;
    }

    private void initWebViewSettings() {
        //add by fyb
        webView.setInitialScale(0);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setBackgroundColor(0);
        XWalkSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);

//        settings.setCacheMode(XWalkSettings.LOAD_DEFAULT);
        boolean zOrderOnTop = preferences == null ? false : preferences.getBoolean(XWALK_Z_ORDER_ON_TOP, false);
        webView.setZOrderOnTop(zOrderOnTop);

        // Set xwalk webview settings by Cordova preferences.
        String xwalkUserAgent = preferences == null ? "" : preferences.getString(XWALK_USER_AGENT, "");
        if (!xwalkUserAgent.isEmpty()) {
            webView.setUserAgentString(xwalkUserAgent);
        }
        if (preferences.contains("BackgroundColor")) {
            int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
            webView.setBackgroundColor(backgroundColor);
        }
    }

    private static void exposeJsInterface(XWalkView webView, CordovaBridge bridge) {
        XWalkExposedJsApi exposedJsApi = new XWalkExposedJsApi(bridge);
        webView.addJavascriptInterface(exposedJsApi, "_cordovaNative");
    }

    @Override
    public boolean canGoBack() {
        if (!activityDelegate.isXWalkReady()) return false;
        return this.webView.getNavigationHistory().canGoBack();
    }

    @Override
    public boolean goBack() {
        if (this.webView.getNavigationHistory().canGoBack()) {
            this.webView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
            return true;
        }
        return false;
    }

    @Override
    public void setPaused(boolean value) {
        if (!activityDelegate.isXWalkReady()) return;
        if (value) {
            // TODO: I think this has been fixed upstream and we don't need to override pauseTimers() anymore.
            webView.pauseTimersForReal();
        } else {
            webView.resumeTimers();
        }
    }

    @Override
    public void destroy() {
        Log.i("WebViewSDK","XWalkWebViewEngine destroy 1111");
        if (!activityDelegate.isXWalkReady()) {
            Log.i("WebViewSDK","XWalkWebViewEngine destroy 2222");
            return;
        }
        webView.onDestroy();
    }

    @Override
    public void evaluateJavascript(String js, ValueCallback<String> callback) {

    }

    @Override
    public void clearHistory() {
        if (!activityDelegate.isXWalkReady()) return;
        this.webView.getNavigationHistory().clear();
    }

    @Override
    public void stopLoading() {
        if (!activityDelegate.isXWalkReady()) return;
        this.webView.stopLoading();
    }

    @Override
    public void clearCache() {
        if (!activityDelegate.isXWalkReady()) return;
        webView.clearCache(true);
    }

    @Override
    public String getUrl() {
        if (!activityDelegate.isXWalkReady()) return null;
        return this.webView.getUrl();
    }

    @Override
    public ICordovaCookieManager getCookieManager() {
        return cookieManager;
    }

    @Override
    public void loadUrl(String url, boolean clearNavigationStack) {
        Log.i("WebViewSDK","XWalkWebViewEngine loadUrl url 111= " + url);
        if (!activityDelegate.isXWalkReady()) {
//            Assert.assertNull(startUrl);
            startUrl = url;
            return;
        }
        Log.i("WebViewSDK","XWalkWebViewEngine loadUrl url = " + url);
        webView.load(url, null);
    }

    public boolean isXWalkReady() {
        return activityDelegate.isXWalkReady();
    }

    @Override
    public void reload() {
        if (!activityDelegate.isXWalkReady()) return;
        webView.reload(0);
    }

    @Override
    public boolean goForward() {
        if (!activityDelegate.isXWalkReady()) return false;
        if (this.webView.getNavigationHistory().canGoForward()) {
            this.webView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.FORWARD, 1);
            return true;
        }
        return false;
    }

    @Override
    public void setUserAgent(String ua) {
        if (!activityDelegate.isXWalkReady()) return;
        this.webView.setUserAgentString(ua);
    }

    @Override
    public String getTitle() {
        if (!activityDelegate.isXWalkReady()) return null;
        return webView.getTitle();
    }

    @Override
    public Bitmap getFavicon() {
        if (!activityDelegate.isXWalkReady()) return null;
        return null;
    }

    @Override
    public void loadUrl(String url, Map<String, String> header, boolean clearNavigationStack) {
        //webView.loadUrl(url, header);
        Log.i("WebViewSDK","XWalkWebViewEngine LoadUrl no implement");
    }
}
