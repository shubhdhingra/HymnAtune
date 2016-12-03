package com.syncbrothers.hymnatune;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.syncbrothers.hymnatune.Wifi.Home_Screen;


public class Home_Logo extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_logo);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(),Select_Screen.class);
                startActivity(intent);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable,2000);
    }
}
