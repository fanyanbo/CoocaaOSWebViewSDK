package com.coocaa.sky.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cordova.CordovaBaseActivity;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.ccapi.paydata.ApiDefData;
import com.coocaa.ccapi.paydata.DefData;
import com.coocaa.ccapi.paydata.PayBackData;
import com.coocaa.dataer.api.SkyDataer;
import com.coocaa.dataer.api.event.page.custom.PageCustomEvent;
import com.coocaa.dataer.api.event.page.lifecycle.PageProperty;
import com.coocaa.sky.api.utils.CcLog;
import com.coocaa.sky.api.utils.EncodeUtils;
import com.coocaa.sky.api.utils.SkyVooleCfg;
import com.coocaa.sky.api.wblogin.CfgFile;
import com.coocaa.sky.api.wblogin.WbLogin;
import com.coocaa.sky.api.wbpay.WbPay;
import com.coocaa.systemwebview.R;
import com.skyworth.framework.skysdk.app.SkyAppInfo;
import com.skyworth.framework.skysdk.app.SkyAppService;
import com.skyworth.framework.skysdk.ipc.SkyApplication;
import com.skyworth.framework.skysdk.ipc.SkyCmdProcessInstance;
import com.skyworth.framework.skysdk.ipc.SkyContext;
import com.skyworth.framework.skysdk.logger.SkyLogger;
import com.skyworth.framework.skysdk.util.SkyJSONUtil;
import com.skyworth.ui.blurbg.BlurBgLayout;
import com.skyworth.util.Util;
import com.tianci.net.api.NetApiForCommon;
import com.tianci.system.api.TCSettingApi;
import com.tianci.user.data.UserInfo;
import com.tianci.vip.api.SkyVipApi;
import com.tianci.vip.data.PayMissionParams;

public class MActivity extends BaseActivity {
    private WebView wb = null;
    private Handler mHandler = new Handler();
    private String cmd = null;
    private Intent mIntent;
    private String url;
    private String token;
    private String idCard;
    private FrameLayout layout;
    private LoadingView loadView;
    private PayBackData pb = null;
    private boolean isOver;
    private String payAction;
    private boolean isOnPause;
    private PayBackListener payBackListener;

    public class JavaScriptInterface {
        @JavascriptInterface
        public void startApk(String pkName) {
            PackageManager packageManager = MActivity.this.getPackageManager();
            Intent intent = new Intent();
            try {
                intent = packageManager.getLaunchIntentForPackage(pkName);
            } catch (Exception e) {
            }
            startActivity(intent);
        }

        @JavascriptInterface
        public void loadBg() {
            handler.sendEmptyMessage(1);
        }

        @JavascriptInterface
        public void exit() {
            onexit();
        }

        @JavascriptInterface
        public void clearUserAccount() {
            clearAccount();
        }

        @JavascriptInterface
        public void payCenterExit() {
            Log.i("CCAPI", "payCenter exit!");
            MActivity.this.finish();
        }

        @JavascriptInterface
        public void subLogString(String map) {
            try {
                JSONObject joData = new JSONObject(map);
                Iterator iterator = joData.keys();
                Map logMap = new HashMap();
                while (iterator.hasNext()) {
                    String key = iterator.next() + "";
                    String value = joData.getString(key);
                    logMap.put(key, value);
                    Log.i("ccapi", "key is >" + key + " ,value is >" + value);
                }
                webviewsubLog(logMap, logMap.get("journal").toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.i("ccapi", "subLog exception");
            }
        }

    }

    protected interface PayBackListener {
        public void pBack(int resultstatus, String tradeId, String resultmsg,
                          String purchWay);
    }

    protected void setPayBackListener(PayBackListener listener) {
        this.payBackListener = listener;
    }

