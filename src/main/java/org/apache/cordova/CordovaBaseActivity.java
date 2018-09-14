package org.apache.cordova;


import android.os.Bundle;
import android.util.Log;

import com.skyworth.framework.skysdk.ipc.SkyActivity;
import com.skyworth.framework.skysdk.ipc.SkyApplication.SkyCmdConnectorListener;
import com.skyworth.framework.skysdk.util.SkyObjectByteSerialzie;
import com.tianci.net.command.TCNetworkBroadcast;
import com.tianci.net.define.NetworkDefs;
import com.tianci.system.command.TCSystemDefs;
import com.tianci.user.data.UserCmdDefine;

import org.json.JSONObject;

import coocaa.plugin.api.CoocaaOSApi;

public class CordovaBaseActivity extends SkyActivity{
	
	
	private static final String Tag = "WebViewSDK";
    protected static SkyCmdConnectorListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Log.i(Tag, "CordovaBaseActivity2 onCreate");
	}
	
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	//	listener = null;
        Log.i(Tag, "CordovaBaseActivity2: onDestroy");
	}
    
	@Override
	public void onCmdConnectorInit() {
		// TODO Auto-generated method stub
        Log.i(Tag,"CordovaBaseActivity2 onCmdConnectorInit");
        listener = this;
        onSuperCmdInit();
	}
	
    public void onSuperCmdInit() {
    	
    }
	
	public static SkyCmdConnectorListener getCmdConnectorListener() {
    	return listener;
    }

	@Override
	public byte[] onHandler(String fromtarget, String cmd, byte[] body) {
		// TODO Auto-generated method stub
		if (TCSystemDefs.TCSystemBroadcast.TC_SYSTEM_BROADCAST_MEDIA_MOUNTED //外接设备接入
				.toString().equals(cmd)) {
			String path = SkyObjectByteSerialzie.toObject(body, String.class);
			CoocaaOSApi.broadCastUsbChangged(this, true, path == null ? "" : path);
		} else if (TCSystemDefs.TCSystemBroadcast.TC_SYSTEM_BROADCAST_MEDIA_REMOVED //外接设备拔出
				.toString().equals(cmd)) {
			String path = SkyObjectByteSerialzie.toObject(body, String.class);
			CoocaaOSApi.broadCastUsbChangged(this, false, path == null ? "" : path);
		} else if (TCNetworkBroadcast.TC_NETWORK_BROADCAST_NET_ETH_EVENT
				.toString().equals(cmd)) {
			NetworkDefs.EthEvent ethEvnet = SkyObjectByteSerialzie.toObject(body, NetworkDefs.EthEvent.class);
			if (ethEvnet != null) {
				JSONObject mJson = CoocaaOSApi.getEthEventString("ethnet", ethEvnet);
				if (mJson != null) {
					CoocaaOSApi.broadCastNetChangged(this,mJson);
				}
			}
		} else if (TCNetworkBroadcast.TC_NETWORK_BROADCAST_NET_WIFI_EVENT
				.toString().equals(cmd)) {
			NetworkDefs.WifiEvent wifiEvent = SkyObjectByteSerialzie.toObject(body, NetworkDefs.WifiEvent.class);
			switch (wifiEvent) {
				case EVENT_WIFI_CONNECT_SUCCEEDED:
				case EVENT_WIFI_CONNECT_DISCONNECTED:
					JSONObject mJson = CoocaaOSApi.getWifiEventString("wifi", wifiEvent);
					if (mJson != null) {
						CoocaaOSApi.broadCastNetChangged(this,mJson);
					}
					break;
				default:
					break;
			}
		} else if (UserCmdDefine.ACCOUNT_CHANGED.toString().equals(cmd)) {
			CoocaaOSApi.broadCastUesrChangged(this);
		}

		return new byte[0];
	}

	@Override
	public void onResult(String fromtarget, String cmd, byte[] body) {
		// TODO Auto-generated method stub

	}

	@Override
	@Deprecated
	public byte[] requestPause(String fromtarget, String cmd, byte[] body) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public byte[] requestResume(String fromtarget, String cmd, byte[] body) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public byte[] requestRelease(String fromtarget, String cmd, byte[] body) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public byte[] requestStartToVisible(String fromtarget, String cmd,
			byte[] body) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public byte[] requestStartToForground(String fromtarget, String cmd,
			byte[] body) {
		// TODO Auto-generated method stub
		return null;
	}



}
