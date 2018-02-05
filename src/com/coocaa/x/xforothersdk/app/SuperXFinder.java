package com.coocaa.x.xforothersdk.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.x.xforothersdk.framework.data.JObject;
import com.coocaa.x.xforothersdk.framework.pm.XLaunchComponent;
import com.coocaa.x.xforothersdk.framework.pm.XPackageArchive;
import com.coocaa.x.xforothersdk.framework.pm.XPackageManager;
import com.coocaa.x.xforothersdk.framework.utils.LiteURI;
import com.coocaa.x.xforothersdk.provider.db.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lu on 16-3-29.
 */
public class SuperXFinder {
    private static List<String> getInstalledPackages(Context c) {
        PackageManager pm = c.getPackageManager();
        List<PackageInfo> list = pm.getInstalledPackages(0);
        List<String> packages = new ArrayList<String>();
        for (PackageInfo pi : list)
            packages.add(pi.packageName);
        return packages;
    }

    private static Object getMetaData(Context context, String packageName, String key) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                Object value = null;
                if (applicationInfo.metaData != null) {
                    value = applicationInfo.metaData.get(key);
                }
                if (value == null) {
                    return null;
                }
                return value;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Created by lu on 16-3-25.
     */
    public static class XIntent extends JObject {
        public static final String DOWHAT_START_ACTIVITY = "startActivity";
        public static final String DOWHAT_START_SERVICE = "startService";
        public static final String DOWHAT_SEND_BROADCAST = "sendBroadcast";
        public static final String DOWHAT_SEND_INTERNALBROADCAST = "sendInternalBroadcast";

        public static final String BYWHAT_ACTION = "action";
        public static final String BYWHAT_CLASS = "class";
        public static final String BYWHAT_URI = "uri";

        public String packagename;
        public String dowhat;
        public String bywhat;
        public String byvalue;
        public Map<String, String> params = new HashMap<String, String>();

        public XIntent setPackagename(String packagename) {
            this.packagename = packagename;
            return this;
        }

        public XIntent setParams(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public XIntent setDowhat(String dowhat) {
            this.dowhat = dowhat;
            return this;
        }

        public XIntent setByvalue(String byvalue) {
            this.byvalue = byvalue;
            return this;
        }

        public XIntent setBywhat(String bywhat) {
            this.bywhat = bywhat;
            return this;
        }


        public static XIntent create(String packageName) {
            XIntent data = new XIntent();
            data.setPackagename(packageName);
            return data;
        }

        public String getDowhat() {
            return dowhat;
        }

        public String getBywhat() {
            return bywhat;
        }

        public String getByvalue() {
            return byvalue;
        }

        public void addParam(String key, String value) {
            synchronized (params) {
                params.put(key, value);
            }
        }

        public String getParam(String key) {
            synchronized (params) {
                return params.get(key);
            }
        }

        public String getPackagename() {
            return packagename;
        }

        public XIntent build() {
            return this;
        }


        public Intent buildIntent(Context c) {
            Intent intent = null;
            if (bywhat != null && !bywhat.equals("") && !bywhat.equals("null")) {
                intent = new Intent();
                if (!TextUtils.isEmpty(packagename))
                    intent.setPackage(packagename);
                if (bywhat.equals(BYWHAT_ACTION) && !TextUtils.isEmpty(byvalue)) {
                    intent.setAction(byvalue);
                } else if (bywhat.equals(BYWHAT_CLASS) && !TextUtils.isEmpty(byvalue)) {
                    intent.setClassName(packagename, byvalue);
                } else if (bywhat.equals(BYWHAT_URI) && !TextUtils.isEmpty(byvalue)) {
                    intent.setData(Uri.parse(byvalue));
                } else {
                    byvalue = getLauncherActivity(c, packagename);
                    intent.setClassName(packagename, byvalue);
                }
                if (params != null && params.size() > 0) {
                    Iterator iterator = params.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        intent.putExtra((String) entry.getKey(), (String) entry.getValue());
                    }
                }
            }
            return intent;
        }

        public void start(Context c) {
            Intent intent = buildIntent(c);
            if (dowhat.equals(DOWHAT_START_ACTIVITY)) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(intent);
            } else if (dowhat.equals(DOWHAT_START_SERVICE)) {
                c.startService(intent);
            } else if (dowhat.equals(DOWHAT_SEND_BROADCAST)) {
                c.sendBroadcast(intent);
            } else if (dowhat.equals(DOWHAT_SEND_INTERNALBROADCAST)) {
//            c.sendI(intent);
            }
        }

