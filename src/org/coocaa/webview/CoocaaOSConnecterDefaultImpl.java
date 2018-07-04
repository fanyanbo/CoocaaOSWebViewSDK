package org.coocaa.webview;

import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;

import com.coocaa.cordova.plugin.CoocaaOSApi;
import com.coocaa.cordova.plugin.CoocaaUserInfoParser;
import com.coocaa.push.api.PushApi;
import com.skyworth.framework.skysdk.ipc.SkyApplication;
import com.skyworth.framework.skysdk.ipc.SkyCmdURI;
import com.skyworth.framework.skysdk.util.SkyJSONUtil;
import com.skyworth.framework.skysdk.util.SkyObjectByteSerialzie;
import com.tianci.media.api.SkyMediaApi;
import com.tianci.media.api.SkyMediaApiParam;
import com.tianci.media.base.SkyMediaItem;
import com.tianci.net.api.NetApiForCommon;
import com.tianci.net.command.TCNetworkBroadcast;
import com.tianci.net.data.SkyIpInfo;
import com.tianci.net.define.NetworkDefs;
import com.tianci.system.api.TCSystemService;
import com.tianci.system.command.TCSystemDefs;
import com.tianci.system.data.TCInfoSetData;
import com.tianci.system.data.TCSetData;
import com.tianci.system.define.SkyConfigDefs;
import com.tianci.system.define.TCEnvKey;
import com.tianci.user.api.SkyUserApi;
import com.tianci.user.data.UserCmdDefine;

public class CoocaaOSConnecterDefaultImpl implements CoocaaOSConnecter{
    private TCSystemService systemApi;
    private NetApiForCommon netApi;
    private SkyUserApi userApi;
	private PushApi pushApi;
	private SkyMediaApi mediaApi;
	SkyApplication.SkyCmdConnectorListener mListener;
	public static final String TAG = "WebViewSDK";

    /**
     * @param listener 酷开系统ipc通信核心对象
     */
	public CoocaaOSConnecterDefaultImpl(Context context, SkyApplication.SkyCmdConnectorListener listener) {
		Log.i(TAG, "CoocaaOSConnecterDefaultImpl listener = " + listener);
		if (listener != null) {
			mListener = listener;
			systemApi = new TCSystemService(listener);
			netApi = new NetApiForCommon(listener);
			userApi = new SkyUserApi(listener);
			mediaApi = new SkyMediaApi(listener);
			mediaApi.setContext(context);
			pushApi = new PushApi();
		}
	}

