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

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by rico on 2016/3/21.
 */
public class DynamicPluginHttpLoader{
    private String TAG = getClass().getSimpleName();
    private static int BUFFER_SIZE = 2048;

    private HashMap<String,DynamicPluginInfoEx> mExPluginInfoMap;

    private String mSaveDir;
    private SharedPreferences mSp;
    public DynamicPluginHttpLoader( HashMap<String,DynamicPluginInfoEx>mergeInfoMap, String saveDir, SharedPreferences sp)
    {
        mExPluginInfoMap = mergeInfoMap;

        mSaveDir = saveDir;
        mSp = sp;
    }

    public boolean sync()
    {
        boolean isSuccess = false;
        for (String key:mExPluginInfoMap.keySet()
             ) {
            DynamicPluginInfoEx mExinfo = mExPluginInfoMap.get(key);
            try{
                isSuccess = saveToFile(mExinfo);
            }catch (IOException e)
            {
                Log.v(TAG,"Sync file error!");
                e.printStackTrace();
            }
            if (!isSuccess)
            {
                return false;
            }
        }
        return true;
    }

    private boolean saveToFile(DynamicPluginInfoEx extraInfo) throws IOException {
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        HttpURLConnection httpUrl = null;
        URL url = null;
        byte[] buf = new byte[BUFFER_SIZE];
        int size = 0;

        url = new URL(extraInfo.info.url);
        httpUrl = (HttpURLConnection) url.openConnection();
        httpUrl.setConnectTimeout(5000);// 5s time out
        httpUrl.connect();
        long serverModify = httpUrl.getLastModified();
        if(extraInfo.serverModifyTime == serverModify)
        {
            Log.v(TAG, "not need download " + extraInfo.info.name + " because file on server is not modified!");
            httpUrl.disconnect();
            return true;
        }

        bis = new BufferedInputStream(httpUrl.getInputStream());
        String fileName = mSaveDir + File.separator + extraInfo.info.name + ".jar";
        File file = new File(fileName);
        if(!file.exists()) {
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        fos = new FileOutputStream(fileName);
        while ((size = bis.read(buf)) != -1)
            fos.write(buf, 0, size);

        fos.close();
        bis.close();
        httpUrl.disconnect();

        extraInfo.localModifyTime = file.lastModified();
        extraInfo.serverModifyTime = serverModify;
        Log.v(TAG, " extraInfo.localModifyTime= " + extraInfo.localModifyTime + "  extraInfo.serverModifyTime= " + extraInfo.serverModifyTime);

        saveToSharedPerference(extraInfo);
        return true;
    }

    private void saveToSharedPerference(DynamicPluginInfoEx exinfo)
    {
        SharedPreferences.Editor editor = mSp.edit();
        JSONObject exInfoObj = new JSONObject();
        try {
            exInfoObj.put("localmodifytime",exinfo.localModifyTime);
            exInfoObj.put("servermodifytime",exinfo.serverModifyTime);
            exInfoObj.put("plugininfo",exinfo.info.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.putString(exinfo.info.name+".jar" , exInfoObj.toString());
        editor.commit();
    }
}