        private static String getLauncherActivity(Context context, String packageName) {
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setPackage(packageName);
            List<ResolveInfo> resolveInfo = pm.queryIntentActivities(intent, PackageManager.GET_DISABLED_COMPONENTS);
            if (resolveInfo != null && resolveInfo.size() > 0) {
                ResolveInfo info = resolveInfo.get(0);
                return info.activityInfo.name;
            }
            return "";
        }
    }


    public static abstract class ProviderAuthorityFinder {
        private static final String URI_FORMAT = "content://%s/%s";

        private static LiteURI createUri(String auth, String path) {
            return LiteURI.create(String.format(URI_FORMAT, auth, path));
        }

        public static class ProviderAuthority {
            public String authority;
            public String packageName;

            public LiteURI create(String path) {
                return createUri(authority, path);
            }
        }


        public abstract ProviderAuthority findXProviderAuthority();

        public abstract ProviderAuthority findDBProviderAuthority();

        public final LiteURI createXUri(String path) {
            ProviderAuthority auth = findXProviderAuthority();
            if (auth == null) {
                return null;
            }
            return createUri(auth.authority, path);
        }

        public final LiteURI createDBUri(String path) {
            ProviderAuthority auth = findDBProviderAuthority();
            if (auth == null) {
                return null;
            }
            return createUri(auth.authority, path);
        }

        protected static ProviderAuthority find(String key) {
            List<String> packages = getInstalledPackages(mContext);
            for (String packageName : packages) {
                Object obj = getMetaData(mContext, packageName, key);
                if (obj != null) {
                    String _obj = (String) obj;
                    if (!TextUtils.isEmpty(_obj)) {
                        ProviderAuthority auth = new ProviderAuthority();
                        auth.authority = _obj;
                        auth.packageName = packageName;
                        return auth;
                    }
                }
            }
            return null;
        }
    }

    public static final ProviderAuthorityFinder CC_APPSTORE_AUTH_FINDER = new ProviderAuthorityFinder() {
        private static final String CC_APPSTORE_PROVIDER_X = "CC_APPSTORE_PROVIDER_X";
        private static final String CC_APPSTORE_PROVIDER_DB = "CC_APPSTORE_PROVIDER_DB";

        private ProviderAuthority x = null;
        private ProviderAuthority db = null;


        @Override
        public synchronized ProviderAuthority findXProviderAuthority() {
            if (x == null)
                x = find(CC_APPSTORE_PROVIDER_X);
            return x;
        }

        @Override
        public synchronized ProviderAuthority findDBProviderAuthority() {
            if (db == null)
                db = find(CC_APPSTORE_PROVIDER_DB);
            return db;
        }
    };

    public static final ProviderAuthorityFinder CC_GAMECENTER_AUTH_FINDER = new ProviderAuthorityFinder() {
        private static final String CC_GAMECENTER_PROVIDER_X = "CC_GAMECENTER_PROVIDER_X";
        private static final String CC_GAMECENTER_PROVIDER_DB = "CC_GAMECENTER_PROVIDER_DB";

        private ProviderAuthority x = null;
        private ProviderAuthority db = null;

        @Override
        public synchronized ProviderAuthority findXProviderAuthority() {
            if (x == null)
                x = find(CC_GAMECENTER_PROVIDER_X);
            return x;
        }

        @Override
        public synchronized ProviderAuthority findDBProviderAuthority() {
            if (db == null)
                db = find(CC_GAMECENTER_PROVIDER_DB);
            return db;
        }
    };

    public static final ProviderAuthorityFinder DEFAULT_AUTH_FINDER = CC_APPSTORE_AUTH_FINDER;
