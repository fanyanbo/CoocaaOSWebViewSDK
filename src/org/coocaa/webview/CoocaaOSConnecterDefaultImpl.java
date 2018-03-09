package org.coocaa.webview;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemProperties;
import android.util.Log;

import com.coocaa.cordova.plugin.CoocaaUserInfoParser;
import com.skyworth.framework.skysdk.ipc.SkyApplication;
import com.tianci.net.api.NetApiForCommon;
import com.tianci.net.data.SkyIpInfo;
import com.tianci.system.api.TCSystemService;
import com.tianci.system.data.TCInfoSetData;
import com.tianci.system.data.TCSetData;
import com.tianci.system.define.SkyConfigDefs;
import com.tianci.system.define.TCEnvKey;
import com.tianci.user.api.SkyUserApi;

public class CoocaaOSConnecterDefaultImpl implements CoocaaOSConnecter{
    private TCSystemService systemApi;
    private NetApiForCommon netApi;
    private SkyUserApi userApi;

    /**
     * @param listener 酷开系统ipc通信核心对象
     */
    public  CoocaaOSConnecterDefaultImpl(SkyApplication.SkyCmdConnectorListener listener) {
    	Log.i("WebViewSDK","CoocaaOSConnecterDefaultImpl listener = " + listener);
    	if(listener != null) {
            systemApi = new TCSystemService(listener);
            netApi = new NetApiForCommon(listener);
            userApi = new SkyUserApi(listener);
    	}
    }
	@Override
	public String hasCoocaaUserLogin() {
		// TODO Auto-generated method stub
		if (userApi != null) {
			boolean isLogin = userApi.hasLogin();
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("haslogin", String.valueOf(isLogin));
				return jsonObject.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} 
		return null;
	}

	@Override
	public String getUserInfo() {
		// TODO Auto-generated method stub
		if (userApi != null) {
			Map<String, Object> userInfo = userApi.getAccoutInfo();
			JSONObject jsonObject = CoocaaUserInfoParser
					.parseUserInfo(userInfo);
			return jsonObject.toString();
		}
		return null;
	}

