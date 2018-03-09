/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package org.apache.cordova;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;

import org.coocaa.webview.CoocaaOSConnecter;
import org.json.JSONException;
import org.json.JSONObject;

import com.skyworth.systemwebview.extra.SysWebviewCompatLayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default implementation of CordovaInterface.
 */
public class CordovaInterfaceImpl implements CordovaInterface {
    private static final String TAG = "CordovaInterfaceImpl";
    protected Activity activity;
    protected ExecutorService threadPool;
    protected PluginManager pluginManager;

    protected ActivityResultHolder savedResult;
    public CordovaPlugin activityResultCallback;
    protected CordovaPlugin permissionResultCallback;
    protected String initCallbackService;
    protected int activityResultRequestCode;
    protected boolean activityWasDestroyed = false;
    protected Bundle savedPluginState;

    @Override
	public void setPluginImlListener(CordovaPlugin plugin) {
    	Log.i("WebViewSDK","CordovaInterfaceImpl setPluginImlListener");
    	((CordovaBaseActivity)this.activity).setPlugin(plugin);
	}

	public CordovaInterfaceImpl(Activity activity) {
        this(activity, Executors.newCachedThreadPool());
    }

    public CordovaInterfaceImpl(Activity activity, ExecutorService threadPool) {
        this.activity = activity;
    //	this.context = context;
        this.threadPool = threadPool;
    }
    
    public interface CordovaInterfaceListener
    {
    	public void	onPageStarted(String url);
    	public void onPageLoadingFinished(String url);
    	public void onReceivedError(int errorCode, String description, String failingUrl);
    	public void doUpdateVisitedHistory(String url, boolean isReload);
    	public boolean shouldOverrideUrlLoading(String url);
    	public void onReceivedTitle(String title);
    	public void onReceivedIcon(Bitmap icon);
    	public void onProgressChanged(int process);
    }
    
    private CordovaInterfaceListener mCordovaListener;
    private CoocaaOSConnecter mCoocaaOSConnecter;
    //set Listener
    public void setCordovaInterfaceListener(CordovaInterfaceListener corListener)
    {
    	mCordovaListener = corListener;
    }
    
	@Override
	public void setCoocaaOSConnecter(CoocaaOSConnecter connecter) {
		// TODO Auto-generated method stub
		mCoocaaOSConnecter = connecter;
	}
	
	@Override
	public CoocaaOSConnecter getCoocaaOSConnecter() {
		// TODO Auto-generated method stub
		return mCoocaaOSConnecter;
	}

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        setActivityResultCallback(command);
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (RuntimeException e) { // E.g.: ActivityNotFoundException
            activityResultCallback = null;
            throw e;
        }
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        // Cancel any previously pending activity.
        if (activityResultCallback != null) {
            activityResultCallback.onActivityResult(activityResultRequestCode, Activity.RESULT_CANCELED, null);
        }
        activityResultCallback = plugin;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }
    
