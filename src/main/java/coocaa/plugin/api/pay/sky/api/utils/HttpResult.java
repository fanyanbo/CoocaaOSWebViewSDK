/**
 * Copyright (C) 2012 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2014-5-15         lenovo
 *
 */

package coocaa.plugin.api.pay.sky.api.utils;

import com.alibaba.fastjson.TypeReference;
import com.skyworth.framework.skysdk.util.SkyJSONUtil;
import com.skyworth.framework.skysdk.util.SkyObjectByteSerialzie;
import com.tianci.user.data.ByteUtil;

import java.io.Serializable;
import java.util.Map;

public class HttpResult implements Serializable
{
    private static final long serialVersionUID = -3272364520939963684L;
    public int httpStatus;
    public String result;

    public HttpResult()
    {
    }

    public HttpResult(byte[] body)
    {
        HttpResult httpResult = SkyObjectByteSerialzie.toObject(body, HttpResult.class);
        if (httpResult != null)
        {
            this.httpStatus = httpResult.httpStatus;
            this.result = httpResult.result;
        }
    }

    public String getAccessToken()
    {
        if (result != null)
        {
            Map<String, String> tokens = parseMsg();
            if (tokens != null)
            {
                return tokens.get("access_token");
            }
        }
        return null;
    }

    public Map<String, String> parseMsg()
    {
        return SkyJSONUtil.getInstance().parseObject(result,
                new TypeReference<Map<String, String>>()
                {
                });
    }

    public Map<String, Object> parseMsgToObject()
    {
        return SkyJSONUtil.getInstance().parseObject(result,
                new TypeReference<Map<String, Object>>()
                {
                });
    }

    private final String KEY_CODE = "code";

    /**
     * @Description 获取通信返回码<br/>
     * @return int
     * @date 2015年10月16日
     */
    public int getCode()
    {
        int code = 0;
        Map<String, String> map = parseMsg();
        if (map != null)
        {
            code = ByteUtil.pasreInt(map.get(KEY_CODE));
        }
        return code;
    }

    private final String KEY_MSG = "msg";

    /**
     * @Description 获取HTTP通信结果中的提示信息<br/>
     * @return String
     * @date 2015年10月19日
     */
    public String getMessage()
    {
        String message = null;
        Map<String, String> map = parseMsg();
        if (map != null)
        {
            message = map.get(KEY_MSG);
        }
        return message;
    }

    public byte[] getByte()
    {
        return SkyObjectByteSerialzie.toBytes(this);
    }

    @Override
    public String toString()
    {
        return "HttpResult [httpStatus=" + httpStatus + ", result=" + result + "]";
    }
}
