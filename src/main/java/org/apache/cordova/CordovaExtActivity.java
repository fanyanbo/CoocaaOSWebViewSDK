package org.apache.cordova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.cordova.CordovaInterfaceImpl.CordovaInterfaceListener;
import org.apache.cordova.CordovaMainLayout.OnThemeChangedListener;

import org.json.JSONException;
import org.json.JSONObject;

import com.coocaa.systemwebview.R;

import com.skyworth.framework.skysdk.properties.SkySystemProperties;
import com.skyworth.ui.api.SkyWithBGLoadingView;
import com.skyworth.ui.blurbg.BlurBgLayout;
import com.skyworth.util.SkyScreenParams;
import com.skyworth.theme.SkyThemeEngine;
import com.skyworth.theme.ThemeColorSeriesEnum;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import coocaa.plugin.api.BusinessDataListener;
import coocaa.plugin.api.CoocaaOSApi;
import coocaa.plugin.api.CoocaaOSConnecter;
import coocaa.plugin.api.CoocaaOSConnecterDefaultImpl;

public class CordovaExtActivity extends CordovaBaseActivity implements OnThemeChangedListener{

	   public static final String TAG = "WebViewSDK";

	    // The webview for our app
	    protected CordovaWebView appView;

	    private static int ACTIVITY_STARTING = 0;
	    private static int ACTIVITY_RUNNING = 1;
	    private static int ACTIVITY_EXITING = 2;
	    
	    public static final String IE9_USERAGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";
	    public static final String IPAD_USERAGENT = "Mozilla/5.0 (iPad; CPU OS 4_3_2 like Mac OS X;en-us) "
	            + "AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5";
	    
	    private final int ERROR_DISCONNECT = 1;
	    private final int ERROR_SIGNALWEAK = 2;

	    // Keep app running when pause is received. (default = true)
	    // If true, then the JavaScript and native code continue to run in the background
	    // when another application (activity) is started.
	    protected boolean keepRunning = true;

	    // Flag to keep immersive mode if set to fullscreen
	    protected boolean immersiveMode;

	    // Read from config.xml:
	    protected CordovaPreferences preferences;
	    protected String launchUrl;
	    protected ArrayList<PluginEntry> pluginEntries;
	    protected CordovaInterfaceImpl cordovaInterface;

		private Context mContext;
	    //protected FrameLayout mainLayout;
	    protected CordovaMainLayout mainLayout;
	    protected FrameLayout mErrorView = null;
	    protected FrameLayout mErrorBgView = null;
	    protected SkyWithBGLoadingView mLoadingView = null;
	    protected boolean mErrorPageIsShown = false;
	    protected String mMachineName = null;
	    protected BlurBgLayout mErrorBgLayout = null;
	    protected BlurBgLayout mMainBgLayout = null;
	    protected TextView mTextView = null;
	    protected Button mBtnView = null;
	    protected ImageView mImageView = null;
	    protected String mLoadingUrl = null;
	    protected boolean isNeedThemeBg = false;
	    protected boolean isNeedErrorPageBtn = true;
//	    protected boolean isPageHref = true;
	    protected boolean isLoading = false;
	    protected int cacheMode = 0;//0:no-cache,1:default,2:cache_only,3:cache_else_network
	    protected int userAgentMode = 0;//0:Android,1:IE9,2:IPad
	    protected int displayPolicy = 0;//0:100%-display,1:always-display
	    protected String mOriginalUrl = null;
	    private long mEndTime = 0, mStartTime = 0;
	    
	    private CordovaWebViewListener mWebViewListener = null;
	    private CordovaWebPageListener mWebPageListener = null;
	    private CordovaErrorPageListener mErrorPageListener = null;
		private BusinessDataListener.CordovaBusinessDataListener mBusinessListener = null;
	    private JsBroadcastReceiver mJsBC = null;
		private VoiceBroadcastReceiver mVoiceBC = null;
		private CoocaaOSConnecter mCoocaaOSConnecter = null;

	    LocalBroadcastManager mLocalBroadcastManager;

	    public interface CordovaWebViewListener
	    {
	    	public void onPageStarted(String url);
	    	public void onPageFinished(String url);
	    	public void onPageError(int errorCode, String description, String failingUrl);
	    }
	    
	    public interface CordovaWebPageListener
	    {
	    	public void notifyMessage(String data);
	    	public void notifyLogInfo(String eventId, Map<String,String> map);
			public void notifyPageResume(String eventId, Map<String,String> map);
			public void notifyPagePause(String eventId);
	    }
	    
