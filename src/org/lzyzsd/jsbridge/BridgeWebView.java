package org.lzyzsd.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge {

	private final String TAG = "BridgeWebView";

	public static final String toLoadJs = "WebViewJavascriptBridge.js";
	Map<String, ICallBackFunction> responseCallbacks = new HashMap<String, ICallBackFunction>();
	Map<String, IBridgeHandler> messageHandlers = new HashMap<String, IBridgeHandler>();
	IBridgeHandler defaultHandler = new CCDefaultHandler();

	private List<Message> startupMessage = new ArrayList<Message>();

	public List<Message> getStartupMessage() {
		return startupMessage;
	}

	public void setStartupMessage(List<Message> startupMessage) {
		this.startupMessage = startupMessage;
	}

	private long uniqueId = 0;

	public BridgeWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BridgeWebView(Context context) {
		super(context);
		init();
	}

	/**
	 *
	 * @param handler
	 *            default handler,handle messages send by js without assigned handler name,
     *            if js message has handler name, it will be handled by named handlers registered by native
	 */
	public void setDefaultHandler(IBridgeHandler handler) {
       this.defaultHandler = handler;
	}

    private void init() {
		this.setVerticalScrollBarEnabled(false);
		this.setHorizontalScrollBarEnabled(false);
		this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
		initWebViewSettings();
		this.setWebViewClient(getBridgeWebViewClient());
        this.setWebChromeClient(getBridgeWebViewChromeClient());
	}

	private BridgeWebViewClient bridgeWebViewClient = null;
    protected BridgeWebViewClient getBridgeWebViewClient() {
    	if (bridgeWebViewClient == null) {
			bridgeWebViewClient = new BridgeWebViewClient(this);
		}
    	return bridgeWebViewClient;
    }

	private BridgeWebViewChromeClient bridgeWebViewChromeClient = null;
    protected BridgeWebViewChromeClient getBridgeWebViewChromeClient() {
		if (bridgeWebViewChromeClient == null) {
			bridgeWebViewChromeClient = new BridgeWebViewChromeClient();
		}
		return bridgeWebViewChromeClient;
	}

    /**
     * 获取到CallBackFunction data执行调用并且从数据集移除
     * @param url
     */
	void handlerReturnData(String url) {
		String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
		ICallBackFunction f = responseCallbacks.get(functionName);
		String data = BridgeUtil.getDataFromReturnUrl(url);
		if (f != null) {
			f.onCallBack(data);
			responseCallbacks.remove(functionName);
			return;
		}
	}

	@Override
	public void send(String data) {
		send(data, null);
	}

	@Override
	public void send(String data, ICallBackFunction responseCallback) {
		doSend(null, data, responseCallback);
	}

    /**
     * 保存message到消息队列
     * @param handlerName handlerName
     * @param data data
     * @param responseCallback CallBackFunction
     */
	private void doSend(String handlerName, String data, ICallBackFunction responseCallback) {
		Message m = new Message();
		if (!TextUtils.isEmpty(data)) {
			m.setData(data);
		}
		if (responseCallback != null) {
			String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
			responseCallbacks.put(callbackStr, responseCallback);
			m.setCallbackId(callbackStr);
		}
		if (!TextUtils.isEmpty(handlerName)) {
			m.setHandlerName(handlerName);
		}
		queueMessage(m);
	}

    /**
     * list<message> != null 添加到消息集合否则分发消息
     * @param m Message
     */
	private void queueMessage(Message m) {
		if (startupMessage != null) {
			startupMessage.add(m);
		} else {
			dispatchMessage(m);
		}
	}

    /**
     * 分发message 必须在主线程才分发成功
     * @param m Message
     */
	void dispatchMessage(Message m) {
        String messageJson = m.toJson();
        //escape special characters for json string  为json字符串转义特殊字符
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        // 必须要找主线程才会将数据传递出去 --- 划重点
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            this.loadUrl(javascriptCommand);
        }
    }

    /**
     * 刷新消息队列
     */
	void flushMessageQueue() {
		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			loadUrl(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, new ICallBackFunction() {

				@Override
				public void onCallBack(String data) {
					// deserializeMessage 反序列化消息
					List<Message> list = null;
					try {
						list = Message.toArrayList(data);
					} catch (Exception e) {
                        e.printStackTrace();
						return;
					}
					if (list == null || list.size() == 0) {
						return;
					}
					for (int i = 0; i < list.size(); i++) {
						Message m = list.get(i);
						String responseId = m.getResponseId();
						// 是否是response  CallBackFunction
						if (!TextUtils.isEmpty(responseId)) {
							ICallBackFunction function = responseCallbacks.get(responseId);
							String responseData = m.getResponseData();
							function.onCallBack(responseData);
							responseCallbacks.remove(responseId);
						} else {
							ICallBackFunction responseFunction = null;
							// if had callbackId 如果有回调Id
							final String callbackId = m.getCallbackId();
							if (!TextUtils.isEmpty(callbackId)) {
								responseFunction = new ICallBackFunction() {
									@Override
									public void onCallBack(String data) {
										Message responseMsg = new Message();
										responseMsg.setResponseId(callbackId);
										responseMsg.setResponseData(data);
										queueMessage(responseMsg);
									}
								};
							} else {
								responseFunction = new ICallBackFunction() {
									@Override
									public void onCallBack(String data) {
										// do nothing
									}
								};
							}
							// BridgeHandler执行
							IBridgeHandler handler;
							if (!TextUtils.isEmpty(m.getHandlerName())) {
								handler = messageHandlers.get(m.getHandlerName());
							} else {
								handler = defaultHandler;
							}
							if (handler != null){
								handler.handler(m.getData(), responseFunction);
							}
						}
					}
				}
			});
		}
	}


	public void loadUrl(String jsUrl, ICallBackFunction returnCallback) {
		this.loadUrl(jsUrl);
        // 添加至 Map<String, CallBackFunction>
		responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl), returnCallback);
	}

	/**
	 * register handler,so that javascript can call it
	 * 注册处理程序,以便javascript调用它
	 * @param handlerName handlerName
	 * @param handler BridgeHandler
	 */
	public void registerHandler(String handlerName, IBridgeHandler handler) {
		if (handler != null) {
            // 添加至 Map<String, BridgeHandler>
			messageHandlers.put(handlerName, handler);
		}
	}
	
	/**
	 * unregister handler
	 * 
	 * @param handlerName
	 */
	public void unregisterHandler(String handlerName) {
		if (handlerName != null) {
			messageHandlers.remove(handlerName);
		}
	}

	/**
	 * call javascript registered handler
	 * 调用javascript处理程序注册
     * @param handlerName handlerName
	 * @param data data
	 * @param callBack CallBackFunction
	 */
	public void callHandler(String handlerName, String data, ICallBackFunction callBack) {
        doSend(handlerName, data, callBack);
	}

	// add by fyb
	private IWebPageListener webPageListener;
	void setWebPageListener(IWebPageListener listener) {
		this.getBridgeWebViewClient().setWebPageListener(listener);
		this.getBridgeWebViewChromeClient().setWebPageListener(listener);
	}

	private void initWebViewSettings() {
		this.setInitialScale(0);
		this.setVerticalScrollBarEnabled(false);
		this.setHorizontalScrollBarEnabled(false);
		this.setBackgroundColor(0); // 设置背景色

		// Enable JavaScript
		final WebSettings settings = this.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		//add by fyb
		if (android.os.Build.VERSION.SDK_INT >= 21/*android.os.Build.VERSION_CODES.LOLLIPOP*/) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}

		//We don't save any form data in the application
		settings.setSaveFormData(false);
		settings.setSavePassword(false);

		// Jellybean rightfully tried to lock this down. Too bad they didn't give us a whitelist
		// while we do this
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			settings.setAllowUniversalAccessFromFileURLs(true);
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			settings.setMediaPlaybackRequiresUserGesture(false);
		}

		// Enable DOM storage
		settings.setDomStorageEnabled(true);

		// Enable built-in geolocation
		settings.setGeolocationEnabled(true);

		// Enable AppCache
		// Fix for CB-2282
		settings.setAppCacheMaxSize(5 * 1048576);
		settings.setAppCacheEnabled(true);

		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
	}

}
