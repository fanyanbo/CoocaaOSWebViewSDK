package org.apache.cordova.plugin.api.xforothersdk.framework.utils;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LiteURI {
    public static LiteURI create(String uri) {
        return new LiteURI(uri);
    }

    private String mUri = null;
    private Map<String, String> params = new HashMap<String, String>();

    private LiteURI(String uri) {
        mUri = uri;
    }

    public LiteURI addParams(String key, String value) {
        params.put(key, value);
        return this;
    }

    public final Uri toUri() {
        Uri _uri = Uri.parse(mUri);
        Uri.Builder builder = _uri.buildUpon();

        Set<String> keys = params.keySet();
        for (String key : keys)
            builder = builder.appendQueryParameter(key, params.get(key));
        return builder.build();
//        String _mUri = mUri + "?";
//        Set<String> keys = params.keySet();
//        int i = 0;
//        for (String key : keys)
//        {
//            if (i++ > 0)
//                _mUri += "&";
//            _mUri += String.format("%s=%s", key, params.get(key));
//        }
//        return Uri.parse(_mUri);
    }

    @Override
    public String toString() {
        return toUri().toString();
    }
}
