package com.coocaa.sky.api.wblogin;

import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;

import com.coocaa.sky.api.utils.PropertyUtils;
import com.coocaa.sky.api.utils.SignCore;

import java.util.HashMap;
import java.util.Map;

public class WbLogin
{
    // private static String TAG ="WbLogin";
    private String url = null;
    private WebView wb = null;
    private static Intent intent = null;

    public WbLogin(String url, WebView wb, Intent intent)
    {
        this.setUrl(url);
        this.setWb(wb);
        this.setIntent(intent);
        String iurl = inteOverSeaGration(url, intent);
        Login(iurl);
    }

    private String getUpUrl(String url, Intent intent)
    {
        String ret = url;
        String tel = intent.getStringExtra("tel");
        String token = intent.getStringExtra("token");
        String mac = PropertyUtils.getMac();
        String barcode = PropertyUtils.getBarcode();
        String model = PropertyUtils.getModel();
        String version = intent.getStringExtra("version");
        String uppara = "mobile=" + tel + "&" + "mac=" + mac + "&" + "barcode=" + barcode + "&"
                + "model=" + model + "&" + "version=" + version + "&" + "access_token=" + token
                + "&localBackGround=true";
        ret = ret + "?" + uppara;
        Log.i("CCAPI", ret);
        return ret;
    }

    private String inteOverSeaGration(String url, Intent intent)
    {
        String ret = url +"?sign2=";

        Map<String, String> payData = new HashMap<>();
        payData.put("useraccount", intent.getStringExtra("token"));
        payData.put("mac", intent.getStringExtra("MAC"));
        payData.put("language", intent.getStringExtra("language"));

        String data = "";
        for(String key : payData.keySet())
        {
            data = data + "&" +  key + "=" + payData.get(key);
        }
        ret = ret + SignCore.buildRequestMysign(payData, intent.getStringExtra("idCard")) + data;
        Log.i("CCAPI", payData.toString());
        Log.i("CCAPI", "idCard is >" + intent.getStringExtra("idCard"));
        Log.i("CCAPI", ret);
        return ret;
    }

    public void Login(String iurl)
    {
        wb.loadUrl(iurl);
        // wb.loadUrl("http://223.202.11.120:8080/view/exchange?token2.4a331c9bd08c40df883268f8b007de2f&version=503170584");
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    public void setWb(WebView wb)
    {
        this.wb = wb;
    }

    public WebView getWb()
    {
        return wb;
    }

    @SuppressWarnings("static-access")
    public void setIntent(Intent intent)
    {
        this.intent = intent;
    }

    public Intent getIntent()
    {
        return intent;
    }

}
