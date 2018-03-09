package org.apache.cordova;

import java.util.ArrayList;

import org.apache.cordova.CordovaExtActivity.CordovaWebViewListener;
import org.apache.cordova.CordovaInterfaceImpl.CordovaInterfaceListener;

import com.coocaa.systemwebview.R;
import com.skyworth.ui.api.SkyWithBGLoadingView;
import com.skyworth.ui.blurbg.BlurBgLayout;
import com.skyworth.util.SkyScreenParams;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CordovaExtWebView extends FrameLayout
{
	public static String TAG = "WebViewSDK";
	private Context mContext;
	
	// The webview for our app
	protected CordovaWebView appView;
    // Read from config.xml:
    protected CordovaPreferences preferences;
    protected ArrayList<PluginEntry> pluginEntries;
    protected CordovaInterfaceImpl cordovaInterface;
    
    protected boolean keepRunning = true;
    protected boolean isNeedThemeBg = false;
    protected boolean isNeedErrorPageBtn = true;
    protected FrameLayout mainLayout = null;
    protected FrameLayout mErrorPageLayout = null;
	protected SkyWithBGLoadingView mLoadingView = null;
    protected BlurBgLayout mThemeBgLayout = null;
    protected BlurBgLayout mErrorPageBgLayout = null;
    protected TextView mErrorPageTextView = null;
    protected Button mErrorPageBtnView = null;
    protected ImageView mErrorPageImageView = null;
    protected String mCurRequstUrl = null;
    
    private final int ERROR_DISCONNECT = 1;
    private final int ERROR_SIGNALWEAK = 2;
    private long mEndTime = 0, mStartTime = 0;
	
    public CordovaWebViewListener mListener = null;
    
	public CordovaExtWebView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		
		loadConfig();
		
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
						if(title != null){
							if(title.contains("404")){
								if(mListener != null)
									mListener.onPageError(404, "404 Not Found", mCurRequstUrl);
								isNeedErrorPageBtn = false;
								showErrorPage(ERROR_SIGNALWEAK);
							}
						}
					}

					@Override
					public void onReceivedIcon(Bitmap icon) {

					}

					@Override
					public void onReceivedError(final int errorCode,
							String description, String failingUrl) {
						Log.v(TAG,"CordovaWebView onReceivedError description = " + description + ",errorCode = " + errorCode);
						if(mListener != null)
							mListener.onPageError(errorCode, description, failingUrl);
						
						mStartTime = SystemClock.uptimeMillis();
						Log.i(TAG,"onReceivedError mStartTime="+mStartTime);
						
						if(errorCode == -2){
							showErrorPage(ERROR_DISCONNECT);
						}else{
							showErrorPage(ERROR_SIGNALWEAK);
						}
					}

					@Override
					public void onProgressChanged(int process) {
						Log.v(TAG,"CordovaWebView onProgressChanged process == "+ process);
					}

					@Override
					public void onPageStarted(String url) {
						Log.v(TAG,"CordovaWebView onPageStarted url == "+ url);
						
						mCurRequstUrl = url;
						
						mEndTime = SystemClock.uptimeMillis();
						Log.i(TAG,"onPageStarted (mEndTime - mStartTime)="+(mEndTime - mStartTime));
						if((mEndTime - mStartTime) < 500l) return;
						
						if(mListener != null)
							mListener.onPageStarted(url);
						
						if(mLoadingView!=null)
							mLoadingView.showLoading();
					}

					@Override
					public void onPageLoadingFinished(String url) {
						Log.v(TAG,"CordovaWebView onPageLoadingFinished url == "+ url);
						
						mEndTime = SystemClock.uptimeMillis();
						Log.i(TAG,"onPageLoadingFinished (mEndTime - mStartTime)="+(mEndTime - mStartTime));
						if((mEndTime - mStartTime) < 520l) return;
						
						if(mListener != null)
							mListener.onPageFinished(url);
						
						if(mLoadingView!=null)
							mLoadingView.dismissLoading();
					}

					@Override
					public void doUpdateVisitedHistory(String url,
							boolean isReload) {
					}
				});

	    init();   
	}
	
	public void setListener(CordovaWebViewListener listener)
	{
		Log.i(TAG,"CordovaWebView----->setListener");
		mListener = listener;
	}

	OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.i(TAG, "CordovaExtActivity OnClickListener "
					+ v.getTag().toString());
		}
	};
	
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

        // If keepRunning
        this.keepRunning = preferences.getBoolean("KeepRunning", true);

        appView.loadUrlIntoView(url, true);
    }
    
    protected CordovaInterfaceImpl makeCordovaInterface() {    	
        return new CordovaInterfaceImpl((Activity)mContext);
    }

    protected void init() {
        appView = makeWebView();
        createViews();
        if (!appView.isInitialized()) {
            appView.init(cordovaInterface, pluginEntries, preferences,0);
        }
        cordovaInterface.onCordovaInit(appView.getPluginManager());
    }
    
    protected void createViews() {
    	
    	mainLayout = new FrameLayout(mContext);
    	
        if(isNeedThemeBg){
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
        
        mLoadingView = new SkyWithBGLoadingView(mContext);
        FrameLayout.LayoutParams loading_p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        loading_p.gravity = Gravity.CENTER;
        mainLayout.addView(mLoadingView, loading_p);
        
        this.addView(mainLayout,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
    }
    
    protected boolean initErrorPage(int errorType) {
    	
    	boolean isInitThis = false;
    	
    	if(mErrorPageBgLayout == null) {
            mErrorPageLayout = new FrameLayout(mContext);
            mErrorPageBgLayout = new BlurBgLayout(mContext);
            mErrorPageBgLayout.setPageType(BlurBgLayout.PAGETYPE.SECONDE_PAGE);
            mErrorPageBgLayout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    		mErrorPageLayout.addView(mErrorPageBgLayout);
    		
    		double heightRate = getHeight() / 1080.0;		
    		Log.i(TAG, "-------------->width = " + getWidth() + ", height = " +getHeight() + ",heightRate = " + heightRate);
    		
    		mErrorPageImageView = new ImageView(mContext);			
			FrameLayout.LayoutParams imgViewLp = new FrameLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			imgViewLp.gravity = Gravity.CENTER_HORIZONTAL;
			imgViewLp.topMargin = SkyScreenParams.getInstence(mContext).getResolutionValue((int)(300*heightRate));
			
			mErrorPageTextView = new TextView(mContext);
			mErrorPageTextView.setTextSize(SkyScreenParams.getInstence(mContext).getTextDpiValue(36));
			mErrorPageTextView.setTextColor(getResources().getColor(R.color.c_4));	
			FrameLayout.LayoutParams txtViewLp = new FrameLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			txtViewLp.gravity = Gravity.CENTER_HORIZONTAL;
			txtViewLp.topMargin = SkyScreenParams.getInstence(mContext).getResolutionValue((int)(552*heightRate));
	
			mErrorPageBtnView = new Button(mContext);
			mErrorPageBtnView.setFocusable(true);
			mErrorPageBtnView.setFocusableInTouchMode(true);
			mErrorPageBtnView.setTextSize(SkyScreenParams.getInstence(mContext).getTextDpiValue(30));
			mErrorPageBtnView.setTextColor(Color.BLACK);
			mErrorPageBtnView.setOnClickListener(clickListener);
			mErrorPageBtnView.setBackgroundResource(R.drawable.ui_sdk_btn_focus_shadow_bg);
			mErrorPageBtnView.requestFocus();
			FrameLayout.LayoutParams btnViewLp = new FrameLayout.LayoutParams(
					SkyScreenParams.getInstence(mContext).getResolutionValue(410), SkyScreenParams.getInstence(mContext).getResolutionValue(238));
			btnViewLp.gravity = Gravity.CENTER_HORIZONTAL;
			btnViewLp.topMargin = SkyScreenParams.getInstence(mContext).getResolutionValue((int)(575*heightRate));
			
			mErrorPageLayout.addView(mErrorPageImageView,imgViewLp);
			mErrorPageLayout.addView(mErrorPageTextView,txtViewLp);	
			mErrorPageLayout.addView(mErrorPageBtnView,btnViewLp);
			
			if(mErrorPageBtnView != null && !isNeedErrorPageBtn)
				mErrorPageBtnView.setVisibility(View.GONE);
			
			isInitThis = true;
		}
		
		switch(errorType){
		case 1:
			mErrorPageTextView.setText(R.string.error_webview_netdisconnect);
			mErrorPageImageView.setImageResource(R.drawable.new_disconnect);
			mErrorPageBtnView.setText(R.string.error_webview_reset);
			mErrorPageBtnView.setTag("1");
			break;
		case 2:
		default:
			mErrorPageTextView.setText(R.string.error_webview_neterror);
			mErrorPageImageView.setImageResource(R.drawable.new_refresh);
			mErrorPageBtnView.setText(R.string.error_webview_refresh);
			mErrorPageBtnView.setTag("2");
			break;
		}
		
		return isInitThis;
    }
    
    protected void showErrorPage(int errorType) {
    	
    	boolean isInit = initErrorPage(errorType);
    	if(isInit) {
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mainLayout.addView(mErrorPageLayout, lp);
		}
		else {
			mErrorPageLayout.setVisibility(View.VISIBLE);
		}
    	
    	mErrorPageBtnView.requestFocus();
    }
    
	protected void hideErrorPage() {
		
		if(mErrorPageLayout!=null)
			mErrorPageLayout.setVisibility(View.INVISIBLE);
	}
    
    protected CordovaWebView makeWebView() {
        return new CordovaWebViewImpl(makeWebViewEngine());
    }

    protected CordovaWebViewEngine makeWebViewEngine() {
        return CordovaWebViewImpl.createEngine(mContext, preferences);
    }
    
    public void onCordovaWebViewPause()
    {
        if (this.appView != null) {
            // CB-9382 If there is an activity that started for result and main activity is waiting for callback
            // result, we shoudn't stop WebView Javascript timers, as activity for result might be using them
            boolean keepRunning = this.keepRunning || this.cordovaInterface.activityResultCallback != null;
            this.appView.handlePause(keepRunning);
        }
    }
    
    public void onCordovaWebViewResume()
    {
        if (this.appView == null) {
            return;
        }

        this.appView.handleResume(this.keepRunning);
        
        this.appView.getView().setVisibility(View.VISIBLE);
    }
    
    public void onCordovaWebViewStart()
    {
        if (this.appView == null) {
            return;
        }
        this.appView.handleStart();
    }
    
    public void onCordovaWebViewStop()
    {
        if (this.appView == null) {
            return;
        }
        this.appView.handleStop();
    }
    
    public void onCordovaWebViewDestroy()
    {
        if (this.appView != null) {
            appView.handleDestroy();
        }
    }
    
    public void setThemeBg(boolean value) {
    	isNeedThemeBg = value;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
