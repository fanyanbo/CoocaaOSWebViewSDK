package coocaa.plugin.api;
import com.skyworth.framework.skysdk.ipc.ICmdBaseProceess;
import com.skyworth.framework.skysdk.ipc.SkyApplication;
import com.skyworth.framework.skysdk.ipc.SkyCmdURI;

import java.net.URISyntaxException;

/**
 * Created by tianjisheng on 2018/7/4.
 */

public class PushApi
{
    public PushApi()
    {

    }

    public String getPushIdByPackageName(ICmdBaseProceess proceess, SkyApplication.SkyCmdConnectorListener listener, String packageName)
    {
        if (proceess == null || listener == null || packageName == null || packageName.length() == 0)
        {
            return "";
        }
        SkyCmdURI cmdURI = getPushUri("GET_PUSH_ID_FROM_WEB");
        if (cmdURI == null)
        {
            return "";
        }
        byte[] result = null;
        try
        {
            result = proceess.execCommand(listener,cmdURI, packageName.getBytes());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        if (result == null || result.length == 0)
        {
            return "";
        }
        String pushId;
        try
        {
            pushId = new String(result);
            return pushId;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private SkyCmdURI getPushUri(String cmd)
    {
        SkyCmdURI uri = null;
        try
        {
            uri = new SkyCmdURI("tianci://com.tianci.push/com.tianci.push.PushService?cmd=" + cmd);
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        } catch (SkyCmdURI.SkyCmdPathErrorException e)
        {
            e.printStackTrace();
        }
        return uri;
    }
}
