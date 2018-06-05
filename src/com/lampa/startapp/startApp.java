/**
	com.lampa.startapp
	https://github.com/lampaa/com.lampa.startapp
	
	Phonegap 3 plugin for check or launch other application in android device (iOS support).
	bug tracker: https://github.com/lampaa/com.lampa.startapp/issues
*/
package com.lampa.startapp;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import java.util.Iterator;
import android.net.Uri;
import android.util.Log;

/**
 * This class provides access to vibration on the device.
 */
public class startApp extends CordovaPlugin {

    /**
     * Constructor.
     */
    public startApp() { }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArray of arguments for the plugin.
     * @param callbackContext   The callback context used when calling back into JavaScript.
     * @return                  True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            this.start(args, callbackContext);
        }
		else if(action.equals("check")) {
			this.check(args.getString(0), callbackContext);
		}
		else if(action.equals("play")) {
			this.play(args,callbackContext);
		}
		
		return true;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------
    /**
     * startApp
     */
    public void start(JSONArray args, CallbackContext callback) {
		
		Intent LaunchIntent;
		
		String com_name = null;
		
		String activity = null;
		String spackage = null;
		String intetype = null;
		String intenuri = null;
		String intentFlag = null;
		
		try {
			if (args.get(0) instanceof JSONArray) {
				com_name = args.getJSONArray(0).getString(0);
				activity = args.getJSONArray(0).getString(1);
				
				if(args.getJSONArray(0).length() > 2) {
					spackage = args.getJSONArray(0).getString(2);
				}

				if(args.getJSONArray(0).length() > 3) {
					intetype = args.getJSONArray(0).getString(3);					
				}
				
				if(args.getJSONArray(0).length() > 4) {
					intenuri = args.getJSONArray(0).getString(4);
				}

				if(args.getJSONArray(0).length() > 5) {
					intentFlag = args.getJSONArray(0).getString(5);
				}
			}
			else {
				com_name = args.getString(0);
			}
						 
			Log.i("WebViewSDK","com_name = " + com_name + ",activity = " + activity + ",spackage = " + spackage + ",intetype = " + intetype + ",intenuri = " + intenuri + ",intentFlag = " + intentFlag);
			 
			/**
			 * call activity
			 */
			
			if(activity != null && activity.length() != 0) {
				if(com_name.equals("action")) {
					/**
					 * . < 0: VIEW
					 * . >= 0: android.intent.action.VIEW
					 */
					if(activity.indexOf(".") < 0) {
						activity = "android.intent.action." + activity;
					}
					
					// if uri exists
					if(intenuri != null && intenuri.length() != 0) {
						LaunchIntent = new Intent(activity, Uri.parse(intenuri));
					}
					else {
						LaunchIntent = new Intent(activity);
					}
				}
				else {
					LaunchIntent = new Intent();
					LaunchIntent.setComponent(new ComponentName(com_name, activity));
				}
			}
			else {
				LaunchIntent = this.cordova.getActivity().getPackageManager().getLaunchIntentForPackage(com_name);
			}
			
			/**
			 * setPackage, http://developer.android.com/intl/ru/reference/android/content/Intent.html#setPackage(java.lang.String)
			 */
			if(spackage != null && spackage.length() != 0) {
				LaunchIntent.setPackage(spackage);
			}

			/**
			 * setType, http://developer.android.com/intl/ru/reference/android/content/Intent.html#setType(java.lang.String)
			 */
			if(intetype != null && intetype.length() != 0) {
				LaunchIntent.setType(intetype);
			}

			if(intentFlag != null && intentFlag.length() != 0) {
				if("new_task".equals(intentFlag)){
					LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				}else if("multi_task".equals(intentFlag)){
					LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				}
			}

			/**
			 * put arguments
			 */
			if(args.length() > 1) {
				JSONArray params = args.getJSONArray(1);
				String key;
				Object value;
				
				for(int i = 0; i < params.length(); i++) {
					if (params.get(i) instanceof JSONObject) {
						Iterator<String> iter = params.getJSONObject(i).keys();

						 while (iter.hasNext()) {
							key = iter.next();
							try {
								JSONObject obj = params.getJSONObject(i);
								if(obj.optBoolean(key))
								{
									value = obj.getBoolean(key);
									LaunchIntent.putExtra(key, (Boolean)value);
								}
								else 
								{
									value = obj.getString(key);
									LaunchIntent.putExtra(key, (String)value);
								}
							} catch (JSONException e) {
								callback.error("json params: " + e.toString());
							}
						}
					}
					else {
						LaunchIntent.setData(Uri.parse(params.getString(i)));
					}
				}
			}	
			
			if(args.length() > 2) {
				
				String intentKey = null;
				Intent paramIntent = null;
				
				if(args.get(2) instanceof JSONArray) {
					JSONArray params = args.getJSONArray(2);
					intentKey = params.getString(0);
					String intentType = params.getString(1);
					String intentValue = params.getString(2);
					
					Log.i("WebViewSDK","intentKey == " + intentKey);
					Log.i("WebViewSDK","intentType == " + intentType);
					Log.i("WebViewSDK","intentValue == " + intentValue);
					
					paramIntent = new Intent();
					
					if(intentType.equals("action")){
						paramIntent.setAction(intentValue);
					}else{
						paramIntent.setComponent(new ComponentName(intentType, intentValue));
					}
					
					String key;
					Object value;
					
					for(int i = 3; i < params.length(); i++) {
						if (params.get(i) instanceof JSONObject) {
							Iterator<String> iter = params.getJSONObject(i).keys();

							 while (iter.hasNext()) {
								key = iter.next();
								try {
									JSONObject obj = params.getJSONObject(i);
									if(obj.optBoolean(key))
									{
										value = obj.getBoolean(key);
										paramIntent.putExtra(key, (Boolean)value);
									}
									else 
									{
										value = obj.getString(key);
										paramIntent.putExtra(key, (String)value);
									}
									Log.i("WebViewSDK","key == " + key + ", value == " + value);
								} catch (JSONException e) {
									callback.error("json params: " + e.toString());
								}
							}
						}
					}
				}	
				
				if(intentKey != null && paramIntent != null){
					LaunchIntent.putExtra(intentKey, paramIntent);
				}
			}
			
			/**
			 * start activity
			 */
			this.cordova.getActivity().startActivity(LaunchIntent);
			callback.success();
			
		} catch (JSONException e) {
			callback.error("json: " + e.toString());
		} catch (Exception e) {
			callback.error("intent: " + e.toString());
        }
    }

    /**
     * checkApp
     */	 
	public void check(String component, CallbackContext callback) {
		PackageManager pm = this.cordova.getActivity().getApplicationContext().getPackageManager();
		try {
			/**
			 * get package info
			 */
			PackageInfo PackInfo = pm.getPackageInfo(component, PackageManager.GET_ACTIVITIES);
			
			/**
			 * create json object
			 */
			JSONObject info = new JSONObject();
			
			info.put("versionName", PackInfo.versionName);
			info.put("packageName", PackInfo.packageName);
			info.put("versionCode", PackInfo.versionCode);
			info.put("applicationInfo", PackInfo.applicationInfo);
			
			callback.success(info);
		} catch (Exception e) {
			callback.error(e.toString());
		}
	}
	
	public void play(JSONArray args, CallbackContext callback) {
		
		Intent launchIntent = new Intent();
		String strAction = null;
		String strPkgName = null;
		try {
			strAction = args.getString(0);
			strPkgName = args.getString(1);
			if(strAction != null && strPkgName != null) {
				
				launchIntent.setAction(strAction);
				launchIntent.setPackage(strPkgName);
				
				Log.i("WebViewSDK","action = " + strAction + ",package = " + strPkgName);
				
				if(args.length() > 2) {
					JSONArray params = args.getJSONArray(2);
					String key;
					Object value;
					
					for(int i = 0; i < params.length(); i++) {
						if (params.get(i) instanceof JSONObject) {
							Iterator<String> iter = params.getJSONObject(i).keys();

							 while (iter.hasNext()) {
								key = iter.next();
								try {
									JSONObject obj = params.getJSONObject(i);
									if(obj.optBoolean(key))
									{
										value = obj.getBoolean(key);
										launchIntent.putExtra(key, (Boolean)value);
									}
									else 
									{
										value = obj.getString(key);
										launchIntent.putExtra(key, (String)value);
									}
								} catch (JSONException e) {
									callback.error("json params: " + e.toString());
								}
							}
						}
					}
				}	
				this.cordova.getActivity().startService(launchIntent);
				callback.success();
			}
		} catch (Exception e) {
			callback.error("intent: " + e.toString());
		}
	}
}
