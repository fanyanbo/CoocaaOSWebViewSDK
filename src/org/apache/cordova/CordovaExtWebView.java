package org.apache.cordova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cordova.CordovaInterfaceImpl.CordovaInterfaceListener;
import org.coocaa.webview.CoocaaOSConnecter;
import org.json.JSONException;
import org.json.JSONObject;

import com.coocaa.cordova.plugin.BusinessDataListener;
import com.coocaa.cordova.plugin.CoocaaOSApi;
import com.coocaa.webviewsdk.version.SystemWebViewSDK;
import com.skyworth.framework.skysdk.properties.SkySystemProperties;
import com.skyworth.ui.blurbg.BlurBgLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class CordovaExtWebView extends FrameLayout
{
	public static final String TAG = "WebViewSDK";
	private Context mContext;
	
	// The webview for our app
	protected CordovaWebView appView;
    // Read from config.xml:
    protected CordovaPreferences preferences;
    protected ArrayList<PluginEntry> pluginEntries;
    protected CordovaInterfaceImpl cordovaInterface;
    
    protected boolean keepRunning = true;
    protected boolean mNeedThemeBg = false;
    protected FrameLayout mainLayout = null;
    protected BlurBgLayout mThemeBgLayout = null;

//	  protected SkyWithBGLoadingView mLoadingView = null;
//    protected boolean isNeedErrorPageBtn = true;
//    protected FrameLayout mErrorPageLayout = null;
//    protected BlurBgLayout mErrorPageBgLayout = null;
//    protected TextView mErrorPageTextView = null;
//    protected Button mErrorPageBtnView = null;
//    protected ImageView mErrorPageImageView = null;
    protected String mCurRequestUrl = null;

	public static final String IE9_USERAGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";
	public static final String IPAD_USERAGENT = "Mozilla/5.0 (iPad; CPU OS 4_3_2 like Mac OS X;en-us) "
			+ "AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5";
    private final int ERROR_DISCONNECT = 1;
    private final int ERROR_SIGNALWEAK = 2;
	private final int STATUS_NO_LOADING = 0;
	private final int STATUS_LOADING = 1;
	private final int STATUS_LOADED_SUCCESS = 2;
	private final int STATUS_LOADED_ERROR = 3;
    private long mEndTime = 0, mStartTime = 0;
	private int mStatus = 0, mLoadingProgress = 0;
	protected int mCacheMode = 1;//0:no-cache,1:default,2:cache_only,3:cache_else_network
	protected int mUserAgentMode = 0;//0:Android,1:IE9,2:IPad
	protected int mDisplayPolicy = 0;//0:100%-display,1:always-display
	protected String mMachineName = null;
    
    private CordovaExtWebViewListener mWebViewListener = null;
	private CordovaExtWebViewDataListener mWebViewDataListener = null;
	private JsBroadcastReceiver mJsBC = null;
	private VoiceBroadcastReceiver mVoiceBC = null;
	private LocalBroadcastManager mLocalBroadcastManager;
    
    public interface CordovaExtWebViewListener
    {
    	public void onPageStarted(String url);
		public void onPageExit();
    	public void onPageFinished(String url);
    	public void onPageError(int errorCode, String description, String failingUrl);
        public void onPageSslError(int errorCode, String failingUrl);
    	public void onProgressChanged(int process);
    }

	public interface CordovaExtWebViewDataListener
	{
		public void notifyMessage(String data);
		public void notifyLogInfo(String eventId, Map<String,String> map);
		public void notifyPageResume(String pageName, Map<String,String> map);
		public void notifyPagePause(String pageName);
	}

	private class JsBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if("notify.js.log".equals(intent.getAction()) || "notify.js.log.resume".equals(intent.getAction())){
				String eventId = intent.getStringExtra("eventId");
				String params = intent.getStringExtra("params");
				if(eventId == null) return;
				if(params != null && !"".equals(params)){
					try {
						JSONObject jsonObject = new JSONObject(params);
						Map<String,String> map = new HashMap<String,String>();
						Iterator<String> keys = jsonObject.keys();
						while(keys.hasNext()){
							String key = keys.next();
							String value = jsonObject.getString(key);
							map.put(key, value);
						}
						if(mWebViewDataListener != null ){
							if("notify.js.log.resume".equals(intent.getAction()))
								mWebViewDataListener.notifyPageResume(eventId,map);
							else
								mWebViewDataListener.notifyLogInfo(eventId,map);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					if(mWebViewDataListener != null){
						if("notify.js.log.resume".equals(intent.getAction()))
							mWebViewDataListener.notifyPageResume(eventId,null);
						else
							mWebViewDataListener.notifyLogInfo(eventId,null);
					}
				}
			}else if("notify.js.message".equals(intent.getAction())){
				String data = intent.getStringExtra("key");
				if(mWebViewDataListener != null)
					mWebViewDataListener.notifyMessage(data);
			}else if("notify.js.log.pause".equals(intent.getAction())){
				String eventId = intent.getStringExtra("eventId");
				if(eventId != null && mWebViewDataListener != null)
					mWebViewDataListener.notifyPagePause(eventId);
			}
		}
	}

	private class VoiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "VoiceBroadcastReceiver action = " + intent.getAction());
			if ("com.skyworth.srtnj.action.voice.outcmd".equals(intent.getAction())) {
				String value = intent.getStringExtra("voicecmd");
				Log.v(TAG, "com.skyworth.srtnj.action.voice.outcmd key = voicecmd, value = " + value);
				Map<String, String> map = new HashMap<String, String>();
				map.put("voicecmd", value);
				CoocaaOSApi.broadCastVoiceChanged(mContext, map);
			}
		}
	}
    
	public CordovaExtWebView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		
		loadConfig();

        registerJsDataReceiver();

		mMachineName = SkySystemProperties.getProperty("ro.product.name");
		Log.i(TAG,"CordovaExtWebView constructor WebviewSDK version = " + SystemWebViewSDK.getVersionName() + ",mMachineName = " + mMachineName);

		cordovaInterface = makeCordovaInterface();
		cordovaInterface
				.setCordovaInterfaceListener(new CordovaInterfaceListener() {

					@Override
					public boolean shouldOverrideUrlLoading(String url) {
						return false;
					}

					@Override
					public void onReceivedTitle(String title) {
						Log.v(TAG, "CordovaWebView onReceivedTitle title == " + title);
						if (title != null) {
							if (title.contains("404")) {
								if (mWebViewListener != null)
									mWebViewListener.onPageError(404, "404 Not Found", mCurRequestUrl);
								mStatus = STATUS_LOADED_ERROR;
							}
						}
					}

					@Override
					public void onReceivedIcon(Bitmap icon) {

					}

					@Override
					public void onReceivedError(final int errorCode,
												String description, String failingUrl) {
						Log.v(TAG, "CordovaWebView onReceivedError description = " + description + ",errorCode = " + errorCode);
						if (mWebViewListener != null)
							mWebViewListener.onPageError(errorCode, description, failingUrl);

						mStartTime = SystemClock.uptimeMillis();
						Log.i(TAG, "onReceivedError mStartTime=" + mStartTime);

						mStatus = STATUS_LOADED_ERROR;
					}

					@Override
					public void onReceivedSslError(int errorCode, String failingUrl) {
						Log.v(TAG, "onReceivedSslError errorCode = " + errorCode + ",failingUrl = " + failingUrl);

						if (mWebViewListener != null)
							mWebViewListener.onPageSslError(errorCode, failingUrl);
					}

					@Override
					public void onProgressChanged(int process) {
						Log.v(TAG, "CordovaWebView onProgressChanged process == " + process);
						mLoadingProgress = process;

						if (mWebViewListener != null)
							mWebViewListener.onProgressChanged(process);
					}

					@Override
					public void onPageStarted(String url) {
						Log.v(TAG, "CordovaWebView onPageStarted url == " + url);

						mCurRequestUrl = url;

						mEndTime = SystemClock.uptimeMillis();
						Log.i(TAG, "onPageStarted (mEndTime - mStartTime)=" + (mEndTime - mStartTime));
						if ((mEndTime - mStartTime) < 500l) return;

						if (mWebViewListener != null)
							mWebViewListener.onPageStarted(url);

						mStatus = STATUS_LOADING;
						mLoadingProgress = 0;
					}

					@Override
					public void onPageExit() {
						if (mWebViewListener != null)
							mWebViewListener.onPageExit();
					}

					@Override
					public void onPageLoadingFinished(String url) {
						Log.v(TAG, "CordovaWebView onPageLoadingFinished url == " + url);

						mEndTime = SystemClock.uptimeMillis();
						Log.i(TAG, "onPageLoadingFinished (mEndTime - mStartTime)=" + (mEndTime - mStartTime));
						if ((mEndTime - mStartTime) < 520l) return;

						if (mWebViewListener != null)
							mWebViewListener.onPageFinished(url);

						appView.getView().setVisibility(View.VISIBLE);

						mStatus = STATUS_LOADED_SUCCESS;
					}

					@Override
					public void doUpdateVisitedHistory(String url,
													   boolean isReload) {
					}
				});

