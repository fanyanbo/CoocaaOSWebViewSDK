/**
 * Copyright (C) 2012 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2016年6月1日         wen
 *
 */

package com.coocaa.sky.api.utils;

import android.text.TextUtils;

import com.skyworth.framework.skysdk.properties.SkySystemProperties;

/**
 * @ClassName PropertyUtils
 * @Description 系统属性工具类
 * @author wen
 * @date 2016年6月1日
 */
public class PropertyUtils
{

    public static String getSkytype()
    {
        return SkySystemProperties.getProperty("ro.build.skytype");
    }

    public static String getMac()
    {
        String mac = SkySystemProperties.getProperty("third.get.mac");
        if (!TextUtils.isEmpty(mac))
        {
            mac = mac.replace(":", "");
        }
        return mac;
    }

    public static String getModel()
    {
        return SkySystemProperties.getProperty("ro.build.skymodel");
    }

    public static String getBarcode()
    {
        String barCode = SkySystemProperties.getProperty("third.get.barcode");
        String temp = "";
        char[] barCodeArray = barCode.toCharArray();
        for (int i = 0; i < barCode.length(); i++)
        {
            if ((barCodeArray[i] > 'Z' || barCodeArray[i] < 'A')
                    && (barCodeArray[i] > '9' || barCodeArray[i] < '0') && (barCodeArray[i] != '-'))
            {
                break;
            }
            temp += barCodeArray[i];
        }
        barCode = temp;

        return barCode;
    }


    // public String getVersionName()
    // {
    // try
    // {
    // String version = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
    // return version;
    // } catch (NameNotFoundException e)
    // {
    // e.printStackTrace();
    // return "";
    // }
    // }

    // public static String getSkytype()
    // {
    // return SkySystemProperties.getProperty("ro.build.skytype");
    // }
    //
    // public static String getMac()
    // {
    // String mac = "";
    // mac = SkySystemProperties.getProperty("third.get.mac").replace(":", "");
    // return mac;
    // }
    //
    // public static String getModel()
    // {
    // return SkySystemProperties.getProperty("ro.build.skymodel");
    // }
    //
    // public static String getBarcode()
    // {
    // String barCode = SkySystemProperties.getProperty("third.get.barcode");
    // String temp = "";
    // char[] barCodeArray = barCode.toCharArray();
    // for (int i = 0; i < barCode.length(); i++)
    // {
    // if ((barCodeArray[i] > 'Z' || barCodeArray[i] < 'A')
    // && (barCodeArray[i] > '9' || barCodeArray[i] < '0') && (barCodeArray[i] != '-'))
    // {
    // break;
    // }
    // temp += barCodeArray[i];
    // }
    // barCode = temp;
    //
    // return barCode;
    // }

}
