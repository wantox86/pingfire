package com.wsoft.pingfire;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import org.json.JSONObject;

import java.net.URL;
import java.util.Hashtable;

import static com.wsoft.pingfire.utils.CommonConst.kHttp_Reply_JSon;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String strUrl = "http://156.67.218.127:9004/ws/regtoken?";
    public static final String strUrlSendPing = "http://156.67.218.127:9004/ws/sendpush?";
    public static Context appContext;
    public TextView tvPingID;
    public EditText edRecv;
    public EditText edMsgSend;
    public EditText edMsgLog;

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.wsoft.pingfire.onMessageReceived");
        MyBroadcastReceiver receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = getApplicationContext();
        /*
            Create Channel Notification
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

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
        /*
        Custom Config UI
         */
        tvPingID = (TextView) findViewById(R.id.txtVPingID);
        tvPingID.setTextSize(36);
        tvPingID.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        edMsgLog = (EditText) findViewById(R.id.editTextMsg);
        edMsgLog.setEnabled(false);
        edMsgLog.setTextSize(12);

        edRecv = (EditText) findViewById(R.id.editTPingIDDest);
        edMsgSend = (EditText) findViewById(R.id.editTextSend);
        /*
            Pooling for get Ping ID, retry for 3 times
         */
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
            Log.v(TAG, Log.getStackTraceString(e));
        }

    }

    /*
        Handler for button Send Ping
     */
    public void onClickSendPing(View view)
    {

        new SendPing(this,
                tvPingID.getText().toString(),
                edRecv.getText().toString(),
                edMsgSend.getText().toString(),
                edMsgSend,
                edMsgLog).execute();
    }

    /*
        Handler show message from notif to edittextMsg
    */
    private class MyBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();
            String body = extras.getString("fcm.body");
            String titles = extras.getString("fcm.title");
//            Toast.makeText(getApplicationContext(), "Found Broadcast Msg " + titles + " - "+ body, Toast.LENGTH_LONG).show();
            edMsgLog.setText(edMsgLog.getText() + "\n" + titles + " - "+ body);
        }
    }

    /*
        Private Class for retrive Ping ID for this user from server
        calling by pooling onCreate
     */
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
                Log.v(TAG, Log.getStackTraceString(e));
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
                tvPingID.setText(pingID);
                Toast.makeText(getApplicationContext(),textDisplay,Toast.LENGTH_LONG).show();
            }
            catch (Exception e)
            {
                Log.v(TAG, Log.getStackTraceString(e));
            }

        }
    }

    /*
        Private Class for Send Ping to Server
        trigger by Click Button Send
     */
    private class SendPing extends AsyncTask<String, Void, Hashtable>
    {
        private Context context;
        private String pingIDSender;
        private String pingIDReceiver;
        private String pingMsgToSend;
        private EditText etMsgToSend;
        private EditText etMsgLog;
        Hashtable hashReply = new Hashtable();
        public SendPing(Context c, String pingSender, String pingRecv, String pingMsg, EditText etMsg, EditText etLog)
        {
            this.context = c;
            this.pingIDSender = pingSender;
            this.pingIDReceiver = pingRecv;
            this.pingMsgToSend = pingMsg;
            this.etMsgToSend = etMsg;
            this.etMsgLog = etLog;
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
                reqMap.put("msg", pingMsgToSend);

                HTTPUrlConn httpUrlConn = new HTTPUrlConn();
                hashReply = httpUrlConn.processHttpPost(strUrlSendPing, reqMap);
                JSONObject replyJson = (JSONObject) hashReply.get(kHttp_Reply_JSon);
            }
            catch (Exception e)
            {
                Log.v(TAG, Log.getStackTraceString(e));
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
                    etMsgLog.setText(etMsgLog.getText() + "\n" + etMsgToSend.getText());
                    etMsgToSend.setText("");
                    Toast.makeText(getApplicationContext(),"Success send ping to " + pingIDReceiver,Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Failed send ping to " + pingIDReceiver,Toast.LENGTH_LONG).show();
                }

            }
            catch (Exception e)
            {
                Log.v(TAG, Log.getStackTraceString(e));
            }
        }
    }
}
