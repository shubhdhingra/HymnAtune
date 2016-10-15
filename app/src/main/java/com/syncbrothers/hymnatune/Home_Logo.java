package com.syncbrothers.hymnatune;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;


public class Home_Logo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_logo);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(),Home_Screen.class);
                startActivity(intent);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable,2000);
    }
}
