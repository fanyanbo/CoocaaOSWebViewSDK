package org.lzyzsd.jsbridge;


public interface WebViewJavascriptBridge {
	
	public void send(String data);
	public void send(String data, ICallBackFunction responseCallback);

}
