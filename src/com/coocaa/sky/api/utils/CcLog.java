/**
 * Copyright (C) 2012 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2016年6月29日         wen
 *
 */

package com.coocaa.sky.api.utils;

import com.skyworth.framework.skysdk.logger.SkyLogger;

public class CcLog
{
    public static final String TAG = "ccapi";

    /**
     * 概述：打印info信息<br/>
     * 
     * @param msg
     *            日志内容
     * @date 2013-10-22
     */
    public static void i(String msg)
    {
        SkyLogger.i(TAG, msg);
    }

    /**
     * 概述：打印error日志<br/>
     * 
     * @param msg
     *            日志内容
     * @date 2013-10-22
     */
    public static void e(String msg)
    {
        SkyLogger.e(TAG, msg);
    }

    /**
     * 概述：打印warning日志<br/>
     * 
     * @param msg
     *            日志内容
     * @date 2013-10-22
     */
    public static void v(String msg)
    {
        SkyLogger.v(TAG, msg);
    }

    /**
     * 概述：打印debug日志<br/>
     * 
     * @param msg
     *            日志内容
     * @date 2013-10-22
     */
    public static void d(String msg)
    {
        SkyLogger.d(TAG, msg);
    }

    /**
     * 概述：打印warning日志<br/>
     * 
     * @param msg
     *            日志内容
     * @date 2013-10-22
     */
    public static void w(String msg)
    {
        SkyLogger.w(TAG, msg);
    }

}