	@Override
	public String getDeviceInfo() {
		// TODO Auto-generated method stub
		Log.i("WebViewSDK","CoocaaOSConnecterDefaultImpl getDeviceInfo = " + systemApi);
		if (systemApi != null) {
			// 屏幕尺寸
			TCSetData pannelSetData = systemApi
					.getSetData(TCEnvKey.SKY_SYSTEM_ENV_PANEL_SIZE);
			String pannelString = "";
			if (pannelSetData != null) {

				TCInfoSetData pannelInfoData = (TCInfoSetData) pannelSetData;
				pannelString = pannelInfoData.getCurrent();
			}

			// 酷开版本号
			TCSetData verSetData = systemApi
					.getSetData(TCEnvKey.SKY_SYSTEM_ENV_TIANCI_VER);
			String verString = "";
			if (verSetData != null) {
				TCInfoSetData verInfoData = (TCInfoSetData) verSetData;
				verString = verInfoData.getCurrent();
			}

			// 机芯
			TCSetData modelSetData = systemApi
					.getSetData(TCEnvKey.SKY_SYSTEM_ENV_MODEL);
			String modelString = "";
			if (modelSetData != null) {
				TCInfoSetData modelInfoData = (TCInfoSetData) modelSetData;
				modelString = modelInfoData.getCurrent();
			}

			// 机型
			TCSetData typeSetData = systemApi
					.getSetData(TCEnvKey.SKY_SYSTEM_ENV_TYPE);
			String typeString = "";
			if (typeSetData != null) {
				TCInfoSetData typeInfoData = (TCInfoSetData) typeSetData;
				typeString = typeInfoData.getCurrent();
			}

			// MAC
			TCSetData macSetData = systemApi
					.getSetData(TCEnvKey.SKY_SYSTEM_ENV_MAC);
			String macString = "";
			if (macSetData != null) {
				TCInfoSetData macInfoData = (TCInfoSetData) macSetData;
				macString = macInfoData.getCurrent();
			}

			// chip id
			TCSetData chipSetData = systemApi
					.getSetData(TCEnvKey.SKY_SYSTEM_ENV_CHIPID);
			String chipString = "";
			if (chipSetData != null) {
				TCInfoSetData chipInfoData = (TCInfoSetData) chipSetData;
				chipString = chipInfoData.getCurrent();
			}

			// 设备id
			TCSetData devidSetData = systemApi
					.getSetData(TCEnvKey.SKY_SYSTEM_ENV_MACHINE_CODE);
			TCInfoSetData devidInfoData = null;
			if (devidSetData != null) {
				devidInfoData = (TCInfoSetData) devidSetData;
			}
			String devidString = null;
			if (devidInfoData != null) {
				devidString = devidInfoData.getCurrent();
			}

			// 激活id
			TCSetData activeidSetData = systemApi
					.getSetData(TCEnvKey.SKY_SYSTEM_ENV_ACTIVE_ID);
			TCInfoSetData activeidInfoData = null;
			if (activeidSetData != null) {
				activeidInfoData = (TCInfoSetData) activeidSetData;
			}
			String activeidString = null;
			if (activeidInfoData != null) {
				activeidString = activeidInfoData.getCurrent();
			}

			TCSetData emmcidSetData = systemApi
					.getSetData(SkyConfigDefs.SKY_CFG_EMMC_CID);
			TCInfoSetData emmcidInfoData = null;
			if (emmcidSetData != null) {
				emmcidInfoData = (TCInfoSetData) emmcidSetData;
			}
			String emmcidString = "";
			if (emmcidInfoData != null) {
				emmcidString = emmcidInfoData.getCurrent();
			}

			if (verString != null && verString.length() > 0
					&& typeString != null && typeString.length() > 0) {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("panel", pannelString);
					jsonObject.put("version", verString);
					jsonObject.put("model", typeString);
					jsonObject.put("chipid", chipString);
					jsonObject.put("mac", macString);
					jsonObject.put("chip", modelString);
					jsonObject.put("androidsdk",
							android.os.Build.VERSION.SDK_INT);
					jsonObject.put("devid", devidString);
					jsonObject.put("activeid", activeidString);
					jsonObject.put("emmcid", emmcidString);
					jsonObject.put("brand",
							SystemProperties.get("ro.product.brand"));
					return jsonObject.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public String isNetConnected() {
		// TODO Auto-generated method stub
        if(netApi != null)
        {
            boolean isConnect = netApi.isConnect();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("isnetworking", String.valueOf(isConnect));
                return jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
		return null;
	}

	@Override
	public String getNetType() {
		// TODO Auto-generated method stub
		if (netApi != null) {
			String netType = netApi.getNetType();
			if (netType != null) {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("nettype", netType);
					return jsonObject.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public String getIpInfo() {
		// TODO Auto-generated method stub
		if (netApi != null) {
			SkyIpInfo ipInfo = netApi.getIpInfo();
			if (ipInfo != null) {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("dns0", ipInfo.dns0);
					jsonObject.put("dns1", ipInfo.dns1);
					jsonObject.put("gateway", ipInfo.gateway);
					jsonObject.put("ip", ipInfo.ip);
					jsonObject.put("mac", ipInfo.mac);
					jsonObject.put("netmask", ipInfo.netmask);
					return jsonObject.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public String getDeviceLocation() {
		// TODO Auto-generated method stub
		if (systemApi != null) {
			TCSetData locationData = systemApi
					.getSetData(TCEnvKey.SKY_SYSTEM_ENV_LOCATION);
			if (locationData != null) {
				TCInfoSetData locInfoData = (TCInfoSetData) locationData;
				String locationString = null;
				if (locInfoData != null) {
					locationString = locInfoData.getCurrent();
					try {
						JSONObject jsonObj = new JSONObject();
						jsonObj.put("location", locationString);
						return jsonObj.toString();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getUserAccessToken() {
		// TODO Auto-generated method stub
		if (userApi != null) {
			String token = userApi.getToken("ACCESS");
			if (token != null) {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("accesstoken", token);
					return jsonObject.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
