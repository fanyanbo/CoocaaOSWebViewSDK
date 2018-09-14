package org.apache.cordova.plugin.api.xforothersdk.provider.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.alibaba.fastjson.JSONObject;

import org.apache.cordova.plugin.api.xforothersdk.provider.ProviderData;

/**
 * Created by lu on 15-12-1.7
 */
public class Table implements ProviderData.IProviderData {
    protected static Context mContext = null;
    protected static ContentResolver CR = null;

    public static void setContext(Context c) {
        mContext = c;
        CR = getContext().getContentResolver();
    }

    protected static Context getContext() {
        return mContext;
    }

    public static final String COLUMNS_ID = "id";

    /**
     * 数据库自动生成的主键id
     */
    private long id;

    public Table() {

    }

    public long getId() {
        return id;
    }

    public final String getStringID() {
        return String.valueOf(id);
    }

    public final ContentValues toContentValues() {
        return ProviderData.toContentValues(this);
    }


    public static <T> T parseJObject(String json, Class<T> clazz) {
        Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
        return JSONObject.parseObject(json, clazz);
    }

    public final String toJSONString() {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        return JSONObject.toJSONString(this);
    }
}
