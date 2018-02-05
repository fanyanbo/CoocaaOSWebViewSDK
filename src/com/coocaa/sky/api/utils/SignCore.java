package com.coocaa.sky.api.utils;

import com.skyworth.framework.skysdk.util.MD5;
import com.tianci.media.api.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by win7 on 2015/5/25.
 */
public class SignCore {

    public static String input_charset = "utf-8";
    /**
     * 除去数组中的空值和签名参数
     *
     * @param sArray
     *            签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     *
     * @param params
     *            需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }

    /**
     * 链接参数并将参数的值进行urlEncode编码
     * @param params
     * @return
     */
    public static String createLinkStringUrlEncode(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";
        try {
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String value = null;
                value = URLEncoder.encode(params.get(key), "utf8");
                if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                    prestr = prestr + key + "=" + value;
                } else {
                    prestr = prestr + key + "=" + value + "&";
                }
            }
        } catch (UnsupportedEncodingException e) {

        }
        return prestr;
    }

    /**
     * 将http请求的参数对象转化成map对象
     * @param requestParams
     * @return
     */
    public static Map<String,String> parseRequestParams(Map<String,String[]> requestParams){
        Map<String,String> params = new HashMap<String, String>();

        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? (valueStr + values[i]) : (valueStr + values[i] + ",");
            }
            params.put(name, valueStr);
        }

        return params;
    }

    /**
     * 生成签名结果
     *
     * @param
     *
     * @return 签名结果字符串
     */
    public static String buildRequestMysign(Map<String, String> sParaTemp, String key) {

        Map<String, String> sPara = paraFilter(sParaTemp);

        String prestr = createLinkString(sPara); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = "";

        Log.i("CCAPI", "加密前 data >" + prestr+key);
        mysign = MD5.md5s(prestr+key);
        return mysign;
    }

    /**
     * 生成签名结果
     * @param sParaTemp
     * @param key
     * @return
     */
    public static String buildRequestSign(Map<String, String[]> sParaTemp, String key) {
        Map<String, String> sPara = parseRequestParams(sParaTemp);

        return buildRequestMysign(sPara, key);
    }

}
