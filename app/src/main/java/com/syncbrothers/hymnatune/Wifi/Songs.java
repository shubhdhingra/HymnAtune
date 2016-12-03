package com.syncbrothers.hymnatune.Wifi;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.syncbrothers.hymnatune.R;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class Songs extends ListActivity{

    private MediaCursorAdapter mediaAdapter = null;
    private String currentFile = "";

    static final int SocketServerPORT = 8080;
    private static final int SHARE_PICTURE = 2;
    private static final int REQUEST_PATH = 1;
    private static final int TIMEOUT = 3000;
    private static final int MAXFILELEN = 65000;

    final int portNum = 3238;
    InetAddress ip = null;
    NetworkInterface networkInterface = null;
    ServerSocket serverSocket;
    private ArrayList<String> recQue;
    private String values;
    private ArrayAdapter adapter;
    private MulticastSocket socket;
    private InetAddress group;
    private MulticastSocket fileSocket;
    private InetAddress fileGroup;
    //private String host_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs);
       // host_username = (String) getIntent().getExtras().get("username");

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

                fileSocket = new MulticastSocket(portNum+1);
                fileSocket.setInterface(ip);
                fileSocket.setBroadcast(true);

                fileGroup = InetAddress.getByName("224.0.0.2");
                fileSocket.joinGroup(new InetSocketAddress(fileGroup, (portNum+1)), networkInterface);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (null != cursor) {
            cursor.moveToFirst();

            mediaAdapter = new Songs.MediaCursorAdapter(this, R.layout.listitem, cursor);
            setListAdapter(mediaAdapter);
        }
    }
    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
/*
        // Animate the background color of clicked Item
        ColorDrawable[] color = {
                new ColorDrawable(Color.parseColor("#b6c75c")),
                new ColorDrawable(Color.parseColor("#FF0000"))
        };
        TransitionDrawable trans = new TransitionDrawable(color);
        view.setBackground(trans);
        trans.startTransition(2000); // duration 2 seconds

        // Go back to the default background color of Item
        ColorDrawable[] color2 = {
                new ColorDrawable(Color.parseColor("#FF0000")),
                new ColorDrawable(Color.parseColor("#b6c75c"))
        };
        TransitionDrawable trans2 = new TransitionDrawable(color2);
        view.setBackground(trans2);
        trans2.startTransition(2000); // duration 2 seconds
*/
        currentFile = (String) view.getTag();
        String filename=currentFile.substring(currentFile.lastIndexOf("/")+1);
        String song_title=" ";
        File src = new File(currentFile);
        MusicMetadataSet src_set = null;
        try {
            src_set = new MyID3().read(src);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } // read metadata

        if (src_set == null) // perhaps no metadata
        {
            Log.i("NULL", "NULL");
        }
        else
        {
            try{
                IMusicMetadata metadata = src_set.getSimplified();
                //      String artist = metadata.getArtist();
                //    String album = metadata.getAlbum();
                song_title = metadata.getSongTitle();
                Toast.makeText(getApplicationContext(),"Metadata: "+song_title,Toast.LENGTH_LONG).show();
                //  Number track_number = metadata.getTrackNumber();
                //Log.i("artist", artist);
                //Log.i("album", album);
            }catch (Exception e) {
                e.printStackTrace();
            }
         /*   File dst = new File(currentFile);
           eta.setSongTitle(filename);
            //meta.setAlbum("");
           */
            MusicMetadata meta = new MusicMetadata("name");
            try {
                new MyID3().update(src, src_set, meta);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ID3WriteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }  // write updated metadata
        }
        Log.e("Title",song_title);
        sendMessage sendMessage = new sendMessage(filename);
        sendMessage.execute((Void) null);
        Intent intent = new Intent(Songs.this, PlayScreenHost.class);
        intent.putExtra("currentFile", currentFile);
        startActivity(intent);
    }

    private class MediaCursorAdapter extends SimpleCursorAdapter {

        public MediaCursorAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c,
                    new String[]{MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.TITLE, MediaStore.Audio.AudioColumns.DURATION},
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

    }

    @Override
    protected void onDestroy() {
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

//        fileReciveThread = new FileReciveThread();
        //      fileReciveThread.start();

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
    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {

                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    private class sendMessage extends AsyncTask<Void, Void, Boolean> {

        String textMsg;

        sendMessage(String message) {

            textMsg =message;
            Toast.makeText(getApplicationContext(),"Send: "+ textMsg,Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            byte[] data = textMsg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, group, portNum);

            try {
                socket.send(packet);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}