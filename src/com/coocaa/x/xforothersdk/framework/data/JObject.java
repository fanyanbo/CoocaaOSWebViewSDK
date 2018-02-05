/**
 * Copyright (C) 2012 The SkyTvOS Project
 * <p/>
 * Version     Date           Author
 * ─────────────────────────────────────
 * 2014-11-3         Root.Lu
 */

package com.coocaa.x.xforothersdk.framework.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JObject implements Parcelable {
    protected static final String EMPTY_STRING = "";

    private transient JSONObject _self = null;

    public JObject() {

    }

    public void setJSONObject(JSONObject _self) {
        this._self = _self;
    }

    public synchronized <T> T findAttribute(String attrName, Class<T> attrType) {
        try {
            return _self.getObject(attrName, attrType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void addAttr(String attrName, Object attr) {
        if (_self == null)
            _self = new JSONObject();
        _self.put(attrName, attr);
    }

    public String toJSONString() {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        JSONObject jo = JSONObject.parseObject(JSONObject.toJSONString(this));
        if (_self != null) {
            Set<Map.Entry<String, Object>> sets = _self.entrySet();
            for (Map.Entry<String, Object> set : sets) {
                if (!jo.containsKey(set.getKey()))
                    jo.put(set.getKey(), set.getValue());
            }
        }
        return jo.toJSONString();
    }

    @Override
    public String toString() {
        return toJSONString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(toString());
    }


    public static <T> T parseJObject(String json, Class<T> clazz) {
        Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
        T ret = JSONObject.parseObject(json, clazz);
        try {
            Log.i("JO", "clazz:" + clazz.getName());
            Method method = ret.getClass().getMethod("setJSONObject", JSONObject.class);
            method.invoke(ret, JSONObject.parseObject(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static <T> List<T> parseArray(String json, Class<T> clazz) throws Exception {
        Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
        List<String> array = JSONObject.parseArray(json, String.class);
        List<T> ret = new ArrayList<T>();
        for (String _json : array) {
            ret.add(parseJObject(_json, clazz));
        }
        return JSONObject.parseArray(json, clazz);
    }

    public static <T extends JObject> T fromJSONObject(JSONObject jo, Class<T> clazz) {
        String json = jo.toJSONString();
        T item = T.parseJObject(json, clazz);
        item.setJSONObject(jo);
        return item;
    }

    public static <T> Creator<T> createCREATOR(final Class<T> clazz,
                                               Creator<T> c) {
        if (c != null)
            return c;
        return new Creator<T>() {
            @Override
            public T createFromParcel(Parcel source) {
                return JObject.parseJObject(source.readString(), clazz);
            }

            @Override
            public T[] newArray(int size) {
                return clazz.getEnumConstants();
            }
        };
    }

    public static final Creator<JObject> CREATOR = createCREATOR(JObject.class, null);
}
