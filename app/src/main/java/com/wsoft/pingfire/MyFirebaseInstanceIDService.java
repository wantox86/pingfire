package com.wsoft.pingfire;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import java.net.URL;
import java.util.Hashtable;
import com.wsoft.pingfire.lib.HTTPUrlConn;
import static com.wsoft.pingfire.utils.CommonConst.*;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by lia on 14/11/17.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
{
    private static final String TAG = "MyFirebaseIIDService";
    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token)
    {
        String USER_AGENT = "Mozilla/5.0";
        String strUrl = MainActivity.strUrl;

        try
        {
            URL url = new URL(strUrl);

            Hashtable reqMap = new Hashtable();
            reqMap.put("token", token);

            HTTPUrlConn httpUrlConn = new HTTPUrlConn();
            Hashtable hashReply = httpUrlConn.processHttpPost(strUrl, reqMap);
            JSONObject replyJson = (JSONObject) hashReply.get(kHttp_Reply_JSon);
            String pingID = replyJson.getJSONObject("data").getString("ping_id");

        }
        catch (Exception e)
        {

        }
    }
}
