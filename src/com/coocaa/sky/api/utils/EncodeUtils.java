/**
 * Copyright (C) 2012 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2016年5月30日         wen
 *
 */

package com.coocaa.sky.api.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @ClassName EncodeUtils
 * @Description 编码、解码工具类
 * @author wen
 * @date 2016年5月30日
 */
public class EncodeUtils
{

    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F' };

    private static String toHexString(byte[] b)
    {
        // String to byte
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++)
        {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    private static String md5(String s)
    {
        try
        {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            return toHexString(messageDigest);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * @Description 编码字符串<br/>
     * @param inString
     *            待编码字符串
     * @param random
     *            随机种子
     * @return String 编码结果
     * @date 2016年5月30日
     */
    public static String encode(String inString, int random)
    {
        String encryptingCode = null;
        // String pd = md5(md5("" + random).substring(5, 21)).substring(3, 19);
        try
        {
            encryptingCode = SimpleCrypto.encrypt(generateSeed(random), inString);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return encryptingCode;
    }

    /**
     * @Description 解码字符串<br/>
     * @param inString
     *            待解码字符串
     * @param random
     *            随机数
     * @return String 解码后的字符串
     * @date 2016年5月30日
     */
    public static String decode(String inString, int random)
    {
        String encryptingCode = null;
        // String pd = md5(md5("" + random).substring(5, 21)).substring(3, 19);
        try
        {
            encryptingCode = SimpleCrypto.decrypt(generateSeed(random), inString);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return encryptingCode;
    }

    private static String generateSeed(int random)
    {
        return md5(md5("" + random).substring(5, 21)).substring(3, 19);
    }
}
