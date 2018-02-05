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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class DynamicPluginLoader extends CordovaPlugin
{
    private  String TAG = getClass().getSimpleName();

    private static final String SP_PLUGININFO = "dynamicplugininfo";

    ////////////////////////////Actions///////////////////////////////////
    private static final String ACTION_LOAD_DYNAMIC_PLUGIN = "loadDynamicPlugin";

    private HashMap<String,DynamicPluginInfoEx> exPluginInfoMap = new HashMap<String, DynamicPluginInfoEx>();
    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        Log.v(TAG, TAG + ": initialization");
        super.initialize(cordova, webView);

//        this.cordova.getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // Clear flag FLAG_FORCE_NOT_FULLSCREEN which is set initially
//                // by the Cordova.
//
//            }
//        });
        String currentDexPath = this.cordova.getActivity().getFilesDir().getAbsolutePath() + File.separator + "plugindir";
        File dexFileDir = new File(currentDexPath);
        if(!dexFileDir.exists())
        {
            dexFileDir.mkdirs();
            dexFileDir.setReadable(true);
            dexFileDir.setWritable(true);
        }
        checkLocalPlugins(this.cordova.getActivity(),dexFileDir);
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if (ACTION_LOAD_DYNAMIC_PLUGIN.equals(action))
        {
            ArrayList<DynamicPluginInfo> lists = parseDynamicPluginInfo(args.getJSONArray(0));
            if(lists == null)
            {
                Log.v(TAG,"parseDynamicPluginInfo error because json exception happans!");
                callbackContext.error("parse plugin config error!");
            }
            else if(lists!=null && lists.size()>0)
            {
                exPluginInfoMap = mergeInfoMap(lists);
                this.cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        String currentDexPath = cordova.getActivity().getFilesDir().getAbsolutePath() + File.separator + "plugindir";
                        SharedPreferences sp = cordova.getActivity().getSharedPreferences(SP_PLUGININFO, Context.MODE_PRIVATE);
                        DynamicPluginHttpLoader httpLoader = new DynamicPluginHttpLoader(exPluginInfoMap,currentDexPath,sp);
                        if(httpLoader.sync())
                        {
                            DynamicDexLoader dexloader = new DynamicDexLoader(exPluginInfoMap,
                                    currentDexPath,cordova.getActivity().getDir("temp", Context.MODE_PRIVATE).getAbsolutePath(),
                                    cordova.getActivity().getClassLoader(),webView.getPluginManager());
                            if(dexloader.paddingPlugins())
                            {
                                callbackContext.success();
                            }
                            else
                            {
                                callbackContext.error(ACTION_LOAD_DYNAMIC_PLUGIN + " exec error because http download success but padding pluin failed!");
                            }
                        }
                        else
                        {
                            callbackContext.error(ACTION_LOAD_DYNAMIC_PLUGIN + " exec error because http download failure");
                        }
                    }
                });
            }
            else
            {
                Log.v(TAG,"parseDynamicPluginInfo size == 0 ,no dynamic plugin need to loaded!");
                callbackContext.success();
            }
            return true;
        }
        return false;
    }

    private class DexFilter implements  FileFilter
    {
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().endsWith("jar"))
            {
                return true;
            }
            return false;
        }
    }

    //read from sp,compare last modify time with local jar file
    private void checkLocalPlugins(Activity activity,File dexDir)
    {
        SharedPreferences sp = activity.getSharedPreferences(SP_PLUGININFO, Context.MODE_PRIVATE);
        exPluginInfoMap = getDynamicPluginInfoEx(sp);

        ArrayList<File> needDelFiles = new ArrayList<File>();
        File[] dexDirFiles = dexDir.listFiles();
        boolean removeAllExMap = false;
        if(dexDirFiles== null)
        {
            removeAllExMap = true;
        }
        if(dexDirFiles!=null && dexDirFiles.length==0)
        {
            removeAllExMap = true;
        }
        if (removeAllExMap)
        {
            SharedPreferences.Editor spedit =  sp.edit();
            spedit.clear();
            spedit.commit();
            exPluginInfoMap.clear();
            return;
        }

        for (File dexfile:dexDir.listFiles())
        {
            String filename = dexfile.getName();
            long lastModifyTime = dexfile.lastModified();

            DynamicPluginInfoEx  exinfo = exPluginInfoMap.get(filename);
            if (exinfo!=null&&lastModifyTime!=exinfo.localModifyTime )
            {
                Log.v(TAG, "checkLocalPlugins add to delete " + filename + " because local file timestamp not equal!");
                needDelFiles.add(dexfile);
            }
            else if(exinfo == null)
            {
                Log.v(TAG, "checkLocalPlugins add to delete " + filename + " because dynamic plugin info in shared preference is null!");
                needDelFiles.add(dexfile);
            }
        }

        if(needDelFiles.size()>0)
        {
            SharedPreferences.Editor spedit =  sp.edit();
            for (int i = 0;i<needDelFiles.size();i++)
            {
                File todelfile = needDelFiles.get(i);
                todelfile.delete();
                exPluginInfoMap.remove(todelfile.getName());
                spedit.remove(todelfile.getName());
                Log.v(TAG, "checkLocalPlugins remove " + todelfile.getName() + " !");
            }
            spedit.commit();
        }

    }

    private ArrayList<DynamicPluginInfo> parseDynamicPluginInfo(JSONArray jsonArray) throws JSONException
    {
        ArrayList<DynamicPluginInfo> dynamicInfos = new ArrayList<DynamicPluginInfo>();
        for(int i=0;i<jsonArray.length();i++){
            JSONObject jsonobj=jsonArray.getJSONObject(i);
            DynamicPluginInfo info = new DynamicPluginInfo(jsonobj);
            if (!info.isLegal())
            {
                //return null assume that plugin info parse error!
                return null;
            }
            dynamicInfos.add(info);
        }
        return dynamicInfos;
    }

    private HashMap<String,DynamicPluginInfoEx> getDynamicPluginInfoEx(SharedPreferences sp)
    {
        HashMap<String,DynamicPluginInfoEx> exinfoMap = new HashMap<String, DynamicPluginInfoEx>();
        HashMap<String,String> allSpInfo = (HashMap<String, String>) sp.getAll();
        if(allSpInfo!=null)
        {
            for (String key:allSpInfo.keySet()
                 ) {
                String value = allSpInfo.get(key);
                try {
                    DynamicPluginInfoEx exinfo = new DynamicPluginInfoEx();
                    JSONObject exInfoObj = new JSONObject(value);
                    exinfo.localModifyTime = exInfoObj.getLong("localmodifytime");
                    exinfo.serverModifyTime = exInfoObj.getLong("servermodifytime");
                    String pluginInfoString = exInfoObj.getString("plugininfo");
                    JSONObject pluginInfoObj = new JSONObject(pluginInfoString);
                    DynamicPluginInfo newinfo = new DynamicPluginInfo(pluginInfoObj);
                    exinfo.info = newinfo;
                    exinfoMap.put(key,exinfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return  exinfoMap;
    }

    private HashMap<String,DynamicPluginInfoEx> mergeInfoMap(ArrayList<DynamicPluginInfo> reclist)
    {
        for (DynamicPluginInfo info:reclist
                ) {
            boolean isFound = false;
            for (String key:exPluginInfoMap.keySet()
                    ) {
                DynamicPluginInfoEx exinfo = exPluginInfoMap.get(key);
                if (info.name.equals(exinfo.info.name)) {
                    exinfo.info = info;
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                DynamicPluginInfoEx newex = new DynamicPluginInfoEx();
                newex.info = info;
                newex.serverModifyTime = -1;//if set time = 0, error occurs when server resource is missing
                newex.localModifyTime = -1;
                exPluginInfoMap.put(info.name+".jar",newex);
            }
        }
        return  exPluginInfoMap;
    }

}