package com.coocaa.x.xforothersdk.framework.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;

import com.coocaa.x.xforothersdk.framework.data.JObject;
import com.coocaa.x.xforothersdk.framework.utils.MD5;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lu on 16-2-26.
 */
public class XLaunchComponent extends JObject {
    public String action;
    public String packageName, className;
    public String label;
    /**
     * icon资源uri，可能的取值情况及使用说明如下
     * android.resource://[packageName]/rid   apk中的uri
     * Drawable d = Drawable.createFromStream(context.getContentResolver().openInputStream(Uri.parse(icon)), null);
     * <p/>
     * file://absoultlypath  本地存储的资源uri
     * Drawable d = Drawable.createFromStream(context.getContentResolver().openInputStream(Uri.parse(icon)), null);
     * <p/>
     * http(s)://xxx.xxx.xxx/xxx.png(jpg...)    在线资源
     * 需通过应用使用的加载网络图片的方式加载
     */
    public String icon = null;

    public Map<String, String> extra = new HashMap<String, String>();

    public XPackageInfo packageInfo;

    public XLaunchComponent() {
    }

    public ComponentName getComponent() {
        return new ComponentName(packageName, className);
    }

    public Intent toIntent() {
        Intent intent = new Intent();
        if (!TextUtils.isEmpty(action))
            intent.setAction(action);
        if (!TextUtils.isEmpty(packageName))
            intent.setPackage(packageName);
        if (!TextUtils.isEmpty(className) && !TextUtils.isEmpty(packageName))
            intent.setClassName(packageName, className);
        synchronized (extra) {
            Set<String> keys = extra.keySet();
            for (String key : keys)
                intent.putExtra(key, extra.get(key));
        }
        return intent;
    }

    public String toMD5() {
        String str = getComponent().toShortString();
        return MD5.md5s(str);
    }

    public static final Creator<XLaunchComponent> CREATOR = createCREATOR(XLaunchComponent.class, null);
}
