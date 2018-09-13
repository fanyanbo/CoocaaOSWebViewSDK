package org.apache.cordova.plugin.api.xforothersdk.app.appstore3.controller.data;

import org.apache.cordova.plugin.api.xforothersdk.framework.data.JObject;

/**
 * Created by admin on 2016/3/10.
 */
public class MyAppBaseData extends JObject {
    public String title = "";        //仅用于显示
    public long usedTimes_a = 0;
    public long usedTimes_t = 0;
    public long recentlyUsedTimes = 0;
    public long createTime = 0;
    //新增加的
    public long Top = 0;             //通过这个值的大小判断谁在前面，默认为0
    public String topName = "";      //用于存储top信息
    public int type = -1;            // 0 代表应用   1 代表文件夹   2 代表推荐位
}
