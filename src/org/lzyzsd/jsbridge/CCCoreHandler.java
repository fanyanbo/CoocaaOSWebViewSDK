package org.lzyzsd.jsbridge;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fanyanbo on 2018/8/24.
 * Email: fanyanbo@skyworth.com
 */
public class CCCoreHandler implements IBridgeHandler {

    private static final String TAG = "jsbridge";
    private Context context = null;

    public CCCoreHandler(Context context) {
        this.context = context;
    }
    @Override
    public void handler(String data, ICallBackFunction function) {

        Log.i(TAG, "handler = corePlugin, data from web = " + data);
        try {
            JSONObject jsonObj = new JSONObject(data);
            String action = jsonObj.getString("action");
            if ("exit".equals(action)) {
                if(context instanceof Activity)
                    ((Activity)context).finish();
            } else {
                function.onCallBack("CoocaaOSApiPlugin this is unknown!!!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
