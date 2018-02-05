package com.coocaa.x.xforothersdk.framework.pm;

import android.content.pm.ApplicationInfo;

import com.coocaa.x.xforothersdk.framework.data.JObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lu on 16-2-26.
 */
public class XPackageInfo extends JObject {
    public String packageName;
    public int versionCode;
    public String versionName;
    public int packageType;
    public long firstInstallTime = 0;
    public int flags = 0;
    public boolean isSystemUserId = false;
    public String label;
    public long apkSize = 0;
    public String icon;
    public List<XLaunchComponent> launchComponents = new ArrayList<XLaunchComponent>();


    public boolean isSystemApp() {
        return ((flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public static final Creator<XPackageInfo> CREATOR = createCREATOR(XPackageInfo.class, null);
}
