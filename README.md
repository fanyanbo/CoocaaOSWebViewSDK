# CoocaaOSWebViewSDK【feature-20180309分支】

- **概述**
 > 该SDK是基于Apache Cordova WebApp开源框架进行的一层封装，并扩展了若干个功能插件深度对接酷开系统，将酷开系统的能力提供给Web使用，是基于酷开系统的Web前端开发的核心支撑组件。

- **功能描述**
 > Android App集成该SDK后具备展示web页面的能力，同时Web前端页面能够通过该SDK深度对接到酷开系统，使用酷开系统提供的各项能力，从而能够开发基于酷开系统的功能强大的混合应用。
 > 该SDK对于Android App使用而言，提供了Activity和View两种集成方式。

- **版本描述**
 
>  **v2.4.0** 【历史版本描述参见master分支README.md】
> * 对Android App提供View的集成方式<br/>
 
 - **使用到的第三方库/版本/LICENSE说明**
 > 此SDK为酷开系统的一部分, 采用了Cordova框架，基于原始Cordova框架之上做了修改，原始Cordova框架遵循 Apache License Version 2.0 开放源代码授权协议。

- **联系方式**
 > 樊彦博  电子邮件：fanyanbo@skyworth.com

- **API文档** (`该文档供Web前端开发参考，Android App集成该SDK的文档请见下面说明`)

