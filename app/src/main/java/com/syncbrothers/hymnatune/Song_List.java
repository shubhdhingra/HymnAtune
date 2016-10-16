package com.syncbrothers.hymnatune;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TabHost;


public class Song_List extends TabActivity implements TabHost.OnTabChangeListener {

    TabHost TabHostWindow;

    /*
        Channel mChannel;
        WifiP2pManager mManager;
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song);
        /*Intent intent = new Intent(this, Songs.class);
        startActivity(intent);
      */  //Assign id to Tabhost.


        TabHostWindow = (TabHost) findViewById(android.R.id.tabhost);

        TabHostWindow.setOnTabChangedListener(this);

        //Creating tab menu.
        TabHost.TabSpec TabMenu1 = TabHostWindow.newTabSpec("First tab");
        TabHost.TabSpec TabMenu2 = TabHostWindow.newTabSpec("Second Tab");

        //Setting up tab 1 name.
        TabMenu1.setIndicator("Songs");
        //Set tab 1 activity to tab 1 menu.
        TabMenu1.setContent(new Intent(this, Songs.class));

        //Setting up tab 2 name.
        TabMenu2.setIndicator("Album");
        //Set tab 3 activity to tab 1 menu.
        TabMenu2.setContent(new Intent(this, Album.class));

        //Adding tab1, tab2 to tabhost view.

        TabHostWindow.addTab(TabMenu1);
        TabHostWindow.addTab(TabMenu2);

        for (int i = 0; i < TabHostWindow.getTabWidget().getChildCount(); i++) {
            TabHostWindow.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#6c5f83"));
        }
        TabHostWindow.getTabWidget().setCurrentTab(1);
        TabHostWindow.getTabWidget().getChildAt(1).setBackgroundColor(Color.parseColor("#53a8b9"));

    }

    @Override
    public void onTabChanged(String tabId) {
        // TODO Auto-generated method stub
        for (int i = 0; i < TabHostWindow.getTabWidget().getChildCount(); i++) {
            TabHostWindow.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#6c5f83"));
        }

        TabHostWindow.getTabWidget().getChildAt(TabHostWindow.getCurrentTab()).setBackgroundColor(Color.parseColor("#53a8b9"));
    }
}


/*
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
*/



/*
public class Connect extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed!  We should probably do something about
            // that.
            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }
            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");



        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed!  We should probably do something about
            // that.
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // We are connected with the other device, request connection
                // info to find group owner IP

                mManager.requestConnectionInfo(mChannel, connectionListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }

    }
};

@Override
public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(receiver, intentFilter);
        }

@Override
public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
        }


        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

@Override
public void onSuccess() {
        // Code for when the discovery initiation is successful goes here.
        // No services have actually been discovered yet, so this method
        // can often be left blank.  Code for peer discovery goes in the
        // onReceive method, detailed below.
        }

@Override
public void onFailure(int reasonCode) {
        // Code for when the discovery initiation fails goes here.
        // Alert the user that something went wrong.
        }
        });


private PeerListListener peerListListener = new PeerListListener() {
@Override
public void onPeersAvailable(WifiP2pDeviceList peerList) {

        // Out with the old, in with the new.
        peers.clear();
        peers.addAll(peerList.getDeviceList());

        // If an AdapterView is backed by this data, notify it
        // of the change.  For instance, if you have a ListView of available
        // peers, trigger an update.
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
        Log.d(WiFiDirectActivity.TAG, "No devices found");
        return;
        }
        }
        }

@Override
public void connect() {
        // Picking the first device found on the network.
        WifiP2pDevice device = peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new ActionListener() {

@Override
public void onSuccess() {
        // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
        }

@Override
public void onFailure(int reason) {
        Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
        Toast.LENGTH_SHORT).show();
        }
        });
        }


@Override
public void onConnectionInfoAvailable(final WifiP2pInfo info) {

        // InetAddress from WifiP2pInfo struct.
        InetAddress groupOwnerAddress = info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
        // Do whatever tasks are specific to the group owner.
        // One common case is creating a server thread and accepting
        // incoming connections.
        } else if (info.groupFormed) {
        // The other device acts as the client. In this case,
        // you'll want to create a client thread that connects to the group
        // owner.
        }
        }

        */