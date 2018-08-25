package org.lzyzsd.jsbridge;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.skyworth.ui.api.SkyWithBGLoadingView;

public class JsBridgeActivity extends Activity {

	private static final String TAG = "jsbridge";
	private static final String API_PLUGIN = "CoocaaOSApiPlugin";
	private static final String START_PLUGIN = "startPlugin";
	private static final String CORE_PLUGIN = "corePlugin";
	private static final String KEY_MESSAGE = "KeyMessage";
	private static final String BC_MESSAGE = "BroadcastMessage";
	protected FrameLayout mainLayout = null;
	protected SkyWithBGLoadingView mLoadingView = null;
	protected BridgeWebView webView;

    @Override
    protected void onResume() {
        super.onResume();
		webView.setVisibility(View.VISIBLE);
		webView.callHandler(KEY_MESSAGE, "onResume", null);
    }

	@Override
	protected void onPause() {
		super.onPause();
		webView.callHandler(KEY_MESSAGE, "onPause", null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mainLayout = new FrameLayout(this);
		webView = new BridgeWebView(this);
		webView.setLayoutParams(new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		mainLayout.addView(webView);
		mLoadingView = new SkyWithBGLoadingView(this);
		FrameLayout.LayoutParams loading_p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
		loading_p.gravity = Gravity.CENTER;
		mainLayout.addView(mLoadingView, loading_p);
		if(mLoadingView!=null) {
			mLoadingView.showLoading();
		}
		setContentView(mainLayout);

		webView.setDefaultHandler(new CCDefaultHandler());
		webView.registerHandler(API_PLUGIN, new CCOSApiHandler());
		webView.registerHandler(START_PLUGIN, new CCLaunchHandler(this));
		webView.registerHandler(CORE_PLUGIN, new CCCoreHandler(this));

        webView.setWebPageListener(new IWebPageListener() {
			@Override
			public void onPageStarted(String url) {
				Log.i(TAG, "setWebPageListener url = " + url);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						if(mLoadingView!=null) {
							mLoadingView.showLoading();
						}
						webView.setVisibility(View.INVISIBLE);
					}
				});
			}

			@Override
			public void onPageLoadingFinished(String url) {
				Log.i(TAG, "onPageLoadingFinished url = " + url);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if(mLoadingView != null){
							mLoadingView.dismissLoading();
						}
						webView.setVisibility(View.VISIBLE);
					}
				});
			}

			@Override
			public void onReceivedError(int errCode, String desc, String url) {
				Log.i(TAG, "onReceivedError desc = " + desc + ",url = " + url);
			}

			@Override
			public void onProgressChanged(int newValue) {
				Log.i(TAG, "onProgressChanged newValue = " + newValue);
			}
		});

//		webView.loadUrl("file:///android_asset/demo.html");
	}

	protected void loadUrl(String url) {
    	if(webView != null) {
    		webView.loadUrl(url);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(webView != null)
			webView.destroy();
	}
}