	@Override
	public String hasUserLogin() {
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
        Log.i(TAG,"CoocaaOSConnecterDefaultImpl getUserInfo");
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
		Log.i(TAG,"CoocaaOSConnecterDefaultImpl getDeviceInfo");
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
				if(activeidString == null || "".equals(activeidString)){
					activeidString = SystemProperties.get("persist.sys.active_id");
				}
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
					Log.e(TAG,e.getMessage());
				}
			}
		}
		return null;
	}

    @Override
    public String getPushInfo(String pkgList) {
        JSONObject jsonParams = null;
        JSONObject resultObject = new JSONObject();
        try {
            jsonParams = new JSONObject(pkgList);
            JSONArray params = jsonParams.getJSONArray("pkgList");
            if (params.length() > 0) {
                for (int i = 0; i < params.length(); i++) {
                    String pkgName = params.getString(i);
                    Log.i("WebViewSDK", "get pushid pkgName = " + pkgName);
                    JSONObject valueObject = new JSONObject();
                    String pushId = pushApi.getPushIdByPackageName(SkyApplication.getApplication(), mListener, pkgName);
                    if (pushId != null && pushId.length() > 0) {
                        valueObject.put("status", "0");
                        valueObject.put("pushId", pushId);
                    } else {
                        valueObject.put("status", "-1");
                        valueObject.put("pushId", "");
                    }
                    resultObject.put(pkgName, valueObject);
                }
                return resultObject.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
	public String isNetConnected() {

        Log.i(TAG,"CoocaaOSConnecterDefaultImpl isNetConnected");
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

        Log.i(TAG,"CoocaaOSConnecterDefaultImpl getNetType");
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

        Log.i(TAG,"CoocaaOSConnecterDefaultImpl getIpInfo");
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

		Log.i(TAG, "CoocaaOSConnecterDefaultImpl getDeviceLocation");
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
	public String getLoginUserInfo() {

		Log.i(TAG, "CoocaaOSConnecterDefaultImpl getLoginUserInfo");
		if (userApi != null) {
			Map<String, Object> userInfo = userApi.getAccoutInfo();
			if (userInfo != null && userInfo.size() > 0) {
				String jsonString = SkyJSONUtil.getInstance().compile(userInfo);
				return jsonString;
			}
		}
		return null;
	}

	@Override
	public String getUserAccessToken() {

		Log.i(TAG, "CoocaaOSConnecterDefaultImpl getUserAccessToken");
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

	@Override
	public void setUserLogout() {
		logoutSync();
	}

	/**
	 * 内部方法，退出账户登录，同步操作
	 *
	 * @return
	 */
	private boolean logoutSync() {
		byte[] resultBody = execCmd(UserCmdDefine.ACCOUNT_LOGOUT, null);
		return getBooleanFromBytes(resultBody);
	}

	private boolean getBooleanFromBytes(byte[] byteResult) {
		if (byteResult != null && byteResult.length > 0) {
			Boolean result = SkyObjectByteSerialzie.toObject(byteResult, Boolean.class);
			return result == null ? false : result;
		}
		return false;
	}

	private byte[] execCmd(String cmd, byte[] body) {
		if (mListener == null)
			return null;

		SkyCmdURI uri = getUserUri(cmd);
		if (uri == null) {
			Log.e(TAG, "execCmd(), uri is null");
			return null;
		} else {
			return SkyApplication.getApplication().execCommand(mListener, uri, body);
		}
	}

	private SkyCmdURI getUserUri(String cmd) {
		SkyCmdURI uri = null;
		try {
			uri = new SkyCmdURI("tianci://com.tianci.user/com.tianci.user.UserService?cmd=" + cmd);
		} catch (URISyntaxException e) {
			Log.e(TAG, "URISyntaxException = " + e.getMessage());
		} catch (SkyCmdURI.SkyCmdPathErrorException e) {
			Log.e(TAG, "SkyCmdPathErrorException = " + e.getMessage());
		}
		return uri;
	}

	@Override
	public void startQQAcount() {

        Log.i(TAG,"CoocaaOSConnecterDefaultImpl startQQAcount");
        if(userApi != null) {
            userApi.loginByType(SkyUserApi.AccountType.qq);
        }
		return;
	}

	@Override
	public void startOnlinePlayer(String url, String name, String needParse, String urlType) {
		SkyMediaItem[] items = new SkyMediaItem[1];
		SkyMediaItem item = new SkyMediaItem();
		item.type = SkyMediaItem.SkyMediaType.MOVIE;

		if ("true".equals(needParse) || "false".equals(needParse)) {
			item.setNeedParse(Boolean.valueOf(needParse));
		} else {
			item.setNeedParse(false);
		}
		item.url = url;
		item.name = name;
		item.extra.put("url_type", urlType);
		items[0] = item;
		SkyMediaApiParam param = new SkyMediaApiParam();
		param.setPlayList(items, 0);
		mediaApi.startOnlinePlayer(param);
	}

	@Override
	public byte[] onHandler(Context context, String fromtarget, String cmd, byte[] body) {

        Log.i(TAG,"CoocaaOSConnecterDefaultImpl onHandler fromtarget = " + fromtarget + ",cmd = " + cmd);
		if(context == null) return new byte[0];
		if (TCSystemDefs.TCSystemBroadcast.TC_SYSTEM_BROADCAST_MEDIA_MOUNTED //外接设备接入
				.toString().equals(cmd)) {
			String path = SkyObjectByteSerialzie.toObject(body, String.class);
			CoocaaOSApi.broadCastUsbChangged(context, true, path == null ? "" : path);
		} else if (TCSystemDefs.TCSystemBroadcast.TC_SYSTEM_BROADCAST_MEDIA_REMOVED //外接设备拔出
				.toString().equals(cmd)) {
			String path = SkyObjectByteSerialzie.toObject(body, String.class);
			CoocaaOSApi.broadCastUsbChangged(context, false, path == null ? "" : path);
		} else if (TCNetworkBroadcast.TC_NETWORK_BROADCAST_NET_ETH_EVENT
				.toString().equals(cmd)) {
			NetworkDefs.EthEvent ethEvnet = SkyObjectByteSerialzie.toObject(body, NetworkDefs.EthEvent.class);
			if (ethEvnet != null) {
				JSONObject mJson = CoocaaOSApi.getEthEventString("ethnet", ethEvnet);
				if (mJson != null) {
					CoocaaOSApi.broadCastNetChangged(context,mJson);
				}
			}
		} else if (TCNetworkBroadcast.TC_NETWORK_BROADCAST_NET_WIFI_EVENT
				.toString().equals(cmd)) {
			NetworkDefs.WifiEvent wifiEvent = SkyObjectByteSerialzie.toObject(body, NetworkDefs.WifiEvent.class);
			switch (wifiEvent) {
				case EVENT_WIFI_CONNECT_SUCCEEDED:
				case EVENT_WIFI_CONNECT_DISCONNECTED:
					JSONObject mJson = CoocaaOSApi.getWifiEventString("wifi", wifiEvent);
					if (mJson != null) {
						CoocaaOSApi.broadCastNetChangged(context,mJson);
					}
					break;
				default:
					break;
			}
		} else if (UserCmdDefine.ACCOUNT_CHANGED.toString().equals(cmd)) {
			CoocaaOSApi.broadCastUesrChangged(context);
		}

		return new byte[0];
	}
}