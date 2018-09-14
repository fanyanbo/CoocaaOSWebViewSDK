package org.apache.cordova.plugin.api.xforothersdk.app.appstore3.controller.data;


import org.apache.cordova.plugin.api.xforothersdk.framework.pm.XLaunchComponent;

/**
 * Created by admin on 2016/3/10.
 */
public class MyAppData extends MyAppBaseData {
    public String packageName;
    public int versionCode;
    public String versionName;
    public int packageType;
    public boolean isSystemApp = false;
    public boolean isSystemUserId = false;
    public XLaunchComponent component;
}
