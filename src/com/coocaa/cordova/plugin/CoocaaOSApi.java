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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.coocaa.webviewsdk.version.SystemWebViewSDK;
import com.coocaa.x.xforothersdk.app.SuperXFinder;
import com.coocaa.x.xforothersdk.provider.db.table.download.TableDownload;
import com.coocaa.x.xforothersdk.provider.db.table.download.TableDownload.TableDownloadMonitor;

import com.skyworth.framework.skycommondefine.SkyworthBroadcastKey;
import com.skyworth.framework.skysdk.ipc.SkyApplication.SkyCmdConnectorListener;
import com.skyworth.framework.skysdk.util.SkyObjectByteSerialzie;
import com.skyworth.theme.SkyThemeEngine;
import com.skyworth.theme.ThemeColorSeriesEnum;
import com.tianci.media.api.SkyMediaApi;
import com.tianci.media.api.SkyMediaApiParam;
import com.tianci.media.base.SkyMediaItem;
import com.tianci.net.api.NetApiForCommon;
import com.tianci.net.command.TCNetworkBroadcast;
import com.tianci.net.data.SkyIpInfo;
import com.tianci.net.define.NetworkDefs;
import com.tianci.system.api.TCSystemService;
import com.tianci.system.command.TCSystemDefs;
import com.tianci.system.data.TCInfoSetData;
import com.tianci.system.data.TCSetData;
import com.tianci.system.define.SkyConfigDefs;
import com.tianci.system.define.TCEnvKey;
import com.tianci.user.api.SkyUserApi;
import com.tianci.user.api.SkyUserApi.AccountType;
import com.tianci.user.data.UserCmdDefine;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaBaseActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
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
    private static final String GET_DEVICE_INFO = "getDeviceInfo";//获取当前设备信息
    private static final String IS_NET_CONNECTED = "isNetConnected";//获取当前网路连接状态
    private static final String GET_NET_TYPE = "getNetType";//获取当前网络类型//有线、无线
    private static final String GET_IP_INFO = "getIpInfo";//获取当前网络的ip地址//内网
    private static final String GET_DEVICE_LOCATION = "getDeviceLocation";//获取当前设备的城市地址
    private static final String GET_USER_ACCESS_TOKEN = "getUserAccessToken";//获取用户的登录token
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
    /***************************************新添加*******************************************/
    private static final String GET_MOVIEPLATFORM_INFO = "getMoviePlatformInfo";
    private static final String GET_APP_INFO = "getAppInfo";
    private static final String GET_PROPERTY_VALUE = "getPropertiesValue";
    private static final String GET_WEBVIEWSDK_INFO = "getWebViewSDKInfo";
    private static final String GET_CURRENT_THEME = "getCurTheme";
    private static final String SET_FOCUS_POSITION = "setFocusPosition";
    private static final String NOTIFY_JS_MESSAGE = "notifyJSMessage";
    private static final String NOTIFY_JS_LOG = "notifyJSLogInfo";

    private static Context mContext;
    private CoocaaOSApiListener mCoocaaListener;

    private volatile boolean isCmdBindSuccess = false;

    private MyTableDownloadListener mDownloadListenrer;
    private MyTableMoniteDownloadListener mProcessListener;
    private SkyMediaApi mediaApi;
    
    private CallbackBroadcastReceiver mCallbackBC = null;
    private static final String PAY_ACTION = "coocaa.webviewsdk.action.pay";  

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

        Log.v("WebViewSDK", TAG + "CoocaaOSApi initialization");
        cordova.setPluginImlListener(this);  
             
        if (mCallbackBC == null)
        	mCallbackBC = new CallbackBroadcastReceiver();
        
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
	                 broadCastPurchase(jsonObject);
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
            broadCastAppDownloadTaskChangged(jsonObject);
        }

        @Override
        public void onStartDownloading(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_DOWNLOADING);
            broadCastAppDownloadTaskChangged(jsonObject);
        }

        @Override
        public void onPaused(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_PAUSED);
            broadCastAppDownloadTaskChangged(jsonObject);
        }

        @Override
        public void onComplete(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_COMPLETE);
            broadCastAppDownloadTaskChangged(jsonObject);
        }

        @Override
        public void onRemoved(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_REMOVED);
            broadCastAppDownloadTaskChangged(jsonObject);

        }

        @Override
        public void onStopped(long id, int code, String extra) {
            JSONObject jsonObject = getCallBackJson(id, code, extra, TableDownload.DOWNLOAD_STATUS.ON_STOPPED);
            broadCastAppDownloadTaskChangged(jsonObject);
        }

        @Override
        public void onDownloading(TableDownload t) {
            if(t!=null)
            {
                put(Long.valueOf(t.getId()),t);
                JSONObject jsonObject = getCallBackJson(t.getId(), t.getOncode(), t.getOnextra(), TableDownload.DOWNLOAD_STATUS.ON_DOWNLOADING);
                broadCastAppDownloadTaskChangged(jsonObject);
            }
        }
        
        public void onProcess(TableDownload t) {
            if(t!=null)
            {
                JSONObject jsonObject = getCallBackJson(t.getId(), t.getOncode(), t.getOnextra(), t.getStatus(),t.getProgress());
                broadCastAppDownloadTaskChangged(jsonObject);
            }
        }
    }
    
    public void onCmdInit()
    {
    	Log.i("WebViewSDK","CoocaaOSApi onCmdInit");
    	isCmdBindSuccess = true;
    	if(mCoocaaListener == null){
    		Log.i("WebViewSDK","CoocaaOSApi onCmdInit CordovaBaseActivity.getCmdConnectorListener()="+CordovaBaseActivity.getCmdConnectorListener());
    		mCoocaaListener = new CoocaaOSApiListener(CordovaBaseActivity.getCmdConnectorListener());
    	}
    }
    
    public byte[] onHandler(String fromtarget, String cmd, byte[] body) {
    	return mCoocaaListener.onHandler(fromtarget, cmd, body);
    }
    
    public class CoocaaOSApiListener /*implements SkyCmdProcessInstance.SkyCmdProcessInstanceListener*/
    {
        private TCSystemService systemApi;
        private NetApiForCommon netApi;
        private SkyUserApi userApi;
        public CoocaaOSApiListener(SkyCmdConnectorListener listener)
        {
            systemApi = new TCSystemService(listener);
            netApi = new NetApiForCommon(listener);
            userApi = new SkyUserApi(listener);
            mediaApi = new SkyMediaApi(listener);
            mediaApi.setContext(mContext);
        }

        public JSONObject isNetConnected()
        {
            if(netApi!=null && isCmdBindSuccess)
            {
                boolean isConnect = netApi.isConnect();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("isnetworking", String.valueOf(isConnect));
                    return jsonObject;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        
        public void startQQAcount()
        {
            if(userApi!=null && isCmdBindSuccess)
            {
            	userApi.loginByType(AccountType.qq);
            }
        }
        
        public JSONObject getUserAccessToken()
        {
        	if(userApi!=null && isCmdBindSuccess)
            {
            	  String token = userApi.getToken("ACCESS");
                  if(token!=null)
                  {
                  	 JSONObject jsonObject = new JSONObject();
                       try {
                           jsonObject.put("accesstoken", token);
                           return jsonObject;
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                  }
            }
        	return null;
        }

        public JSONObject getNetType()
        {
            if(netApi!=null && isCmdBindSuccess)
            {
                String netType = netApi.getNetType();
                if(netType!=null)
                {
                	 JSONObject jsonObject = new JSONObject();
                     try {
                         jsonObject.put("nettype", netType);
                         return jsonObject;
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                }
               
            }
            return null;
        }

        public JSONObject getIpInfo()
        {
            if(netApi!=null && isCmdBindSuccess)
            {
                SkyIpInfo ipInfo = netApi.getIpInfo();
                if(ipInfo!=null)
                {
                	 JSONObject jsonObject = new JSONObject();
                     try {
                         jsonObject.put("dns0", ipInfo.dns0);
                         jsonObject.put("dns1", ipInfo.dns1);
                         jsonObject.put("gateway", ipInfo.gateway);
                         jsonObject.put("ip", ipInfo.ip);
                         jsonObject.put("mac", ipInfo.mac);
                         jsonObject.put("netmask", ipInfo.netmask);
                         return jsonObject;
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                }
               
            }
            return null;
        }

        public JSONObject getLocation()
        {
            if(systemApi!=null && isCmdBindSuccess)
            {
                TCSetData locationData = systemApi.getSetData(TCEnvKey.SKY_SYSTEM_ENV_LOCATION);
                if(locationData!=null)
                {
                	TCInfoSetData locInfoData = (TCInfoSetData) locationData;
                    String locationString = null;
                    if (locInfoData!=null)
                    {
                        locationString = locInfoData.getCurrent();
                        Log.v(TAG,"location String=" + locationString);
                        try {
                            JSONObject jsonObj = new  JSONObject();
                            jsonObj.put("location",locationString);
                            return  jsonObj;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
            }
            return null;
        }

        public JSONObject getDeviceInfo()
        {
            if(systemApi!=null && isCmdBindSuccess)
            {
                //屏幕尺寸
                TCSetData pannelSetData = systemApi.getSetData(TCEnvKey.SKY_SYSTEM_ENV_PANEL_SIZE);
                String pannelString = "";
                if(pannelSetData != null)
                {

                    TCInfoSetData pannelInfoData = (TCInfoSetData) pannelSetData;
                    pannelString = pannelInfoData.getCurrent();
                }

                //酷开版本号
                TCSetData verSetData = systemApi.getSetData(TCEnvKey.SKY_SYSTEM_ENV_TIANCI_VER );
                String verString = "";
                if(verSetData!=null)
                {
                    TCInfoSetData verInfoData = (TCInfoSetData) verSetData;
                    verString = verInfoData.getCurrent();
                }

                // 机芯
                TCSetData modelSetData = systemApi.getSetData(TCEnvKey.SKY_SYSTEM_ENV_MODEL );
                String modelString = "";
                if(modelSetData!=null)
                {
                	 TCInfoSetData modelInfoData = (TCInfoSetData) modelSetData;
                     modelString = modelInfoData.getCurrent();
                }

                // 机型
                TCSetData typeSetData = systemApi.getSetData(TCEnvKey.SKY_SYSTEM_ENV_TYPE  );
                String typeString = "";
                if(typeSetData!=null)
                {
                    TCInfoSetData typeInfoData = (TCInfoSetData) typeSetData;
                    typeString = typeInfoData.getCurrent();
                }

                //MAC
                TCSetData macSetData = systemApi.getSetData(TCEnvKey.SKY_SYSTEM_ENV_MAC );
                String macString = "";
                if(macSetData!=null)
                {
                    TCInfoSetData macInfoData = (TCInfoSetData) macSetData;
                    macString = macInfoData.getCurrent();
                }

                //chip id
                TCSetData chipSetData = systemApi.getSetData(TCEnvKey.SKY_SYSTEM_ENV_CHIPID );
                String chipString = "";
                if(chipSetData!=null)
                {
                    TCInfoSetData chipInfoData = (TCInfoSetData) chipSetData;
                    chipString = chipInfoData.getCurrent();
                }

                //设备id
                TCSetData devidSetData = systemApi.getSetData(TCEnvKey.SKY_SYSTEM_ENV_MACHINE_CODE );
                TCInfoSetData devidInfoData = null;
                if(devidSetData!=null)
                {
                	devidInfoData = (TCInfoSetData) devidSetData;
                }
                String devidString = null;
                if(devidInfoData!=null)
                {
                    devidString = devidInfoData.getCurrent();
                }

                //激活id
                TCSetData activeidSetData = systemApi.getSetData(TCEnvKey.SKY_SYSTEM_ENV_ACTIVE_ID );
                TCInfoSetData activeidInfoData  = null;
                if(activeidSetData!=null)
                {
                	activeidInfoData = (TCInfoSetData) activeidSetData;
                }
                String  activeidString = null;
                if(activeidInfoData!=null)
                {
                    activeidString = activeidInfoData.getCurrent();
                }
                
                TCSetData emmcidSetData = systemApi.getSetData(SkyConfigDefs.SKY_CFG_EMMC_CID);
                TCInfoSetData emmcidInfoData  = null;
                if(emmcidSetData != null)
                {
                	emmcidInfoData = (TCInfoSetData) emmcidSetData;
                }
                String emmcidString = "";
                if(emmcidInfoData != null)
                {
                	emmcidString = emmcidInfoData.getCurrent();
                }
                
                if(verString !=null && verString.length()>0 && typeString!=null && typeString.length()>0)
                {
                	  JSONObject jsonObject = new JSONObject();
                      try {
                          jsonObject.put("panel", pannelString);
                          jsonObject.put("version", verString);
                          jsonObject.put("model", typeString);
                          jsonObject.put("chipid", chipString);
                          jsonObject.put("mac", macString);
                          jsonObject.put("chip", modelString);
                          jsonObject.put("androidsdk",android.os.Build.VERSION.SDK_INT);
                          jsonObject.put("devid",devidString);
                          jsonObject.put("activeid",activeidString);
                          jsonObject.put("emmcid", emmcidString);
                          jsonObject.put("brand", SystemProperties.get("ro.product.brand"));
                          return jsonObject;
                      } catch (JSONException e) {
                          e.printStackTrace();
                      }
                }
            }
            return null;
        }

        public JSONObject hasUserLogin()
        {
            if(userApi!=null && isCmdBindSuccess)
            {
                boolean isLogin = userApi.hasLogin();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("haslogin", String.valueOf(isLogin));
                    return jsonObject;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        public JSONObject getLoginUserInfo()
        {
            if(userApi!=null && isCmdBindSuccess)
            {
                Map<String, Object> userInfo= userApi.getAccoutInfo();
                JSONObject jsonObject = CoocaaUserInfoParser.parseUserInfo(userInfo);
                return jsonObject;
            }
            return null;
        }

        private JSONObject getEthEventString(String type,NetworkDefs.EthEvent event)
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

        private JSONObject getWifiEventString(String type,NetworkDefs.WifiEvent event)
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

        public byte[] onHandler(String fromtarget, String cmd, byte[] body) {
        	
            if(TCSystemDefs.TCSystemBroadcast.TC_SYSTEM_BROADCAST_MEDIA_MOUNTED //外接设备接入
                    .toString().equals(cmd))
            {
                String path = SkyObjectByteSerialzie.toObject(body,String.class);
                broadCastUsbChangged(true,path==null?"":path);
            }
            else if(TCSystemDefs.TCSystemBroadcast.TC_SYSTEM_BROADCAST_MEDIA_REMOVED //外接设备拔出
                    .toString().equals(cmd))
            {
               String path = SkyObjectByteSerialzie.toObject(body,String.class);
                broadCastUsbChangged(false,path==null?"":path);
            }
            else if(TCNetworkBroadcast.TC_NETWORK_BROADCAST_NET_ETH_EVENT
                    .toString().equals(cmd))
            {
                NetworkDefs.EthEvent ethEvnet = SkyObjectByteSerialzie.toObject(body,NetworkDefs.EthEvent.class);
                if(ethEvnet!=null)
                {
                    JSONObject mJson = getEthEventString("ethnet",ethEvnet);
                    if(mJson!=null)
                    {
                        broadCastNetChangged(mJson);
                    }
                }
            }
            else if(TCNetworkBroadcast.TC_NETWORK_BROADCAST_NET_WIFI_EVENT
                    .toString().equals(cmd))
            {
                NetworkDefs.WifiEvent wifiEvent = SkyObjectByteSerialzie.toObject(body,NetworkDefs.WifiEvent.class);
                switch(wifiEvent)
                {
                case EVENT_WIFI_CONNECT_SUCCEEDED:
                case EVENT_WIFI_CONNECT_DISCONNECTED:
                	 JSONObject mJson = getWifiEventString("wifi", wifiEvent);
                     if(mJson!=null)
                     {
                         broadCastNetChangged(mJson);
                     }
                	break;
                default:
                	break;
                }
            }
            else if(UserCmdDefine.ACCOUNT_CHANGED.toString().equals(cmd))
            {
            	broadCastUesrChangged();
            }

            return new byte[0];
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
        
        if(mDownloadListenrer!=null)
        {
            TableDownload._destroyTableDownloadListener(mContext,mDownloadListenrer);
            mDownloadListenrer.clear();
            mDownloadListenrer = null;
        }
        if(mCallbackBC != null)
        	mContext.unregisterReceiver(mCallbackBC);
        if(mCoocaaListener != null)
        	mCoocaaListener = null;

        mContext = null;
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
                	Log.i("WebViewSDK","WAIT_OS_READY isCmdBindSuccess:" + isCmdBindSuccess);
                    while(!isCmdBindSuccess/* && mRef <= 1*/)
                    {
                        try {
                            Thread.sleep(50);
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
        	if(paramObj != null){
        		String strPos = paramObj.getString("focusposition");
        		int iPos = 0;
        		try{
        			iPos = Integer.parseInt(strPos);
        			SystemWebViewSDK.setFocusPosition(iPos);
        		}catch(Exception e){
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
        else if(NOTIFY_JS_MESSAGE.equals(action))
        {
			JSONObject paramObj = args.getJSONObject(0);
			String data = "";
			if(paramObj != null){
				data = paramObj.getString("webInfo");
			}
        	Intent intent = new Intent("notify.js.message");
        	intent.putExtra("key", data);
        	LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        	callbackContext.success();
        	return true;
        }
        else if(LAUNCH_ONLINE_MOVIE_PLAYER.equals(action))
        {
            if(mCoocaaListener!=null && mediaApi!=null)
            {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            JSONObject urlObj = args.getJSONObject(0);
                            JSONObject nameObj = args.getJSONObject(1);
                            JSONObject needParseObj = args.getJSONObject(2);
                            JSONObject urlTypeObj = args.getJSONObject(3);
                            String urlType = urlTypeObj.getString("urlType");
                            SkyMediaItem[] items = new SkyMediaItem[1];
                            SkyMediaItem item = new SkyMediaItem();
                            item.type = SkyMediaItem.SkyMediaType.MOVIE;
                            
                            String needParseString = "false";
                            if(needParseObj!=null)
                            {
                            	needParseString = needParseObj.getString("needparse");
                            }
                            
                            if("true".equals(needParseString) || "false".equals(needParseString))
                            {
                                item.setNeedParse(Boolean.valueOf(needParseString));
                            }
                            else
                            {
                                item.setNeedParse(false);
                            }
                            item.url = urlObj.getString("url");
                            item.name = nameObj.getString("name");
                            item.extra.put("url_type", urlType);
                            items[0] = item;
                            SkyMediaApiParam param = new SkyMediaApiParam();
                            param.setPlayList(items,0);
                            mediaApi.startOnlinePlayer(param);
                            callbackContext.success();
                        }catch(JSONException e)
                        {
                            callbackContext.error(e.toString());
                        }
                    }
                });
            }
            else
            {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if (GET_USER_INFO.equals(action))
        {
            if(mCoocaaListener!=null)
            {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject successobj = mCoocaaListener.getLoginUserInfo();
                        if(successobj!=null)
                        {
                            callbackContext.success(successobj);
                        }
                        else
                        {
                            callbackContext.error("error occurs when called getLoginUserInfo");
                        }
                    }
                });
            }
            else
            {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if (GET_DEVICE_INFO.equals(action))
        {
            if(mCoocaaListener!=null)
            {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject successobj = mCoocaaListener.getDeviceInfo();
                        if(successobj!=null)
                        {
                            callbackContext.success(successobj);
                        }
                        else
                        {
                            callbackContext.error("error occurs when called getDeviceInfo");
                        }
                    }
                });
            }
            else
            {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(GET_MOVIEPLATFORM_INFO.equals(action))
        {
        	PackageManager pm = this.cordova.getActivity().getPackageManager();
        	String versionName = "";
        	int versionCode  = 0;
        	
        	try {
        		PackageInfo info = pm.getPackageInfo("com.tianci.movieplatform", 0);
        		versionName = info.versionName;
        		versionCode = info.versionCode;
        		JSONObject result = new JSONObject();
        		result.put("versionName", versionName);
        		result.put("versionCode", versionCode);
        		result.put("packageName", "com.tianci.movieplatform");
                callbackContext.success(result);

			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				callbackContext.error("error occurs when called getMovieplatformInfo");
			}
            return true;
        }
        else if(GET_APP_INFO.equals(action))
        {
        	PackageManager pm = this.cordova.getActivity().getPackageManager();
        	String versionName = "";
        	int versionCode  = 0;
        	
        	try {
        		JSONObject pkgNameObj = args.getJSONObject(0);
        		String packageName = pkgNameObj.getString("packageName");
        		PackageInfo info = pm.getPackageInfo(packageName, 0);
        		if(info == null){
        			callbackContext.error("this App is not installed :" + packageName);
        		}else{
            		versionName = info.versionName;
            		versionCode = info.versionCode;
            		JSONObject result = new JSONObject();
            		result.put("versionName", versionName);
            		result.put("versionCode", versionCode);
                    callbackContext.success(result);
        		}
			} catch (NameNotFoundException e) {
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
				e.printStackTrace();
				callbackContext.error("error occurs when called getCurTheme");
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
        else if(IS_NET_CONNECTED.equals(action))
        {
            if(mCoocaaListener!=null)
            {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject successobj = mCoocaaListener.isNetConnected();
                        if(successobj!=null)
                        {
                            callbackContext.success(successobj);
                        }
                        else
                        {
                            callbackContext.error("error occurs when called isNetConnected");
                        }
                    }
                });
            }
            else
            {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(GET_NET_TYPE.equals(action))
        {
            if(mCoocaaListener!=null)
            {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject successobj = mCoocaaListener.getNetType();
                        if(successobj!=null)
                        {
                            callbackContext.success(successobj);
                        }
                        else
                        {
                            callbackContext.error("error occurs when called getNetType");
                        }
                    }
                });
            }
            else
            {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(GET_DEVICE_LOCATION.equals(action))
        {
            if(mCoocaaListener!=null)
            {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject successobj = mCoocaaListener.getLocation();
                        if(successobj!=null)
                        {
                            callbackContext.success(successobj);
                        }
                        else
                        {
                            callbackContext.error("error occurs when called getNetType");
                        }
                    }
                });
            }
            else
            {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(GET_IP_INFO.equals(action))
        {
            if(mCoocaaListener!=null)
            {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject successobj = mCoocaaListener.getIpInfo();
                        if(successobj!=null)
                        {
                            callbackContext.success(successobj);
                        }
                        else
                        {
                            callbackContext.error("error occurs when called getIpInfo");
                        }
                    }
                });
            }
            else
            {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if (HAS_USER_LOGIN.equals(action))
        {
            if(mCoocaaListener!=null)
            {
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject successobj = mCoocaaListener.hasUserLogin();
                        if(successobj!=null)
                        {
                            callbackContext.success(successobj);
                        }
                        else
                        {
                            callbackContext.error("error occurs when called hasUserLogin");
                        }
                    }
                });
            }
            else
            {
                callbackContext.error("mCoocaaListener is not ready!");
            }
            return true;
        }
        else if(START_QQ_ACOUNT.equals(action))
        {
        	 if(mCoocaaListener!=null)
             {
                 this.cordova.getThreadPool().execute(new Runnable() {
                     @Override
                     public void run() {
                        mCoocaaListener.startQQAcount();
                        callbackContext.success();
                     }
                 });
             }
             else
             {
                 callbackContext.error("mCoocaaListener is not ready!");
             }
             return true;
        }
        else if(GET_USER_ACCESS_TOKEN.equals(action))
        {
        	   if(mCoocaaListener!=null)
               {
                   this.cordova.getThreadPool().execute(new Runnable() {
                       @Override
                       public void run() {
                           JSONObject successobj = mCoocaaListener.getUserAccessToken();
                           if(successobj!=null)
                           {
                               callbackContext.success(successobj);
                           }
                           else
                           {
                               callbackContext.error("error occurs when called getUserAccessToken");
                           }
                       }
                   });
               }
               else
               {
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
        return false;
    }

    /*
    * 网络状态发生变化的回调
     */
    private void broadCastNetChangged(JSONObject myObject)
    {
        final Intent intent = new Intent(BROADCAST_NETCHANGED);

        Bundle b = new Bundle();
        b.putString( "userdata", myObject.toString());
        intent.putExtras(b);

        LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
    }

    /*
       * u盘发生变化的回调
        */
    private void broadCastUsbChangged(boolean insert,String usbpath)
    {
        final Intent intent = new Intent(BROADCAST_USBCHANGGED);
        Bundle b = new Bundle();
        b.putString( "userdata", "{'usbmount':'"+(insert?"true":"false")+ "','mountpath':'"+ usbpath + "'}" );
        intent.putExtras( b);
        LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
    }
    
    /*
     * 用户登录发生改变的广播
     */
    private void broadCastUesrChangged()
    {
    	 final Intent intent = new Intent(BROADCAST_USERCHANGGED);
         Bundle b = new Bundle();
         b.putString( "userdata", "{'userchangged':'true'}" );
         intent.putExtras(b);
         LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
    }

    /*
     * 下载任务回调
     */
    private void broadCastAppDownloadTaskChangged(JSONObject myObject)
    {
        if(myObject!=null)
        {
            final Intent intent = new Intent(BROADCAST_APPDOWNLOADTASK);
            Bundle b = new Bundle();
            b.putString( "userdata", myObject.toString());
            intent.putExtras( b);
            LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
        }
    }
    
    /*
     * 支付信息回调
     */
    private void broadCastPurchase(JSONObject myObject)
    {
    	if(myObject!=null)
    	{
    		final Intent intent = new Intent(BROADCAST_PURCHASE);
    		Bundle b = new Bundle();
    		b.putString( "userdata", myObject.toString());
    		intent.putExtras( b);
    		LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
    	}
    }

    public static void broadCastCommonChanged(JSONObject myObject)
    {
        if(myObject!=null)
        {
            final Intent intent = new Intent(BROADCAST_COMMON_CHANGED);
            Bundle b = new Bundle();
            b.putString("userdata", myObject.toString());
            intent.putExtras(b);
            LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
        }
    }
    
}