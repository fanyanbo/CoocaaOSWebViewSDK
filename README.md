# CoocaaOSWebViewSDK【feature-20180309分支】

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

### Api文档 (`该文档供Web前端开发参考，Android App集成该SDK的文档请见下面说明`)

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
    <td> 网络和本地url均支持，可在线程中调用  </td>
  </tr>
  <tr>
    <td bgcolor=#00FF00> setCoocaaOSConnecter </td>
    <td> 调用该方法后，web前端才能获取酷开系统能力，可自己实现接口，也可使用默认实现 </td>
    <td> 在调用loadUrl前进行设置  </td>
  <tr>
    <td bgcolor=rgb(0,10,0)>setThemeBg </td>
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
        if(listener != null)
            mCoocaaWebView.setCoocaaOSConnecter(new CoocaaOSConnecterDefaultImpl(listener));

        mCoocaaWebView.setCordovaExtWebViewListener(new CordovaExtWebView.CordovaExtWebViewListener() {

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

    @Override
    public byte[] onHandler(String fromtarget, String cmd, byte[] body) {
        return new byte[0];
    }

    //其余省略
} 

```

### 以Activity的方式进行集成


> * 新建Activity，继承自 CordovaExtActivity <br/>
> * 在新建的Activity onCreate中调用loadUrl方法即可加载相应Web页面 <br/>
> * Android App集成酷开系统ipc通信框架 <br/>
> * 具备和View集成相同的能力<br/>


     > loadUrl介绍

 > * public void loadUrl(String url)<br/>
:加载web页面，不带系统背景，采用默认的错误页面背景，错误页上有“去连网”或“刷新试试”按钮
 > * loadUrl(String url, boolean isNeedBg)<br/>
;加载web页面，第二个参数决定是否带系统背景，采用默认的错误页面背景，错误页上有“去连网”或“刷新试试”按钮
 > * public void loadUrl(String url, FrameLayout errorPageBg)<br/>
；加载web页面，不带系统背景，第二个参数决定是否采用默认的错误页面背景，错误页上有“去连网”或“刷新试试”按钮 -->
 > * public void loadUrl(String url, boolean isNeedBg, boolean isNeedBtn, FrameLayout errorPageBg)<br/>
<!-- 加载web页面，第二个参数决定是否带系统背景，第三个参数决定是否在错误页上显示“去连网”或“刷新试试”按钮，第四个参数决定是否采用默认的错误页面背景 -->

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
 > 1. 请集成Framework\SkyAndroidLibrary\SystemWebViewSDK里面的工程，该工程是一个Android Library工程。<br/><br/>
 > 2. 需要依赖 Framework\SkyJavaLibrary\SystemWebViewSDKExtra\6.0 下的Java Jar工程，这个工程是用来解决一些不同安卓版本上在服务器上编译问题而设立的，自己编译时，请直接采用6.0的版本即可。<br/><br/>
 > 3. SystemWebViewSDK另外需要依赖以下SDK，请添加：SkySDK，CommonUISDK，SystemServiceSDK，UserServiceSDK，SkyThemeSDK，SystemWebViewSDKExtra。<br/><br/>

- **其他**
 > 无 
