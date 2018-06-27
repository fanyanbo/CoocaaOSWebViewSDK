# CoocaaOSWebViewSDK【封装View功能】

- **概述**
 > 该SDK是基于Apache Cordova WebApp开源框架进行的一层封装，并扩展了若干个功能插件深度对接酷开系统，将酷开系统的能力提供给Web使用，它是基于酷开系统Web前端开发的核心支撑组件。

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

### Api文档 (`该文档供Web前端开发参考使用，Android App集成该SDK的说明请见下文`)

* 该SDK提供给Web端的具体能力如下：
> * 提供获取酷开系统相关信息的能力（如ip信息，设备信息，定位信息，账户信息，app信息等) <br/>
> * 提供提交日志信息到酷开大数据后台的能力（自定义事件，页面曝光时长）<br/>
> * 提供发送自定义消息到Android Native的能力 <br/>
> * 提供监听通用状态变化的能力（播放状态、网络状态、外接设备插拔状态等）<br/>
> * 提供监听特殊键值按键的能力 (返回键、主页键等）<br/>
> * 提供监听特殊事件发生的能力 (监听Android Activity生命周期的resume和pause事件) <br/>

> ``Web前端集成和功能使用文档:`` [链接地址](https://github.com/xavier0509/webDocument/blob/master/web%E6%8E%A5%E5%8F%A3%E9%9B%86%E6%88%90%E8%B0%83%E7%94%A8.md) <br/>
> ``Web前端打包与发布文档:`` [链接地址](https://github.com/xavier0509/webDocument/blob/master/web--fis.md)


## Android App集成文档

### 无论是以继承Activity的方式还是用View的方式，Android App一定要集成酷开系统IPC通讯框架，否则该SDK无法正常加载需要与酷开系统进行通信的Web页面
- **继承Activity的方式**
* `项目工程依赖SkySDK` 
* `继承SkyApplication` <br/>
* `修改AndroidManifest.xml文件` <br/>
- **代码示例**
```
//MainApplication.java文件
public class MainApplication extends SkyApplication{

    private static final String mTag = "WebViewSDK";

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.i(mTag,"MainApplication onCreate");
    }
}

//AndroidManifest.xml文件，仅修改android:name的名称
    <application
        android:name=".MainApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
	...    
```

### 以View的方式进行集成

* `提供该集成方式的目的是能够进行预加载，在隐藏加载好web页面，在需要的时候显示，以改善用户体验` 
* `只支持在Activity环境中使用，暂不支持在dialog环境中使用` <br/>
* `Android App需基于酷开系统ipc通信框架，否则web前端无法对接到酷开系统能力，仅能展示纯H5页面` <br/>
* `创建view对象后，当不再使用时需显式调用方法进行释放` <br/>
* `在Api文档一节中描述了若干能力给到web端使用，Android App在集成的过程中请参照下面示例代码，否则会遗漏某些能力的实现` <br/>

#### 基本使用

- **配置网络权限**
> * `<uses-permission android:name="android.permission.INTERNET"/>` <br/>

- **创建对象**
> * `CordovaExtWebView mCoocaaWebView = new CordovaExtWebView(this)` <br/>

- **常用方法**
<table>
  <tr>
    <th width=10%, bgcolor=yellow >方法</th>
    <th width=40%, bgcolor=yellow>功能说明</th>
    <th width="50%", bgcolor=yellow>备注</th>
  </tr>
  <tr>
    <td bgcolor=#eeeeee> loadUrl </td>
    <td> 加载链接地址url  </td>
    <td> 网络和本地url均支持，可在线程中调用，参数url, isNeedThemeBg, header</td>
  </tr>
  <tr>
    <td bgcolor=#00FF00> setCoocaaOSConnecter </td>
    <td> 调用该方法后，web前端才能获取酷开系统能力，可自己实现接口，也可使用默认实现 </td>
    <td> 在调用loadUrl前进行设置  </td>
  <tr>
    <td bgcolor=rgb(0,10,0)>setNeedThemeBg </td>
    <td> view是否显示主题背景 </td>
    <td>  在loadUrl前进行设置 </td>
  </tr>
  <tr>
    <td bgcolor=#eeeeee> getStatus </td>
    <td> 获取组件当前状态  </td>
    <td> 0：无加载，1：正在加载web页面，2：加载成功，3：加载失败  </td>
  </tr>
  <tr>
    <td bgcolor=#00FF00>getPageLoadingProgress </td>
    <td> 获取当前页面加载进度 </td>
    <td> 取值范围0~100 </td>
  <tr>
    <td bgcolor=rgb(0,10,0)>setCacheMode </td>
    <td> 设置缓存模式 </td>
    <td>  在loadUrl前进行设置（0:no-cache,1:default,2:cache_only,3:cache_else_network） </td>
  </tr>
  <tr>
    <td bgcolor=#eeeeee> setUserAgentMode </td>
    <td> 设置用户代理模式 </td>
    <td> 在loadUrl前进行设置（0:Android,1:IE9,2:IPad）  </td>
  </tr>
  <tr>
    <td bgcolor=#00FF00>setWebViewDisplayPolicy </td>
    <td> 设置view显示策略 </td>
    <td> 在loadUrl前进行设置（0:100%-display,1:always-display </td>
  <tr>
    <td bgcolor=rgb(0,10,0)>onPause </td>
    <td> 同Android Activity生命周期 </td>
    <td>  建议在Activity生命周期回调中调用对应的方法，否则web前端无法监听到相应事件 </td>
  </tr>
  <tr>
    <td bgcolor=rgb(0,10,0)>onResume </td>
    <td> 同上 </td>
    <td>  同上 </td>
  </tr>
  <tr>
    <td bgcolor=rgb(0,10,0)>onStart </td>
    <td> 同上 </td>
    <td>  同上 </td>
  </tr>
  <tr>
    <td bgcolor=rgb(0,10,0)>onStop </td>
    <td> 同上 </td>
    <td>  同上 </td>
  </tr>
  <tr>
    <td bgcolor=rgb(0,10,0)>onDestroy </td>
    <td> 同上，进行释放处理 </td>
    <td> 同上，请务必调用 </td>
  </tr>
  <tr>
    <td bgcolor=rgb(0,10,0)>setCordovaExtWebViewListener </td>
    <td> 可监听web页面加载开始，加载进度，加载结束，加载错误的事件 </td>
    <td>  </td>
  </tr>
  <tr>
    <td bgcolor=rgb(0,10,0)>setCordovaExtWebViewDataListener </td>
    <td> 可监听消息传递，日志提交的事件 </td>
    <td>  </td>
  </tr>
</table>


- **代码示例**

```
 public class CordovaExtWebViewActivity extends SkyActivity {

    private String mDefaultUrl = "http://beta.webapp.skysrt.com/fyb/webapp/index.html";
    private FrameLayout mMainLayout = null;
    private CordovaExtWebView mCoocaaWebView = null;
    private CoocaaOSConnecter mCoocaaOSConnecter = null;
    private SkyApplication.SkyCmdConnectorListener listener = null;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
	
        initUI();
	
	mCoocaaWebView.loadUrl(mDefaultUrl);
    }

    private void initUI() {

        mMainLayout = new FrameLayout(this);
        mCoocaaWebView = new CordovaExtWebView(this);
        mCoocaaWebView.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams mWebViewLp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mWebViewLp.gravity = Gravity.CENTER_HORIZONTAL;
        if(listener != null){
            mCoocaaOSConnecter = new CoocaaOSConnecterDefaultImpl(listener);
            mCoocaaWebView.setCoocaaOSConnecter(mCoocaaOSConnecter);
        }

        mCoocaaWebView.setCordovaExtWebViewListener(new CordovaExtWebView.CordovaExtWebViewListener() {

            @Override
            public void onPageStarted(String url) {
            }
	    
	    @Override
            public void onPageExit() {
            }
	    
            @Override
            public void onPageFinished(String url) {
            }

            @Override
            public void onPageError(int errorCode, String description, String failingUrl) {
            }

            @Override
            public void onProgressChanged(int process) {
            }
        });

        mCoocaaWebView.setCordovaExtWebViewDataListener(new CordovaExtWebView.CordovaExtWebViewDataListener() {
            @Override
            public void notifyMessage(String data) {
            }

            @Override
            public void notifyLogInfo(String eventId, Map<String, String> map) {
            }

            @Override
            public void notifyPageResume(String pageName, Map<String, String> map) {
            }

            @Override
            public void notifyPagePause(String pageName) {
            }
        });
        mMainLayout.addView(mCoocaaWebView,mWebViewLp);

        FrameLayout.LayoutParams mainLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
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
        listener = this;
    }

    //如果不传递给CoocaaOSConnecter实例对象，web前端将无法接收到酷开系统的相关状态变化，如U盘插拔事件，网络插拔事件等
    @Override
    public byte[] onHandler(String fromtarget, String cmd, byte[] body) {
        return mCoocaaOSConnecter.onHandler(this,fromtarget,cmd,body);
    }

    //其余省略
} 

```

### 以Activity的方式进行集成


> * 新建Activity，继承自 CordovaExtActivity <br/>
> * 在新建的Activity onCreate中调用loadUrl方法即可加载相应Web页面 <br/>
> * Android App集成酷开系统ipc通信框架 <br/>
> * 具备和View集成相同的能力<br/>

<table>
  <tr>
    <th width=40%, bgcolor=yellow > </th>
    <th width=60%, bgcolor=yellow>说明</th>
  </tr>
  <tr>
    <td bgcolor=#eeeeee>loadUrl(String url)</td>
    <td>不带系统背景，采用默认的错误页面背景，错误页上有“去连网”或“刷新试试”按钮 </td>
  </tr>
  <tr>
    <td bgcolor=#00FF00>loadUrl(String url, boolean isNeedBg)</td>
    <td>第二个参数决定是否带系统背景，采用默认的错误页面背景，错误页上有“去连网”或“刷新试试”按钮</td>
  <tr>
    <td bgcolor=rgb(0,10,0)>loadUrl(String url, FrameLayout errorPageBg)</td>
    <td>不带系统背景，第二个参数决定是否采用默认的错误页面背景，错误页上有“去连网”或“刷新试试”按钮</td>
  </tr>
  <tr>
    <td bgcolor=#eeeeee>loadUrl(String url, boolean isNeedBg, boolean isNeedBtn, FrameLayout errorPageBg)</td>
    <td>第二个参数决定是否带系统背景，第三个参数决定是否在错误页上显示“去连网”或“刷新试试”按钮，第四个参数决定是否采用默认的错误页面背景 </td>
  </tr>
</table>

- **代码示例**

```
public class WebViewActivity extends CordovaExtActivity
        implements CordovaExtActivity.CordovaWebViewListener, CordovaExtActivity.CordovaWebViewDataListener, CordovaExtActivity.CordovaErrorPageListener {

    private String mDefaultUrl = "http://beta.webapp.skysrt.com/fyb/webapp/index.html";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setCordovaWebViewListener(this);
        setCordovaWebViewDataListener(this);
        setCordovaErrorPageListener(this);

        LOG.setLogLevel(LOG.VERBOSE);
        setWebViewDisplayPolicy(1);
        setCacheMode(1);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSuperCmdInit() {
        loadUrl(mDefaultUrl);
    }

    @Override
    public void onPageStarted(String url) {
    }

    @Override
    public void onPageFinished(String url) {
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
    }

    @Override
    public void notifyMessage(String data) {
    }

    @Override
    public void notifyLogInfo(String eventId, Map<String, String> map) {
    }

    @Override
    public void notifyPageResume(String pageName, Map<String, String> map) {
    }

    @Override
    public void notifyPagePause(String pageName) {
    }

    @Override
    public void handleUI(String value) {
    }
}
```

- **集成及使用方式(默认支持Windows，请写出对应平台的集成方式(windows/linux/mac))**
 > 如果你的安卓应用需要使用到SystemWebViewSDK：<br/><br/>
 > 1. 如果是系统应用请集成Framework\SkyAndroidLibrary\SystemWebViewSDK里面的工程，该工程是一个Android Library工程。在SDK裁剪了一些功能，也暂不支持view的集成<br/><br/>
 > 2. 如果是独立应用，需要依赖 gogs/Web-X/SystemWebViewSDK工程<br/><br/>
 > 3. SystemWebViewSDK依赖SystemWebViewSDKExtra Java Jar工程，它分了4.4 5.0 6.0几个版本，是用来解决在不同安卓版本上系统自动编译的问题，自己编译时，请直接采用6.0的版本即可。<br/><br/>
 > 3. 在工程配置中SystemWebViewSDK需要依赖SkySDK，CommonUISDK，SystemServiceSDK，UserServiceSDK，SkyThemeSDK，SystemWebViewSDKExtra等sdk<br/><br/>

- **其他**
 > 无 
