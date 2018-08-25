package org.lzyzsd.jsbridge;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by fanyanbo on 2018/8/22.
 * Email: fanyanbo@skyworth.com
 */
public class BridgeWebViewChromeClient extends WebChromeClient {

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if(listener != null) {
            listener.onProgressChanged(newProgress);
        }
    }

    private IWebPageListener listener = null;
    public void setWebPageListener(IWebPageListener listener) {
        this.listener = listener;
    }
}
