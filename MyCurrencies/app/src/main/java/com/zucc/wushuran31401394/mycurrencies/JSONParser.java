package com.zucc.wushuran31401394.mycurrencies;

/**
 * Created by wushu on 2017/7/2.
 */
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
public class JSONParser {
    static InputStream sInputStream = null;
    static JSONObject sReturnJsonObject = null;
    static String sRawJsonString = "";
    public JSONParser() {}
    public JSONObject getJSONFromUrl(String url) {
        //尝试从服务器获取响应
        Log.d("JSONParser",url);
        try {
            //http客户端
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            //获取http的内容
            sInputStream = httpEntity.getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //在数据流里读取内容
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    sInputStream, "iso-8859-1"), 8);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                //一行一行读取变成一个整体
                stringBuilder.append(line + "\n");
            }
            sInputStream.close();
            //辅助
            sRawJsonString = stringBuilder.toString();
            Log.d("result",sRawJsonString);
        } catch (Exception e) {
            Log.e("Error reading from: " + e.toString(), this.getClass().getSimpleName());
        }
        try {
            //解析
            sReturnJsonObject = new JSONObject(sRawJsonString);
            Log.d("JSON",sReturnJsonObject.getString("disclaimer"));
        } catch (JSONException e) {
            Log.e("Parser", "Error when parsing data " + e.toString());
        }
        //return json object
        return sReturnJsonObject;
    }
}
