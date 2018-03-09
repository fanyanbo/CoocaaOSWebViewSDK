package org.coocaa.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

@SuppressLint("SetJavaScriptEnabled")
public class CoocaaWebView {
	
	private final static String mTag = "WebViewSDK";
	
	private static CoocaaWebView instance = null;
	private WebView mWebView = null;
	private int mStatus = 0;
	private CoocaaOSConnecter connecter = nullSet;
	
	   private static final CoocaaOSConnecter nullSet = new CoocaaOSConnecter() {

			@Override
			public String hasCoocaaUserLogin() {
				// TODO Auto-generated method stub
				return "not set<hasCoocaaUserLogin>";
			}

			@Override
			public String getUserInfo() {
				// TODO Auto-generated method stub
				return "not set<getUserInfo>";
			}

			@Override
			public String getDeviceInfo() {
				// TODO Auto-generated method stub
				return "not set<getDeviceInfo>";
			}

			@Override
			public String isNetConnected() {
				// TODO Auto-generated method stub
				return "not set<isNetConnected>";
			}

			@Override
			public String getNetType() {
				// TODO Auto-generated method stub
				return "not set<getNetType>";
			}

			@Override
			public String getIpInfo() {
				// TODO Auto-generated method stub
				return "not set<getIpInfo>";
			}

			@Override
			public String getDeviceLocation() {
				// TODO Auto-generated method stub
				return "not set<getDeviceLocation>";
			}

			@Override
			public String getUserAccessToken() {
				// TODO Auto-generated method stub
				return "not set<getUserAccessToken>";
			}
	    };
	
	private CoocaaWebView(Context context) {
		
        init(context);
	}  

    public static synchronized CoocaaWebView getInstance(Context context) {  
         if (instance == null) {    
        	 instance = new CoocaaWebView(context);  
         }    
        return instance;  
    }
    
    private void init(Context context) {
		
		Log.i(mTag,"CordovaWebView initUI");
		
		mWebView = new WebView(context);
		mWebView.setBackgroundColor(0);
		FrameLayout.LayoutParams webViewLp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mWebView.setLayoutParams(webViewLp);
		
		WebSettings webSettings = mWebView.getSettings();

		webSettings.setJavaScriptEnabled(true);  
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);

		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);

		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setAllowFileAccess(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setLoadsImagesAutomatically(true); 
		webSettings.setDefaultTextEncodingName("utf-8");
		
		mWebView.setInitialScale(0);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setFocusable(true);
		
		mWebView.setWebViewClient(new WebViewClient() {

			@SuppressWarnings("deprecation")
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				
				Log.i(mTag,"onReceivedError url = " + failingUrl + ",description = " + description);
				
				mStatus = -1;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				
				Log.i(mTag,"onPageFinished url = " + url);
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				
				mStatus = 100;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				
				Log.i(mTag,"onPageStarted url = " + url);
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				
				mStatus = 0;
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				Log.i(mTag,"shouldOverrideUrlLoading url = " + url);
				return super.shouldOverrideUrlLoading(view, url);
			}
		});

		mWebView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {

				Log.i(mTag,"onProgressChanged newProgress = " + newProgress);
				
				mStatus = newProgress;
			}

		});
		
		CoocaaOSExposedJsApi exposedJsApi = new CoocaaOSExposedJsApi(this);
		mWebView.addJavascriptInterface(exposedJsApi, "CoocaaOSApi");

	}
    
    public void loadUrl(final String url){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mWebView.loadUrl(url);
			}
		}).start();
    }

    public WebView getView() {
    		
    	return mWebView;
    }
    
    public int getstatus() {
    	return mStatus;
    }
    
    public void setCoocaaOSConnecter (CoocaaOSConnecter connecter) {
    	this.connecter = connecter;
    }
    
    public CoocaaOSConnecter getCoocaaOSConnecter() {
        if (connecter == nullSet)
            Log.e(mTag, "Do you forget to call setCoocaaOSConnecter()!!!");
        return connecter;
    }
}


















