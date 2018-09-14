package coocaa.plugin.api.pay.ccapi.paydata;

public class DefData
{
    public static String CMDLOGIN = "login";
    public static String CMDLOGIN_OVERSEA = "login_oversea";
    public static String CMDPAY = "pay";
    public static String CMDPAY_OVERSEA = "pay_oversea";
    public static String CMDPAY_NOLOGIN = "pay_nologin";
    public static String AesPasswd = "9005e4d84149a2c0";
    public static String CFGFILEPATH = "/skydir/lgcfg.xml";
    public static String CONFNAME = "CURRENT_PAY_SERVER";
    public static String udisk_root_dir    = "/skydir/config/general_config.xml";
    public static String PAYURL            = "/MyCoocaa/v2/pay/pay.action";
    public static String PAYURL_OVERSEA   ="/MyCoocaa/overseas/pay.action";
    public static String LOGINURL = "/MyCoocaa/v5/paycenter/protocols/index.action";
    public static String LOGINURL_OVERSEA ="/MyCoocaa/overseas/to_billing_agreement.action";
    public static String GETBALANCEURL     = "http://pay.coocaatv.com:8080/TransactionWebService/userinfoservice/getsecurebalance";
}
