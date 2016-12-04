package com.syncbrothers.hymnatune.Wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.syncbrothers.hymnatune.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Join extends ListActivity {
    private ArrayAdapter<String> adapter;
    WifiManager mainWifi;
    List<ScanResult> wifiList;
    private String networkSSID;
    private String wifiPassword;
    ScanResult result;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join);
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mainWifi.isWifiEnabled() == false) {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();
            mainWifi.setWifiEnabled(true);
        }

//        receiverWifi = new WifiReceiver();
//        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        updateWifiList();
    }
    public void updateWifiList()
    {
        adapter=null;
        wifiList=mainWifi.getScanResults();
        String [] wifiName = new String[wifiList.size()];

        for(int i=0;i<wifiList.size();i++)
        {
            wifiName[i]=wifiList.get(i).SSID;
        }
        adapter=new ArrayAdapter<>(this,R.layout.list_item_wifi,wifiName);
        setListAdapter(adapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        networkSSID=(String)l.getItemAtPosition(position);
        final int pos=position;

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText password = new EditText(getApplicationContext());
        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        alert.setMessage("Enter Password");
        alert.setTitle(networkSSID);
        password.setGravity(Gravity.LEFT);
        alert.setView(password);
        wifiPassword="";
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                result=wifiList.get(pos);
                wifiPassword=password.getText().toString();

                JoinWifiNetwork joinWifiNetwork = new JoinWifiNetwork();
                joinWifiNetwork.execute((Void) null);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.show();
    }
/*
    private class MediaCursorAdapter extends SimpleCursorAdapter {

        public MediaCursorAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c,
                    new String[]{wifiList.toString()},
                    new int[]{R.id.displayname, R.id.title, R.id.duration});
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflator = LayoutInflater.from(context);

            View v = inflator.inflate(R.layout.listitem, parent, false);
            bindView(v, context, cursor);
            return v;
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView name = (TextView) view.findViewById(R.id.displayname);
            TextView duration = (TextView) view.findViewById(R.id.duration);

            name.setText(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));
            title.setText(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));
            //updated again
            long durationInMs=0;
            if(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION))!=null) {
                durationInMs = Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
            }

            double durationInMin = ((double) durationInMs / 1000.0) / 60.0;

            durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(2, BigDecimal.ROUND_UP).doubleValue();

            duration.setText("" + durationInMin);

            view.setTag(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
        }

    }*/
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        mainWifi.startScan();
        Toast.makeText(getApplicationContext(),"Starting Scan...",Toast.LENGTH_SHORT).show();

        updateWifiList();
        return super.onMenuItemSelected(featureId, item);
    }

    private class JoinWifiNetwork extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", networkSSID);
            wifiConfig.preSharedKey = String.format("\"%s\"", wifiPassword);
            WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
            int netId = wifiManager.addNetwork(wifiConfig);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Joined to "+networkSSID,Toast.LENGTH_SHORT).show();
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(getApplicationContext(), Receiver.class);
                    startActivity(i);
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable,5000);
        }
    }

}
//
//    protected void onPause() {
//     //   unregisterReceiver(receiverWifi);
//        super.onPause();
//    }
//
//    protected void onResume() {
//     //   registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        super.onResume();
//    }
//    class WifiReceiver extends BroadcastReceiver {
//
//        public void onReceive(Context c, Intent intent) {
//
//            sb = new StringBuilder();
//            sb.append("\n        Number Of Wifi connections :" + wifiList.size() + "\n\n");
//
//            for (int i = 0; i < wifiList.size(); i++) {
//
//                sb.append(new Integer(i + 1).toString() + ". ");
//                sb.append((wifiList.get(i)).toString());
//                sb.append("\n\n");
//            }
//
//        }
//
//    }
//
