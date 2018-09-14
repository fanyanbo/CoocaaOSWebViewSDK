/**
 * Copyright (C) 2012 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2016年6月29日         wen
 *
 */

package coocaa.plugin.api.pay.sky.api;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout.LayoutParams;

import com.skyworth.ui.blurbg.BlurBgLayout;
import com.skyworth.ui.blurbg.BlurBgLayout.PAGETYPE;

import coocaa.plugin.api.pay.sky.api.BaseActivity;
import coocaa.plugin.api.pay.sky.api.LoadingView;
import coocaa.plugin.api.pay.sky.api.utils.CcLog;

public class CcActivity extends BaseActivity
{
    private BlurBgLayout blurBgLayout;
    // private FrameLayout blurBgLayout;
    private WebView webView;
    private LoadingView loadView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        blurBgLayout = new BlurBgLayout(this);
        blurBgLayout.setPageType(PAGETYPE.FIRST_PAGE);
        // blurBgLayout = new BlurBgLayout(this);
        // blurBgLayout.setBackgroundResource(R.drawable.ui_sdk_main_page_bg);
        // blurBgLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        // LayoutParams.MATCH_PARENT));
        setContentView(blurBgLayout);

        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        // WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        webView = new WebView(this);
        blurBgLayout.addView(webView, new LayoutParams(getResolutionValue(1920 / 4 * 3),
                getResolutionValue(1080 / 4 * 3), Gravity.CENTER));
        loadView = new LoadingView(this);
        blurBgLayout.addView(loadView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        loadView.setVisibility(View.VISIBLE);

        setWebViewProperty();
        // webView.setBackgroundColor(Color.TRANSPARENT);
        String payTestUrl = "http://42.120.20.233:8380/MyCoocaa/v2/pay/pay.action?"
                + "jsonParam=D949EA8517D9D25E12595FC9212A88D07C29DFD4190402B9269"
                + "E5500CA682B8636862EEAAC95F7F898175D232284DC4964C43B611EC2257E"
                + "C2A79FCE4F463E70289B9087944B3463325717A7E0C16A9C96A0F0CBDA16B"
                + "6E6E72B4696F39BB3CF70A2784008725B7437B337216D97B82BE43BB6774E"
                + "7C63B335AC8E341242540EACB7F2C581036A0988AC163E6607F865CC8502C"
                + "D9DAE57868A02767AC89F76F4AA7B5EAA8F7A3452E757A5014987518B732E"
                + "13132DAFAFDA06890F171A9A38B4B809DB7EE1445663DC24C4F342D42344B"
                + "92CC9D01A10BEFF7854FF0242D91743&ra=707&isTest=&address=null&"
                + "access_token=2.ded14b5775334f1c82b899ac4c80b3ff&isuser=no";
        String baiduUrl = "https://www.baidu.com";
        String payBetaHttpsUrl = "https://beta.passport.coocaa.com/html2/login.html";
        String payHttpsUrl = "https://passport.coocaa.com/html2/login.html";
        String payNewUrl = "https://pay.coocaa.com/MyCoocaa/v2/paycenter/index.action?version=";
        String payTestUrl1 = "https://pay.coocaa.com/MyCoocaa/v2/paycenter/go.action?"
                + "mac=111111111111&barcode=42E8DRS-AC860RA&mobile=&access_token=2.492eaa7267694031b062fb9dbc2e1817";
        webView.loadUrl(payTestUrl1);
    }

    private void setWebViewProperty()
    {
        // webView.setBackgroundResource(resid);
        // webView.setBackgroundColor(Color.LTGRAY); // 设置背景色
        // webView.setBackgroundColor(0);
        // webView.getBackground().setAlpha(99); // 设置填充透明度 范围：0-255
        webView.setBackgroundColor(Color.TRANSPARENT);
        // webView.setBackgroundColor(Color.YELLOW);
        // webView.addJavascriptInterface(new onPayBackJavaScript(), "onPayBack");
        // webView.addJavascriptInterface(new onDefBacckJavaScript(), "onDefaultBack");
        // webView.addJavascriptInterface(new JavaScriptInterface(this), "Android");

        webView.setWebViewClient(new PayWebViewClient());
        // WebSettings settings = webView.getSettings();
        // settings.setDomStorageEnabled(true);
        // settings.setBuiltInZoomControls(true);
        // settings.setLoadWithOverviewMode(true);
        // settings.setNeedInitialFocus(false);
        // settings.setBlockNetworkImage(true);
        // settings.setJavaScriptEnabled(true);
        //
        // settings.setAllowFileAccess(true);
        // settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // settings.setAppCacheEnabled(true);
        // settings.setDatabaseEnabled(true);

        // webView.setFocusable(false);
    }

    private class PayWebViewClient extends WebViewClient
    {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
        {
            // super.onReceivedSslError(view, handler, error);
            CcLog.i("onReceivedSslError, error = " + error.getPrimaryError());
            // handler.cancel(); //默认的处理方式，WebView变成空白页
            handler.proceed();// 信任所有的证书，加载
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            CcLog.i("shouldOverrideUrlLoading, url = " + url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            CcLog.i("onPageFinished, url = " + url);
            loadView.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl)
        {
            super.onReceivedError(view, errorCode, description, failingUrl);
            CcLog.i("onReceivedError, url = " + failingUrl);

            CcActivity.this.finish();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            CcLog.i("onPageStarted, url = " + url);
            super.onPageStarted(view, url, favicon);
        }

    }

}
