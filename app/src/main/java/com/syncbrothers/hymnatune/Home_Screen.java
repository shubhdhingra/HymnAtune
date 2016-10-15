package com.syncbrothers.hymnatune;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Home_Screen extends AppCompatActivity {

    TextView host;
    TextView join;
    TextView solo;
    Integer isGranted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        host=(TextView) findViewById(R.id.host);
        join=(TextView) findViewById(R.id.join);
        solo=(TextView) findViewById(R.id.solo);

    }
    void addedSoonToast(final View v)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"To Be Added Soon",Toast.LENGTH_SHORT).show();
                v.setBackgroundResource(R.drawable.rect_rounded_preclick);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable,50);

    }
    public void onClick(View v) {
        if(v.getId()==R.id.host)
        {
            host.setBackgroundResource(R.drawable.rect_rounded_postclick);
            addedSoonToast(host);
        }
        else if(v.getId()==R.id.join)
        {
            join.setBackgroundResource(R.drawable.rect_rounded_postclick);
            addedSoonToast(join);
        }
        else
        {
            solo.setBackgroundResource(R.drawable.rect_rounded_postclick);

            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

          //  ActivityCompat.requestPermissions(this, permissions, isGranted);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(),Song_List.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    solo.setBackgroundResource(R.drawable.rect_rounded_preclick);
                }
            };
            Handler handler = new Handler();
    //        if(isGranted && permissions[0]==PackageManager.PERMISSION_GRANTED)
      //      {
                handler.postDelayed(runnable,50);
        //    }
        }
    }


}
