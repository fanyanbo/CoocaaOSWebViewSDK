package org.lzyzsd.jsbridge;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fanyanbo on 2018/8/24.
 * Email: fanyanbo@skyworth.com
 */
public class CCLaunchHandler implements IBridgeHandler {

    private static final String TAG = "jsbridge";
    private Context context = null;

    public CCLaunchHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handler(String data, ICallBackFunction function) {
        Log.i(TAG, "handler = startPlugin, data from web = " + data);
        try {
            JSONObject jsonObj = new JSONObject(data);
            String action = jsonObj.getString("action");
            if ("start".equals(action)) {
                String pkgName = jsonObj.getString("packageName");
                String actName = jsonObj.getString("actionName");
                String clsName = jsonObj.getString("activityName");
                String params = jsonObj.getString("params");
                Log.i(TAG, "startPlugin, pkgName = " + pkgName + ",actName = " + actName);
                Intent intent = new Intent();
                intent.setClassName(pkgName,clsName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                function.onCallBack("startPlugin ok!!!");
            }else if("check".equals(action)){
                function.onCallBack("startPlugin this is check!!!");
            }else{
                function.onCallBack("startPlugin this is unknown!!!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
