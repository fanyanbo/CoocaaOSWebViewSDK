/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package com.coocaa.cordova.plugin;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.coocaa.webviewsdk.version.SystemWebViewSDK;
import com.coocaa.x.xforothersdk.app.SuperXFinder;
import com.coocaa.x.xforothersdk.provider.db.table.download.TableDownload;
import com.coocaa.x.xforothersdk.provider.db.table.download.TableDownload.TableDownloadMonitor;

import com.skyworth.framework.skycommondefine.SkyworthBroadcastKey;
import com.skyworth.theme.SkyThemeEngine;
import com.skyworth.theme.ThemeColorSeriesEnum;
import com.tianci.net.define.NetworkDefs;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.coocaa.webview.CoocaaOSConnecter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CoocaaOSApi extends CordovaPlugin
{
    private String TAG = getClass().getSimpleName();

    private static final String WAIT_OS_READY = "waitForOSReady";//判断酷开系统是否bind成功。
    private static final String LAUNCH_SOURCE_LIST = "launchSourceList";//启动信号源
    private static final String LAUNCH_ONLINE_MOVIE_PLAYER = "startOnLinePlayer";//启动影视播放器
    private static final String RESUME_APP_TASK = "resumeDownloadTask";//恢复应用下载
    private static final String PAUSE_APP_TASK = "pauseDownloadTask";//暂停应用下载
    private static final String DELETE_APP_TASK = "deleteDownloadTask";//删除下载任务
    private static final String HAS_USER_LOGIN = "hasCoocaaUserLogin";//当前用户是否登录
    private static final String GET_USER_INFO = "getUserInfo";//获取用户信息;
    private static final String START_QQ_ACOUNT = "startQQAccount";//启动qq登录
    private static final String GET_DEVICE_INFO = "getDeviceInfo";//获取酷开设备信息
    private static final String GET_BASE_INFO = "getBaseInfo";//获取硬件设备信息
    private static final String IS_NET_CONNECTED = "isNetConnected";//获取当前网路连接状态
    private static final String GET_NET_TYPE = "getNetType";//获取当前网络类型//有线、无线
    private static final String GET_IP_INFO = "getIpInfo";//获取当前网络的ip地址//内网
    private static final String GET_DEVICE_LOCATION = "getDeviceLocation";//获取当前设备的城市地址
    private static final String GET_USER_ACCESS_TOKEN = "getUserAccessToken";//获取用户的登录token
    private static final String GET_APP_INFO = "getAppInfo";
    private static final String GET_PROPERTY_VALUE = "getPropertiesValue";
    private static final String GET_WEBVIEWSDK_INFO = "getWebViewSDKInfo";
    private static final String GET_CURRENT_THEME = "getCurTheme";
    private static final String GET_SHOWN_STATUS = "getShownStatus";
    private static final String SET_FOCUS_POSITION = "setFocusPosition";
    /***************************************应用商城任务**********************************************/
    private static final String CREATE_APP_TASK = "createDownloadTask";//创建下载任务
    /***************************************支付**********************************************/
    private static final String PURCHASE_ORDER = "purchaseOrder";//支付订单
    /***************************************任务监听**********************************************/
    private static final String BROADCAST_APPDOWNLOADTASK = "APP_TASK_CALLBACK";//任务下载状态监听
    private static final String BROADCAST_NETCHANGED = "NET_CHANGGED";//网络发生变化的广播
    private static final String BROADCAST_USBCHANGGED = "USB_CHANGGED";//u盘广播
    private static final String BROADCAST_USERCHANGGED = "USER_CHANGGED";//用户广播
    private static final String BROADCAST_PURCHASE = "PURCHASE_CALLBACK";//支付状态
    private static final String BROADCAST_COMMON_CHANGED = "COMMON_CHANGED";//抽象出来的通用状态变化
    /***************************************消息与日志*******************************************/
    private static final String NOTIFY_JS_MESSAGE = "notifyJSMessage";
    private static final String NOTIFY_JS_LOG = "notifyJSLogInfo";
    private static final String NOTIFY_JS_LOG_EXTRA = "notifyJSLogInfoExtra";
    private static final String SET_BUSINESS_DATA = "setBusinessData";
    private static final String GET_BUSINESS_DATA = "getBusinessData";

    private Context mContext;
  //  private CoocaaOSApiListener mCoocaaListener;
    private CoocaaOSConnecter mCoocaaOSConnecter = null;

    private volatile boolean isCmdBindSuccess = false;

    private MyTableDownloadListener mDownloadListenrer;
    private MyTableMoniteDownloadListener mProcessListener;
    
    private CallbackBroadcastReceiver mCallbackBC = null;
    private BusinessDataListener.CordovaBusinessDataListener mBusinessListener = null;
    private static final String PAY_ACTION = "coocaa.webviewsdk.action.pay";

    private CordovaWebView mWebView;

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        Log.v(TAG, TAG + ": initialization");
        super.initialize(cordova, webView);
        mContext = cordova.getActivity();

        mWebView = webView;
        Log.v("WebViewSDK", "CoocaaOSApi initialization isShown:" + mWebView.getView().isShown());
        
        mCoocaaOSConnecter = cordova.getCoocaaOSConnecter();
        if(mCoocaaOSConnecter != null) isCmdBindSuccess = true;
        Log.v("WebViewSDK", "CoocaaOSApi initialization CoocaaOSConnecter:" + mCoocaaOSConnecter);

        mBusinessListener = cordova.getCordovaBusinessDataListener();

        if (mCallbackBC == null) mCallbackBC = new CallbackBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PAY_ACTION);
        mContext.registerReceiver(mCallbackBC, filter);
    }

    private class CallbackBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.i("WebViewSDK","onReceive getAction = " + intent.getAction());
			if(PAY_ACTION.equals(intent.getAction())){
	            int resultstatus = intent.getIntExtra("resultstatus", -1);
	            String tradeId = intent.getStringExtra("tradeId");
	            String resultmsg = intent.getStringExtra("resultmsg");
	            String purchWay = intent.getStringExtra("purchWay");

				JSONObject jsonObject = new JSONObject();
	             try {
	                 jsonObject.put("presultstatus", resultstatus);
	                 jsonObject.put("ptradeid", tradeId);
	                 jsonObject.put("presultmsg", resultmsg);
	                 jsonObject.put("ppurchWay", purchWay);
	                 broadCastPurchase(mContext, jsonObject);
	             } catch (JSONException e) {
	                 e.printStackTrace();
	                 Log.e(TAG, "WebPurchaseCallBack error:"+ e.toString());
	             }
			}
		}
    }

    private class MyTableMoniteDownloadListener implements TableDownloadMonitor
    {
		@Override
		public void onDownloading(TableDownload t) {
			if(mDownloadListenrer!=null)
			{
				mDownloadListenrer.onProcess(t);
			}
		}  	
    }
      
    private class MyTableDownloadListener implements TableDownload.TableDownloadListener
    {
        private HashMap<Long,TableDownload> mTaskMaps = new HashMap<Long,TableDownload>();
        private final ReadWriteLock rwl = new ReentrantReadWriteLock();
        private final Lock readLock = rwl.readLock();
        private final Lock writeLock = rwl.writeLock();
        public MyTableDownloadListener()
        {
            super();
        }

        public boolean pauseTaskId(long taskID)
        {
            TableDownload loader = get(Long.valueOf(taskID));

            if(loader!=null)
            {
               return  loader._pause(taskID);
            }
            return false;
        }

        public boolean resumeTaskId(long taskID)
        {
            TableDownload loader = get(Long.valueOf(taskID));

            if(loader!=null)
            {
                return  loader._startNow(taskID);
            }
            return false;
        }

        public boolean deleteTaskId(long taskID)
        {
            TableDownload loader = get(Long.valueOf(taskID));

            if(loader!=null)
            {
                return  loader._remove(taskID);
            }
            return false;
        }

        public void addDownloadTask(TableDownload downloadTask)
        {
            put(downloadTask.getId(), downloadTask);
        }

        private TableDownload get(Long key) {
            readLock.lock();
            try {
                return mTaskMaps.get(key);
            } finally {
                readLock.unlock();
            }
        }

        private Long[] allKeys() {
            readLock.lock();
            try {
                return (Long[]) mTaskMaps.keySet().toArray();
            } finally {
                readLock.unlock();
            }
        }

        private TableDownload put(Long key, TableDownload value) {
            writeLock.lock();
            try {
                return mTaskMaps.put(key, value);
            } finally {
                writeLock.unlock();
            }
        }

        private void clear() {
            writeLock.lock();
            try {
                mTaskMaps.clear();
            } finally {
                writeLock.unlock();
            }
        }

        private JSONObject getCallBackJson(long id,int code,String extra,TableDownload.DOWNLOAD_STATUS status)
        {
            TableDownload loader = get(Long.valueOf(id));
            if (loader!=null)
            {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("taskid", id);
                    jsonObject.put("status", status.name());
                    jsonObject.put("name", loader.getName());
                    jsonObject.put("url", loader.getUrl());
                    jsonObject.put("progress", loader.getLength()>0?loader.getProgress():0);
                    jsonObject.put("createtime", loader.getCreatetime());
                    jsonObject.put("code", code);
                    jsonObject.put("extra", extra);
                    return jsonObject;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        
        private JSONObject getCallBackJson(long id,int code,String extra,TableDownload.DOWNLOAD_STATUS status,int process)
        {
            TableDownload loader = get(Long.valueOf(id));
            if (loader!=null)
            {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("taskid", id);
                    jsonObject.put("status", status.name());
                    jsonObject.put("name", loader.getName());
                    jsonObject.put("url", loader.getUrl());
                    jsonObject.put("progress",process>0?process:0);
                    jsonObject.put("createtime", loader.getCreatetime());
                    jsonObject.put("code", code);
                    jsonObject.put("extra", extra);
                    return jsonObject;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        public void onEnqueued(long id) {
          
        }

        @Override
        public void onStarting(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id,code,extra, TableDownload.DOWNLOAD_STATUS.ON_STARTING);
            broadCastAppDownloadTaskChangged(mContext, jsonObject);
        }

        @Override
        public void onStartDownloading(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_DOWNLOADING);
            broadCastAppDownloadTaskChangged(mContext, jsonObject);
        }

        @Override
        public void onPaused(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_PAUSED);
            broadCastAppDownloadTaskChangged(mContext, jsonObject);
        }

        @Override
        public void onComplete(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_COMPLETE);
            broadCastAppDownloadTaskChangged(mContext, jsonObject);
        }

        @Override
        public void onRemoved(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_REMOVED);
            broadCastAppDownloadTaskChangged(mContext, jsonObject);

        }

        @Override
        public void onStopped(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_STOPPED);
            broadCastAppDownloadTaskChangged(mContext, jsonObject);
        }

        @Override
        public void onDownloading(TableDownload t) {
            if(t!=null)
            {
                put(Long.valueOf(t.getId()),t);
                JSONObject jsonObject = getCallBackJson(t.getId(), t.getOncode(), t.getOnextra(), TableDownload.DOWNLOAD_STATUS.ON_DOWNLOADING);
                broadCastAppDownloadTaskChangged(mContext, jsonObject);
            }
        }
        
        public void onProcess(TableDownload t) {
            if(t!=null)
            {
                JSONObject jsonObject = getCallBackJson(t.getId(), t.getOncode(), t.getOnextra(), t.getStatus(),t.getProgress());
                broadCastAppDownloadTaskChangged(mContext, jsonObject);
            }
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDownloadListenrer != null) {
            TableDownload._destroyTableDownloadListener(mContext, mDownloadListenrer);
            mDownloadListenrer.clear();
            mDownloadListenrer = null;
        }
        if (mCallbackBC != null)
            mContext.unregisterReceiver(mCallbackBC);

    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
    	Log.i("WebViewSDK","CoocaaOSApi execute action = " + action);
        if(WAIT_OS_READY.equals(action))
        {
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                	Log.i("WebViewSDK","WAIT_OS_READY isCmdBindSuccess:" + isCmdBindSuccess + ",tid = " + android.os.Process.myTid());
                    while(!isCmdBindSuccess/* && mRef <= 1*/)
                    {
                        try {
                            Thread.sleep(200);
                            mCoocaaOSConnecter = cordova.getCoocaaOSConnecter();
                            if(mCoocaaOSConnecter != null) isCmdBindSuccess = true;
                            Log.i("WebViewSDK","WAIT_OS_READY isCmdBindSuccess:" + isCmdBindSuccess + ",tid = " + android.os.Process.myTid());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    callbackContext.success();
                }
            });
            return true;
        }
        else if(LAUNCH_SOURCE_LIST.equals(action))
        {
            Intent intent = new Intent("startSourceList");
            intent.putExtra("specialKey", SkyworthBroadcastKey.SKY_BROADCAST_KEY_SIGNAL);
            cordova.getActivity().sendBroadcast(intent);
            callbackContext.success();
            return true;
        }
        else if(SET_FOCUS_POSITION.equals(action))
        {
            JSONObject paramObj = args.getJSONObject(0);
            if (paramObj != null) {
                String strPos = paramObj.getString("focusposition");
                int iPos = 0;
                try {
                    iPos = Integer.parseInt(strPos);
                    SystemWebViewSDK.setFocusPosition(iPos);
                } catch (Exception e) {
                    callbackContext.error(e.toString());
                }
            }
            callbackContext.success();
            return true;
        }
        else if(NOTIFY_JS_LOG.equals(action))
        {
        	String eventId = "";
        	String params = "";
        	JSONObject eventIdObj = args.getJSONObject(0);
        	JSONObject paramsObj = args.getJSONObject(1);
        	if(eventIdObj != null && paramsObj != null){
        		eventId = eventIdObj.getString("eventId");
        		params = paramsObj.getString("params");
        	}
            Intent intent = new Intent("notify.js.log");
            intent.putExtra("eventId", eventId);
            intent.putExtra("params", params);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        	callbackContext.success();
        	return true;
        }
        else if(NOTIFY_JS_LOG_EXTRA.equals(action))
        {
            String eventId = "", params = "", type = "";
            JSONObject eventIdObj = args.getJSONObject(0);
            JSONObject paramsObj = args.getJSONObject(1);
            JSONObject typeObj = args.getJSONObject(2);
            if (eventIdObj != null && paramsObj != null && typeObj != null) {
                eventId = eventIdObj.getString("eventId");
                params = paramsObj.getString("params");
                type = typeObj.getString("type");
            }
            if ("resume".equals(type)) {
                Intent intent = new Intent("notify.js.log.resume");
                intent.putExtra("eventId", eventId);
                intent.putExtra("params", params);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            } else if ("pause".equals(type)) {
                Intent intent = new Intent("notify.js.log.pause");
                intent.putExtra("eventId", eventId);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
            callbackContext.success();
            return true;
        }
        else if(NOTIFY_JS_MESSAGE.equals(action))
        {
            JSONObject paramObj = args.getJSONObject(0);
            String data = "";
            if (paramObj != null) {
                data = paramObj.getString("webInfo");
            }
            Intent intent = new Intent("notify.js.message");
            intent.putExtra("key", data);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            callbackContext.success();
            return true;
        }
        else if(SET_BUSINESS_DATA.equals(action))
        {
            JSONObject dataObj = args.getJSONObject(0);
            JSONObject typeObj = args.getJSONObject(1);
            String cc_data = "", cc_type = "";
            if(dataObj != null){
                cc_data = dataObj.getString("cc_data");
                cc_type = typeObj.getString("cc_type");
            }
            Log.i("WebViewSDK","SET_BUSINESS_DATA cc_type = " + cc_type + ",cc_data = " + cc_data);
            if("sync".equals(cc_type)) {
                if(mBusinessListener != null) {
                    boolean ret = mBusinessListener.setBusinessData(cc_data, new BusinessDataListener.BussinessCallback() {
                        @Override
                        public void onResult(String value) {
                            Log.i("WebViewSDK","setBusinessData onResule = " + value);
                            if ("success".equals(value))
                                callbackContext.success();
                            else{
                                callbackContext.error("error occurs when called setBusinessData");
                            }
                        }
                    });
                    if (ret) {
                        callbackContext.success();
                    }
                }else{
                    callbackContext.error("no implement");
                }
            } else {
                final String finalData = cc_data;
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {

                        if (mBusinessListener != null) {

                            boolean ret = mBusinessListener.setBusinessData(finalData, new BusinessDataListener.BussinessCallback() {
                                @Override
                                public void onResult(String value) {
                                    Log.i("WebViewSDK","setBusinessData onResule = " + value);
                                    if ("success".equals(value))
                                        callbackContext.success();
                                    else{
                                        callbackContext.error("error occurs when called setBusinessData");
                                    }
                                }
                            });
                            if (ret) {
                                callbackContext.success();
                            }
                        } else {
                            callbackContext.error("no implement");
                        }
                    }
                });
            }
            return true;
        }
        else if(GET_BUSINESS_DATA.equals(action))
        {
            JSONObject dataObj = args.getJSONObject(0);
            JSONObject typeObj = args.getJSONObject(1);
            String cc_data = "",cc_type = "";
            if(dataObj != null){
                cc_data = dataObj.getString("cc_data");
                cc_type = typeObj.getString("cc_type");
            }
            Log.i("WebViewSDK","GET_BUSINESS_DATA cc_type = " + cc_type + ",cc_data = " + cc_data);
            if("sync".equals(cc_type)) {
                if(mBusinessListener != null) {
                    String ret = mBusinessListener.getBusinessData(cc_data, new BusinessDataListener.BussinessCallback() {
                        @Override
                        public void onResult(String value) {
                            Log.i("WebViewSDK","getBusinessData onResule = " + value);
                            callbackContext.success(value);
                        }
                    });
                    if(ret != null && !"".equals(ret)) {
                        callbackContext.success(ret);
                    }
                }else{
                    callbackContext.error("no implement");
                }
            } else{
                final String finalData = cc_data;
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {

                        if(mBusinessListener != null){
                            String ret = mBusinessListener.getBusinessData(finalData, new BusinessDataListener.BussinessCallback() {
                                @Override
                                public void onResult(String value) {
                                    Log.i("WebViewSDK","getBusinessData onResule = " + value);
                                    callbackContext.success(value);
                                }
                            });
                            if(ret != null && !"".equals(ret)) {
                                callbackContext.success(ret);
                            }
                        }else{
                            callbackContext.error("no implement");
                        }
                    }
                });
            }
            return true;
        }
        else if(LAUNCH_ONLINE_MOVIE_PLAYER.equals(action))
        {
            return true;
        }
        else if (GET_USER_INFO.equals(action))
        {
            if (mCoocaaOSConnecter != null) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        String result = mCoocaaOSConnecter.getLoginUserInfo();
                        if (result == null) {
                            callbackContext.error("error occurs when called getLoginUserInfo");
                        } else {
                            try {
                                callbackContext.success(new JSONObject(result));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                callbackContext.error(e.toString());
                            }
                        }
                    }
                });
            } else {
                callbackContext.error("mCoocaaOSConnecter is not ready!");
            }
            return true;
        }
        else if (GET_DEVICE_INFO.equals(action))
        {
            if (mCoocaaOSConnecter != null) {
                Log.v("WebViewSDK", "getDeviceInfo myTid() = " + android.os.Process.myTid());
                String result = mCoocaaOSConnecter.getDeviceInfo();
                if (result == null) {
                    callbackContext.error("error occurs when called getDeviceInfo");
                } else {
                    try {
                        callbackContext.success(new JSONObject(result));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        callbackContext.error(e.toString());
                    }
                }
            } else {
                callbackContext.error("mCoocaaOSConnecter is not ready!");
            }
            return true;
        }
        else if (GET_SHOWN_STATUS.equals(action)){
            Log.v("WebViewSDK", "webview isShown = " + mWebView.getView().isShown());
            callbackContext.error("no implement");
            return true;
        }
        else if(GET_APP_INFO.equals(action))
        {
            PackageManager pm = this.cordova.getActivity().getPackageManager();
            JSONObject resultObject = new JSONObject();
            try {
                JSONObject pkgListObj = args.getJSONObject(0);
                String pkgListStr = pkgListObj.getString("pkgList");
                JSONObject jsonParams = new JSONObject(pkgListStr);
                JSONArray params = jsonParams.getJSONArray("pkgList");
                Log.i("WebViewSDK" , "length = " + params.length());
                if (params.length() > 0) {
                    for(int i=0; i<params.length(); i++){
                        String pkgName = params.getString(i);
                        Log.i("WebViewSDK" , "pkgName = " + pkgName);
                        JSONObject valueObject = new JSONObject();
                        PackageInfo info = null;
                        try{
                            info = pm.getPackageInfo(pkgName, 0);
                            if (info != null) {
                                valueObject.put("status", "0");
                                valueObject.put("versionName", info.versionName);
                                valueObject.put("versionCode", info.versionCode);
                            }
                        }catch (NameNotFoundException e){
                            valueObject.put("status", "-1");
                            valueObject.put("versionName", "-1");
                            valueObject.put("versionCode", -1);
                        }
                        Log.i("WebViewSDK" , "valueObject = " + valueObject);
                        resultObject.put(pkgName, valueObject);
                    }
                    callbackContext.success(resultObject.toString());
                } else {
                    callbackContext.error("params error occurs when called getAppInfo");
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                callbackContext.error("error occurs when called getAppInfo");
            }
            return true;
        }
        else if(GET_CURRENT_THEME.equals(action))
        {
        	String theme = "";
        	try {
    	        if(SkyThemeEngine.getInstance().getThemeColorSeries() == ThemeColorSeriesEnum.E_THEME_COLOR_SERIES_DARK){
    	        	theme = "dark";
    	        }else{
    	        	theme = "light";
    	        }
        		JSONObject result = new JSONObject();
        		result.put("theme", theme);
                callbackContext.success(result);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				callbackContext.error(e.toString());
			}
            return true;
        }
        else if(GET_WEBVIEWSDK_INFO.equals(action))
        {
            String versionName = "";
            int versionCode  = 0;

            try {
                versionName = SystemWebViewSDK.getVersionName();
                versionCode = SystemWebViewSDK.getVersionCode();
                JSONObject result = new JSONObject();
                result.put("versionName", versionName);
                result.put("versionCode", versionCode);
                callbackContext.success(result);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                callbackContext.error("error occurs when called getWebViewSDKInfo");
            }
            return true;
        }
        else if(CREATE_APP_TASK.equals(action))
        {
        	try
        	{
        		//appstore 5.x support!
        		if(mDownloadListenrer == null)
            	{
            		SuperXFinder.setContext(mContext);
                    mDownloadListenrer = new MyTableDownloadListener();
                    TableDownload._createTableDownloadListener(mContext, mDownloadListenrer);
                    if(mProcessListener == null)
            		{
            			mProcessListener = new MyTableMoniteDownloadListener();
            			TableDownload._addTableDownloadMonitor(mProcessListener);
            		}
            	}
        		
        	}catch (Exception e)
        	{
        		e.printStackTrace();
        	}
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try{
                    	
                        JSONObject urlObj = args.getJSONObject(0);
                        JSONObject md5Obj = args.getJSONObject(1);
                        JSONObject titleObj = args.getJSONObject(2);
                        JSONObject pkgObj = args.getJSONObject(3);
                        JSONObject appidObj = args.getJSONObject(4);
                        JSONObject iconObj = args.getJSONObject(5);
                        String url = urlObj.getString("url");
                        String md5 = md5Obj.getString("md5");
                        String title = titleObj.getString("title");
                        String pkgname = pkgObj.getString("pkg");
                        String appid = appidObj.getString("appid");
                        String icon = iconObj.getString("icon");
                        TableDownload checkdownloader = TableDownload._queryDownloadByUrl(url);
                        if (checkdownloader==null)
                        {
                            checkdownloader = TableDownload._createAppDownload(url, md5, title, pkgname, appid, icon);
                        }
                        if (checkdownloader!=null)
                        {
                            if(mDownloadListenrer!=null)
                            {
                                mDownloadListenrer.addDownloadTask(checkdownloader);
                            }
                            TableDownload._start(checkdownloader.getId());
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("taskid", checkdownloader.getId());
                                jsonObject.put("status", checkdownloader.getStatus());
                                jsonObject.put("name", checkdownloader.getName());
                                jsonObject.put("url", checkdownloader.getUrl());
                                jsonObject.put("progress", checkdownloader.getLength()>0?checkdownloader.getProgress():0);
                                jsonObject.put("createtime", checkdownloader.getCreatetime());
                                jsonObject.put("code", checkdownloader.getOncode());
                                jsonObject.put("extra", checkdownloader.getOnextra());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            callbackContext.success(jsonObject);
                        }
                        else
                        {
                            callbackContext.error("error occurs when called createDownloadTask");
                        }
                    }catch(JSONException e)
                    {
                        callbackContext.error(e.toString());
                    }
                }
            });
            return true;
        }
        else if(RESUME_APP_TASK.equals(action))
        {
            JSONObject taskIdObj = args.getJSONObject(0);
            String taskId = taskIdObj.getString("taskid");
            if(mDownloadListenrer!=null)
            {
                mDownloadListenrer.resumeTaskId(Long.valueOf(taskId));
            }
            return true;
        }
        else if(DELETE_APP_TASK.equals(action))
        {
            JSONObject taskIdObj = args.getJSONObject(0);
            String taskId = taskIdObj.getString("taskid");
            if(mDownloadListenrer!=null)
            {
                mDownloadListenrer.deleteTaskId(Long.valueOf(taskId));
            }
            return true;
        }
        else if(PAUSE_APP_TASK.equals(action))
        {
            JSONObject taskIdObj = args.getJSONObject(0);
            String taskId = taskIdObj.getString("taskid");
            if(mDownloadListenrer!=null)
            {
                mDownloadListenrer.pauseTaskId(Long.valueOf(taskId));
            }
            return true;
        }
        else if(IS_NET_CONNECTED.equals(action)) {
            if (mCoocaaOSConnecter != null) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        String result = mCoocaaOSConnecter.isNetConnected();
                        if (result != null) {
                            try {
                                callbackContext.success(new JSONObject(result));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                callbackContext.error(e.toString());
                            }
                        } else {
                            callbackContext.error("error occurs when called isNetConnected");
                        }
                    }
                });
            } else {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(GET_NET_TYPE.equals(action)) {
            if (mCoocaaOSConnecter != null) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        String result = mCoocaaOSConnecter.getNetType();
                        if (result != null) {
                            try {
                                callbackContext.success(new JSONObject(result));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                callbackContext.error(e.toString());
                            }
                        } else {
                            callbackContext.error("error occurs when called getNetType");
                        }
                    }
                });
            } else {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(GET_DEVICE_LOCATION.equals(action)) {
            if (mCoocaaOSConnecter != null) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        String result = mCoocaaOSConnecter.getDeviceLocation();
                        if (result != null) {
                            try {
                                callbackContext.success(new JSONObject(result));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                callbackContext.error(e.toString());
                            }
                        } else {
                            callbackContext.error("error occurs when called getDeviceLocation");
                        }
                    }
                });
            } else {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(GET_IP_INFO.equals(action)) {
            if (mCoocaaOSConnecter != null) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        String result = mCoocaaOSConnecter.getIpInfo();
                        if (result != null) {
                            try {
                                callbackContext.success(new JSONObject(result));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                callbackContext.error(e.toString());
                            }
                        } else {
                            callbackContext.error("error occurs when called getIpInfo");
                        }
                    }
                });
            } else {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if (HAS_USER_LOGIN.equals(action)) {
            if (mCoocaaOSConnecter != null) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        String result = mCoocaaOSConnecter.hasUserLogin();
                        if (result != null) {
                            try {
                                callbackContext.success(new JSONObject(result));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                callbackContext.error(e.toString());
                            }
                        } else {
                            callbackContext.error("error occurs when called hasUserLogin");
                        }
                    }
                });
            } else {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(START_QQ_ACOUNT.equals(action)) {
            if (mCoocaaOSConnecter != null) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        mCoocaaOSConnecter.startQQAcount();
                        callbackContext.success();
                    }
                });
            } else {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(GET_USER_ACCESS_TOKEN.equals(action)) {
            if (mCoocaaOSConnecter != null) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        String result = mCoocaaOSConnecter.getUserAccessToken();
                        if (result != null) {
                            try {
                                callbackContext.success(new JSONObject(result));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                callbackContext.error(e.toString());
                            }
                        } else {
                            callbackContext.error("error occurs when called getUserAccessToken");
                        }
                    }
                });
            } else {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(GET_PROPERTY_VALUE.equals(action))
        {
        	try {
        		String propertiesValue  = "";
        		JSONObject pkgNameObj = args.getJSONObject(0);
        		String propertiesKey = pkgNameObj.getString("propertiesKey");
        		if(propertiesKey != null)
        			propertiesValue = SystemProperties.get(propertiesKey);
            	JSONObject result = new JSONObject();
            	result.put("propertiesValue", propertiesValue);
                callbackContext.success(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				callbackContext.error("error occurs when called getPropertiesValue");
			}
            return true;
        }
        else if(PURCHASE_ORDER.equals(action))
        {
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                   	try{
                        JSONObject appCodeObj = args.getJSONObject(0);//商户编号ID,由酷开发布给第三方
                        JSONObject tradeidObj = args.getJSONObject(1);//订单编号ID
                        JSONObject productNameObj = args.getJSONObject(2);//商品名称，例如“影视包年”
                        JSONObject specialtypeObj = args.getJSONObject(3);//必填，通知支付结果给第三方开发者服务器URL，必须以http://开头，目前支持80端口 ，参数内容为，json格式字符串 例如：{"notify_url":"http://tv.coocaa.com/notify_url.html"}
                        JSONObject amountObj = args.getJSONObject(4);//商品价格，以“元”为单位
                        JSONObject productTypeObj = args.getJSONObject(5);
                        JSONObject payActionObj = args.getJSONObject(6);
                        JSONObject cmdObj = args.getJSONObject(7);
                        JSONObject tokenObj = args.getJSONObject(8);
                        JSONObject phoneNumObj = args.getJSONObject(9);
                        
                        String appcode = appCodeObj.getString("appcode");
                        String Tradeid = tradeidObj.getString("Tradeid");
                        String ProductName = productNameObj.getString("ProductName");
                        String SpecialType = specialtypeObj.getString("SpecialType");
                        String ProductType = productTypeObj.getString("ProductType");
                        String payAction = payActionObj.getString("payAction");
                        String cmd = cmdObj.getString("cmd");
                        double amount = amountObj.getDouble("amount");
                        String token = tokenObj.getString("token");
                        String phoneNum = phoneNumObj.getString("tel");
                        
                        Intent mIntent = new Intent("coocaa.intent.movie.pay");
                        String pkgName = mContext.getPackageName();
                        if(pkgName != null)
                        	mIntent.setPackage(pkgName);
                        else
                        	mIntent.setPackage("com.tianci.movieplatform");
                        mIntent.putExtra("appcode", appcode);
                        mIntent.putExtra("ProductName", ProductName);
                        mIntent.putExtra("Tradeid", Tradeid);
                        mIntent.putExtra("amount", amount / 100.0);
                        mIntent.putExtra("ProductType", ProductType);
                        mIntent.putExtra("SpecialType", "{\"notify_url\":\"" + SpecialType + "\"}");
                        mIntent.putExtra("payAction", PAY_ACTION);
                        mIntent.putExtra("cmd", cmd);
                        mIntent.putExtra("token", token);
                        mIntent.putExtra("tel", phoneNum);
                        
                        mContext.startActivity(mIntent);
                        callbackContext.success();
                        
                	}catch(Exception e)
                    {
                        callbackContext.error(e.toString());
                    }
                }
            });
        	return true;
        }
        else if(GET_BASE_INFO.equals(action))
        {
            try {
                long totalMem = 0, leftMem = 0;
                ActivityManager activityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(outInfo);
                totalMem = outInfo.totalMem;
                leftMem = outInfo.availMem;

                long totalSpace = 0L, freeSpace = 0L;
                long blockSize = 0L, availableBlocks = 0L, totalBlocks = 0L;
                File path = Environment.getDataDirectory();
                StatFs stat = new StatFs(path.getPath());
                blockSize = stat.getBlockSize();
                availableBlocks = stat.getAvailableBlocks();
                totalBlocks = stat.getBlockCount();
                totalSpace = blockSize * totalBlocks;
                freeSpace = blockSize * availableBlocks;

                JSONObject result = new JSONObject();
                result.put("totalMem", totalMem);
                result.put("leftMem", leftMem);
                result.put("totalSpace", totalSpace);
                result.put("freeSpace", freeSpace);
                Log.i("WebViewSDK",result.toString());
                callbackContext.success(result);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                callbackContext.error("error occurs when called getBaseInfo");
            }
            return true;
        }
        return false;
    }


    public static JSONObject getEthEventString(String type,NetworkDefs.EthEvent event)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nettype", type);
            jsonObject.put("netevent", event.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getWifiEventString(String type,NetworkDefs.WifiEvent event)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nettype", type);
            jsonObject.put("netevent", event.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /*
    * 网络状态发生变化的回调
     */
    public static void broadCastNetChangged(Context context, JSONObject myObject) {
        if (context != null && myObject != null) {
            final Intent intent = new Intent(BROADCAST_NETCHANGED);
            Bundle b = new Bundle();
            b.putString("userdata", myObject.toString());
            intent.putExtras(b);

            LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
        }
    }

    /*
       * u盘发生变化的回调
        */
    public static void broadCastUsbChangged(Context context, boolean insert, String usbpath) {
        if (context != null) {
            final Intent intent = new Intent(BROADCAST_USBCHANGGED);
            Bundle b = new Bundle();
            b.putString("userdata", "{'usbmount':'" + (insert ? "true" : "false") + "','mountpath':'" + usbpath + "'}");
            intent.putExtras(b);
            LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
        }
    }

    /*
     * 用户登录发生改变的广播
     */
    public static void broadCastUesrChangged(Context context) {
        if (context != null) {
            final Intent intent = new Intent(BROADCAST_USERCHANGGED);
            Bundle b = new Bundle();
            b.putString("userdata", "{'userchangged':'true'}");
            intent.putExtras(b);
            LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
        }
    }

    /*
     * 下载任务回调
     */
    public static void broadCastAppDownloadTaskChangged(Context context, JSONObject myObject) {
        if (context != null && myObject != null) {
            final Intent intent = new Intent(BROADCAST_APPDOWNLOADTASK);
            Bundle b = new Bundle();
            b.putString("userdata", myObject.toString());
            intent.putExtras(b);
            LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
        }
    }

    /*
     * 支付信息回调
     */
    public static void broadCastPurchase(Context context, JSONObject myObject) {
        if (context != null && myObject != null) {
            final Intent intent = new Intent(BROADCAST_PURCHASE);
            Bundle b = new Bundle();
            b.putString("userdata", myObject.toString());
            intent.putExtras(b);
            LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
        }
    }

    public static void broadCastCommonChanged(Context context, Map<String,String> map)
    {
        if(map != null && context != null) {
            try {
                JSONObject myObject = new JSONObject();
                Set<Map.Entry<String, String>> entryseSet = map.entrySet();
                for (Map.Entry<String, String> entry:entryseSet) {
                    myObject.put(entry.getKey(),entry.getValue());
                }
                myObject.put("cc_type","common");
                final Intent intent = new Intent(BROADCAST_COMMON_CHANGED);
                Bundle b = new Bundle();
                b.putString("userdata", myObject.toString());
                intent.putExtras(b);
                LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
            } catch (JSONException e) {
                Log.e("WebViewSDK", "broadCastCommonChanged error:" + e.toString());
                e.printStackTrace();
            }
        }
    }

    public static void broadCastVoiceChanged(Context context, Map<String,String> map)
    {
        if(map != null && context != null) {
            try {
                JSONObject myObject = new JSONObject();
                Set<Map.Entry<String, String>> entryseSet = map.entrySet();
                for (Map.Entry<String, String> entry:entryseSet) {
                    myObject.put(entry.getKey(),entry.getValue());
                }
                myObject.put("cc_type","voice");
                final Intent intent = new Intent(BROADCAST_COMMON_CHANGED);
                Bundle b = new Bundle();
                b.putString("userdata", myObject.toString());
                intent.putExtras(b);
                LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
            } catch (JSONException e) {
                Log.e("WebViewSDK", "broadCastVoiceChanged error:" + e.toString());
                e.printStackTrace();
            }
        }
    }
    
}