//	    init();   //???
	}

	public void setCordovaExtWebViewListener(CordovaExtWebViewListener listener) {
		Log.i(TAG, "setCordovaExtWebViewListener");
		mWebViewListener = listener;
	}

	public void setCordovaExtWebViewDataListener(CordovaExtWebViewDataListener listener) {
		Log.i(TAG, "setCordovaExtWebViewDataListener");
		mWebViewDataListener = listener;
	}

    @SuppressWarnings("deprecation")
    protected void loadConfig() {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(mContext);
        preferences = parser.getPreferences();
        pluginEntries = parser.getPluginEntries();
    }
    
    /**
     * Load the url into the webview.
     */
	public void loadUrl(String url) {
		if (appView == null) {
			init();
		}
		mStatus = STATUS_NO_LOADING;
		// If keepRunning
		this.keepRunning = preferences.getBoolean("KeepRunning", true);

		appView.loadUrlIntoView(url, true);
	}

	public void loadUrl(String url, boolean isNeedThemeBg) {
		this.mNeedThemeBg = isNeedThemeBg;
		loadUrl(url);
	}

	public void loadUrl(String url, Map<String, String> header) {

		if (appView == null) {
			init();
		}
		mStatus = STATUS_NO_LOADING;
		// If keepRunning
		this.keepRunning = preferences.getBoolean("KeepRunning", true);

		if (header != null)
			appView.loadUrlIntoView(url, header, true);
		else
			appView.loadUrlIntoView(url, true);
	}

	public void loadUrl(String url, boolean isNeedThemeBg, Map<String, String> header) {
		this.mNeedThemeBg = isNeedThemeBg;
		this.loadUrl(url, header);
	}

	public void loadUrlIntoView(String url, boolean recreatePlugins) {
		if (appView != null) {
			appView.loadUrlIntoView(url, recreatePlugins);
		}
	}

    protected CordovaInterfaceImpl makeCordovaInterface() {    	
        return new CordovaInterfaceImpl(mContext);
    }
    
    public void setCoocaaOSConnecter(CoocaaOSConnecter connecter) {
    	cordovaInterface.setCoocaaOSConnecter(connecter);
    }

	public void setCordovaBusinessDataListener(BusinessDataListener.CordovaBusinessDataListener listener) {
		if(cordovaInterface != null) {
			cordovaInterface.setCordovaBusinessDataListener(listener);
		}
	}

    protected void init() {
        appView = makeWebView();
        createViews();
        if (!appView.isInitialized()) {
            appView.init(cordovaInterface, pluginEntries, preferences, mCacheMode);
			if(mUserAgentMode == 1) appView.setUserAgentString(IE9_USERAGENT);
        }
        cordovaInterface.onCordovaInit(appView.getPluginManager());
    }
    
    protected void createViews() {
    	
    	mainLayout = new FrameLayout(mContext);
		Log.i("WebViewSDK","createViews isNeedThemeBg = " + mNeedThemeBg);
		if (mNeedThemeBg) {
			mThemeBgLayout = new BlurBgLayout(mContext);
			mThemeBgLayout.setPageType(BlurBgLayout.PAGETYPE.SECONDE_PAGE);
			mThemeBgLayout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			mainLayout.addView(mThemeBgLayout);
		}
		//Why are we setting a constant as the ID? This should be investigated
        appView.getView().setId(100);
        appView.getView().setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mainLayout.addView(appView.getView());

        appView.getView().setBackgroundColor(Color.BLACK);
        appView.getView().requestFocusFromTouch();
        
//        mLoadingView = new SkyWithBGLoadingView(mContext);
//        FrameLayout.LayoutParams loading_p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
//        loading_p.gravity = Gravity.CENTER;
//        mainLayout.addView(mLoadingView, loading_p);
        
        this.addView(mainLayout,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
    }

    protected CordovaWebView makeWebView() {
        return new CordovaWebViewImpl(makeWebViewEngine());
    }

    protected CordovaWebViewEngine makeWebViewEngine() {
        return CordovaWebViewImpl.createEngine(mContext, preferences);
    }

	public void onPause() {

		if (mVoiceBC != null) {
			mContext.unregisterReceiver(mVoiceBC);
			mVoiceBC = null;
		}

		if (this.appView != null) {
			// CB-9382 If there is an activity that started for result and main activity is waiting for callback
			// result, we shoudn't stop WebView Javascript timers, as activity for result might be using them
			boolean keepRunning = this.keepRunning || this.cordovaInterface.activityResultCallback != null;
			this.appView.handlePause(keepRunning);
		}
	}

	public void onResume() {

		if (mVoiceBC == null) mVoiceBC = new VoiceBroadcastReceiver();
		IntentFilter voicefilter = new IntentFilter();
		voicefilter.addAction("com.skyworth.srtnj.action.voice.outcmd");
		mContext.registerReceiver(mVoiceBC, voicefilter);

		if (this.appView == null) {
			return;
		}

		this.appView.handleResume(this.keepRunning);

		this.appView.getView().setVisibility(View.VISIBLE);
	}

	public void onStart() {
		if (this.appView == null) {
			return;
		}
		this.appView.handleStart();
	}

	public void onStop() {
		if (this.appView == null) {
			return;
		}
		this.appView.handleStop();
	}

	public void onDestroy() {
		if (this.appView != null) {
			appView.handleDestroy();
		}
		unregisterJsDataReceiver();
	}

	public void setNeedThemeBg(boolean value) {
    	this.mNeedThemeBg = value;
    }
    
    public int getStatus() {
		return mStatus;
	}

	public int getPageLoadingProgress() {
		return mLoadingProgress;
	}

	public void setCacheMode(int value) {
		if(value < 0 || value > 3)
			value = 0;
		mCacheMode = value;
	}

	public void setUserAgentMode(int value) {
		if(value < 0 || value > 3)
			value = 0;
		mUserAgentMode = value;
	}

	public void setWebViewDisplayPolicy(int value) {
		if(value < 0 || value > 1)
			mDisplayPolicy = 0;
		mDisplayPolicy = value;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (appView != null && event.getAction() == KeyEvent.ACTION_DOWN){
			int keyCode = 0;
			if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP){
				keyCode = 38;
			}else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN){
				keyCode = 40;
			}
			if(keyCode > 0 && mMachineName != null && ( mMachineName.equals("rtd299x_tv010_4k") || mMachineName.equals("h25ref"))){
				Log.i(TAG,"CordovaExtWebView dispatchKeyEvent keyCode = " + keyCode);
				appView.loadUrlIntoView(
						"javascript:(function(){var ev=document.createEvent('HTMLEvents');ev.which=ev.keyCode=" +
								keyCode + ";ev.initEvent('" + "keydown" +
								"',true, true);document.body.dispatchEvent(ev);})()", false);
				return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	public void clearHistory() {
		if (appView != null) {
			appView.clearHistory();
		}
	}

	public boolean canGoBack() {
		if (appView != null) {
			return appView.canGoBack();
		}
		return false;
	}

	public boolean backHistory() {
		if (appView != null) {
			return appView.backHistory();
		}
		return false;
	}

	public void stopLoading() {
		if (appView != null) {
			appView.pauseLoading();
			appView.clearHistory();
			appView.clearCache();
		}
	}

	private void registerJsDataReceiver() {
        if (mJsBC == null) mJsBC = new JsBroadcastReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        IntentFilter filter = new IntentFilter();
        filter.addAction("notify.js.message");
        filter.addAction("notify.js.log");
        filter.addAction("notify.js.log.resume");
        filter.addAction("notify.js.log.pause");
        mLocalBroadcastManager.registerReceiver(mJsBC, filter);
    }

    private void unregisterJsDataReceiver() {
        if (mJsBC != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mJsBC);
            mJsBC = null;
        }
    }
}
