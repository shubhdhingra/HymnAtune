package com.syncbrothers.hymnatune.Wifi;

/**
 * Created by Shubham on 16-11-2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jesusm.holocircleseekbar.lib.HoloCircleSeekBar;
import com.syncbrothers.hymnatune.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Enumeration;

public class PlayScreenClient extends Activity {

    // wifi connection

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


    private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;

    private TextView selectedFile = null;
    private MediaPlayer player = null;
    private ImageButton playButton = null;
    //  private ImageButton prevButton = null;
    // private ImageButton nextButton = null;
    //  private SeekBar seekBar=null;
    private HoloCircleSeekBar picker;
    private TextView music_duration=null;

    private boolean isStarted=true;
    private String currentFile=" ";
    private boolean isMovingSeekBar=false;

    private final Handler handler=new Handler();

    private final Runnable updatePositionRunnable=new Runnable() {
        @Override
        public void run() {
            updatePosition();
        }
    };

    private int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_play);

        selectedFile=(TextView)findViewById(R.id.selectedfile);
        //    seekBar=(SeekBar)findViewById(R.id.seekbar);
        playButton=(ImageButton)findViewById(R.id.play);
        //   prevButton=(ImageButton)findViewById(R.id.prev);
        // nextButton=(ImageButton)findViewById(R.id.next);
        picker = (HoloCircleSeekBar) findViewById(R.id.picker);
        music_duration=(TextView) findViewById(R.id.music_duration);

        player=new MediaPlayer();

        player.setOnCompletionListener(onCompletion);
        player.setOnErrorListener(onError);
        //  seekBar.setOnSeekBarChangeListener(seekBarChanged1);
        picker.setOnSeekBarChangeListener(seekBarChanged);

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


        Cursor cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
        if(null!=cursor)
        {
            cursor.moveToFirst();
            playButton.setOnClickListener(onButtonClick);
            //   prevButton.setOnClickListener(onButtonClick);
            // nextButton.setOnClickListener(onButtonClick);

        }

        if(flag==0) {
            Intent CurrentFileIntent = getIntent();
            currentFile = CurrentFileIntent.getExtras().getString("currentFile");

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    startplay(currentFile);
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable, 2400);
//        startplay(currentFile);
        flag=1;
        }
        else
        {
            PlayScreenClient.receiverMessage recvMsgThread = new PlayScreenClient.receiverMessage(recQue);
            recvMsgThread.execute((Void) null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(updatePositionRunnable);
        player.stop();
        player.reset();
        player.release();

        player=null;

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

    private void check(final String song) {
        //   text2=(EditText)findViewById(R.id.textView2);
        //       al=new ArrayList<String>();
        //     alname=new ArrayList<String>();

        if (song!=null && song.equals("0")) {

            handler.removeCallbacks(updatePositionRunnable);
            player.pause();
            playButton.setImageResource(R.drawable.play);
        }
        else {
        /*    String fullpath = "";
            String[] STAR = {"*"};
            Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            //    Intent intent;
            Cursor cursor = getContentResolver().query(allsongsuri, STAR, selection, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        //  Toast.makeText(getApplicationContext(), "song name" + song_name, Toast.LENGTH_SHORT).show();
                        fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        //           al.add(fullpath);
                        String filename = fullpath.substring(fullpath.lastIndexOf("/") + 1);
                        if (filename.equals(song)) {
                            Toast.makeText(getApplicationContext(), " song found", Toast.LENGTH_SHORT).show();

                            break;
                        }
                    } while (cursor.moveToNext());

//                    Runnable runnable = new Runnable() {
                    //                      @Override
                    //                    public void run() {
                    startplay(currentFile);
                    //                  }
                    //            };
                    //          Handler handler = new Handler();
                    //        handler.postDelayed(runnable,2398);}
                    cursor.close();
          */
            handler.removeCallbacks(updatePositionRunnable);
            player.pause();

            playButton.setImageResource(R.drawable.pause_button);
            //updatePosition();

                }
            }



    private void updateListView(final String message) {

        values = new String[recQue.size()];
        for (int x = 0; x < recQue.size(); x++) {
            values[x] = recQue.get(x);
        }
        //runOnUiThread(new Runnable() {
          //  @Override
            //public void run() {
      //          Toast.makeText(getApplicationContext(),"1:"+ values[1], Toast.LENGTH_SHORT).show();
               Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();

                //  text=(EditText)findViewById(R.id.textView);
                //text.setText(message);
        player.pause();
        check(message);

        //    }
      //  });
        Log.d("cSharing", "Send : " + message);
    }

    private void startplay(String file){

        String filename=file.substring(file.lastIndexOf("/")+1);
        selectedFile.setText(filename);
        //seekBar.setProgress(0);
        picker.setValue(0);
        player.stop();
        player.reset();

        try{
            player.setDataSource(file);
            player.prepare();

            player.start();
        }catch(IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        // seekBar.setMax(player.getDuration());
        picker.setMax(player.getDuration());
        playButton.setImageResource(R.drawable.pause_button);
        updatePosition();
        isStarted=true;
    }

    private void stopPlay()
    {
        player.stop();
        player.reset();
        playButton.setImageResource(R.drawable.play);
        handler.removeCallbacks(updatePositionRunnable);
        //seekBar.setProgress(0);
        picker.setValue(0);
        long durationInMs=player.getCurrentPosition();
        int seconds = (int) (durationInMs / 1000) % 60 ;
        int minutes = (int) ((durationInMs / (1000*60)) % 60);
        music_duration.setText(minutes+":"+seconds);
        isStarted=false;
    }

    private void updatePosition(){
        handler.removeCallbacks(updatePositionRunnable);
        //seekBar.setProgress(player.getCurrentPosition());
        picker.setValue(player.getCurrentPosition());
        long durationInMs=player.getCurrentPosition();
        int seconds = (int) (durationInMs / 1000) % 60 ;
        int minutes = (int) ((durationInMs / (1000*60)) % 60);
        //double durationInMin = ((double) durationInMs / 1000.0) / 60.0;

//        durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(2, BigDecimal.ROUND_UP).doubleValue();
        if(seconds<=9)
            music_duration.setText("0"+minutes+":0"+seconds);
        else
            music_duration.setText("0"+minutes+":"+seconds);


        handler.postDelayed(updatePositionRunnable,UPDATE_FREQUENCY);
    }
    private View.OnClickListener onButtonClick=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.play: {
                    if(player.isPlaying()){
                        handler.removeCallbacks(updatePositionRunnable);
                        player.pause();
                        playButton.setImageResource(R.drawable.play);
                    }
                    else
                    {
                        if(isStarted){
                            player.start();
                            playButton.setImageResource(R.drawable.pause_button);
                            updatePosition();
                        }
                        else
                        {
                            startplay(currentFile);
                        }
                    }
                    break;
                }
/*                case R.id.next: {
                    int seekto=player.getCurrentPosition()+STEP_VALUE;
                    if(seekto>player.getDuration())

                        seekto=player.getDuration();
                    player.pause();
                    player.seekTo(seekto);
                    player.start();

                    break;
                }
                case R.id.prev: {
                    int seekto=player.getCurrentPosition()-STEP_VALUE;

                    if(seekto<0)
                        seekto=0;

                    player.pause();
                    player.seekTo(seekto);
                    player.start();
                    break;
                }*/
            }
        }
    };

    private MediaPlayer.OnCompletionListener onCompletion=new MediaPlayer.OnCompletionListener(){

        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlay();
        }
    };

    private MediaPlayer.OnErrorListener onError=new MediaPlayer.OnErrorListener(){
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };
    /*

        private SeekBar.OnSeekBarChangeListener seekBarChanged1=new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar=false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar=true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isMovingSeekBar){
                    player.seekTo(progress);
                }
            }
        };
    */
    private HoloCircleSeekBar.OnCircleSeekBarChangeListener seekBarChanged=new HoloCircleSeekBar.OnCircleSeekBarChangeListener(){
        @Override
        public void onStopTrackingTouch(HoloCircleSeekBar seekBar) {
            isMovingSeekBar=false;
        }

        @Override
        public void onStartTrackingTouch(HoloCircleSeekBar seekBar) {
            isMovingSeekBar=true;
        }

        @Override
        public void onProgressChanged(HoloCircleSeekBar seekBar, int progress, boolean fromUser) {
            if(isMovingSeekBar){
                player.seekTo(progress);
            }
        }
    };
}
