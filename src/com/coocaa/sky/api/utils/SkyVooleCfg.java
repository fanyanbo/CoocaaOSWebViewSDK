package com.coocaa.sky.api.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <p>Description:</p> 
 * <p>write something</p>
 * @ClassName SkyVooleCfg
 * @author C_Ruby
 * @date 2017年3月23日
 * @version V*.*.*
 */
public class SkyVooleCfg
{
    public static String getVooleCfgPath()
    {
        int tc = 0;
        
        try
        {
            String tcVersion = getFullTianciVersion().replace(".","");
            tc = Integer.parseInt(tcVersion);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        
        if(tc >= 500000000)
        {
            return "/system/bin/";
        }else
        {
            return "/skydir/sharefiles/product/voole/";
        }
    }

    public static int getTcVersion()
    {
        int tc = 0;

        try
        {
            String tcVersion = getFullTianciVersion().replace(".","");
            tc = Integer.parseInt(tcVersion);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return tc;
    }
    
    private static String readFileLine(String filePath)
    {
        String line = null;
        try
        {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath)));
            line = br.readLine();
            br.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("/system/vendor/TianciVersion : " + line);
        if (line == null)
            line = "";
        return line;
    }
    
    public static String getFullTianciVersion()
    {
        return readFileLine("/system/vendor/TianciVersion");
    }
}
