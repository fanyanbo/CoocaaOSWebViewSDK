/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package com.coocaa.cordova.plugin;

import android.content.Context;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

/**
 * Created by rico on 2016/3/23.
 */
public class DynamicDexLoader {
    private  String TAG = getClass().getSimpleName();

    private HashMap<String,DynamicPluginInfoEx> exPluginInfoMap = new HashMap<String, DynamicPluginInfoEx>();

    private  String optimizedDexOutputPath;

    private String originDexPath;

    private ClassLoader clsLoader;

    private PluginManager pluginManager;

    public DynamicDexLoader(HashMap<String,DynamicPluginInfoEx> pluginMap,final String originPath,final String optimizedPath,final ClassLoader loader,PluginManager manager)
    {
        exPluginInfoMap = pluginMap;
        optimizedDexOutputPath = optimizedPath;
        originDexPath = originPath;
        clsLoader = loader;
        pluginManager = manager;
    }

    public boolean paddingPlugins()
    {
        boolean isSuccess = true;
        for (String key:exPluginInfoMap.keySet()
                ) {
            DynamicPluginInfoEx mExinfo = exPluginInfoMap.get(key);
            String jarname = exPluginInfoMap.get(key).info.name+".jar";
            String origanJarFile = originDexPath + File.separator + jarname;

            DexClassLoader classLoader = new DexClassLoader(origanJarFile,optimizedDexOutputPath, null,clsLoader);
            try {
                CordovaPlugin iinterface = (CordovaPlugin) classLoader.loadClass(mExinfo.info.clsname).newInstance();
                PluginEntry entry = new PluginEntry(mExinfo.info.name,iinterface);
                pluginManager.addService(entry);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                isSuccess = false;
            }
        }
        return isSuccess;
    }
}
