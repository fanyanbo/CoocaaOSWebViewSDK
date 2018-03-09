package org.coocaa.webview;

import org.apache.cordova.CordovaBridge;
import org.json.JSONException;

import android.webkit.JavascriptInterface;

public class CoocaaOSExposedJsApi {
	
    private static final String WAIT_OS_READY = "waitForOSReady";//判断酷开系统是否bind成功。
    private static final String LAUNCH_SOURCE_LIST = "launchSourceList";//启动信号源
    private static final String HAS_USER_LOGIN = "hasCoocaaUserLogin";//当前用户是否登录
    private static final String GET_USER_INFO = "getUserInfo";//获取用户信息;
    private static final String GET_DEVICE_INFO = "getDeviceInfo";//获取当前设备信息
    private static final String IS_NET_CONNECTED = "isNetConnected";//获取当前网路连接状态
    private static final String GET_NET_TYPE = "getNetType";//获取当前网络类型
    private static final String GET_IP_INFO = "getIpInfo";//获取当前网络的ip地址
    private static final String GET_DEVICE_LOCATION = "getDeviceLocation";//获取当前设备的城市地址
    private static final String GET_USER_ACCESS_TOKEN = "getUserAccessToken";//获取用户的登录token
    private static final String GET_APP_INFO = "getAppInfo";
    private static final String GET_PROPERTY_VALUE = "getPropertiesValue";
    private static final String GET_WEBVIEWSDK_INFO = "getWebViewSDKInfo";
    private static final String GET_CURRENT_THEME = "getCurTheme";
    private static final String SET_FOCUS_POSITION = "setFocusPosition";
    private static final String NOTIFY_JS_MESSAGE = "notifyJSMessage";
    private static final String NOTIFY_JS_LOG = "notifyJSLogInfo";
    
    private final CoocaaWebView coocaaWebView;

    CoocaaOSExposedJsApi(CoocaaWebView webview) {
        this.coocaaWebView = webview;
    }

    @JavascriptInterface
    public String exec(String service, String action, String callbackId, String arguments) throws JSONException, IllegalAccessException {
    	
    	if(WAIT_OS_READY.equals(action)) {
    		this.coocaaWebView.getCoocaaOSConnecter();
    		
    	}
    	else if(LAUNCH_SOURCE_LIST.equals(action)) {
    		
    	}
    	else if(HAS_USER_LOGIN.equals(action)) {
    		
    	}
    	else if(GET_USER_INFO.equals(action)) {
	
    	}
    	else if(GET_DEVICE_INFO.equals(action)) {
    		
    	}
    	else if(IS_NET_CONNECTED.equals(action)) {
    		
    	}
    	else if(GET_NET_TYPE.equals(action)) {
    		
    	}
    	else if(GET_IP_INFO.equals(action)) {
    		
    	}
    	else if(GET_DEVICE_LOCATION.equals(action)) {
    		
    	}
    	else if(GET_USER_ACCESS_TOKEN.equals(action)) {
    		
    	}
    	else if(GET_APP_INFO.equals(action)) {
    		
    	}
    	else if(GET_PROPERTY_VALUE.equals(action)) {
    		
    	}
    	else if(GET_WEBVIEWSDK_INFO.equals(action)) {
    		
    	}
    	else if(GET_CURRENT_THEME.equals(action)) {
    		
    	}
    	else if(SET_FOCUS_POSITION.equals(action)) {
    		
    	}
    	else if(NOTIFY_JS_MESSAGE.equals(action)) {
    		
    	}
    	else if(NOTIFY_JS_LOG.equals(action)) {
    		
    	}
    	
        return null;
    }
}
