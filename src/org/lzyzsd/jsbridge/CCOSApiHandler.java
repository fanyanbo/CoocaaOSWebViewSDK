package org.lzyzsd.jsbridge;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fanyanbo on 2018/8/24.
 * Email: fanyanbo@skyworth.com
 */
public class CCOSApiHandler implements IBridgeHandler {

    private static final String TAG = "jsbridge";

    @Override
    public void handler(String data, ICallBackFunction function) {
        Log.i(TAG, "handler = CoocaaOSApiPlugin, data from web = " + data);
        try {
            JSONObject jsonObj = new JSONObject(data);
            String action = jsonObj.getString("action");
            if ("getDeviceInfo".equals(action)) {
                function.onCallBack("CoocaaOSApiPlugin this is deviceInfo!!!");
            }else if("getUserInfo".equals(action)) {
                function.onCallBack("CoocaaOSApiPlugin this is userInfo!!!");
            }else {
                function.onCallBack("CoocaaOSApiPlugin this is unknown!!!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