> * 提供获取酷开系统相关信息的能力（如ip信息，设备信息，定位信息，账户信息，app信息等) <br/>
> * 提供提交日志信息到酷开大数据后台的能力（自定义事件，页面曝光时长）<br/>
> * 提供发送自定义消息到Android Native的能力 <br/>
> * 提供监听通用状态变化的能力（播放状态、网络状态、外接设备插拔状态等）<br/>
> * 提供监听特殊键值按键的能力 (返回键、主页键等）<br/>
> * 提供监听特殊事件发生的能力 (监听Android Activity生命周期的resume和pause事件) <br/>

> ``Web前端集成和调用说明请详见:`` [文档链接](http://www.baidu.com/)


## Android App集成文档

### 以View的方式进行集成

* `提供该集成方式的目的是能够进行预加载，在隐藏加载好web页面，在需要的时候显示，以改善用户体验` 
* `只支持在Activity环境中使用，暂不支持在dialog环境中使用` <br/>
* `Android App需基于酷开系统ipc通信框架，否则web前端无法对接到酷开系统能力，仅能展示纯H5页面` <br/>
* `创建view对象后，当不再使用时需显式调用方法进行释放` <br/>

#### 基本使用
* 配置网络权限
<uses-permission android:name="android.permission.INTERNET"/>
* 创建对象	
CordovaExtWebView mCoocaaWebView = new CordovaExtWebView(this);
* 常用方法
void loadUrl(String url): 加载链接地址url，网络和本地url均支持
void setCoocaaOSConnecter(CoocaaOSConnecter connecter)：在loadUrl前进行设置，调用该方法后，web前端才能获取酷开系统能力，可自己实现接口，也可使用默认实现
void setThemeBg(boolean value)：在loadUrl前进行设置，view是否显示主题背景
int getStatus()：获取组件当前状态（0：无加载，1：正在加载web页面，2：加载成功，3：加载失败）
int getPageLoadingProgress()：获取当前加载进度（取值范围0~100）
void setCacheMode(int value)：设置缓存模式，在loadUrl前进行设置（0:no-cache,1:default,2:cache_only,3:cache_else_network）
void setUserAgentMode(int value)：设置用户代理模式，在loadUrl前进行设置（0:Android,1:IE9,2:IPad）
void setWebViewDisplayPolicy(int value)：设置view显示策略，在loadUrl前进行设置（0:100%-display,1:always-display）
void onPause()：同Android Activity生命周期，建议在Activity生命周期回调中调用对应的方法，否则web前端无法监听到相应事件
void onResume()：同上
void onStart()：同上
void onStop()：同上
void onDestroy()：进行一些释放处理，请务必调用
void setCordovaExtWebViewListener(CordovaExtWebViewListener listener)：设置监听，可监听web页面加载开始，加载进度，加载结束，加载错误的事件
void setCordovaExtWebViewDataListener(CordovaExtWebViewDataListener listener)：设置监听，可监听消息传递，日志提交的事件


> 代码示例

```
 public class CordovaExtWebViewActivity extends SkyActivity {

    private String mDefaultUrl = "http://beta.webapp.skysrt.com/fyb/webapp/index.html";
    private FrameLayout mMainLayout = null;
    private SkyWithBGLoadingView mLoadingView = null;
    private CordovaExtWebView mCoocaaWebView = null;
    private SkyApplication.SkyCmdConnectorListener listener = null;
    private final static String mTag = "WebViewSDK";

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(mTag, "CordovaExtWebViewActivity onCreate threadId = " + android.os.Process.myTid());

        initUI();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { //web页面加载后将接收不到事件回调

        Log.i(mTag, "onKeyDown keyCode = " + keyCode + ",event = " + event.getAction() + ",tid = " + android.os.Process.myTid());
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_1:
                mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/appstore/webxtest/test7/test.html");
                break;
            case KeyEvent.KEYCODE_2:
                mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/lxw/ceshi/nativeinfo2/index.html");
                break;
            case KeyEvent.KEYCODE_3:
                mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/lxw/guide2/index.html");
                break;
            case KeyEvent.KEYCODE_4:
                mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/lxw/ceshi/nfc2/index.html");
                break;
            case KeyEvent.KEYCODE_5:
                mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/games/old/log/index.html ");
                break;
            case KeyEvent.KEYCODE_6:
                Log.i(mTag,"getStatus = " + mCoocaaWebView.getStatus());
                break;
            case KeyEvent.KEYCODE_7:
                Log.i(mTag,"getProgress = " + mCoocaaWebView.getPageLoadingProgress());
                break;
            case KeyEvent.KEYCODE_9:
                mCoocaaWebView.loadUrl(mDefaultUrl);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == 1) {
            Log.i(mTag, "dispatchKeyEvent keyCode = " + event.getKeyCode() + ",tid = " + android.os.Process.myTid());
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_1:
                    mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/appstore/webxtest/test7/test.html");
                    break;
                case KeyEvent.KEYCODE_2:
                    mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/lxw/ceshi/nativeinfo2/index.html");
                    break;
                case KeyEvent.KEYCODE_3:
                    mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/lxw/guide2/index.html");
                    break;
                case KeyEvent.KEYCODE_4:
                    mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/lxw/ceshi/nfc2/index.html");
                    break;
                case KeyEvent.KEYCODE_5:
                    mCoocaaWebView.loadUrl("http://beta.webapp.skysrt.com/games/old/log/index.html ");
                    break;
                case KeyEvent.KEYCODE_6:
                    Log.i(mTag,"getStatus = " + mCoocaaWebView.getStatus());
                    break;
                case KeyEvent.KEYCODE_7:
                    Log.i(mTag,"getProgress = " + mCoocaaWebView.getPageLoadingProgress());
                    break;
                case KeyEvent.KEYCODE_9:
                    mCoocaaWebView.loadUrl(mDefaultUrl);
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void initUI() {

        Log.i(mTag,"CordovaExtWebViewActivity initUI threadId = " + android.os.Process.myTid());

        mMainLayout = new FrameLayout(this);

        mCoocaaWebView = new CordovaExtWebView(this);
        mCoocaaWebView.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams mWebViewLp = new FrameLayout.LayoutParams(SkyScreenParams.getInstence(this).getResolutionValue(1800), FrameLayout.LayoutParams.MATCH_PARENT);
        mWebViewLp.gravity = Gravity.CENTER_HORIZONTAL;
        if(listener != null)
            mCoocaaWebView.setCoocaaOSConnecter(new CoocaaOSConnecterDefaultImpl(listener));

        mCoocaaWebView.setCordovaExtWebViewListener(new CordovaExtWebView.CordovaExtWebViewListener() {

            @Override
            public void onPageStarted(String url) {
                Log.i(mTag,"mCoocaaWebView onPageStarted url = " + url);
            }

            @Override
            public void onPageFinished(String url) {
                Log.i(mTag,"mCoocaaWebView onPageFinished url = " + url);
            }

            @Override
            public void onPageError(int errorCode, String description, String failingUrl) {
                Log.i(mTag,"mCoocaaWebView onReceivedError url = " + failingUrl + ",description = " + description);
            }

            @Override
            public void onProgressChanged(int process) {
                Log.i(mTag,"mCoocaaWebView onProgressChanged process = " + process);
            }
        });

        mCoocaaWebView.setCordovaExtWebViewDataListener(new CordovaExtWebView.CordovaExtWebViewDataListener() {
            @Override
            public void notifyMessage(String data) {

            }

            @Override
            public void notifyLogInfo(String eventId, Map<String, String> map) {
                Log.i(mTag,"notifyLogInfo eventId = " + eventId);
                Log.i(mTag,"notifyLogInfo map = " + map);
            }

            @Override
            public void notifyPageResume(String pageName, Map<String, String> map) {

            }

            @Override
            public void notifyPagePause(String pageName) {

            }
        });
        mMainLayout.addView(mCoocaaWebView,mWebViewLp);

        mLoadingView = new SkyWithBGLoadingView(this);
        FrameLayout.LayoutParams loading_p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        loading_p.gravity = Gravity.CENTER;
        mLoadingView.setLayoutParams(loading_p);
        mLoadingView.setScaleW_H(SkyScreenParams.getInstence(this).getResolutionValue(120),SkyScreenParams.getInstence(this).getResolutionValue(120));
        mMainLayout.addView(mLoadingView);

        FrameLayout.LayoutParams mainLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mMainLayout.setBackgroundColor(Color.DKGRAY);
        setContentView(mMainLayout,mainLp);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mCoocaaWebView != null)
            mCoocaaWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mCoocaaWebView != null)
            mCoocaaWebView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mCoocaaWebView != null)
            mCoocaaWebView.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mCoocaaWebView != null)
            mCoocaaWebView.onDestroy();
    }

    @Override
    public void onCmdConnectorInit() {

        Log.i(mTag,"CordovaExtWebViewActivity onCmdConnectorInit threadId = " + android.os.Process.myTid());
        listener = this;
    }

    @Override
    public byte[] onHandler(String fromtarget, String cmd, byte[] body) {
        return new byte[0];
    }

    @Override
    public void onResult(String fromtarget, String cmd, byte[] body) {

    }

    @Override
    public byte[] requestPause(String fromtarget, String cmd, byte[] body) {
        return new byte[0];
    }

    @Override
    public byte[] requestResume(String fromtarget, String cmd, byte[] body) {
        return new byte[0];
    }

    @Override
    public byte[] requestRelease(String fromtarget, String cmd, byte[] body) {
        return new byte[0];
    }

    @Override
    public byte[] requestStartToVisible(String fromtarget, String cmd, byte[] body) {
        return new byte[0];
    }

    @Override
    public byte[] requestStartToForground(String fromtarget, String cmd, byte[] body) {
        return new byte[0];
    }
}
 
 > 1. 新建Activity，继承自 CordovaExtActivity <br/>
 > 2. 在新建的Activity onCreate中调用loadUrl方法即可加载相应Web页面 <br/>

     > loadUrl介绍
 > -- 
 > * public void loadUrl(String url)<br/>
<!-- 加载web页面，不带系统背景，采用默认的错误页面背景，错误页上有“去连网”或“刷新试试”按钮 -->
 > * loadUrl(String url, boolean isNeedBg)<br/>
<!-- 加载web页面，第二个参数决定是否带系统背景，采用默认的错误页面背景，错误页上有“去连网”或“刷新试试”按钮 -->
 > * public void loadUrl(String url, FrameLayout errorPageBg)<br/>
<!-- 加载web页面，不带系统背景，第二个参数决定是否采用默认的错误页面背景，错误页上有“去连网”或“刷新试试”按钮 -->
 > * public void loadUrl(String url, boolean isNeedBg, boolean isNeedBtn, FrameLayout errorPageBg)<br/>
<!-- 加载web页面，第二个参数决定是否带系统背景，第三个参数决定是否在错误页上显示“去连网”或“刷新试试”按钮，第四个参数决定是否采用默认的错误页面背景 -->

     > CordovaWebViewListener接口介绍
 > -- 
 > * public void onPageStarted(String url);<br/>
<!-- web页面开始加载的消息回调 -->
 > * public void onPageFinished(String url);<br/>
<!-- web页面完成加载的消息回调 -->
 > * public void onPageError(int errorCode, String description, String failingUrl);<br/>
<!-- web页面加载错误时的消息回调  -->

     > CordovaWebPageListener接口介绍
 > -- 
 > * public void notifyMessage(String data);<br/>
 <!-- 来自Web页面的消息通知 -->
 > * public void notifyLogInfo(String eventId, Map<String,String> map);<br/>
 <!-- 来自Web页面的日志信息 -->



- **集成及使用方式(默认支持Windows，请写出对应平台的集成方式(windows/linux/mac))**
 > 如果你的安卓应用需要使用到SystemWebViewSDK：<br/><br/>
 > 1. 请集成Framework\SkyAndroidLibrary\SystemWebViewSDK里面的工程，该工程是一个Android Library工程。<br/><br/>
 > 2. 需要依赖 Framework\SkyJavaLibrary\SystemWebViewSDKExtra\6.0 下的Java Jar工程，这个工程是用来解决一些不同安卓版本上在服务器上编译问题而设立的，自己编译时，请直接采用6.0的版本即可。<br/><br/>
 > 3. SystemWebViewSDK另外需要依赖以下SDK，请添加：SkySDK，CommonUISDK，SystemServiceSDK，UserServiceSDK，SkyThemeSDK，SystemWebViewSDKExtra。<br/><br/>

- **其他**
 > 无 

代码示例

```
public class TestActivity extends CordovaExtActivity implements CordovaWebViewListener, CordovaWebPageListener
{	
	
	public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        SkyThemeEngine.getInstance().registerActivity(this);

        this.setCordovaWebViewListener(this);
        this.setCordovaWebPageListener(this);  
        LOG.setLogLevel(LOG.VERBOSE);

		Log.i("fyb","SystemWebViewSDK versionCode:" + SystemWebViewSDK.getVersionCode());
        
        String url = getIntent().getStringExtra("url");	
		loadUrl(url);
    }

	@Override
	public void onPageStarted(String url) {
		Log.i("fyb","onPageStarted url = " + url);
	}

	@Override
	public void onPageFinished(String url) {
		Log.i("fyb","onPageFinished url = " + url);	
	}

	@Override
	public void onPageError(int errorCode, String description, String failingUrl) {
		Log.i("fyb","onPageError url = " + failingUrl);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();		
		Log.i("fyb","onDestroy");		
		SkyThemeEngine.getInstance().unRegisterActivity(this);
	}

	@Override
	public void notifyMessage(String data) {
		Log.i("fyb","notifyJsMessage data = " + data);
	}

	@Override
	public void notifyLogInfo(String eventId, Map<String, String> map) {
		Log.i("fyb","notifyLogInfo eventId = " + eventId);
		Log.i("fyb","notifyLogInfo map = " + map);
	}
}
```
