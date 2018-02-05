package com.coocaa.cordova.plugin;

import com.skyworth.framework.skysdk.util.SkyJSONUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


/**
 * Created by rico on 2016/3/31.
 */
public class CoocaaUserInfoParser {

    public static JSONObject parseUserInfo(Map<String, Object> userMap)
    {
        if(userMap!=null && userMap.size()>0)
        {
            String jsonString =  SkyJSONUtil.getInstance().compile(userMap);   
        	            
            if(jsonString!=null && jsonString.length()>0)
            {
                try {
                    JSONObject josnobj = new JSONObject(jsonString);
                    return  josnobj;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
