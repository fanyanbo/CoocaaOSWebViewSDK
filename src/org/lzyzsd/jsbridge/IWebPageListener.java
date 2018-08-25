package org.lzyzsd.jsbridge;

/**
 * Created by fanyanbo on 2018/8/22.
 * Email: fanyanbo@skyworth.com
 */
public interface IWebPageListener {

    void onPageStarted(String url);
    void onPageLoadingFinished(String url);
    void onReceivedError(int errCode, String desc, String url);
    void onProgressChanged(int newValue);
}
