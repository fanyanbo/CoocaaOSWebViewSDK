package com.coocaa.cordova.plugin;

/**
 * Created by fanyanbo on 2018/6/25.
 * Email: fanyanbo@skyworth.com
 */

public class BusinessDataListener {

    public interface BussinessCallback
    {
        public void onResult(String value);
    }

    public interface CordovaBusinessDataListener
    {
        public String getBusinessData(String data, BussinessCallback cb);
        public boolean setBusinessData(String data, BussinessCallback cb);
    }
}
