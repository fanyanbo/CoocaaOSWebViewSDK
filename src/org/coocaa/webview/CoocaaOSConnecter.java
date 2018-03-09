package org.coocaa.webview;

public interface CoocaaOSConnecter {
	/**
     * 当前用户是否登录
     *
     * @return 如果无或错误则返回null
     */
    String hasCoocaaUserLogin();

	/**
     * 获取用户信息
     *
     * @return 如果无或错误则返回null
     */
    String getUserInfo();

	/**
     * 获取当前设备信息
     *
     * @return 如果无或错误则返回null
     */
    String getDeviceInfo();

	/**
     * 获取当前网路连接状态
     *
     * @return 如果无或错误则返回null
     */
    String isNetConnected();

	/**
     * 获取当前网络类型（有线、无线）
     *
     * @return 如果无或错误则返回null
     */
    String getNetType();

	/**
     * 获取当前网络的ip信息
     *
     * @return 如果无或错误则返回null
     */
    String getIpInfo();

    /**
     * 获取当前设备的城市地址
     *
     * @return
     */
    String getDeviceLocation();

    /**
     * 获取用户的登录token
     *
     * @return
     */
    String getUserAccessToken();
}