package com.hughicy.torchapp;

import android.content.Context;
import android.content.pm.PackageInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by hughi on 2016/1/30.
 */
public class UpdateManager {
    private Context mContext;
    public String url;
    public String newVername;
    public String changelog;
    private int versionCode;
    private int serverVersionCode;
    private int index;

    UpdateManager(Context context){
        this.mContext = context;
    }

    public int getVersionCode(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            this.versionCode = packageInfo.versionCode;
            return  this.versionCode;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public int getXML(){
        try {
        /* 获取XML */
            String xmlurl = "http://update.bigrats.net/getxml.php?xml=torchapp";
            HttpURLConnection conn = (HttpURLConnection) new URL(xmlurl).openConnection();
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != 200) return 0;
            InputStream in = conn.getInputStream();
        /* 解析XML */
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);
            org.w3c.dom.Element rootElement = doc.getDocumentElement();
            NodeList items = rootElement.getElementsByTagName("latest-version");
            this.serverVersionCode = Integer.parseInt(items.item(0).getFirstChild().getNodeValue());
            items = rootElement.getElementsByTagName("code");
            this.index = -1;
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                if (Integer.parseInt(item.getFirstChild().getNodeValue()) == this.serverVersionCode) {
                    this.index = i;
                    break;
                }
            }
            if (this.index == -1) return 0;
            items = rootElement.getElementsByTagName("url");
            this.url = items.item(this.index).getFirstChild().getNodeValue();
            items = rootElement.getElementsByTagName("name");
            this.newVername = items.item(this.index).getFirstChild().getNodeValue();
            items = rootElement.getElementsByTagName("changelog");
            this.changelog = items.item(this.index).getFirstChild().getNodeValue();
            return 1;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public boolean checkUpdate(){
        if(getXML() == 1){
            if(getVersionCode(mContext) != 0){
                if(this.serverVersionCode > this.versionCode)
                    return true;
            }
        }
        return false;
    }

}
