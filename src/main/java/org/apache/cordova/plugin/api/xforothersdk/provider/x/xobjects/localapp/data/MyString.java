package org.apache.cordova.plugin.api.xforothersdk.provider.x.xobjects.localapp.data;


import org.apache.cordova.plugin.api.xforothersdk.provider.ProviderData;

/**
 * Created by admin on 2015/12/31.
 */
public class MyString implements ProviderData.IProviderData{
    private String str;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}
