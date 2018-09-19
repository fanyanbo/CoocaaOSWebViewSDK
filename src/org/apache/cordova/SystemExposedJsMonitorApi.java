package org.apache.cordova;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.skyworth.framework.skysdk.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fanyanbo on 2018/8/31.
 * Email: fanyanbo@skyworth.com
 */
public class SystemExposedJsMonitorApi {

    private static final String TAG = "PerformanceMonitor";
    @JavascriptInterface
    public void sendResourceTiming(String jsonStr) {
//        Log.i(TAG,"sendResourceTiming = " + jsonStr);
        try {
            JSONArray arr = new JSONArray(jsonStr);
            Log.d(TAG,"ResourceTiming==>length:" + arr.length());
            Log.d(TAG,"---------------------分割线----------------------");
            for(int i=0; i<arr.length(); i++){
                JSONObject jsonObj = new JSONObject(arr.get(i).toString());
                String name = jsonObj.getString("name");
                String entryType = jsonObj.getString("entryType");
                double startTime = Double.parseDouble(jsonObj.getString("startTime"));
                double duration = Double.parseDouble(jsonObj.getString("duration"));
                Log.d(TAG,i + " ResourceTiming==>资源名称:" + name);
                Log.d(TAG,i + " ResourceTiming==>类型:" + entryType);
                Log.d(TAG,i + " ResourceTiming==>请求开始事件:" + startTime);
                Log.d(TAG,i + " ResourceTiming==>资源请求耗时:" + duration);
                Log.d(TAG,"---------------------分割线----------------------");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void sendNavigationTiming(String jsonStr) {
//        Log.i(TAG,"sendNavigationTiming = " + jsonStr);
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            long navigationStart = Long.parseLong(jsonObj.getString("navigationStart"));
            long fetchStart = Long.parseLong(jsonObj.getString("fetchStart"));
            long domainLookupStart = Long.parseLong(jsonObj.getString("domainLookupStart"));
            long domainLookupEnd = Long.parseLong(jsonObj.getString("domainLookupEnd"));
            long connectStart = Long.parseLong(jsonObj.getString("connectStart"));
            long connectEnd = Long.parseLong(jsonObj.getString("connectEnd"));
            long secureConnectionStart = Long.parseLong(jsonObj.getString("secureConnectionStart"));
            long requestStart = Long.parseLong(jsonObj.getString("requestStart"));
            long responseStart = Long.parseLong(jsonObj.getString("responseStart"));
            long responseEnd = Long.parseLong(jsonObj.getString("responseEnd"));
            long domLoading = Long.parseLong(jsonObj.getString("domLoading"));
            long domInteractive = Long.parseLong(jsonObj.getString("domInteractive"));
            long domContentLoadedEventStart = Long.parseLong(jsonObj.getString("domContentLoadedEventStart"));
            long domContentLoadedEventEnd = Long.parseLong(jsonObj.getString("domContentLoadedEventEnd"));
            long domComplete = Long.parseLong(jsonObj.getString("domComplete"));
            long loadEventStart = Long.parseLong(jsonObj.getString("loadEventStart"));
            long loadEventEnd = Long.parseLong(jsonObj.getString("loadEventEnd"));
            Log.d(TAG,"========================分割线========================");
            Log.d(TAG,"NavigationTiming==>navigationStart:" + navigationStart);
            Log.d(TAG,"NavigationTiming==>fetchStart:" + fetchStart);
            Log.d(TAG,"NavigationTiming==>domainLookupStart:" + domainLookupStart);
            Log.d(TAG,"NavigationTiming==>domainLookupEnd:" + domainLookupEnd);
            Log.d(TAG,"NavigationTiming==>connectStart:" + connectStart);
            Log.d(TAG,"NavigationTiming==>secureConnectionStart:" + secureConnectionStart);
            Log.d(TAG,"NavigationTiming==>connectEnd:" + connectEnd);
            Log.d(TAG,"NavigationTiming==>requestStart:" + requestStart);
            Log.d(TAG,"NavigationTiming==>responseStart:" + responseStart);
            Log.d(TAG,"NavigationTiming==>responseEnd:" + responseEnd);
            Log.d(TAG,"NavigationTiming==>domLoading:" + domLoading);
            Log.d(TAG,"NavigationTiming==>domInteractive:" + domInteractive);
            Log.d(TAG,"NavigationTiming==>domContentLoadedEventStart:" + domContentLoadedEventStart);
            Log.d(TAG,"NavigationTiming==>domContentLoadedEventEnd:" + domContentLoadedEventEnd);
            Log.d(TAG,"NavigationTiming==>domComplete:" + domComplete);
            Log.d(TAG,"NavigationTiming==>loadEventStart:" + loadEventStart);
            Log.d(TAG,"NavigationTiming==>loadEventEnd:" + loadEventEnd);
            Log.d(TAG,"========================分割线========================");
            Log.d(TAG,"NavigationTiming==>DNS寻址耗时:" + (domainLookupEnd-domainLookupStart));
            Log.d(TAG,"NavigationTiming==>TCP连接耗时:" + (connectEnd-connectStart));
            Log.d(TAG,"NavigationTiming==>首包时间:" + (responseStart-navigationStart));
            Log.d(TAG,"NavigationTiming==>HTTP请求响应完成时间:" + (responseEnd-requestStart));
            Log.d(TAG,"NavigationTiming==>DOM开始加载前所花费时间:" + (responseEnd-navigationStart));
            Log.d(TAG,"NavigationTiming==>DOM结构解析完成时间:" + (domInteractive-domLoading));
            Log.d(TAG,"NavigationTiming==>网页内资源（如Js脚本）加载时间:" + (domContentLoadedEventEnd-domContentLoadedEventStart));
            Log.d(TAG,"NavigationTiming==>DOM加载完成时间:" + (domComplete-domLoading));
            Log.d(TAG,"NavigationTiming==>load事件加载时间:" + (loadEventEnd-loadEventStart));
            Log.d(TAG,"NavigationTiming==>页面加载耗时:" + (loadEventEnd-fetchStart));
            Log.d(TAG,"========================分割线========================");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @JavascriptInterface
    public void sendError(String msg) {
        Log.i(TAG,"sendError = " + msg);
    }
}
