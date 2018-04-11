# CoocaaOSWebViewSDK【feature-20180309分支】

- **概述**
 > 该SDK是基于Apache Cordova WebApp开源框架进行的一层封装，并扩展了若干个功能插件深度对接酷开系统，将酷开系统的能力提供给Web使用，是基于酷开系统的Web前端开发的核心支撑组件。

- **功能描述**
 > Android App集成该SDK后具备展示web页面的能力，同时Web前端页面能够通过该SDK深度对接到酷开系统，使用酷开系统提供的各项能力，从而能够开发基于酷开系统的功能强大的混合应用。
 > 该SDK对于Android App使用而言，提供了Activity和View两种集成方式。

- **版本描述**
 
>  v2.4.0 【历史版本描述参见master分支README.md】
> * 对Android App提供View的集成方式<br/>
 
 - **使用到的第三方库/版本/LICENSE说明**
 > 此SDK为酷开系统的一部分, 采用了Cordova框架，基于原始Cordova框架之上做了修改，原始Cordova框架遵循 Apache License Version 2.0 开放源代码授权协议。

- **联系方式**
 > 樊彦博  电子邮件：fanyanbo@skyworth.com

- **API文档** (可以挂超链接另写，也可以写在此处，也可以生成 htmldocument 提交)
 > 使用方法：<br/>
 > -- 
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
