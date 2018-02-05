package com.coocaa.x.xforothersdk.framework.pm;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.x.xforothersdk.app.SuperXFinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lu on 16-2-24.
 */
public class XPackageManager {
    public static XPackageManager instance = new XPackageManager();

    public static final int PACKAGE_TYPE_ANDROID = 1;
    public static final int PACKAGE_TYPE_WEBAPP = 2;

    private static final String X_ACTION = "superx.intent.action";


    private static final String X_ACTION_ADD_PACKAGE = X_ACTION + ".ADD_PACKAGE";
    public static final String X_ACTION_ADD_PACKAGE_EXTRA_ARCHIVE = X_ACTION_ADD_PACKAGE + ".extra.archive";
    public static final String X_ACTION_ADD_PACKAGE_EXTRA_USER_EXTRA = X_ACTION_ADD_PACKAGE + ".extra.user_extra";

    public static final String X_ACTION_ADD_PACKAGE_READY = X_ACTION_ADD_PACKAGE + ".READY";

    public static final String X_ACTION_ADD_PACKAGE_START = X_ACTION_ADD_PACKAGE + ".START";

    public static final String X_ACTION_ADD_PACKAGE_END = X_ACTION_ADD_PACKAGE + ".END";
    public static final String X_ACTION_ADD_PACKAGE_END_EXTRA_PACKAGE = X_ACTION_ADD_PACKAGE_END + ".extra.package";
    public static final String X_ACTION_ADD_PACKAGE_END_EXTRA_RESULT = X_ACTION_ADD_PACKAGE_END + ".extra.result";
    public static final String X_ACTION_ADD_PACKAGE_END_EXTRA_RESULT_SUCCESS = X_ACTION_ADD_PACKAGE_END + ".extra.result.success";


    private static final String X_ACTION_REMOVE_PACKAGE = X_ACTION + ".REMOVE_PACKAGE";
    public static final String X_ACTION_REMOVE_PACKAGE_EXTRA_PACKAGE = X_ACTION_REMOVE_PACKAGE + ".extra.package";
    public static final String X_ACTION_REMOVE_PACKAGE_EXTRA_USER_EXTRA = X_ACTION_REMOVE_PACKAGE + ".extra.user_extra";

    public static final String X_ACTION_REMOVE_PACKAGE_READY = X_ACTION_REMOVE_PACKAGE + ".READY";

    public static final String X_ACTION_REMOVE_PACKAGE_START = X_ACTION_REMOVE_PACKAGE + ".START";

    public static final String X_ACTION_REMOVE_PACKAGE_END = X_ACTION_REMOVE_PACKAGE + ".END";
    public static final String X_ACTION_REMOVE_PACKAGE_END_EXTRA_RESULT = X_ACTION_REMOVE_PACKAGE_END + ".extra.result";
    public static final String X_ACTION_REMOVE_PACKAGE_END_EXTRA_RESULT_SUCCESS = X_ACTION_REMOVE_PACKAGE_END + ".extra.result.success";
    public static final String X_ACTION_REMOVE_PACKAGE_END_EXTRA_RESULT_PACKAGE_NOT_EXIST = X_ACTION_REMOVE_PACKAGE_END + ".extra.result.package_not_exist";


    public static final String X_ACTION_PACKAGE_AVAILABLE = X_ACTION + ".PACKAGE_AVAILABLE";
    public static final String X_ACTION_PACKAGE_AVAILABLE_EXTRA_PACKAGES = X_ACTION_PACKAGE_AVAILABLE + ".extra.packages";

    public static final String X_ACTION_PACKAGE_UNAVAILABLE = X_ACTION + ".PACKAGE_UNAVAILABLE";
    public static final String X_ACTION_PACKAGE_UNAVAILABLE_EXTRA_PACKAGES = X_ACTION_PACKAGE_UNAVAILABLE + ".extra.packages";


    private static final String X_ACTION_MOVE_PACKAGE = X_ACTION + ".MOVE_PACKAGE";
    public static final String X_ACTION_MOVE_PACKAGE_EXTRA_PACKAGE = X_ACTION_MOVE_PACKAGE + ".extra.package";
    public static final String X_ACTION_MOVE_PACKAGE_EXTRA_LOCATION = X_ACTION_MOVE_PACKAGE + ".extra.location";
    public static final String X_ACTION_MOVE_PACKAGE_START = X_ACTION_MOVE_PACKAGE + "_START";
    public static final String X_ACTION_MOVE_PACKAGE_END = X_ACTION_MOVE_PACKAGE + "_END";
    public static final String X_ACTION_MOVE_PACKAGE_END_EXTRA_RESULT = X_ACTION_MOVE_PACKAGE_END + ".extra.result";
    public static final String X_ACTION_MOVE_PACKAGE_END_EXTRA_RESULT_SUCCESS = X_ACTION_MOVE_PACKAGE_END + ".extra.result.success";

