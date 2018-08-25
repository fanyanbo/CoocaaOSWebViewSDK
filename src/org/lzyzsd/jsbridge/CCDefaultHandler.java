package org.lzyzsd.jsbridge;

public class CCDefaultHandler implements IBridgeHandler {

	String TAG = "DefaultHandler";
	
	@Override
	public void handler(String data, ICallBackFunction function) {
		if(function != null){
			function.onCallBack("DefaultHandler response data");
		}
	}

}
