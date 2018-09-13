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
package org.apache.cordova.plugin.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by rico on 2016/3/17.
 */
public class DynamicPluginInfo {
    public String name;
    public String url;
    public String version;
    public String pkgname;
    public String clsname;
    public ArrayList<String> permission;

    private boolean isPluginInfolegal = false;
    private JSONObject mJsonObj;
    public DynamicPluginInfo()
    {

    }

    public DynamicPluginInfo(JSONObject jsonobj)
    {
        if (jsonobj != null)
        {
            mJsonObj = jsonobj;
            try {
                name = jsonobj.getString("name");
                url = jsonobj.getString("url");
                version = jsonobj.getString("version");
                pkgname = jsonobj.getString("pkgname");
                clsname = jsonobj.getString("clsname");
                JSONArray permissionArray = jsonobj.getJSONArray("permission");
                if (permissionArray!=null)
                {
                    permission = new ArrayList<String>(permissionArray.length());
                    for(int j = 0; j<permissionArray.length();j++)
                    {
                        JSONObject permissionObj = permissionArray.getJSONObject(j);
                        String permissionStr = permissionObj.getString("user-permission");
                        permission.add(permissionStr);
                    }
                }
                isPluginInfolegal = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLegal()
    {
        return isPluginInfolegal;
    }

    public String toString()
    {
        if(mJsonObj!=null)
        {
            return mJsonObj.toString();
        }
        return "";
    }
}
