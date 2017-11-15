package com.wsoft.pingfire;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.wsoft.pingfire.lib.HTTPUrlConn;
import com.wsoft.pingfire.utils.CommonHelper;

import static com.wsoft.pingfire.utils.CommonConst.*;

import org.json.JSONObject;

import java.net.URL;
import java.util.Hashtable;

import static com.wsoft.pingfire.utils.CommonConst.kHttp_Reply_JSon;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String strUrl = "http://156.67.218.127:9004/ws/regtoken?";
    public static final String strUrlSendPing = "http://156.67.218.127:9004/ws/sendpush?";
    public static Context appContext;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "fcm_default_channel";
            String channelName = "News";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        textView = (TextView) findViewById(R.id.txtVPingID);
        textView.setTextSize(36);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        int retry = 0;
        try
        {
            while (true)
            {
                if(retry > 2)
                {
                    Toast.makeText(getApplicationContext(), "Failed Connect to Server", Toast.LENGTH_LONG).show();
                    break;
                }
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                if(refreshedToken == null || refreshedToken.equalsIgnoreCase(""))
                {
                    retry++;
                    Thread.sleep(5000);
                }
                else
                {
                    new RetrivePingID(this,refreshedToken).execute();
                    break;
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), CommonHelper.getStackTraceAsString(e), Toast.LENGTH_LONG).show();
        }

    }


    public void onClickRegisterID(View view)
    {
        EditText edRecv = (EditText) findViewById(R.id.editText2);
        new SendPing(this, textView.getText().toString(), edRecv.getText().toString()).execute();
    }

    private class RetrivePingID extends AsyncTask<String, Void, Hashtable>
    {
        private Context context;
        private String refreshedToken;
        Hashtable hashReply = new Hashtable();
        public RetrivePingID(Context c, String token)
        {
            this.context = c;
            this.refreshedToken = token;
        }
        String respServer = new String();

        @Override
        protected Hashtable doInBackground(String... strings)
        {

            String USER_AGENT = "Mozilla/5.0";

            try
            {

                URL url = new URL(strUrl);

                Hashtable reqMap = new Hashtable();
                reqMap.put("token", refreshedToken);

                HTTPUrlConn httpUrlConn = new HTTPUrlConn();
                hashReply = httpUrlConn.processHttpPost(strUrl, reqMap);
                JSONObject replyJson = (JSONObject) hashReply.get(kHttp_Reply_JSon);
            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), CommonHelper.getStackTraceAsString(e),Toast.LENGTH_LONG).show();
            }

            return hashReply;
        }
        @Override
        protected void onPostExecute(Hashtable s)
        {
            try
            {
                String textDisplay = "Success Connect to Server";
                JSONObject replyJson = (JSONObject) hashReply.get(kHttp_Reply_JSon);
                String pingID = replyJson.getJSONObject("data").getString("ping_id");
                textView.setText(pingID);
                Toast.makeText(getApplicationContext(),textDisplay,Toast.LENGTH_LONG).show();
            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), CommonHelper.getStackTraceAsString(e),Toast.LENGTH_LONG).show();
            }

        }
    }


    private class SendPing extends AsyncTask<String, Void, Hashtable>
    {
        private Context context;
        private String pingIDSender;
        private String pingIDReceiver;
        Hashtable hashReply = new Hashtable();
        public SendPing(Context c, String pingSender, String pingRecv)
        {
            this.context = c;
            this.pingIDSender = pingSender;
            this.pingIDReceiver = pingRecv;
        }
        String respServer = new String();

        @Override
        protected Hashtable doInBackground(String... strings)
        {

            String USER_AGENT = "Mozilla/5.0";

            try
            {

                URL url = new URL(strUrlSendPing);

                Hashtable reqMap = new Hashtable();
                reqMap.put("ping_id", pingIDReceiver);
                reqMap.put("sender_id", pingIDSender);

                HTTPUrlConn httpUrlConn = new HTTPUrlConn();
                hashReply = httpUrlConn.processHttpPost(strUrlSendPing, reqMap);
                JSONObject replyJson = (JSONObject) hashReply.get(kHttp_Reply_JSon);
            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), CommonHelper.getStackTraceAsString(e),Toast.LENGTH_LONG).show();
            }

            return hashReply;
        }
        @Override
        protected void onPostExecute(Hashtable s)
        {
            try
            {
                JSONObject replyJson = (JSONObject) hashReply.get(kHttp_Reply_JSon);
                String resp = replyJson.getJSONObject("resp").getString("response.code");
                if(resp.equalsIgnoreCase("0"))
                {
                    Toast.makeText(getApplicationContext(),"Success send ping to " + pingIDReceiver,Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Failed send ping to " + pingIDReceiver,Toast.LENGTH_LONG).show();
                }

            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), CommonHelper.getStackTraceAsString(e),Toast.LENGTH_LONG).show();
            }

        }
    }
}
