package org.apache.cordova;

import com.skyworth.theme.widget.IThemeable;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

public class CordovaMainLayout extends FrameLayout implements IThemeable{

    public interface OnThemeChangedListener
    {
    	public void OnThemeChanged();
    }
    
    private OnThemeChangedListener listener = null;
    
	public CordovaMainLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setListener(OnThemeChangedListener l) {
		listener = l;
	}

	@Override
	public void refreshOnThemeChanged() {
		// TODO Auto-generated method stub
		Log.i("WebViewSDK","CordovaMainLayout--->refreshOnThemeChanged");
		if(listener != null)
			listener.OnThemeChanged();
	}

}
