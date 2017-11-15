package com.wsoft.pingfire;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class WellcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellcome);
        TextView textView = (TextView) findViewById(R.id.textViewWellcome);
        textView.setTextSize(30);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    public void onClickStart(View view)
    {
        Intent myIntent = new Intent(WellcomeActivity.this, MainActivity.class);
        WellcomeActivity.this.startActivity(myIntent);
    }
}
