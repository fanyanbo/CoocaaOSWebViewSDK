package org.apache.cordova.plugin.api.xforothersdk.framework.pm;

import android.content.pm.ApplicationInfo;
import android.os.Parcelable;

import org.apache.cordova.plugin.api.xforothersdk.framework.data.JObject;

import java.util.ArrayList;
import java.util.List;

import static org.apache.cordova.plugin.api.xforothersdk.framework.data.JObject.createCREATOR;

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

    public static final Parcelable.Creator<XPackageInfo> CREATOR = createCREATOR(XPackageInfo.class, null);
}
