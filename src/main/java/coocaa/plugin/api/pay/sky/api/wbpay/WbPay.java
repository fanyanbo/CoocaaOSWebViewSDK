package coocaa.plugin.api.pay.sky.api.wbpay;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import coocaa.plugin.api.pay.ccapi.paydata.DefData;
import coocaa.plugin.api.pay.sky.api.utils.Base64;
import coocaa.plugin.api.pay.sky.api.utils.EncodeUtils;
import coocaa.plugin.api.pay.sky.api.utils.PropertyUtils;
import coocaa.plugin.api.pay.sky.api.utils.SignCore;

public class WbPay
{
    private String url = null;
    private WebView wb = null;
    private static Intent intent = null;

    public WbPay(String url, WebView wb, Intent intent)
    {
        this.setUrl(url);
        this.setWb(wb);
        this.setIntent(intent);
        String upUrl = "";
        if(intent.getStringExtra("cmd").equals(DefData.CMDPAY))
        {
            upUrl = inteGration(url, intent);
        }else
        {
            upUrl = inteOverSeaGration(url, intent);
        }

        getWb().loadUrl(upUrl);
    }

    private String inteGration(String url, Intent intent)
    {
        String ret = url;
        String appcode = intent.getStringExtra("appcode");
        String ProductName = intent.getStringExtra("ProductName");
        String tradeid = intent.getStringExtra("Tradeid");
        double amount = intent.getDoubleExtra("amount", 0.0);
        String tel = intent.getStringExtra("tel");
        String ProductType = intent.getStringExtra("ProductType");
        String mac = PropertyUtils.getMac();
        String SpecialType = intent.getStringExtra("SpecialType");
        String token = intent.getStringExtra("token");
        String appVersion = intent.getStringExtra("ver");
        String appName = intent.getStringExtra("pkg");
        String order_Adress = intent.getStringExtra("order_Address");
        try
        {
            if (order_Adress != null)
                order_Adress = URLEncoder.encode(order_Adress, "UTF-8");
        } catch (UnsupportedEncodingException e1)
        {
            e1.printStackTrace();
        }
        int productcatalog = 1;
        try
        {
            if (ProductType.contains("虚拟"))
            {
                productcatalog = 0;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        String uppara = "{" + "'appcode':" + "'" + appcode + "'" + "," + "'tradeid':" + "'"
                + tradeid + "'" + "," + "'productname':" + "'" + ProductName + "'" + ","
                + "'amount':" + "'" + amount + "'" + "," + "'productcatalog':" + "'"
                + productcatalog + "'" + "," + "'specialtype':" + "'" + SpecialType + "'" + ","
                + "'useraccount':" + "'" + tel + "'" + "," + "'mac':" + "'" + mac + "'" + "}";
        String jsondata = "{" + "'appversion':" + "'" + appVersion + "'" + "," + "'packagename':"
                + "'" + appName + "'" + "}";
        String ba = Base64.encode(jsondata.getBytes());
        String en = ba.replace("+", "-").replace("/", "_").replace("=", ".");

        int random = (int) (Math.random() * 1000);
        String para = EncodeUtils.encode(uppara, random);
        ret = ret + "?" + "jsonParam=" + para + "&" + "ra=" + random + "&" + "access_token="
                + token + "&" + "jsondata=" + en + "&" + "address=" + order_Adress;
        ;
        Log.i("CCAPI", ret);
        Log.i("CCAPI", uppara);
        Log.i("CCAPI", jsondata);
        return ret;
    }

    private String inteOverSeaGration(String url, Intent intent)
    {
        String ret = url +"?sign2=";

        Map<String, String> payData = (Map<String, String>)intent.getSerializableExtra("payData");
//        payData.put("amount", String.valueOf(intent.getDoubleExtra("amount", 0.0)));
//        payData.put("currencycode", intent.getStringExtra("currencycode"));
//        payData.put("appcode", intent.getStringExtra("appcode"));
//        payData.put("tradeid", intent.getStringExtra("Tradeid"));
//        payData.put("notifyurl", intent.getStringExtra("notifyurl"));
//        payData.put("sign", intent.getStringExtra("sign"));
//        payData.put("productname", intent.getStringExtra("ProductName"));
//        payData.put("useraccount", intent.getStringExtra("token"));
//        payData.put("apkversioncode", intent.getStringExtra("verCode"));
//        payData.put("apkpackagename", intent.getStringExtra("pkgName"));

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
