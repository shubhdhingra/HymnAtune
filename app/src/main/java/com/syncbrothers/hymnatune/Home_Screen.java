package com.syncbrothers.hymnatune;

import android.Manifest;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.syncbrothers.hymnatune.R.id.username;

public class Home_Screen extends Activity implements View.OnClickListener  {

    TextView host;
    TextView join;
    TextView solo;
    TextView receive;
    Button send;
    EditText username;
    private String networkSSID = "Hymn Attune";
    private String networkPass = "pass";
    private int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        host=(TextView) findViewById(R.id.host_button);
        join=(TextView) findViewById(R.id.join_button);
        solo=(TextView) findViewById(R.id.solo_button);
        send=(Button) findViewById(R.id.button);
        }
    public void onClick(View v) {
        if(v.getId()==R.id.join_button)
        {
  /*          join.setBackgroundResource(R.drawable.rect_rounded_postclick);
            //addedSoonToast(host);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                    solo.setBackgroundResource(R.drawable.rect_rounded_preclick);
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable,50);
            join.setBackgroundResource(R.drawable.rect_rounded_preclick);*/
            Toast.makeText(getApplicationContext(),"Joining",Toast.LENGTH_LONG).show();

            JoinWifiNetwork joinOne = new JoinWifiNetwork();
            joinOne.execute((Void) null);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Press Start",Toast.LENGTH_LONG).show();
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable,50);
            flag=1;
        }
        else if(v.getId()==R.id.host_button)
        {
           /* host.setBackgroundResource(R.drawable.rect_rounded_postclick);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                    solo.setBackgroundResource(R.drawable.rect_rounded_preclick);
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable,50);
            host.setBackgroundResource(R.drawable.rect_rounded_preclick); */

            CreateWifiAccessPoint createOne = new CreateWifiAccessPoint();
            createOne.execute((Void) null);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Press Start",Toast.LENGTH_LONG).show();
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable,50);
            flag=0;
        }
        else if(v.getId()==R.id.button)
        {
            username=(EditText)findViewById(R.id.username);
       if(flag==0){
           Runnable runnable = new Runnable() {
               @Override
               public void run() {
                   Intent i = new Intent(Home_Screen.this, Songs.class);
                   username=(EditText)findViewById(R.id.username);
                   i.putExtra("username",username.getText().toString());
                   startActivity(i);

               }
           };
           Handler handler = new Handler();
           handler.postDelayed(runnable,50);

       }
            else if(flag==1)
       {
           Runnable runnable = new Runnable() {
               @Override
               public void run() {
                   Intent i = new Intent(Home_Screen.this,Receiver.class);
                   username=(EditText)findViewById(R.id.username);
                   i.putExtra("username",username.getText().toString());
                   startActivity(i);
               }
           };
           Handler handler = new Handler();
           handler.postDelayed(runnable,50);
       }

        }
        else
        {
            solo.setBackgroundResource(R.drawable.rect_rounded_postclick);

            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

          //  ActivityCompat.requestPermissions(this, permissions, isGranted);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(),Play_Solo.class);
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

    private class CreateWifiAccessPoint extends AsyncTask<Void, Void, Boolean> {
        {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            WifiManager wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
            boolean methodFound = false;
            for (Method method : wmMethods) {
                if (method.getName().equals("setWifiApEnabled")) {
                    methodFound = true;
                    WifiConfiguration netConfig = new WifiConfiguration();
                    netConfig.SSID=networkSSID;
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    try {
                        final boolean apStatus = (Boolean) method.invoke(wifiManager, netConfig, true);
                        for (Method isWifiApEnabledMethod : wmMethods)
                            if (isWifiApEnabledMethod.getName().equals("isWifiApEnabled")) {
                                while (!(Boolean) isWifiApEnabledMethod.invoke(wifiManager)) {
                                }
                                for (Method method1 : wmMethods) {
                                    if (method1.getName().equals("getWifiApState")) {
                                    }
                                }
                            }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (apStatus) {
                                    System.out.println("SUCCESS ");
                                    Toast.makeText(getApplicationContext(),"Wifi Hotspot Created",Toast.LENGTH_SHORT).show();
                                } else {
                                    System.out.println("FAILED");
                                    Toast.makeText(getApplicationContext(),"Wifi Hotspot Creation Failed",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return methodFound;

        }
    }

    private class JoinWifiNetwork extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            WifiManager wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                wifiManager.startScan();
            }

            int netId = wifiManager.addNetwork(conf);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Joined to "+networkSSID,Toast.LENGTH_SHORT).show();
                    System.out.println("SUCCESS ");
                }
            });

            return null;
        }

    }
}