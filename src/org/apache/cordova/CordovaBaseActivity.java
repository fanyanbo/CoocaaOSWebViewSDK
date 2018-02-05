package org.apache.cordova;


import android.os.Bundle;
import android.util.Log;

import com.coocaa.cordova.plugin.CoocaaOSApi;
import com.skyworth.framework.skysdk.ipc.SkyActivity;
import com.skyworth.framework.skysdk.ipc.SkyApplication.SkyCmdConnectorListener;
import com.skyworth.framework.skysdk.ipc.SkyCmdProcessInstance;
import com.skyworth.framework.skysdk.ipc.SkyCmdProcessInstance.SkyCmdProcessInstanceListener;

public class CordovaBaseActivity extends SkyActivity{
	
	
	private static final String Tag = "WebViewSDK";
    protected CordovaPlugin currentPlugin = null;
    protected static boolean isCmdListenerReady = false;
    protected static SkyCmdConnectorListener listener;
	
	public void setPlugin(CordovaPlugin plugin){
		Log.i(Tag, "CordovaBaseActivity2 setPlugin");  
    	currentPlugin = plugin;
    	if(isCmdListenerReady){
    		((CoocaaOSApi)currentPlugin).onCmdInit();
    	}
	}
	
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
        isCmdListenerReady = true;
        if(currentPlugin!=null)
    	{
    		((CoocaaOSApi)currentPlugin).onCmdInit();
    	}
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
    	if(currentPlugin!=null)
    	{
    		return ((CoocaaOSApi)currentPlugin).onHandler(fromtarget, cmd, body);
    	}
    	return null;
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