//    @Override
//    public Context getContext() {
//    	return context;
//    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("exit".equals(id)) {
            activity.finish();
        }
        else if("onPageStarted".equals(id))
        {
        	if(data!=null)
        	{
            	String url = data.toString();
            	if(mCordovaListener!=null)
            	{
            		mCordovaListener.onPageStarted(url);
            	}
        	}
        }
        else if("onPageFinished".equals(id))
        {
        	if(data!=null)
        	{
        		String url = data.toString();
        		if(mCordovaListener!=null)
            	{
            		mCordovaListener.onPageLoadingFinished(url);
            	}
        	}
        	
        }
        else if("onReceivedError".equals(id))
        {
        	if(data!=null)
        	{
        		JSONObject jsonData = (JSONObject) data;
        		try {
					int errorCode = jsonData.getInt("errorCode");
					String desString = jsonData.getString("description");
					String failUrl = jsonData.getString("url");
					if(mCordovaListener!=null)
					{
						mCordovaListener.onReceivedError(errorCode, desString, failUrl);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        	}
        }
        else if("onNavigationAttempt".equals(id))
        {
        	if(data!=null)
        	{
        		if(mCordovaListener!=null)
				{
        			String url = data.toString();
					return mCordovaListener.shouldOverrideUrlLoading(url);
				}
        	}
        }
        else if("onReceivedTitle".equals(id))
        {
        	if(mCordovaListener!=null)
        	{
        		String title = null;
        		if(data!=null)
        		{
        			title = data.toString();
        		}
        		mCordovaListener.onReceivedTitle(title);
			}
        }
        else if("onReceivedIcon".equals(id))
        {
        	if(mCordovaListener!=null)
        	{
        		Bitmap icon = null;
        		if(data!=null)
        		{
        			icon = (Bitmap) data;
        		}
        		mCordovaListener.onReceivedIcon(icon);
			}
        }
        else if("onProgressChanged".equals(id))
        {
        	if(mCordovaListener!=null)
        	{
        		int progessData = 0;
        		if(data!=null)
        		{
        			progessData =  (Integer) data;
        		}
        		mCordovaListener.onProgressChanged(progessData);
			}
        }
        else if("doUpdateVisitedHistory".equals(id))
        {
        	if(mCordovaListener!=null)
        	{
        		if(data!=null)
        		{
        			JSONObject jsonObj =   (JSONObject) data;
        			
					try {
						String url = jsonObj.getString("url");
	        			boolean isReload = jsonObj.getBoolean("isReload");
	        			mCordovaListener.doUpdateVisitedHistory(url, isReload);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		
			}
        }
        return null;
    }

    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * Dispatches any pending onActivityResult callbacks and sends the resume event if the
     * Activity was destroyed by the OS.
     */
    public void onCordovaInit(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        if (savedResult != null) {
            onActivityResult(savedResult.requestCode, savedResult.resultCode, savedResult.intent);
        } else if(activityWasDestroyed) {
            // If there was no Activity result, we still need to send out the resume event if the
            // Activity was destroyed by the OS
            activityWasDestroyed = false;
            if(pluginManager != null)
            {
                CoreAndroid appPlugin = (CoreAndroid) pluginManager.getPlugin(CoreAndroid.PLUGIN_NAME);
                if(appPlugin != null) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("action", "resume");
                    } catch (JSONException e) {
                        LOG.e(TAG, "Failed to create event message", e);
                    }
                    appPlugin.sendResumeEvent(new PluginResult(PluginResult.Status.OK, obj));
                }
            }

        }
    }

    /**
     * Routes the result to the awaiting plugin. Returns false if no plugin was waiting.
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        CordovaPlugin callback = activityResultCallback;
        if(callback == null && initCallbackService != null) {
            // The application was restarted, but had defined an initial callback
            // before being shut down.
            savedResult = new ActivityResultHolder(requestCode, resultCode, intent);
            if (pluginManager != null) {
                callback = pluginManager.getPlugin(initCallbackService);
                if(callback != null) {
                    callback.onRestoreStateForActivityResult(savedPluginState.getBundle(callback.getServiceName()),
                            new ResumeCallback(callback.getServiceName(), pluginManager));
                }
            }
        }
        activityResultCallback = null;

        if (callback != null) {
            Log.d(TAG, "Sending activity result to plugin");
            initCallbackService = null;
            savedResult = null;
            callback.onActivityResult(requestCode, resultCode, intent);
            return true;
        }
        Log.w(TAG, "Got an activity result, but no plugin was registered to receive it" + (savedResult != null ? " yet!" : "."));
        return false;
    }

    /**
     * Call this from your startActivityForResult() overload. This is required to catch the case
     * where plugins use Activity.startActivityForResult() + CordovaInterface.setActivityResultCallback()
     * rather than CordovaInterface.startActivityForResult().
     */
    public void setActivityResultRequestCode(int requestCode) {
        activityResultRequestCode = requestCode;
    }

    /**
     * Saves parameters for startActivityForResult().
     */
    public void onSaveInstanceState(Bundle outState) {
        if (activityResultCallback != null) {
            String serviceName = activityResultCallback.getServiceName();
            outState.putString("callbackService", serviceName);
        }
        if(pluginManager != null){
            outState.putBundle("plugin", pluginManager.onSaveInstanceState());
        }

    }

    /**
     * Call this from onCreate() so that any saved startActivityForResult parameters will be restored.
     */
    public void restoreInstanceState(Bundle savedInstanceState) {
        initCallbackService = savedInstanceState.getString("callbackService");
        savedPluginState = savedInstanceState.getBundle("plugin");
        activityWasDestroyed = true;
    }

    private static class ActivityResultHolder {
        private int requestCode;
        private int resultCode;
        private Intent intent;

        public ActivityResultHolder(int requestCode, int resultCode, Intent intent) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.intent = intent;
        }
    }

    /**
     * Called by the system when the user grants permissions
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
       /*  FIXME:  add (Build.VERSION.SDK_INT >= 23)
        * invoke  
        * 	onRequestPermissionResult
        * 
        if(permissionResultCallback != null)
        {
            permissionResultCallback.onRequestPermissionResult(requestCode, permissions, grantResults);
            permissionResultCallback = null;
        }
        */
    	if(Build.VERSION.SDK_INT >= 23)
    	{
    		 if(permissionResultCallback != null)
             {
    				Class<? extends CordovaPlugin> clazz = permissionResultCallback.getClass();
    				Method onRequestPermissionResult;
    				try {
    					onRequestPermissionResult = clazz.getDeclaredMethod(
    							"onRequestPermissionResult", new Class[]{Integer.TYPE, String[].class,int[].class});
    					onRequestPermissionResult.invoke(permissionResultCallback,new Object[]{requestCode,permissions,grantResults});
    				} catch (NoSuchMethodException e) {
    					e.printStackTrace();
    				} catch (IllegalAccessException e) {
    					e.printStackTrace();
    				} catch (IllegalArgumentException e) {
    					e.printStackTrace();
    				} catch (InvocationTargetException e) {
    					e.printStackTrace();
    				}
                 permissionResultCallback = null;
             }
    	}
    	
    }

    public void requestPermission(CordovaPlugin plugin, int requestCode, String permission) {
    	/* FIXME: add (Build.VERSION.SDK_INT >= 23)
    	 * 
        * 
        permissionResultCallback = plugin;
        String[] permissions = new String [1];
        permissions[0] = permission;
        getActivity().requestPermissions(permissions, requestCode);
        */
    	if(Build.VERSION.SDK_INT >= 23)
    	{
            permissionResultCallback = plugin;
            String[] permissions = new String [1];
            permissions[0] = permission;
            //getActivity().requestPermissions(permissions, requestCode);
            SysWebviewCompatLayer.activityRequestPermissions(getActivity(), permissions, requestCode);
    	}
    }

    public void requestPermissions(CordovaPlugin plugin, int requestCode, String [] permissions)
    {
    	/* FIXME:  add (Build.VERSION.SDK_INT >= 23)
        permissionResultCallback = plugin;
        getActivity().requestPermissions(permissions, requestCode);
        */
    	if(Build.VERSION.SDK_INT >= 23)
    	{
    		 permissionResultCallback = plugin;
    	     //getActivity().requestPermissions(permissions, requestCode);
    	     SysWebviewCompatLayer.activityRequestPermissions(getActivity(), permissions, requestCode);
    	}
    }

    public boolean hasPermission(String permission)
    {
    	/* FIXME: modify Build.VERSION_CODES.M ->23
    	 * 
    	 */
        if(Build.VERSION.SDK_INT >= 23)
        {
            //int result = activity.checkSelfPermission(permission);
            int result = SysWebviewCompatLayer.activitycheckSelfPermission(activity, permission);
            return PackageManager.PERMISSION_GRANTED == result;
        }
        else
        {
            return true;
        }
    }
}
