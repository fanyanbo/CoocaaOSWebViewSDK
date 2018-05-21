## 如何集成该SDK？

### ``说明文档`` [请猛戳这里](https://github.com/fanyanbo/CoocaaOSWebViewSDK/blob/feature-20180309/README.md)

* 如以view的方式集成，请checkout v2.4.0分支，其他分支暂不支持该功能
  
  
v2.5.2
==================
 * 对接语音模块，使web前端具备监听特定语音口令的能力
 * 增加通道接口，使web前端具备setData和getData的能力(跟具体业务相关)
 * 完善获取appinfo的接口，支持列表参数     
 
v2.5.1
==================
 * fixBug:完善获取激活id的逻辑，防止在5.0系统上出现为空的情况
 * 增加获取内存信息的接口
 * 增加发送语音广播通知Web前端的接口

v2.5.0  【增加了PromotionSDK依赖】
==================
 * 增加提交活动任务数据接口，对接PromotionSDK
 
v2.3.12 
==================
 * 增加退出酷开账号登录的接口（同步接口）
 
v2.3.11 
==================
 * 增加网络断开事件监听
 
v2.3.10 
==================
 * 增加动态设置全屏背景是否显示的接口
 * 封装发送广播通知Web前端的接口
 * 完善提交日志接口
 * 第二次加载url时，不重复加载插件

v2.3.9 
==================
 * 替换错误页的图标
 * 替换MActivity的图标

v2.3.8 
==================
 * 增加设置webview显示策略的接口
 * 优化当页面加载失败的处理逻辑
 * 适配调用在线播放接口的处理逻辑

v2.3.7
==================
 * 优化初始化时序，提前显示loading
 * 去掉14u的特殊处理

v2.3.6
==================
 * 调用start接口时增加设置uri和type的启动方式

v2.3.5 
==================
 * 增加获取系统属性值得接口
 * 增加获取app版本信息接口

v2.3.4 
==================
 * 增加了调用本地播放的接口
 * 启动其他应用时可以带intent参数对象
 
2.3.3 
==================
 * fixbug:解决了RTK Android4.2 E710U不响应上下键焦点切换的问题

v2.3.2
==================
 * 规范Log打印
 * 增加设置UserAgent模式的接口，解决浏览站点显示异常的问题
 * 增加页面加载的超时时长

v2.3.1
==================
 * 增加了loadUrl的重载
 * 获取设备信息内容中增加品牌信息
 * fixbug:修复了无法发送pause事件消息到js的问题

2.3.0 
==================
 * 重构了ipc通信的初始化过程
 * 整合了支付相关代码

v2.2.8 
==================
* 按返回键时，分别发送keydown和keyup事件到web页面

v2.2.7 
==================
 * 在web页面下响应菜单长按键
 * fixbug:修复loading状态下webview信息显示的问题
 * 增加UI操作通知接口
 * 在框架中增加监听主页按键的逻辑，可以实现屏蔽主页键

v2.2.6 
==================
 * 在web页面下响应菜单长按键
 * fixbug:修复loading状态下webview信息显示的问题
 * 增加UI操作通知接口

v2.2.5 
==================
* 加载url资源时增加不同的缓存模式

v2.2.4 
==================
* 优化当不支持系统主题时url拼接的问题

v2.2.3
==================
 * fixbug:修复调用酷开商城详情页失败的问题， 参数默认都解析成String
 * 整合支付模块,注：web启动支付页面的参数有调整
 * fixbug:在web页面下响应遥控器蓝牙按键
 * 集成通过Android通道提交web页面日志
 * webview水平方向不支持滚动条

v2.2.1 
==================
 * 增加404错误页面通知
 * 增加消息通知接口

v2.2.0 
==================
 * 增加获取影视信息接口
 * 增加获取应用圈信息接口
 * 增加获取主题接口
 * 增加获取WebViewSDK信息接口
 * 增加设置web页面焦点边界位置的接口

v2.1.0 
==================
* 随酷开系统6.0增加主题

v2.0.0
==================
 * fix bug:android-21版本及以上，http和https混合访问受限制的问题；
 * fix bug:apk release模式下，访问https受限制的问题；