//            new ProviderAuthorityFinder() {
//        private ProviderAuthority x = null;
//        private ProviderAuthority db = null;
//
//        @Override
//        public synchronized ProviderAuthority findXProviderAuthority() {
//            if (x == null) {
//                x = new ProviderAuthority();
//                x.authority = mContext.getPackageName() + ".provider.x";
//                x.packageName = mContext.getPackageName();
//            }
//            return x;
//        }
//
//        @Override
//        public synchronized ProviderAuthority findDBProviderAuthority() {
//            if (db == null) {
//                db = new ProviderAuthority();
//                db.authority = mContext.getPackageName() + ".provider.db";
//                db.packageName = mContext.getPackageName();
//            }
//            return db;
//        }
//    };


    public static abstract class CustomActionBuilder {
        public static class CustomActionBuilderException extends Exception {
            public CustomActionBuilderException() {
                super("superx's liteservice not found!!!");
            }
        }

        public static final String CUSTOM_ACTION_INSTALL_PACKAGE = "com.coocaa.x.service.lite.custom_action.INSTALL_PACKAGE";
        public static final String CUSTOM_ACTION_UNINSTALL_PACKAGE = "com.coocaa.x.service.lite.custom_action.UNINSTALL_PACKAGE";
        public static final String CUSTOM_ACTION_START_COMPONENT = "com.coocaa.x.service.lite.custom_action.START_COMPONENT";
        public static final String CUSTOM_ACTION_DELETE_FILE = "com.coocaa.x.service.lite.custom_action.DELETE_FILE";

        protected static String servicePackageName = null;

        protected XIntent intent = new XIntent();

        public CustomActionBuilder(String action) throws CustomActionBuilderException {
            synchronized (CUSTOM_ACTION_INSTALL_PACKAGE) {
                if (servicePackageName == null) {
                    servicePackageName = find(CC_LITESERVICE);
                    if (servicePackageName == null) {
                        throw new CustomActionBuilderException();
                    }
                }
            }
            intent.packagename = servicePackageName;
            intent.dowhat = XIntent.DOWHAT_START_SERVICE;
            intent.bywhat = XIntent.BYWHAT_ACTION;
            intent.byvalue = action;
        }

        public final CustomActionBuilder addExtra(String key, String value) {
            intent.addParam(key, value);
            return this;
        }

        public final XIntent build() {
            return intent;
        }


        private static final String CC_LITESERVICE = "CC_LITESERVICE";

        private static String find(String key) {
            List<String> packages = getInstalledPackages(mContext);
            for (String packageName : packages) {
                Object obj = getMetaData(mContext, packageName, key);
                if (obj != null) {
                    String _obj = (String) obj;
                    if (!TextUtils.isEmpty(_obj)) {
                        return _obj;
                    }
                }
            }
            return null;
        }
    }

    public static class UninstallPackageCustomActionBuilder extends CustomActionBuilder {

        public static UninstallPackageCustomActionBuilder createBuilder() throws CustomActionBuilderException {
            return new UninstallPackageCustomActionBuilder();
        }

        public static final String EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME";
        public static final String EXTRA_PARAMS_MAP = "EXTRA_PARAMS_MAP";

        private UninstallPackageCustomActionBuilder() throws CustomActionBuilderException {
            super(CUSTOM_ACTION_UNINSTALL_PACKAGE);
        }

        public UninstallPackageCustomActionBuilder setPackageName(String packageName) {
            intent.addParam(EXTRA_PACKAGE_NAME, packageName);
            return this;
        }

        public UninstallPackageCustomActionBuilder setParamsMap(Map<String, String> map) {
            intent.addParam(EXTRA_PARAMS_MAP, JSONObject.toJSONString(map));
            return this;
        }
    }

    public static class StartComponentCustomActionBuilder extends CustomActionBuilder {

        public static StartComponentCustomActionBuilder createBuilder() throws CustomActionBuilderException {
            return new StartComponentCustomActionBuilder();
        }

        public static final String EXTRA_LAUNCH_COMPONENT = "EXTRA_LAUNCH_COMPONENT";


        private StartComponentCustomActionBuilder() throws CustomActionBuilderException {
            super(CUSTOM_ACTION_START_COMPONENT);
        }

        public StartComponentCustomActionBuilder setLaunchComponent(XLaunchComponent cn) {
            intent.addParam(EXTRA_LAUNCH_COMPONENT, cn.toJSONString());
            return this;
        }
    }

    public static class InstallPackageCustomActionBuilder extends CustomActionBuilder {
        public static final String EXTRA_PACKAGE_ARCHIVE = "EXTRA_PACKAGE_ARCHIVE";
        public static final String EXTRA_INSTALL_REPLACE = "INSTALL_REPLACE";
        public static final String EXTRA_INSTALL_EXTRA = "EXTRA_INSTALL_EXTRA";

        public static InstallPackageCustomActionBuilder createBuilder() throws CustomActionBuilderException {
            return new InstallPackageCustomActionBuilder();
        }

        private InstallPackageCustomActionBuilder() throws CustomActionBuilderException {
            super(CUSTOM_ACTION_INSTALL_PACKAGE);
        }

        public InstallPackageCustomActionBuilder setPackageArchive(XPackageArchive archive) {
            intent.addParam(EXTRA_PACKAGE_ARCHIVE, archive.toJSONString());
            return this;
        }

        public InstallPackageCustomActionBuilder setInstallExtra(XPackageManager.InstallExtraBuilder builder) {
            intent.addParam(EXTRA_INSTALL_EXTRA, builder.build());
            return this;
        }

        public InstallPackageCustomActionBuilder setInstallReplace(boolean v) {
            intent.addParam(EXTRA_INSTALL_REPLACE, String.valueOf(v));
            return this;
        }
    }

    private static Context mContext = null;

    public static void setContext(Context c) {
        mContext = c;
        Table.setContext(c);
    }

    public static Context getContext() {
        return mContext;
    }
}