	    public interface CordovaErrorPageListener
	    {
	    	public void handleUI(String value);
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
							if(mWebPageListener != null ){
								if("notify.js.log.resume".equals(intent.getAction()))
									mWebPageListener.notifyPageResume(eventId,map);
								else
									mWebPageListener.notifyLogInfo(eventId,map);
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						if(mWebPageListener != null){
							if("notify.js.log.resume".equals(intent.getAction()))
								mWebPageListener.notifyPageResume(eventId,null);
							else
								mWebPageListener.notifyLogInfo(eventId,null);
						}
					}
				}else if("notify.js.message".equals(intent.getAction())){
					String data = intent.getStringExtra("key");
					if(mWebPageListener != null)
			        	mWebPageListener.notifyMessage(data);
				}else if("notify.js.log.pause".equals(intent.getAction())){
					String eventId = intent.getStringExtra("eventId");
					if(eventId != null && mWebPageListener != null)
						mWebPageListener.notifyPagePause(eventId);
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
	    
	    public void setCordovaWebViewListener(CordovaWebViewListener listener) {
	    	this.mWebViewListener = listener;
	    }
	    
	    public void setCordovaWebPageListener(CordovaWebPageListener listener) {
	    	this.mWebPageListener = listener;
	    }
	    
	    public void setCordovaErrorPageListener(CordovaErrorPageListener listener) {
	    	this.mErrorPageListener = listener;
	    }

		public void setCordovaBusinessDataListener(BusinessDataListener.CordovaBusinessDataListener listener) {
			if(cordovaInterface != null) {
				cordovaInterface.setCordovaBusinessDataListener(listener);
			}
		}
	    /**
	     * Called when the activity is first created.
	     */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        LOG.i(TAG, "Apache Cordova native platform version " + CordovaWebView.CORDOVA_VERSION + " is starting");
	        LOG.d(TAG, "CordovaActivity.onCreate()");

			mContext = this;
	        // need to activate preferences before super.onCreate to avoid "requestFeature() must be called before adding content" exception
	        loadConfig();
	        if (!preferences.getBoolean("ShowTitle", false)) {
	            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	        }

	        if (preferences.getBoolean("SetFullscreen", false)) {
	            Log.d(TAG, "The SetFullscreen configuration is deprecated in favor of Fullscreen, and will be removed in a future version.");
	            preferences.set("Fullscreen", true);
	        }
	        if (preferences.getBoolean("Fullscreen", false)) {
	            if (Build.VERSION.SDK_INT >= 19 /*Build.VERSION_CODES.KITKAT*/) {
	                immersiveMode = true;
	            } else {
	                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
	            }
	        } else {
	            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
	                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	        }
	        
	        cordovaInterface = makeCordovaInterface();
	        if (savedInstanceState != null) {
	            cordovaInterface.restoreInstanceState(savedInstanceState);
	        }
	        
	//        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);        
	        mMachineName = SkySystemProperties.getProperty("ro.product.name");
	        Log.i(TAG,"CordovaExtActivity onCreate SystemWebviewSDK version = " + org.apache.cordova.plugin.api.version.SystemWebViewSDK.getVersionName() + ",mMachineName = " + mMachineName);
	        
	        mainLayout = new CordovaMainLayout(this);
	        mainLayout.setListener(this);

	        cordovaInterface.setCordovaInterfaceListener(new CordovaInterfaceListener() {

				@Override
				public void onReceivedTitle(String title) {
					Log.v(TAG, "onReceivedTitle title == " + title);
					if(title != null){
						if(title.contains("404")){
							if(mWebViewListener != null)
								mWebViewListener.onPageError(404, "404 Not Found", mLoadingUrl);
							
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									isNeedErrorPageBtn = false;
									showErrorPage(ERROR_SIGNALWEAK);
								}
							});
						}
					}
				}
				
