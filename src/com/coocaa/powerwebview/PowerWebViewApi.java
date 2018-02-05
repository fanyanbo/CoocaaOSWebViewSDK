package com.coocaa.powerwebview;

import android.content.Context;
import android.content.Intent;

public class PowerWebViewApi {

	private Context mContext;
	
	private static final String ACTION_POWERWEBVIEW = "com.coocaa.powerwebview.start";
	private static final String EXTRA_URL_KEY = "extraurlkey";
	
	public PowerWebViewApi(Context context)
	{
		mContext = context;
	}
	
	public boolean startPowerWebView(String url)
	{
		Intent mIntent = new Intent(ACTION_POWERWEBVIEW);
		mIntent.setPackage(mContext.getPackageName());
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mIntent.putExtra(EXTRA_URL_KEY, url);
		
		if(mContext.getPackageManager().resolveActivity(mIntent, 0)!=null)
		{
			mContext.startActivity(mIntent);
			return true;
		}
		return false;
	}
	
	public String getWebViewUrl(Intent mIntent)
	{
		if(mIntent!=null)
		{
			return mIntent.getStringExtra(EXTRA_URL_KEY);
		}
		return null;
	}
}
