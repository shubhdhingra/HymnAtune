package com.syncbrothers.hymnatune.Wifi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.syncbrothers.hymnatune.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class Home_Screen extends Activity implements View.OnClickListener  {

    TextView host;
    TextView join;
    TextView solo;
    private String networkSSID = "Hymn Attune";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        host=(TextView) findViewById(R.id.host_button);
        join=(TextView) findViewById(R.id.join_button);
        solo=(TextView) findViewById(R.id.solo_button);
        }

    public void onClick(View v) {
        if(v.getId()==R.id.join_button)
        {
            join.setBackgroundResource(R.drawable.rect_rounded_postclick);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    join.setBackgroundResource(R.drawable.rect_rounded_preclick);
                     Intent intent = new Intent(getApplicationContext(),Join.class);
                    startActivity(intent);
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable,50);
        }
        else if(v.getId()==R.id.host_button)
        {
            host.setBackgroundResource(R.drawable.rect_rounded_postclick);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText ssidText = new EditText(getApplicationContext());
            alert.setMessage("Enter Device Name");
            alert.setTitle("HotSpot Generator");
            ssidText.setGravity(Gravity.CENTER);
            alert.setView(ssidText);
            alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    networkSSID=ssidText.getText().toString();
                    CreateWifiAccessPoint createOne = new CreateWifiAccessPoint();
                    createOne.execute((Void) null);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            host.setBackgroundResource(R.drawable.rect_rounded_preclick);
                        }
                    };
                    Handler handler = new Handler();
                    handler.postDelayed(runnable,50);
                }
            });
            alert.setCancelable(true);
            alert.show();
        }
        else
        {
            solo.setBackgroundResource(R.drawable.rect_rounded_postclick);

            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

          //  ActivityCompat.requestPermissions(this, permissions, isGranted);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(),PlaySolo.class);
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
        @Override
        protected Boolean doInBackground(Void... params) {
            WifiManager wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
            boolean methodFound = false;
            for (Method method : wmMethods) {
                if (method.getName()!=null) {
                    if (method.getName().equals("setWifiApEnabled")) {
                        methodFound = true;
                        WifiConfiguration netConfig = new WifiConfiguration();
                        netConfig.SSID = networkSSID;
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
                                if (isWifiApEnabledMethod.getName()!=null && isWifiApEnabledMethod.getName().equals("isWifiApEnabled")) {
                                    while (!(Boolean) isWifiApEnabledMethod.invoke(wifiManager)) {
                                    }
                                    for (Method method1 : wmMethods) {
                                        if (method1.getName()!=null &&method1.getName().equals("getWifiApState")) {
                                        }
                                    }
                                }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (apStatus) {
                                        System.out.println("SUCCESS ");
                                        Toast.makeText(getApplicationContext(), "Wifi Hotspot Created", Toast.LENGTH_SHORT).show();
                                    } else {
                                        System.out.println("FAILED");
                                        Toast.makeText(getApplicationContext(), "Wifi Hotspot Creation Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return methodFound;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(Home_Screen.this, Songs.class);
                    startActivity(i);
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable,1000);
         }
    }

}