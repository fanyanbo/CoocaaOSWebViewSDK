package com.coocaa.webviewsdk.version;

/*version 2.0.0 
 * fix bug:
 * 1.android-21版本及以上，http和https混合访问受限制的问题；
 * 2.apk release模式下，访问https受限制的问题；
 */
/*version 2.1.0 
 * 1.随酷开系统6.0增加主题
 */
/*version 2.2.0 
 * 1.增加获取影视信息接口
 * 2.增加获取应用圈信息接口
 * 3.增加获取主题接口
 * 4.增加获取WebViewSDK信息接口
 * 5.增加设置web页面焦点边界位置的接口
 */
/*version 2.2.1 
 * 1.增加404错误页面通知
 * 2.增加消息通知接口
 */
/*version 2.2.3
 * 1.修复调用酷开商城详情页失败的问题， 参数默认都解析成String
 * 2.整合支付模块,注：web启动支付页面的参数有调整
 * 3.在web页面下响应遥控器蓝牙按键
 * 4.集成通过Android通道提交web页面日志
 * 5.webview水平方向不支持滚动条
 */
/*version 2.2.4
 * 1.优化当不支持系统主题时url拼接的问题
 */
/*version 2.2.5
 * 1.加载url资源时增加不同的缓存模式
 */
/*version 2.2.6
 * 1.在web页面下响应菜单长按键
 * 2.修复loading状态下webview信息显示的问题
 * 3.增加UI操作通知接口
 */
/*version 2.2.7
 * 1.在web页面下响应菜单长按键
 * 2.修复loading状态下webview信息显示的问题
 * 3.增加UI操作通知接口
 * 4.在框架中增加监听主页按键的逻辑，可以实现屏蔽主页键
 */
/*version 2.2.8
 * 1.按返回键时，分别发送keydown和keyup事件到web页面
 */
/*version 2.3.0
 * 1.重构了ipc通信的初始化过程
 * 2.整合了支付相关代码
 */
/*version 2.3.1
 * 1.增加了loadUrl的重载
 * 2.获取设备信息内容中增加品牌信息
 * 3.修复了无法发送pause事件消息到js的问题
 */
/*version 2.3.2
 * 1.规范Log打印
 * 2.增加设置UserAgent模式的接口，解决浏览站点显示异常的问题
 * 3.增加页面加载的超时时长
 */
/*version 2.3.3
 * 1.解决了RTK Android4.2 E710U不响应上下键焦点切换的问题
 */
/*version 2.3.4
 * 1.增加了调用本地播放的接口
 * 2.启动其他应用时可以带intent参数对象
 */
/*version 2.3.5
 * 1.增加获取系统属性值得接口
 * 2.增加获取app版本信息接口
 */
/*version 2.3.6
 * 1.调用start接口时增加设置uri和type的启动方式
 */
/*version 2.3.7
 * 1.优化初始化时序，提前显示loading
 * 2.去掉14u的特殊处理
 */
/*version 2.3.8
 * 1.增加设置webview显示策略的接口
 * 2.优化当页面加载失败的处理逻辑
 * 3.适配调用在线播放接口的处理逻辑
 */
/*version 2.3.9
 * 1.替换错误页的图标
 * 2.替换MActivity的图标
 */
/*version 2.3.10
 * 1.增加动态设置全屏背景是否显示的接口
 * 2.封装发送广播通知Web前端的接口
 * 3.完善日志提交接口
 * 4.第二次调用url时，不重复加载插件
 */
/*version 2.3.11
 * 1.增加网络断开事件监听
 */
/*version 2.3.12
 * 1.增加退出酷开账号登录的接口
 */
/*version 2.5.0
 * 1.增加提交活动任务数据接口
 */

public class SystemWebViewSDK {

	public static final String versionName = "2.5.0";
	
	public static final int versionCode = 2050000;
	
	private static int focusPosition = -1;
	
	public static String getVersionName()
	{
		return versionName;
	}
	
	public static int getVersionCode()
	{
		return versionCode;
	}
	
	public static void setFocusPosition(int param)
	{
		focusPosition = param;
	}
	
	public static int getFocusPosition()
	{
		return focusPosition;
	}
}
