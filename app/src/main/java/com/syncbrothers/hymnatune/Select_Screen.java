package com.syncbrothers.hymnatune;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.syncbrothers.hymnatune.Sharing.home;
import com.syncbrothers.hymnatune.Wifi.Home_Screen;

/**
 * Created by Shubham on 01-12-2016.
 */

public class Select_Screen extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_screen);


    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.share:
            {
                Intent intent=new Intent(Select_Screen.this,home.class);
                startActivity(intent);
                break;
            }
            case R.id.play:
            {
                Intent intent=new Intent(Select_Screen.this, Home_Screen.class);
                startActivity(intent);
                break;
            }
        }
    }
}
