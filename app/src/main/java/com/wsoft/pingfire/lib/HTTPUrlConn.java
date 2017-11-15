package com.wsoft.pingfire.lib;

import android.annotation.TargetApi;
import android.os.Build;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.wsoft.pingfire.utils.CommonConst.*;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by lia on 14/11/17.
 */

public class HTTPUrlConn
{
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public Hashtable processHttpPost(String strUrl, Hashtable reqMap, String strlogging) throws Exception
    {
        Hashtable ret = new Hashtable();

        URL url = new URL(strUrl);
        StringBuilder sbParam = new StringBuilder();
        Set<String> keys = reqMap.keySet();
        boolean first = true;
        if (reqMap.size() > 0)
        {
            for (String strKey : keys)
            {
                if(first)
                    first = false;
                else
                    sbParam.append("&");

                sbParam.append(URLEncoder.encode(strKey, "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(reqMap.get(strKey).toString(), "UTF-8"));
            }
        }
        String urlParameters = sbParam.toString();

        strlogging += " urlParameters : " + urlParameters + " ";
        byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
        int    postDataLength = postData.length;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(15000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setInstanceFollowRedirects( false );

        conn.setRequestMethod("POST");
//                conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
        conn.setFixedLengthStreamingMode(urlParameters.getBytes().length);
//                conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches( false );



        Hashtable reply = new Hashtable();

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(urlParameters);

        writer.flush();
        writer.close();
        os.close();


        int responseCode=conn.getResponseCode();

        ret.put(kHttp_Response_HTTP, responseCode);
        if (responseCode == HttpsURLConnection.HTTP_OK) {

            BufferedReader in=new BufferedReader(new
                    InputStreamReader(
                    conn.getInputStream()));

            StringBuffer sb = new StringBuffer("");
            String line="";

            while((line = in.readLine()) != null) {

                sb.append(line);
                break;
            }

            in.close();
            strlogging +=  sb.toString();
            ret.put(kHttp_Reply_String, sb.toString());
            JSONObject respJson = new JSONObject(sb.toString());
            ret.put(kHttp_Reply_JSon,respJson);
            String respCode = respJson.getJSONObject("trx").getString("response.code");
            String respDesc = respJson.getJSONObject("trx").getString("response.description");
            ret.put(kHttp_Resp_Code, respCode);
            ret.put(kHttp_Resp_Desc, respDesc);

        }
        else
        {
//            return new String("false : "+responseCode);
        }
        return ret;
    }

    public Hashtable processHttpPost(String url, Hashtable reqMap) throws Exception
    {
        String USER_AGENT = "Mozilla/5.0";
        Hashtable ret = new Hashtable();
        ret.put("url", url);
        ret.put("paramtable", reqMap);

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        StringBuilder sbParam = new StringBuilder();
        Set<String> keys = reqMap.keySet();
        if (reqMap.size() > 0)
        {
            for (String strKey : keys)
            {
                sbParam.append(strKey).append("=").append(reqMap.get(strKey)).append("&");
            }
        }
        String urlParameters = sbParam.toString();
        ret.put("parameter", urlParameters);

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null)
        {
            response.append(inputLine);
        }
        in.close();
        ret.put(kHttp_Reply_String, response.toString());
        JSONObject respJson = new JSONObject(response.toString());
        ret.put(kHttp_Reply_JSon,respJson);
        ret.put("responsecode", responseCode);
        ret.put("response", response.toString());
        return ret;
    }

    public String getStackTraceAsString(Throwable thr)
    {
        if (thr != null)
        {
            StringWriter swrt = new StringWriter();
            PrintWriter pwrt = new PrintWriter(swrt);
            thr.printStackTrace(pwrt);
            return swrt.toString();
        }
        else
        {
            return null;
        }
    }
}
