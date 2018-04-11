# CoocaaOSWebViewSDK

- **概述**
 > SystemWebViewSDK，它封装了Cordova WebApp框架，并扩展功能插件深度对接到酷开系统，依赖该SDK的Android App具备Web JS与酷开系统 Native相互通信的能力，从而能够开发出基于酷开系统的功能强大的混合应用。

- **功能描述**
 > SystemWebViewSDK，它允许Web页面访问和使用酷开系统的原生接口的功能；实现Web页面与酷开系统的无缝对接。

- **版本描述**
 
     > v1.0.0 
     > * 初始版本
     > <br/>
     
     > v2.0.0 
     > * fix bug<br/>
     > * android-21版本及以上，http和https混合访问受限制的问题<br/>
     > * apk release模式下，访问https受限制的问题<br/>
     > <br/>

     > v2.1.0 
     > * 随酷开系统6.0增加主题<br/>
     > <br/>

     > v2.2.0 
     > * 增加获取影视信息接口<br/>
     > * 增加获取应用圈信息接口<br/>
     > * 增加获取主题接口<br/>
     > * 增加获取WebViewSDK信息接口<br/>
     > * 增加设置web页面焦点边界位置的接口<br/>
     > <br/>

     > v2.2.1 
     > * 增加404错误页面通知<br/>
     > * 增加消息通知接口<br/>
     > <br/>

     > v2.2.3 
     > * 修复调用酷开商城详情页失败的问题， 参数默认都解析成String<br/>
     > * 整合支付模块,注：web启动支付页面的参数有调整<br/>
     > * 在web页面下响应遥控器蓝牙按键<br/>
     > * 集成通过Android通道提交web页面log<br/>
     > * webview水平方向不支持滚动条<br/>
     > <br/>

     > v2.2.4-v2.2.8
     > * 解决多页面跳转闪背景的问题<br/>
     > * url为空时，显示自定义错误页面<br/>
     > * 加载url资源时增加不同的缓存模式<br/>
     > * 在框架中增加监听主页按键的逻辑，可以实现屏蔽主页键<br/>
     > * 在web页面下响应菜单长按键<br/>
     > * 修复loading状态下webview信息显示的问题<br/>
     > * 优化当不支持系统主题时url拼接的问题<br/>
     > * 增加UI操作通知接口<br/>
     > * 按返回键时，分别发送keydown和keyup事件到web页面<br/>
     > <br/>

     > v2.3.0
     > * 重构了ipc通信的初始化过程<br/>
     > * 更新了支付相关代码<br/>

- **Licensing**
 > 此SDK为酷开系统的一部分

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

- **使用到的第三方库/版本／LICENSE说明**
 > 采用了Cordova框架，基于原始Cordova框架之上做了修改，原始Cordova框架遵循 Apache License Version 2.0 开放源代码授权协议。

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
