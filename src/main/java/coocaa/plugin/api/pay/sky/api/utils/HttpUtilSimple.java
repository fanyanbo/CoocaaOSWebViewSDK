/**
 * Copyright (C) 2012 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2014-5-15         lenovo
 *
 */

package coocaa.plugin.api.pay.sky.api.utils;

import com.tianci.user.data.ULog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class HttpUtilSimple
{
    private final static String CHARSET = "utf-8";
    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f' };

    // 测试
    public static String CLIENT_ID_BETA = "0bbb2e0a0d0f4c539197c315e0f92c7b";
    public static String CLIENT_SECRET_BETA = "7;<i7b.Me}-dY_m*";
    // 正式
    private static String CLIENT_ID_RC = "75a3eaaf3e0e4d90a3cf56b03cc66bb5";
    private static String CLIENT_SECRET_RC = "82Y.8|qD8<LmjTp9";
    private static boolean TEST = false;
    private static boolean isBeta = false;

    public static void setIsBeta(boolean isBeta)
    {
        HttpUtilSimple.isBeta = isBeta;
    }

    public static String getClientId()
    {
        if (isBeta)
        {
            return CLIENT_ID_BETA;
        } else
        {
            return CLIENT_ID_RC;
        }
    }

    public static String getClientSecret()
    {
        if (isBeta)
        {
            return CLIENT_SECRET_BETA;
        } else
        {
            return CLIENT_SECRET_RC;
        }
    }

    /**
     * httpGet
     * 
     * @param uri
     *            请求的资源(包含参数)
     * @return
     * @throws com.skysri.utils.exceptions.ThirdPartException
     */
    public static HttpResult get(String uri)
    {
        return httpMethod(uri, null, "GET");
    }

    /**
     * httpGet
     * 
     * @param url
     *            地址
     * @param params
     *            参数
     * @return
     * @throws com.skysri.utils.exceptions.ThirdPartException
     */
    public static HttpResult get(String url, Map<String, String> params)
    {
        return getOrPut(url, params, "GET");
    }

    /**
     * httpPost
     * 
     * @param url
     *            地址
     * @param params
     *            参数
     */
    public static HttpResult post(String url, Map<String, String> params)
    {
        return httpMethod(url, getQueryString(params), "POST");
    }

    /**
     * httpPut
     * 
     * @param uri
     *            请求的资源(包含参数)
     */
    public static HttpResult put(String uri)
    {
        return httpMethod(uri, null, "PUT");
    }

    /**
     * 
     * @param url
     *            地址
     * @param params
     *            参数
     * @return
     * @throws com.skysri.utils.exceptions.ThirdPartException
     */
    public static HttpResult put(String url, Map<String, String> params)
    {
        return getOrPut(url, params, "PUT");
    }

    public static HttpResult getOrPut(String url, Map<String, String> params, String method)
    {
        if (params == null || params.size() == 0)
        {
            return httpMethod(url, null, method);
        } else
        {
            return httpMethod(url + "?" + getQueryString(params), null, method);
        }
    }

    public static HttpResult httpMethod(String uri, String q, String method)
    {
        if (TEST)
        {
            System.out.println("httpMethod, method" + method);
            System.out.println("httpMethod, uri = " + uri);
            System.out.println("httpMethod, q = " + q);
        } else
        {
            ULog.i("ccapi", "httpMethod, method" + method);
            ULog.i("ccapi", "httpMethod, uri = " + uri);
            ULog.i("ccapi", "httpMethod, query = " + q);
        }
        HttpResult httpReult = new HttpResult();
        PrintWriter out = null;
        BufferedReader in = null;
        try
        {
            HttpURLConnection con = (HttpURLConnection) (new URL(uri)).openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Accept-Charset", CHARSET);
            con.setConnectTimeout(10000);
            if ("POST".equals(method) && q != null)
            {
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                out = new PrintWriter(con.getOutputStream());
                out.write(q);
                out.flush();
            }
            InputStream inputStream;
            if (con.getResponseCode() >= 400)
            {
                inputStream = con.getErrorStream();
            } else
            {
                inputStream = con.getInputStream();
            }

            in = new BufferedReader(new InputStreamReader(inputStream, CHARSET));
            String result = "", line;
            while ((line = in.readLine()) != null)
            {
                result += line;
            }
            int status = con.getResponseCode();
            httpReult.httpStatus = status;
            httpReult.result = result;
            if (TEST)
            {
                System.out.println("httpStatus:" + status + ", result：" + result);
            } else
            {
                ULog.i("ccapi", "httpStatus:" + status + ", result：" + result);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e)
            {
            }

        }
        return httpReult;
    }

    private static String getQueryString(Map<String, String> params)
    {
        if (params == null)
            return "";
        String q = "";
        int i = 0;
        for (Map.Entry<String, String> param : params.entrySet())
        {
            try
            {
                // SkyLog.i("ccapi", "getQueryString, key = " + param.getKey() + ", value = "
                // + param.getValue());
                if (param != null && param.getValue() != null)
                    q += param.getKey() + "=" + URLEncoder.encode(param.getValue(), CHARSET);
            } catch (UnsupportedEncodingException e)
            {
            }
            i++;
            if (i != params.size())
                q += "&";
        }
        return q;
    }

    public static String MD5(String text, String charset)
    {
        MessageDigest msgDigest = null;

        try
        {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("System doesn't support MD5 algorithm.");
        }

        try
        {
            msgDigest.update(text.getBytes(charset)); // 注意改接口是按照指定编码形式签名

        } catch (UnsupportedEncodingException e)
        {

            throw new IllegalStateException("System doesn't support your  EncodingException.");

        }

        byte[] bytes = msgDigest.digest();

        String md5Str = new String(encodeHex(bytes));

        return md5Str;
    }

    public static char[] encodeHex(byte[] data)
    {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++)
        {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return out;
    }
    

}