    private static final String X_ACTION_CLEAR_PACKAGE = X_ACTION + ".CLEAR_PACKAGE";
    public static final String X_ACTION_CLEAR_PACKAGE_EXTRA_PACKAGE = X_ACTION_CLEAR_PACKAGE + ".extra.package";
    public static final String X_ACTION_CLEAR_PACKAGE_START = X_ACTION_CLEAR_PACKAGE + "_START";
    public static final String X_ACTION_CLEAR_PACKAGE_END = X_ACTION_CLEAR_PACKAGE + "_END";
    public static final String X_ACTION_CLEAR_PACKAGE_END_EXTRA_RESULT = X_ACTION_CLEAR_PACKAGE_END + ".extra.result";
    public static final String X_ACTION_CLEAR_PACKAGE_END_EXTRA_RESULT_SUCCESS = X_ACTION_CLEAR_PACKAGE_END + ".extra.result.success";

    private static final String X_ACTION_CLEAR_LIST_PACKAGE = X_ACTION + ".CLEAR_ALL_PACKAGE";
    public static final String X_ACTION_CLEAR_LIST_PACKAGE_EXTRA_PACKAGES = X_ACTION_CLEAR_LIST_PACKAGE + ".extra.packages";
    public static final String X_ACTION_CLEAR_LIST_PACKAGE_START = X_ACTION_CLEAR_LIST_PACKAGE + "_START";
    public static final String X_ACTION_CLEAR_LIST_PACKAGE_END = X_ACTION_CLEAR_LIST_PACKAGE + "_END";
    public static final String X_ACTION_CLEAR_LIST_PACKAGE_END_EXTRA_RESULT = X_ACTION_CLEAR_LIST_PACKAGE + ".extra.result";
    public static final String X_ACTION_CLEAR_LIST_PACKAGE_END_EXTRA_RESULT_SUCCESS = X_ACTION_CLEAR_LIST_PACKAGE + ".extra.result.success";


    public static final String X_ACTION_START_APP = X_ACTION + ".START_APP";
    public static final String X_ACTION_START_APP_COMPONENT = X_ACTION_START_APP + ".component";

    public abstract static class ExtraBuilder {
        private Map<String, String> map = new HashMap<String, String>();

        public ExtraBuilder() {

        }

        public ExtraBuilder(String json) {
            try {
                map = (Map<String, String>) JSONObject.parseObject(json, Map.class);
            } catch (Exception e) {
                map = new HashMap<String, String>();
            }
        }

        protected final void put(String key, String value) {
            synchronized (map) {
                map.put(key, value);
            }
        }

        protected final String get(String key) {
            synchronized (map) {
                return map.get(key);
            }
        }

        public final String build() {
            return JSONObject.toJSONString(map);
        }
    }

    public static final class InstallExtraBuilder extends ExtraBuilder {
        public static InstallExtraBuilder builder() {
            return new InstallExtraBuilder();
        }

        public static InstallExtraBuilder parse(String j) {
            return new InstallExtraBuilder(j);
        }

        public static final String DELETE_ARCHIVE_ON_SUCCESS = "DELETE_ARCHIVE_ON_SUCCESS";
        public static final String OPEN_APP_ON_SUCCESS = "OPEN_APP_ON_SUCCESS";
        public static final String INSTALL_FROM = "INSTALL_FROM";
        public static final String SKYWORTH_HIDE_FLAG_PACKAGENAME = "SKYWORTH_HIDE_FLAG_PACKAGENAME";
        public static final String SHOW_RESULT_TOAST = "SHOW_RESULT_TOAST";

        public InstallExtraBuilder() {
            super();
        }

        public InstallExtraBuilder(String json) {
            super(json);
        }

        public InstallExtraBuilder setShowResultToast(String toast) {
            put(SHOW_RESULT_TOAST, toast);
            return this;
        }

        public String getShowResultToast() {
            return get(SHOW_RESULT_TOAST);
        }

        public InstallExtraBuilder setRunAppOnSuccess(boolean v) {
            put(OPEN_APP_ON_SUCCESS, String.valueOf(v));
            return this;
        }

        public boolean isRunAppOnSuccess() {
            try {
                String v = get(OPEN_APP_ON_SUCCESS);
                return Boolean.valueOf(v);
            } catch (Exception e) {
                return false;
            }
        }

        public InstallExtraBuilder setDeleteArchiveOnSuccess(boolean v) {
            put(DELETE_ARCHIVE_ON_SUCCESS, String.valueOf(v));
            return this;
        }

        public boolean isDeleteArchiveOnSuccess() {
            try {
                String v = get(DELETE_ARCHIVE_ON_SUCCESS);
                return Boolean.valueOf(v);
            } catch (Exception e) {
                return false;
            }
        }

        public InstallExtraBuilder setSkyworthHideFlagPackage() {
            put(SKYWORTH_HIDE_FLAG_PACKAGENAME, SuperXFinder.getContext().getPackageName());
            return this;
        }

        public String getSkyworthHideFlagPackage() {
            return get(SKYWORTH_HIDE_FLAG_PACKAGENAME);
        }

        public InstallExtraBuilder setInstallFrom(String from) {
            put(INSTALL_FROM, from);
            return this;
        }

        public String getInstallFrom() {
            return get(INSTALL_FROM);
        }
    }

    public static final class UninstallExtraBuilder extends ExtraBuilder {
        public static UninstallExtraBuilder builder() {
            return new UninstallExtraBuilder();
        }

        public static UninstallExtraBuilder parse(String j) {
            return new UninstallExtraBuilder(j);
        }

        public UninstallExtraBuilder() {
            super();
        }

        public UninstallExtraBuilder(String json) {
            super(json);
        }
    }
}
