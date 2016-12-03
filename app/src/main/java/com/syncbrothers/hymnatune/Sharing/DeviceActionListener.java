package com.syncbrothers.hymnatune.Sharing;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Akshay on 26-03-2016.
 */
public interface DeviceActionListener {
    void cancelDisconnect() ;
    void showDetails(WifiP2pDevice device) ;
    void connect(WifiP2pConfig config);
    void disconnect();
}
