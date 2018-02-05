package com.coocaa.sky.api;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.coocaa.ccapi.paydata.ApiDefData;
import com.coocaa.ccapi.paydata.DefData;
import com.coocaa.ccapi.paydata.OrderData;
import com.coocaa.ccapi.paydata.OrderState;
import com.coocaa.ccapi.paydata.PayBackData;
import com.coocaa.ccapi.paydata.UserInfo;
import com.coocaa.sky.api.utils.HttpResult;
import com.coocaa.sky.api.utils.HttpUtilSimple;
import com.coocaa.sky.api.utils.SimpleCrypto;
import com.coocaa.sky.api.wblogin.CfgFile;
import com.skyworth.framework.skysdk.app.SkyAppInfo;
import com.skyworth.framework.skysdk.app.SkyAppService;
import com.skyworth.framework.skysdk.properties.SkyGeneralProperties;
import com.skyworth.framework.skysdk.properties.SkyGeneralProperties.GeneralPropKey;
import com.skyworth.framework.skysdk.util.SkyJSONUtil;


import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class CcApi
{
    private Context context = null;
    public String v = null;
    private CfgFile cfgfile = null;
    private UserInfo origus = null;
    private PurchaseCallBack pBack = null;
    private Login_PurchaseCallBack noLogin_pBack = null;
    private OrderData orderData = null;
    private BroadcastReceiver b = null;
    private BroadcastReceiver bR = null;
    private PayListener listener;

    public CcApi(Context context)
    {
        setContext(context);
        v = checkUrl();
    }

    public interface PayListener
    {
        public void recvOrderState(OrderState data);
    }

    /*
     * 读取配置文件支付域名
     */
    public static String checkUrl()
    {
        // return "https://pay.coocaa.com";
        String payEntryPoint = SkyGeneralProperties.getProperty(GeneralPropKey.CURRENT_PAY_SERVER);
        if (TextUtils.isEmpty(payEntryPoint))
        {
            payEntryPoint = NEW_PAY_ENTRY_POINT;
            // payEntryPoint = "pay.coocaatv.com";
        }

        if (!payEntryPoint.startsWith(HTTP_PREFIX))
        {
            // 老的地址使用http，其它走https
            if (OLD_PAY_ENTRY_POINT.equals(payEntryPoint))
            {
                payEntryPoint = HTTP_PROTOCOL + payEntryPoint;
            } else
            {
                payEntryPoint = HTTPS_PROTOCOL + payEntryPoint;
            }
        }

        Log.i("ccapi", " - get pay entry point =  "
                + payEntryPoint);
        return payEntryPoint;
    }
    private static final String OLD_PAY_ENTRY_POINT = "pay.coocaatv.com";
    private static final String NEW_PAY_ENTRY_POINT = "pay.coocaa.com";
    private static final String HTTP_PREFIX = "http";
    private static final String HTTP_PROTOCOL = "http://";
    private static final String HTTPS_PROTOCOL = "https://";

    public static String getUrl(InputStream instream) throws Exception
    {
        String url = null;
        XmlPullParser parser = Xml.newPullParser();// 得到Pull解析器
        parser.setInput(instream, "UTF-8");// 设置下输入流的编码
        int eventType = parser.getEventType();// 得到第一个事件类型
        while (eventType != XmlPullParser.END_DOCUMENT)
        {// 如果事件类型不是文档结束的话则不断处理事件
            switch (eventType)
            {
                case (XmlPullParser.START_DOCUMENT):// 如果是文档开始事件
                    break;
                case (XmlPullParser.START_TAG):// 如果遇到标签开始

                    String tagName = parser.getName();// 获得解析器当前元素的名称
                    if ("config".equals(tagName))
                    {// 如果当前标签名称是<person>
                        if (DefData.CONFNAME.equals(parser.getAttributeValue(0)))
                        {
                            url = parser.getAttributeValue(1);
                            break;
                        }
                    }
                    break;
                case (XmlPullParser.END_TAG):
                    break;
            }
            eventType = parser.next();
        }
        return url;
    }





    public boolean exitPayCenter()
    {
        SkyAppInfo ccinfo = SkyAppService.getSkyAppService(context).getAppInfo(
                "com.coocaa.sky.ccapi");
        if (ccinfo.versionCode >= 10)
        {
            Intent intent = new Intent("com.coocaa.sky.ccapi.exit");
            context.sendBroadcast(intent);
            return true;
        }
        return false;
    }

    public void getOrderPayState(final String appCode, final String tradeId,
            final PayListener listener)
    {
        try
        {
            new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    String data = "{\"appCode\":\"" + appCode + "\",\"ybDealNo\":\"" + tradeId
                            + "\"}";
                    Map<String, String> map = new HashMap<String, String>();
                    int random = (int) (Math.random() * 1000);
                    String para = encode(data, random);
                    map.put("jsonParam", para);
                    map.put("ra", random + "");
                    String url = checkUrl();
                    HttpResult result = HttpUtilSimple.post(url
                            + "/MyCoocaa/orderinfo_interface/viewOrderInfo.action", map);
                    OrderState object = null;
                    if (result != null)
                    {
                        object = SkyJSONUtil.getInstance().parseObject(result.result,
                                OrderState.class);
                    }
                    listener.recvOrderState(object);

                }
            }).start();
        } catch (Exception e)
        {
            listener.recvOrderState(null);
            e.printStackTrace();
        }

    }

    public void purchase(OrderData orderData, PurchaseCallBack pB)
    {
        this.pBack = pB;
        this.orderData = orderData;
            Log.i("ccapi", "go to pay");

            SkyAppInfo info = SkyAppService.getSkyAppService(context).getAppInfo(
                    context.getPackageName());
            Intent mIntent = new Intent();
            ComponentName comp = null;

            mIntent.setAction("android.intent.action.VIEW");
            mIntent.putExtra("cmd", DefData.CMDPAY);
            mIntent.putExtra("appcode", orderData.appcode);
            mIntent.putExtra("ProductName", orderData.ProductName);
            mIntent.putExtra("Tradeid", orderData.TradeId);
            mIntent.putExtra("amount", orderData.amount);
            mIntent.putExtra("isNeedLogin", false);
            mIntent.putExtra("ProductType", orderData.ProductType);
            mIntent.putExtra("SpecialType", orderData.SpecialType);

            mIntent.putExtra("uid", context.getApplicationInfo().uid);
            mIntent.putExtra("ver", info.versionName);
            mIntent.putExtra("verCode", info.versionCode);
            mIntent.putExtra("pkgName", info.appName);
            mIntent.putExtra("pkg", info.pname);

                comp = new ComponentName("com.coocaa.systemwebview", "com.coocaa.sky.ccapi.MActivity");
            mIntent.setComponent(comp);
            context.startActivity(mIntent);

            b = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    if (intent != null)
                    {
                        if (intent.getAction().equals("com.coocaa.onpurchBack"))
                        {
                            PayBackData pb = new PayBackData(ApiDefData.PAYFAILED, null, "", null,
                                    0, "-1", "");
                            try
                            {
                                pb.payStatus = intent.getIntExtra("status", ApiDefData.PAYFAILED);
                                pb.tradeID = intent.getStringExtra("tradeID");
                                pb.retMsg = intent.getStringExtra("retmsg");
                                pb.purchWay = intent.getStringExtra("purchWay");
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            onPayBack(pb);
                        }
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.coocaa.onpurchBack");
            context.registerReceiver(b, intentFilter);
    }


    public boolean isNetConnected()
    {
        boolean bnetconnect = false;
        ConnectivityManager conMan = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo actconnect = conMan.getActiveNetworkInfo();
        WifiManager cm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int info_wifi = cm.getWifiState();
        NetworkInfo wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (actconnect != null
                && actconnect.getState().equals(NetworkInfo.State.CONNECTED)
                || ((info_wifi == WifiManager.WIFI_STATE_ENABLED) && wifi != null && wifi
                        .getState().equals(NetworkInfo.State.CONNECTED)))
        {
            bnetconnect = true;
        }
        return bnetconnect;
    }

    private void setContext(Context context)
    {
        this.context = context;
    }

    @SuppressWarnings("unused")
    private Context getContext()
    {
        return context;
    }

    private void onPayBack(PayBackData pb)
    {
        if (pBack != null)
        {
            pBack.pBack(pb.payStatus, pb.tradeID, pb.retMsg, pb.purchWay);
        }
        try
        {
            if (b != null)
            {
                context.unregisterReceiver(b);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void on_NoLoginPayBack(PayBackData pb)
    {
        if (noLogin_pBack != null)
        {
            noLogin_pBack.pBack(pb.payStatus, pb.tradeID, pb.retMsg, pb.purchWay, pb.address);
        }
        try
        {
            if (b != null)
            {
                context.unregisterReceiver(b);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F' };

    public static String toHexString(byte[] b)
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

    public static String encode(String inString, int random)
    {
        String encryptingCode = null;
        String pd = md5(md5("" + random).substring(5, 21)).substring(3, 19);
        try
        {
            encryptingCode = SimpleCrypto.encrypt(pd, inString);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return encryptingCode;
    }

    public static String decode(String inString, int random)
    {
        String encryptingCode = null;
        String pd = md5(md5("" + random).substring(5, 21)).substring(3, 19);
        try
        {
            encryptingCode = SimpleCrypto.decrypt(pd, inString);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return encryptingCode;
    }

    public interface PurchaseCallBack
    {
        public abstract void pBack(int resultstatus, String tradeId, String resultmsg,
                                   String purchWay);
    }

    public interface Login_PurchaseCallBack
    {
        public abstract void pBack(int resultstatus, String tradeId, String resultmsg,
                                   String purchWay, String address);
    }

}