    public void webviewsubLog(final Map<String, String> logMap, final String eventID) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    PageCustomEvent event = SkyDataer.onEvent().pageEvent().pageCustomEvent();
                    event.withEventID(eventID);
                    if (logMap != null) {
                        for (String headItem : logMap.keySet()) {
                            event.withParam(headItem, logMap.get(headItem));
                        }
                    }
                    event.submit();
                } catch (Exception e) {
                }
            }
        });
    }

    private void purchase() {
        Log.i("ccapi", "go to pay");
        this.payAction = mIntent.getStringExtra("payAction");
        SkyAppInfo info = SkyAppService.getSkyAppService(this)
                .getAppInfo(this.getPackageName());
        pb = new PayBackData(ApiDefData.PAYERROR, null, "", null, 0, "-1", "");
        mIntent.putExtra("isNeedLogin", true);
        mIntent.putExtra("uid", this.getApplicationInfo().uid);
        mIntent.putExtra("ver", info.versionName);
        mIntent.putExtra("verCode", info.versionCode);
        mIntent.putExtra("pkgName", info.appName);
        mIntent.putExtra("pkg", info.pname);
    }

    private void overSeaPurchase() {
        Log.i("ccapi", "go to overSeapay");
        this.payAction = mIntent.getStringExtra("payAction");
        pb = new PayBackData(ApiDefData.PAYERROR, null, "", null, 0, "-1", "");
        mIntent.putExtra("language", getResources().getConfiguration().locale.toString());
        mIntent.putExtra("isNeedLogin", true);
        mIntent.putExtra("MAC", getMAC());
    }

    protected void reLoad(Intent intent) {
        init(intent);
    }

    protected void clearAccount() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("CCAPI", "onNewIntent!");
        isOnPause = false;
        super.onNewIntent(intent);
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            if (msg.what == 1) {
                isOver = true;
                Log.i("CCAPI", "fastPayment is created over!");
                wb.getSettings().setBlockNetworkImage(false);
                loadView.setVisibility(View.INVISIBLE);
            }
        }

        ;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(getIntent());
        Log.i("CCAPI", "coocaa payCenter version :" + getVersionName());

    }

    protected String getOverseaServer() {
        return "https://beta-gpay.coocaa.com";
    }

    protected String getServer() {
        return "https://pay.coocaa.com";
    }

    private void init(Intent intent) {
        this.mIntent = intent;
        cmd = mIntent.getStringExtra("cmd");
        if (TextUtils.isEmpty(cmd)) {
            return;
        }
        token = mIntent.getStringExtra("token");
        idCard = mIntent.getStringExtra("idCard");
        initWebView();
        url = CcApi.checkUrl();
//        url = "http://beta.pay.coocaatv.com";
        if (SkyVooleCfg.getTcVersion() >= 420000000 || SkyVooleCfg.getTcVersion() == 0) {
            if (cmd.equals(DefData.CMDPAY)) {
                url = getServer() + DefData.PAYURL;
                purchase();
            } else if (cmd.equals(DefData.CMDPAY_OVERSEA)) {
                url = getOverseaServer() + DefData.PAYURL_OVERSEA;
                overSeaPurchase();
            } else if (cmd.equals(DefData.CMDLOGIN_OVERSEA)) {
                url = getOverseaServer() + DefData.LOGINURL_OVERSEA;
                overSeaPurchase();
            }
            if (!TextUtils.isEmpty(token) || !TextUtils.isEmpty(idCard)) {
                if (cmd.equals(DefData.CMDPAY) || cmd.equals(DefData.CMDPAY_OVERSEA)) {
                    new WbPay(url, wb, mIntent);
                } else if (cmd.equals(DefData.CMDLOGIN_OVERSEA)) {
                    new WbLogin(url, wb, mIntent);
                } else {
                    onexit();
                }
            } else {
                Log.i("CCAPI", "no Loginpay!");
                if (cmd.equals(DefData.CMDPAY)) {
                    mIntent.putExtra("token", "");
                    mIntent.putExtra("tel", "-1");
                    new WbPay(url, wb, mIntent);
                } else {
                    onexit();
                }

            }

        } else {
            CfgFile file = new CfgFile(DefData.CFGFILEPATH);
            com.coocaa.ccapi.paydata.UserInfo logiInfo = null;
            if (file.isexists()) {
                logiInfo = new com.coocaa.ccapi.paydata.UserInfo();
                try {
                    file.readFile(logiInfo);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (logiInfo != null && logiInfo.loginstatus == ApiDefData.LOGINPASS && !TextUtils.isEmpty(logiInfo.token)) {
                String tel = logiInfo.tel;
                if (cmd.equals(DefData.CMDPAY)) {
                    mIntent.putExtra("token", logiInfo.token);
                    mIntent.putExtra("tel", tel);
                    new WbPay(url + DefData.PAYURL, wb, mIntent);
                } else if (cmd.equals(DefData.CMDLOGIN)) {
                    mIntent.putExtra("token", logiInfo.token);
                    mIntent.putExtra("tel", tel);
                    new WbLogin(url + DefData.LOGINURL, wb, mIntent);
                } else {
                    pb = new PayBackData(ApiDefData.PAYERROR, null, "", null, 0, "-1", "");
                    onexit();
                }
            } else {
                Log.i("CCAPI", "no Login!");
                pb = new PayBackData(ApiDefData.PAYERROR, null, "", null, 0, "-1", "");
                onexit();
            }
        }
    }

    private static String readFileByLines(String fileName) {
        String content = "";
        File file = new File(fileName);
        if (!file.exists()) {
            return content;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                content += tempString;
                System.out.println("line " + line + ": " + tempString);
                line++;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return content;
    }

    private String getMAC() {
        String mac = readFileByLines("/sys/class/net/eth0/address");
        if (!TextUtils.isEmpty(mac))
            return mac.replace(":", "").toUpperCase();
        return "";
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initWebView() {
        layout = new FrameLayout(this);
        layout.setLayoutParams(new LayoutParams(Util.Div(1920), Util.Div(1080)));
        setContentView(layout);
        loadView = new LoadingView(this);
        if (cmd.equals(DefData.CMDLOGIN)) {
            FrameLayout blurBgLayout = new FrameLayout(this);
            blurBgLayout.setBackgroundResource(R.drawable.ui_sdk_main_page_bg_theme_2);
            layout.addView(blurBgLayout);
        }

        wb = new WebView(this);
        layout.addView(wb);

        layout.addView(loadView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        loadView.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadView.setVisibility(View.VISIBLE);
            }
        }, 300);

        wb.setBackgroundColor(Color.TRANSPARENT);
        wb.setWebViewClient(new HelloWebViewClient());
        wb.addJavascriptInterface(new onPayBackJavaScript(), "onPayBack");
        wb.addJavascriptInterface(new onDefBacckJavaScript(), "onDefaultBack");
        wb.addJavascriptInterface(new JavaScriptInterface(), "Android");
        // initWebViewSettings();
        WebSettings settings = wb.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setLoadWithOverviewMode(true);
        settings.setNeedInitialFocus(false);
        settings.setBlockNetworkImage(true);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);

        wb.setFocusable(false);

    }


    @Override
    protected void onResume() {
        super.onResume();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    PageProperty pageProperty = new PageProperty().withName("pay");
                    SkyDataer.onEvent().pageEvent().pageResumeEvent().onResume(pageProperty);
                } catch (Exception e) {
                    SkyLogger.i("UserAction", "submit detailPageResume data faild!!!");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        CcLog.i("--onPause() ");
        isOnPause = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    PageProperty pageProperty = new PageProperty().withName("pay");
                    SkyDataer.onEvent().pageEvent().pagePausedEvent().onPaused(pageProperty);
                } catch (Exception e) {
                    SkyLogger.i("UserAction", "submit detailPageResume data faild!!!");
                }
            }
        });
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // netListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        CcLog.i("--onStop() ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wb != null) {
            if(layout != null)
            {
                layout.removeView(wb);
            }
            wb.removeAllViews();
            wb.destroy();
        }
    }


    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            wb.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                                    String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            layout.removeAllViews();

            FrameLayout blurBgLayout = new FrameLayout(MActivity.this);
            blurBgLayout.setBackgroundResource(R.drawable.ui_sdk_main_page_bg_theme_2);
            layout.addView(blurBgLayout);

            LinearLayout noDataLayout = new LinearLayout(MActivity.this);
            noDataLayout.setOrientation(LinearLayout.VERTICAL);

            ImageView img = new ImageView(MActivity.this);
            img.setImageResource(R.drawable.net_refresh);
            LinearLayout.LayoutParams imlp = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            imlp.gravity = Gravity.CENTER_HORIZONTAL;
            noDataLayout.addView(img, imlp);

            TextView textV = new TextView(MActivity.this);
            textV.setText(R.string.error);
            textV.setTextSize(Util.Dpi(42));
            textV.setTextColor(Color.parseColor("#505050"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.topMargin = Util.Div(30);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            noDataLayout.addView(textV, lp);

            LayoutParams noLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            noLp.gravity = Gravity.CENTER;
            layout.addView(noDataLayout, noLp);

            isOver = false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

        }

    }

    final class onDefBacckJavaScript {
        @JavascriptInterface
        public void onDefBack(final String url) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    cmd = null;
                    onexit();
                }
            });
        }

        ;
    }

    final class onPayBackJavaScript {
        @JavascriptInterface
        public void onPayBack(final String url, final int random) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    pb = new PayBackData(1, null, "", null, 0, null, "");
                    String ret = EncodeUtils.decode(url, random);
                    Log.i("CCAPI", "onPayBack string " + ret);
                    try {
                        JSONObject result = new JSONObject(ret);
                        String status = result.getString("resultstatus");
                        if (status.equals("10000")) {
                            pb.payStatus = ApiDefData.PAYSUCCESS;
                        } else if (status.equals("10001")) {
                            pb.payStatus = ApiDefData.PAYFAILED;
                        } else {
                            pb.payStatus = ApiDefData.PAYERROR;
                        }
                        pb.tradeID = result.getString("tradeid");
                        pb.retMsg = result.getString("resultmsg");
                        pb.purchWay = result.getString("paymentway");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    onexit();
                }
            });
        }

        ;
    }

    public void jiFeng(double price, String tradeID) {
        Log.i("ccapi", "积分成功:" + tradeID);
        SkyVipApi vipApi = new SkyVipApi(SkyContext.getListener());
        PayMissionParams payMissionParams = new PayMissionParams();
        payMissionParams.appCode = mIntent.getStringExtra("appcode");
        payMissionParams.orderId = tradeID;
        payMissionParams.prodName = mIntent.getStringExtra("ProductName");
        vipApi.addMission(payMissionParams);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int key = -99;
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
                key = keyCode + 48 - KeyEvent.KEYCODE_0;
                break;
            case KeyEvent.KEYCODE_NUMPAD_0:
            case KeyEvent.KEYCODE_NUMPAD_1:
            case KeyEvent.KEYCODE_NUMPAD_2:
            case KeyEvent.KEYCODE_NUMPAD_3:
            case KeyEvent.KEYCODE_NUMPAD_4:
            case KeyEvent.KEYCODE_NUMPAD_5:
            case KeyEvent.KEYCODE_NUMPAD_6:
            case KeyEvent.KEYCODE_NUMPAD_7:
            case KeyEvent.KEYCODE_NUMPAD_8:
            case KeyEvent.KEYCODE_NUMPAD_9:
                key = keyCode + 96 - KeyEvent.KEYCODE_NUMPAD_0;
                break;
            case KeyEvent.KEYCODE_BACK:
                key = 27;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                key = 38;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                key = 40;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                key = 37;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                key = 39;
                break;
            case KeyEvent.KEYCODE_NUM:
            case 228:
                return super.onKeyDown(keyCode, event);
            case 3:
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_DEL:
                key = 8;// KEY BACKSPACE
                break;
            case KeyEvent.KEYCODE_ENTER:
                key = 13;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                key = 13;
                break;

        }
        if (key == 27) {
            if (!netIsConnect() && isOver) {
                TCSettingApi settingApi = new TCSettingApi();
                settingApi.connectNetworkWithConfirmUI(MActivity.this);
                return true;
            } else if (!isOver) {
                onexit();
            }
        }
        if (key != -99) {
            {
                simulateKeyEvent("", key, true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void simulateKeyEvent(String key, int code, boolean keyDown) {
        if (wb != null) {
            Log.i("ccapi", "sureBtn clicked!");
            wb.loadUrl("javascript:(function(){var ev=document.createEvent('HTMLEvents');ev.which=ev.keyCode="
                    + code
                    + ";ev.initEvent('"
                    + (keyDown ? "keydown" : "keyup")
                    + "',true, true);document.body.dispatchEvent(ev);})()");
        }
    }

    public void onexit() {
        CcLog.e("onexit()... cmd = " + cmd);
        if (DefData.CMDPAY.equals(cmd) || DefData.CMDPAY_OVERSEA.equals(cmd)) {
            Intent intent = new Intent(payAction);
            intent.putExtra("resultstatus", pb.payStatus);
            intent.putExtra("tradeId", pb.tradeID);
            intent.putExtra("resultmsg", pb.retMsg);
            intent.putExtra("purchWay", pb.purchWay);
            this.sendBroadcast(intent);
            if (payBackListener != null) {
                payBackListener.pBack(pb.payStatus, pb.tradeID, pb.retMsg, pb.purchWay);
            }
        }
        this.finish();
    }

    public boolean netIsConnect() {
        try {
            NetApiForCommon netApi = new NetApiForCommon(SkyContext.getListener());
            Log.i("CRuby_net", "----------- check net -----------");
            if (!netApi.isConnect()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }

    }

    private String versionName;

    public String getVersionName() {
        if (TextUtils.isEmpty(versionName)) {
            try {
                versionName = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
                mIntent.putExtra("version", versionName);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return versionName == null ? "" : versionName;
    }
}