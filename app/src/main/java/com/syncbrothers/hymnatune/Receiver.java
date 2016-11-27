package com.syncbrothers.hymnatune;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by Shubham on 27-11-2016.
 */

public class Receiver extends Activity {
    final int portNum = 3238;
    InetAddress ip = null;
    NetworkInterface networkInterface = null;
    ServerSocket serverSocket;
    String curFileName;
    EditText edittext;
    private ArrayList<String> recQue;
    private String[] values;
    private ListView listView;
    private ArrayAdapter adapter;
    private MulticastSocket socket;
    private InetAddress group;
    private MulticastSocket fileSocket;
    private InetAddress fileGroup;
    private String username;
    private EditText text;
    private EditText text2;

    ArrayList<String> al;
    ArrayList<String> alname;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver);

        username = (String) getIntent().getExtras().get("username");
        // TextView userNm = (TextView) findViewById(R.id.usrName);
        // userNm.setText(username);

        // listView = (ListView) findViewById(R.id.listView);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiManager.MulticastLock lock =
                    wifi.createMulticastLock("cSharing");
            lock.setReferenceCounted(true);
            lock.acquire();
        } else {
            Log.e("cSharing", "Unable to acquire multicast lock");
            Toast.makeText(getApplicationContext(), "Unable to acquire multicast lock", Toast.LENGTH_SHORT).show();

            finish();
        }
        recQue = new ArrayList<>();

        try {
            if (socket == null) {


                Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (enumNetworkInterfaces.hasMoreElements()) {

                    networkInterface = enumNetworkInterfaces.nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();

                        if (inetAddress.isSiteLocalAddress()) {
                            ip = inetAddress;
                            break;
                        }
                    }
                    if (ip != null) {
                        break;
                    }
                }
                socket = new MulticastSocket(portNum);
                socket.setInterface(ip);
                socket.setBroadcast(true);

                group = InetAddress.getByName("224.0.0.1");
                socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);

                fileSocket = new MulticastSocket(portNum + 1);
                fileSocket.setInterface(ip);
                fileSocket.setBroadcast(true);

                fileGroup = InetAddress.getByName("224.0.0.2");
                fileSocket.joinGroup(new InetSocketAddress(fileGroup, (portNum + 1)), networkInterface);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        receiverMessage recvMsgThread = new receiverMessage(recQue);
        recvMsgThread.execute((Void) null);
    }
    @Override
    protected void onDestroy () {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            socket = new MulticastSocket(portNum);
            socket.setInterface(ip);
            socket.setBroadcast(true);

            group = InetAddress.getByName("224.0.0.1");
            socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private class receiverMessage extends AsyncTask<Void, Void, Boolean> {
        ArrayList<String> msgList;

        receiverMessage(ArrayList<String> msgList) {
            recQue = msgList;
            this.msgList = msgList;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Thread newThread = new Thread() {

                public void run() {
                    while (true) {
                        byte[] recvPkt = new byte[1024];
                        DatagramPacket recv = new DatagramPacket(recvPkt, recvPkt.length);
                        try {
                            socket.receive(recv);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        final String medd = new String(recvPkt, 0, recv.getLength());
                        recQue.add(medd);
                        updateListView(medd);
                    }
                }
            };
            newThread.start();
            return null;
        }
    }
    private void check(final String song){
        text2=(EditText)findViewById(R.id.textView2);
        al=new ArrayList<String>();
        alname=new ArrayList<String>();
        String fullpath="";
        String[] STAR = { "*" };
        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Intent intent;
        Cursor cursor = getContentResolver().query(allsongsuri, STAR, selection, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    Toast.makeText(getApplicationContext(), "song name"+ song_name, Toast.LENGTH_SHORT).show();
                    fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    //           al.add(fullpath);
                    String filename=fullpath.substring(fullpath.lastIndexOf("/")+1);
                    if(filename.equals(song))
                    {
                        Toast.makeText(getApplicationContext()," song found" , Toast.LENGTH_SHORT).show();
                              /*   */
                        break;
                    }
                } while (cursor.moveToNext());
                //
                text2.setText("flag changed");
                intent=new Intent(Receiver.this,PlayScreen.class);
                intent.putExtra("currentFile",fullpath);
                startActivity(intent);
            }
            cursor.close();
        }
    }

    private void updateListView(final String message) {

        values = new String[recQue.size()];
        for (int x = 0; x < recQue.size(); x++) {
            values[x] = recQue.get(x);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                text=(EditText)findViewById(R.id.textView);
                text.setText(message);
                check(message);
            }
        });
        Log.d("cSharing", "Send : " + message);
    }
}