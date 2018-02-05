package com.coocaa.sky.api.wblogin;

import android.util.Xml;

import com.coocaa.ccapi.paydata.DefData;
import com.coocaa.ccapi.paydata.UserInfo;
import com.coocaa.sky.api.utils.SimpleCrypto;
import com.tianci.media.api.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CfgFile
{
    private static String TAG = "cfgfile";
    public String barcode = null;
    private File cfgFile = null;
    public String mac = null;
    public String status = null;
    public String tel = null;
    public String userlever = null;

    public CfgFile(String paramString)
    {
        setCfgFile(new File(paramString));
    }

    private String decode(String paramString)
    {
        try
        {
            String str = SimpleCrypto.decrypt(DefData.AesPasswd, paramString);
            return str;
        }
        catch (Exception localException)
        {
            localException.printStackTrace();
        }
        return null;
    }

    private String encode(String paramString)
    {
        try
        {
            String str = SimpleCrypto.encrypt(DefData.AesPasswd, paramString);
            return str;
        }
        catch (Exception localException)
        {
            localException.printStackTrace();
        }
        return null;
    }

    public void createFile(int paramInt, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5)
    {
        try
        {
            this.cfgFile.createNewFile();
            FileOutputStream localFileOutputStream = new FileOutputStream(this.cfgFile);
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            XmlSerializer localXmlSerializer = Xml.newSerializer();
            localXmlSerializer.setOutput(localByteArrayOutputStream, "utf-8");
            localXmlSerializer.startDocument("utf-8", Boolean.valueOf(true));
            localXmlSerializer.startTag(null, "request");
            localXmlSerializer.attribute(null, "xmlns", "http://www.coocaa.com");
            localXmlSerializer.text("\n");
            localXmlSerializer.startTag(null, "Regist");
            localXmlSerializer.text(Integer.toString(paramInt));
            localXmlSerializer.endTag(null, "Regist");
            localXmlSerializer.startTag(null, "Mac");
            localXmlSerializer.text(paramString1);
            localXmlSerializer.endTag(null, "Mac");
            localXmlSerializer.startTag(null, "BarCode");
            localXmlSerializer.text(paramString2);
            localXmlSerializer.endTag(null, "BarCode");
            localXmlSerializer.startTag(null, "Tel");
            localXmlSerializer.text(paramString3);
            localXmlSerializer.endTag(null, "Tel");
            localXmlSerializer.startTag(null, "Userlever");
            localXmlSerializer.text(paramString4);
            localXmlSerializer.endTag(null, "Userlever");
            localXmlSerializer.startTag(null, "Token");
            localXmlSerializer.text(paramString5);
            localXmlSerializer.endTag(null, "Token");
            localXmlSerializer.endTag(null, "request");
            localXmlSerializer.endDocument();
            localByteArrayOutputStream.flush();
            localFileOutputStream.write(encode(localByteArrayOutputStream.toString()).getBytes());
            localFileOutputStream.close();
        }
        catch (Exception localException1)
        {

        }
        try
        {
            Runtime localRuntime = Runtime.getRuntime();
            localRuntime.exec("chmod 666 " + DefData.CFGFILEPATH);
        }
        catch (Exception localException2)
        {
            localException2.printStackTrace();
        }
    }

    public File getCfgFile()
    {
        return this.cfgFile;
    }

    public boolean isexists()
    {
        return getCfgFile().exists();
    }


    public void readFile(UserInfo paramUserInfo) throws  Exception
    {
        byte abyte0[] = new byte[1024];
        FileInputStream fileinputstream = new FileInputStream(cfgFile);
        int j = fileinputstream.read(abyte0);
        XmlPullParser parser = Xml.newPullParser(); //由android.util.Xml创建一个XmlPullParser实例
        String encodeString = decode((new String(abyte0)).substring(0, j));
        Log.i("ccapi","encodeString is >"+encodeString);
        parser.setInput(new ByteArrayInputStream(encodeString.getBytes()), "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("Regist")) {
                        eventType = parser.next();
                        paramUserInfo.loginstatus = Integer.parseInt(parser.getText());
                    }else if (parser.getName().equals("Mac")) {
                        eventType = parser.next();
                        paramUserInfo.mac = parser.getText();
                    } else if (parser.getName().equals("BarCode")) {
                        eventType = parser.next();
                        paramUserInfo.barcode = parser.getText();
                    } else if (parser.getName().equals("Tel")) {
                        eventType = parser.next();
                        paramUserInfo.tel = parser.getText();
                    } else if (parser.getName().equals("Userlever")) {
                        eventType = parser.next();
                        paramUserInfo.userlever = parser.getText();
                    } else if (parser.getName().equals("Token")) {
                        eventType = parser.next();
                        paramUserInfo.token = parser.getText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = parser.next();
        }
    }

    public void saveDataIntoFile(UserInfo paramUserInfo)
    {
        try
        {
            FileOutputStream localFileOutputStream = new FileOutputStream(this.cfgFile);
            localFileOutputStream.write("".getBytes());
            localFileOutputStream.close();
            createFile(paramUserInfo.loginstatus, paramUserInfo.mac, paramUserInfo.barcode, paramUserInfo.tel, paramUserInfo.userlever,paramUserInfo.token);
            return;
        }
        catch (FileNotFoundException localFileNotFoundException)
        {
            createFile(1, "", "", "", "","");
            localFileNotFoundException.printStackTrace();
            return;
        }
        catch (IOException localIOException)
        {
            localIOException.printStackTrace();
        }
    }

    public void setCfgFile(File paramFile)
    {
        this.cfgFile = paramFile;
    }
}