				@Override
				public void onReceivedError(final int errorCode, String description,
						String failingUrl) {
					Log.v(TAG, "errorCode == " +errorCode + " error description = " + description + " failingUrl = " + failingUrl);
					if(mWebViewListener != null)
						mWebViewListener.onPageError(errorCode, description, failingUrl);
					
					mStartTime = SystemClock.uptimeMillis();
					Log.i(TAG,"onReceivedError mStartTime="+mStartTime);
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							if(errorCode == -2){
								showErrorPage(ERROR_DISCONNECT);
							}else{
								showErrorPage(ERROR_SIGNALWEAK);
							}
						}
					});

				}
				
				@Override
				public void onProgressChanged(int process) {
					Log.v(TAG, "onProgressChanged process == " +process);			
				}

				@Override
				public void onPageStarted(String url) {
					Log.v(TAG, "onPageStarted url == " +url);
					
					mLoadingUrl = url;

					mEndTime = SystemClock.uptimeMillis();
					Log.i(TAG,"onPageStarted (mEndTime - mStartTime)="+(mEndTime - mStartTime));
					if((mEndTime - mStartTime) < 500l) return;
					
					if(mWebViewListener != null)
						mWebViewListener.onPageStarted(url);
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							
							startLoading();
							if(displayPolicy == 0) 
								appView.getView().setVisibility(View.INVISIBLE);
							if(mErrorPageIsShown)
							{
								hideErrorPage();
							}
						}
					});
				}

				@Override
				public void onPageExit() {

				}

				@Override
				public void onPageLoadingFinished(String url) {
					Log.v(TAG, "onPageLoadingFinished url == " +url);

					mEndTime = SystemClock.uptimeMillis();
					Log.i(TAG,"onPageLoadingFinished (mEndTime - mStartTime)="+(mEndTime - mStartTime));
					if((mEndTime - mStartTime) < 520l) return;

					if(mWebViewListener != null)
						mWebViewListener.onPageFinished(url);

					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							
							stopLoading();
							if(mErrorPageIsShown)
							{
								if(mErrorView != null)
								{
									if(mBtnView != null)
										mBtnView.requestFocus();
								}
							}else{
								appView.getView().setVisibility(View.VISIBLE);
							}
						}
					});
				}
			});
	        
	        super.onCreate(savedInstanceState);
	    }

		 OnTouchListener changeListener = new OnTouchListener()
		    {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					   if(event.getAction()==MotionEvent.ACTION_DOWN){  
			                v.setBackgroundResource(R.drawable.ui_sdk_shape_focus_white);  
			            }else if(event.getAction()==MotionEvent.ACTION_UP){  
			                v.setBackgroundResource(R.drawable.ui_sdk_btn_unfocus_big_shadow);  
			            }  
			            return false;  
				}

		    };
		    
	    OnClickListener clickListener = new OnClickListener()
	    {
	        @Override
	        public void onClick(View v)
	        {
	        	Log.i(TAG, "CordovaExtActivity OnClickListener " + v.getTag().toString());
	        	if("1".equals(v.getTag().toString()))
	        	{
	        	  	Intent mIntent = new Intent("android.settings.NETWORK_OPERATOR_SETTINGS");
	        	  	mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	  	startActivity(mIntent);
	        	}
	        	else if ("2".equals(v.getTag().toString()))
	        	{
	        		if(appView != null)
	        		{
	        			if(mLoadingUrl != null)
	        				appView.loadUrlIntoView(mLoadingUrl, false);
	        			else
	        				appView.reload();
	        		}
	        	}
	        	if(mErrorPageListener != null)
	        		mErrorPageListener.handleUI(v.getTag().toString());
	        }
	    };
	    
		protected void showErrorPage(int errorType) {
			
			boolean isInit = initErrorPage(errorType);
			if(isInit)
			{
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				mainLayout.addView(mErrorView, lp);
			}
			else
				mErrorView.setVisibility(View.VISIBLE);
			
			stopLoading();
			
			if(mErrorView!=null)
				if(mBtnView != null)
					mBtnView.requestFocus();
			
			mErrorPageIsShown = true;
		}
		
		protected void hideErrorPage() {
			
			mErrorPageIsShown = false;
			if(mErrorView!=null)
			{
				mErrorView.setVisibility(View.GONE);
			}
		}

		protected void setBackgroundPageShown(boolean value) {
			if (value) {
				if(mMainBgLayout != null) mMainBgLayout.setVisibility(View.VISIBLE);
			}else {
				if(mMainBgLayout != null) mMainBgLayout.setVisibility(View.INVISIBLE);
			}
		}
		
		protected void startLoading() {
			if(mLoadingView!=null)
			{
				mLoadingView.showLoading();
				isLoading = true;
			}
		}
		
		protected void stopLoading() {
			if(mLoadingView != null){
				mLoadingView.dismissLoading();
				isLoading = false;
			}
		}
		
		protected boolean isLoading() {
			return isLoading;
		}
		
		protected void setErrorPageBackground() {
			
			mErrorBgView.addView(mErrorBgLayout);
		}
		
		protected boolean initErrorPage(int errorType) {
			
			boolean isInit = false;
			if (mErrorView == null) {
				mErrorView = new FrameLayout(this);

				FrameLayout.LayoutParams bg_p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
		        mErrorView.addView(mErrorBgView,bg_p);
		        
				mImageView = new ImageView(this);			
				FrameLayout.LayoutParams imgViewLp = new FrameLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				imgViewLp.gravity = Gravity.CENTER_HORIZONTAL;
				imgViewLp.topMargin = SkyScreenParams.getInstence(this).getResolutionValue(325);
				
				mTextView = new TextView(this);
				mTextView.setTextSize(SkyScreenParams.getInstence(this).getTextDpiValue(36));
				mTextView.setTextColor(getResources().getColor(R.color.c_4));
				mErrorView.setOnClickListener(null);		
				FrameLayout.LayoutParams txtViewLp = new FrameLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				txtViewLp.gravity = Gravity.CENTER_HORIZONTAL;
				txtViewLp.topMargin = SkyScreenParams.getInstence(this).getResolutionValue(527);
		
				mBtnView = new Button(this);
				mBtnView.setFocusable(true);
				mBtnView.setFocusableInTouchMode(true);
				mBtnView.setTextSize(SkyScreenParams.getInstence(this).getTextDpiValue(30));
				mBtnView.setTextColor(Color.BLACK);
				mBtnView.setOnClickListener(clickListener);
				mBtnView.setOnTouchListener(changeListener);
				mBtnView.setBackgroundResource(R.drawable.ui_sdk_btn_focus_shadow_bg);
				mBtnView.requestFocus();
				FrameLayout.LayoutParams btnViewLp = new FrameLayout.LayoutParams(
						SkyScreenParams.getInstence(this).getResolutionValue(410), SkyScreenParams.getInstence(this).getResolutionValue(238));
				btnViewLp.gravity = Gravity.CENTER_HORIZONTAL;
				btnViewLp.topMargin = SkyScreenParams.getInstence(this).getResolutionValue(550);
				
				mErrorView.addView(mImageView,imgViewLp);
				mErrorView.addView(mTextView,txtViewLp);	
				mErrorView.addView(mBtnView,btnViewLp);
				
				if(mBtnView != null && !isNeedErrorPageBtn)
					mBtnView.setVisibility(View.GONE);
		        
				isInit = true;
			}
			
			switch(errorType){
			case 1:
				mTextView.setText(R.string.error_webview_netdisconnect);
				mImageView.setImageResource(R.drawable.new_disconnect);
				mBtnView.setText(R.string.error_webview_reset);
				mBtnView.setTag("1");
				break;
			case 2:
			default:
				mTextView.setText(R.string.error_webview_neterror);
				mImageView.setImageResource(R.drawable.new_refresh);
				mBtnView.setText(R.string.error_webview_refresh);
				mBtnView.setTag("2");
				break;
			}

			return isInit;
		}
		
	    protected void init(FrameLayout errorPageBg) {
	        appView = makeWebView();
	        createViews(errorPageBg);
	        if (!appView.isInitialized()) {
	        	appView.init(cordovaInterface, pluginEntries, preferences, cacheMode);
	        }
	        cordovaInterface.onCordovaInit(appView.getPluginManager());
	        
	        //set IPC Connecter
			if (getCmdConnectorListener() != null) {
				if (mCoocaaOSConnecter == null) {
					mCoocaaOSConnecter = new CoocaaOSConnecterDefaultImpl(this, getCmdConnectorListener());
					cordovaInterface.setCoocaaOSConnecter(mCoocaaOSConnecter);
				}
			}

	        // Wire the hardware volume controls to control media if desired.
	        String volumePref = preferences.getString("DefaultVolumeStream", "");
	        if ("media".equals(volumePref.toLowerCase(Locale.ENGLISH))) {
	            setVolumeControlStream(AudioManager.STREAM_MUSIC);
	        }
	    }

	@Override
	public void onSuperCmdInit() {
		super.onSuperCmdInit();
		Log.i(TAG,"onSuperCmdInit CoocaaOSConnecter:" + mCoocaaOSConnecter);
		if (mCoocaaOSConnecter == null) {
			mCoocaaOSConnecter = new CoocaaOSConnecterDefaultImpl(this, getCmdConnectorListener());
			cordovaInterface.setCoocaaOSConnecter(mCoocaaOSConnecter);
		}
	}

	@SuppressWarnings("deprecation")
	    protected void loadConfig() {
	        ConfigXmlParser parser = new ConfigXmlParser();
	        parser.parse(this);
	        preferences = parser.getPreferences();
	        preferences.setPreferencesBundle(getIntent().getExtras());
	        launchUrl = parser.getLaunchUrl();
	        pluginEntries = parser.getPluginEntries();
	        Config.parser = parser;
	    }

	    //Suppressing warnings in AndroidStudio
	    @SuppressWarnings({"deprecation", "ResourceType"})
	    protected void createViews(FrameLayout errorPageBg) {
	        //Why are we setting a constant as the ID? This should be investigated
	        appView.getView().setId(100);
	        appView.getView().setLayoutParams(new FrameLayout.LayoutParams(
	                ViewGroup.LayoutParams.MATCH_PARENT,
	                ViewGroup.LayoutParams.MATCH_PARENT));
	        if(mainLayout == null)
	        	mainLayout = new CordovaMainLayout(this);

			mMainBgLayout = new BlurBgLayout(this);
			mMainBgLayout.setPageType(BlurBgLayout.PAGETYPE.SECONDE_PAGE);
			mMainBgLayout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			mainLayout.addView(mMainBgLayout);
			mMainBgLayout.setVisibility(View.INVISIBLE);
			if(isNeedThemeBg) mMainBgLayout.setVisibility(View.VISIBLE);
	        
	        mainLayout.addView(appView.getView());
	        
	        mErrorBgView = new FrameLayout(this);
	        if(errorPageBg != null)
	        {
	        	mErrorBgView.addView(errorPageBg);
	        }else{
				mErrorBgLayout = new BlurBgLayout(this);
				mErrorBgLayout.setPageType(BlurBgLayout.PAGETYPE.SECONDE_PAGE);
				mErrorBgLayout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				mErrorBgView.addView(mErrorBgLayout);
	        }
	           
	        mLoadingView = new SkyWithBGLoadingView(this);
	        FrameLayout.LayoutParams loading_p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
	        loading_p.gravity = Gravity.CENTER;
	        mainLayout.addView(mLoadingView, loading_p);
	        startLoading();
	        setContentView(mainLayout);
//	        mLoadingView.bringToFront();
	        if (preferences.contains("BackgroundColor")) {
	            int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
	            // Background of activity:
	            appView.getView().setBackgroundColor(backgroundColor);
	        }

	        appView.getView().requestFocusFromTouch();
	    }

	    /**
	     * Construct the default web view object.
	     * <p/>
	     * Override this to customize the webview that is used.
	     */
	    protected CordovaWebView makeWebView() {
	        return new CordovaWebViewImpl(makeWebViewEngine());
	    }

	    protected CordovaWebViewEngine makeWebViewEngine() {
	        return CordovaWebViewImpl.createEngine(this, preferences);
	    }

	    protected CordovaInterfaceImpl makeCordovaInterface() {
	        return new CordovaInterfaceImpl(this);/* {
	            @Override
	            public Object onMessage(String id, Object data) {
	                // Plumb this to CordovaActivity.onMessage for backwards compatibility
	                return CordovaActivity.this.onMessage(id, data);
	            }
	        };*/
	    }

	    /**
	     * Load the url into the webview.
	     */
	    public void loadUrl(String url) {
	    	this.loadUrl(url, false, true, null);
	    }
	    
	    public void loadUrl(String url, boolean isNeedBg) {
	    	this.loadUrl(url, isNeedBg, true, null);
	    }
	    
	    public void loadUrl(String url, FrameLayout errorPageBg) {
	    	this.loadUrl(url, false, true, errorPageBg);
	    }

	    public void loadUrl(String url, boolean isNeedBg, Map<String,String> header) {
	    	isNeedThemeBg = isNeedBg;
	    	loadUrl(url, header);
	    }

		public void loadUrl(String url, Map<String,String> header) {
	    	mOriginalUrl = url;
	        if (appView == null) {
	            init(null);
	        }
	        
	        if(url == null){
	        	runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(isNeedErrorPageBtn)
							showErrorPage(ERROR_DISCONNECT);
						else
							showErrorPage(ERROR_SIGNALWEAK);
					}
				});
	        }else{
		        // If keepRunning
		        this.keepRunning = preferences.getBoolean("KeepRunning", true);
		        
		        if(header != null)
		        	appView.loadUrlIntoView(getThemeUrl(url),header,true);   
		        else
		        	appView.loadUrlIntoView(getThemeUrl(url),true); 
	        }
	    }
	    
	    public void loadUrl(String url, boolean isNeedBg, boolean isNeedBtn, FrameLayout errorPageBg) {
	    	
	    	mOriginalUrl = url;
	    	isNeedThemeBg = isNeedBg;
	    	isNeedErrorPageBtn = isNeedBtn;
	    	
	        if (appView == null) {
	            init(errorPageBg);
	        }
	        
	        if(url == null){
	        	runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(isNeedErrorPageBtn)
							showErrorPage(ERROR_DISCONNECT);
						else
							showErrorPage(ERROR_SIGNALWEAK);
					}
				});
	        }else{
		        // If keepRunning
		        this.keepRunning = preferences.getBoolean("KeepRunning", true);
		        
		        appView.loadUrlIntoView(getThemeUrl(url), true);   
	        }
	    }

	private boolean addTheme = true;

	public void setAddTheme(boolean addTheme)
	{
		this.addTheme = addTheme;
		Log.i(TAG,"setAddTheme = " + addTheme);
	}

	public String getThemeUrl(String url){
	    	if(addTheme && url != null){
		        if(SkyThemeEngine.getInstance().getThemeColorSeries() == ThemeColorSeriesEnum.E_THEME_COLOR_SERIES_DARK){
		        	if(url.contains("?")){
		        		url = url + "&theme=dark";
		        	}else{
		        		url = url + "?theme=dark";
		        	}
		        }else if(SkyThemeEngine.getInstance().getThemeColorSeries() == ThemeColorSeriesEnum.E_THEME_COLOR_SERIES_LIGHT){
		        	if(url.contains("?")){
		        		url = url + "&theme=light";
		        	}else{
		        		url = url + "?theme=light";
		        	}
		        }else{
		        	Log.i(TAG,"load getThemeUrl no theme!");
		        }
	    	}
	    	Log.i(TAG,"load getThemeUrl = " + url);
	    	
	    	return url;
	    }
	    
	    public void setCacheMode(int mode) {
	    	if(mode < 0 || mode > 3)
	    		mode = 0;
	    	cacheMode = mode;
	    }
	    
	    public void setUserAgentMode(int mode) {
	    	if(mode < 0 || mode > 3)
	    		mode = 0;
	    	userAgentMode = mode;
	    }
	    
	    public void setWebViewDisplayPolicy(int value) {
	    	if(value < 0 || value > 1)
	    		displayPolicy = 0;
	    	displayPolicy = value;
	    }
	    
	    public int getWebViewFocusPosition(){
	    	return org.apache.cordova.plugin.api.version.SystemWebViewSDK.getFocusPosition();
	    }

	    /**
	     * Called when the system is about to start resuming a previous activity.
	     */
	    @Override
	    protected void onPause() {
	        super.onPause();
	        LOG.d(TAG, "Paused the activity.");

			if (mJsBC != null) {
				LocalBroadcastManager.getInstance(this).unregisterReceiver(mJsBC);
				mJsBC = null;
			}
			if (mVoiceBC != null) {
				unregisterReceiver(mVoiceBC);
				mVoiceBC = null;
			}

	        if (this.appView != null) {
	            // CB-9382 If there is an activity that started for result and main activity is waiting for callback
	            // result, we shoudn't stop WebView Javascript timers, as activity for result might be using them
	            boolean keepRunning = this.keepRunning || this.cordovaInterface.activityResultCallback != null;
	            this.appView.handlePause(keepRunning);
	        }
	    }

	    /**
	     * Called when the activity receives a new intent
	     */
	    @Override
	    protected void onNewIntent(Intent intent) {
	        super.onNewIntent(intent);
	        //Forward to plugins
	        if (this.appView != null)
	            this.appView.onNewIntent(intent);
	    }

	    /**
	     * Called when the activity will start interacting with the user.
	     */
	    @Override
	    protected void onResume() {
	        super.onResume();
	        LOG.d(TAG, "Resumed the activity.");

			if (mJsBC == null) mJsBC = new JsBroadcastReceiver();
			mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
			IntentFilter filter = new IntentFilter();
			filter.addAction("notify.js.message");
			filter.addAction("notify.js.log");
			filter.addAction("notify.js.log.resume");
			filter.addAction("notify.js.log.pause");
			mLocalBroadcastManager.registerReceiver(mJsBC, filter);

			if (mVoiceBC == null) mVoiceBC = new VoiceBroadcastReceiver();
			IntentFilter netfilter = new IntentFilter();
			netfilter.addAction("com.skyworth.srtnj.action.voice.outcmd");
			registerReceiver(mVoiceBC, netfilter);

	        if (this.appView == null) {
	            return;
	        }
	        // Force window to have focus, so application always
	        // receive user input. Workaround for some devices (Samsung Galaxy Note 3 at least)
	        this.getWindow().getDecorView().requestFocus();

	        this.appView.handleResume(this.keepRunning);
	        
			if(mErrorPageIsShown || isLoading())
			{
				if(mErrorView!=null)
				{
					if(mBtnView != null)
						mBtnView.requestFocus();
				}
			}else{
				this.appView.getView().setVisibility(View.VISIBLE);
			}
	    }

	    /**
	     * Called when the activity is no longer visible to the user.
	     */
	    @Override
	    protected void onStop() {
	        super.onStop();
	        LOG.d(TAG, "Stopped the activity.");
	        if (this.appView == null) {
	            return;
	        }
	        this.appView.handleStop();
	    }

	    /**
	     * Called when the activity is becoming visible to the user.
	     */
	    @Override
	    protected void onStart() {
	        super.onStart();
	        LOG.d(TAG, "Started the activity.");
	        if (this.appView == null) {
	            return;
	        }
	        this.appView.handleStart();
	    }
	    
	    /**
	     * The final call you receive before your activity is destroyed.
	     */
	    @Override
	    public void onDestroy() {
	        LOG.d(TAG, "CordovaActivity.onDestroy()");
	        super.onDestroy();

	        if (this.appView != null) {
	        	if(mainLayout != null)
	        		mainLayout.removeView(this.appView.getView());
	        	
	            appView.handleDestroy();
	        }
	    }

	    /**
	     * Called when view focus is changed
	     */
	    @Override
	    public void onWindowFocusChanged(boolean hasFocus) {
	        super.onWindowFocusChanged(hasFocus);
	        if (hasFocus && immersiveMode) {
	            final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
	                    | View.SYSTEM_UI_FLAG_FULLSCREEN
	                    | 4096 /*View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY*/ ;

	            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
	        }
	    }

	    @SuppressLint("NewApi")
	    @Override
	    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
	        // Capture requestCode here so that it is captured in the setActivityResultCallback() case.
	        cordovaInterface.setActivityResultRequestCode(requestCode);
	        super.startActivityForResult(intent, requestCode, options);
	    }

	    /**
	     * Called when an activity you launched exits, giving you the requestCode you started it with,
	     * the resultCode it returned, and any additional data from it.
	     *
	     * @param requestCode The request code originally supplied to startActivityForResult(),
	     *                    allowing you to identify who this result came from.
	     * @param resultCode  The integer result code returned by the child activity through its setResult().
	     * @param intent      An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	     */
	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	        LOG.d(TAG, "Incoming Result. Request code = " + requestCode);
	        super.onActivityResult(requestCode, resultCode, intent);
	        cordovaInterface.onActivityResult(requestCode, resultCode, intent);
	    }

	    /**
	     * Report an error to the host application. These errors are unrecoverable (i.e. the main resource is unavailable).
	     * The errorCode parameter corresponds to one of the ERROR_* constants.
	     *
	     * @param errorCode   The error code corresponding to an ERROR_* value.
	     * @param description A String describing the error.
	     * @param failingUrl  The url that failed to load.
	     */
	    public void onReceivedError(final int errorCode, final String description, final String failingUrl) {
	        final CordovaExtActivity me = this;

	        // If errorUrl specified, then load it
	        final String errorUrl = preferences.getString("errorUrl", null);
	        if ((errorUrl != null) && (!failingUrl.equals(errorUrl)) && (appView != null)) {
	            // Load URL on UI thread
	            me.runOnUiThread(new Runnable() {
	                public void run() {
	                    me.appView.showWebPage(errorUrl, false, true, null);
	                }
	            });
	        }
	        // If not, then display error dialog
	        else {
	            final boolean exit = !(errorCode == WebViewClient.ERROR_HOST_LOOKUP);
	            me.runOnUiThread(new Runnable() {
	                public void run() {
	                    if (exit) {
	                        me.appView.getView().setVisibility(View.GONE);
	                        me.displayError("Application Error", description + " (" + failingUrl + ")", "OK", exit);
	                    }
	                }
	            });
	        }
	    }

	    /**
	     * Display an error dialog and optionally exit application.
	     */
	    public void displayError(final String title, final String message, final String button, final boolean exit) {
	        final CordovaExtActivity me = this;
	        me.runOnUiThread(new Runnable() {
	            public void run() {
	                try {
	                    AlertDialog.Builder dlg = new AlertDialog.Builder(me);
	                    dlg.setMessage(message);
	                    dlg.setTitle(title);
	                    dlg.setCancelable(false);
	                    dlg.setPositiveButton(button,
	                            new AlertDialog.OnClickListener() {
	                                public void onClick(DialogInterface dialog, int which) {
	                                    dialog.dismiss();
	                                    if (exit) {
	                                        finish();
	                                    }
	                                }
	                            });
	                    dlg.create();
	                    dlg.show();
	                } catch (Exception e) {
	                    finish();
	                }
	            }
	        });
	    }

	    /*
	     * Hook in Cordova for menu plugins
	     */
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        if (appView != null) {
	            appView.getPluginManager().postMessage("onCreateOptionsMenu", menu);
	        }
	        return super.onCreateOptionsMenu(menu);
	    }

	    @Override
	    public boolean onPrepareOptionsMenu(Menu menu) {
	        if (appView != null) {
	            appView.getPluginManager().postMessage("onPrepareOptionsMenu", menu);
	        }
	        return true;
	    }

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        if (appView != null) {
	            appView.getPluginManager().postMessage("onOptionsItemSelected", item);
	        }
	        return true;
	    }

	    /**
	     * Called when a message is sent to plugin.
	     *
	     * @param id   The message id
	     * @param data The message data
	     * @return Object or null
	     */
	    public Object onMessage(String id, Object data) {
	        if ("onReceivedError".equals(id)) {
	            JSONObject d = (JSONObject) data;
	            try {
	                this.onReceivedError(d.getInt("errorCode"), d.getString("description"), d.getString("url"));
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }
	        } else if ("exit".equals(id)) {
	            finish();
	        }
	        return null;
	    }

	    protected void onSaveInstanceState(Bundle outState) {
	        cordovaInterface.onSaveInstanceState(outState);
	        super.onSaveInstanceState(outState);
	    }

	    /**
	     * Called by the system when the device configuration changes while your activity is running.
	     *
	     * @param newConfig The new device configuration
	     */
	    @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	        super.onConfigurationChanged(newConfig);
	        if (this.appView == null) {
	            return;
	        }
	        PluginManager pm = this.appView.getPluginManager();
	        if (pm != null) {
	            pm.onConfigurationChanged(newConfig);
	        }
	    }

	    /**
	     * FIXME: add @TargetApi(23)
	     * 	remove override
	     * Called by the system when the user grants permissions
	     *
	     * @param requestCode
	     * @param permissions
	     * @param grantResults
	     */
	    @TargetApi(23)
	    public void onRequestPermissionsResult(int requestCode, String permissions[],
	                                           int[] grantResults) {
	        try
	        {
	            cordovaInterface.onRequestPermissionResult(requestCode, permissions, grantResults);
	        }
	        catch (JSONException e)
	        {
	            LOG.d(TAG, "JSONException: Parameters fed into the method are not valid");
	            e.printStackTrace();
	        }

	    }

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			return super.onKeyDown(keyCode, event);
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			// TODO Auto-generated method stub

			if (appView != null && event.getAction() == KeyEvent.ACTION_DOWN){
				int keyCode = 0;
				if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP){
					keyCode = 38;
				}else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN){
					keyCode = 40;
				}
				if(keyCode > 0 && mMachineName != null && ( mMachineName.equals("rtd299x_tv010_4k") || mMachineName.equals("h25ref"))){
					Log.i(TAG,"CordovaExtActivity dispatchKeyEvent keyCode = " + keyCode);
	                appView.loadUrlIntoView(
	                        "javascript:(function(){var ev=document.createEvent('HTMLEvents');ev.which=ev.keyCode=" +
	                        		keyCode + ";ev.initEvent('" + "keydown" +
	                                "',true, true);document.body.dispatchEvent(ev);})()", false);
	                return true;
				}
			}
			
			return super.dispatchKeyEvent(event);
		}

		@Override
		public void OnThemeChanged() {
			// TODO Auto-generated method stub
			if(mTextView != null)
				mTextView.setTextColor(getResources().getColor(R.color.c_4));
	    	String url = mOriginalUrl;
	    	if(url != null){
	    		appView.loadUrlIntoView(getThemeUrl(url), false);
		        appView.clearHistory();
	    	}
		}

}
