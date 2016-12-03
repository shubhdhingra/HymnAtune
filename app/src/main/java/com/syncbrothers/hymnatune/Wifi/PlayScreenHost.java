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
import android.widget.ImageButton;
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

public class PlayScreenHost extends Activity {

    //wifi connection

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
    private int flag=0;

    private final Handler handler=new Handler();

    private final Runnable updatePositionRunnable=new Runnable() {
        @Override
        public void run() {
            updatePosition();
        }
    };

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

                fileSocket = new MulticastSocket(portNum+1);
                fileSocket.setInterface(ip);
                fileSocket.setBroadcast(true);

                fileGroup = InetAddress.getByName("224.0.0.2");
                fileSocket.joinGroup(new InetSocketAddress(fileGroup, (portNum+1)), networkInterface);
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

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent CurrentFileIntent = getIntent();
                currentFile = CurrentFileIntent.getExtras().getString("currentFile");
                startplay(currentFile);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 3000);
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
                    if (player.isPlaying()) {
                        handler.removeCallbacks(updatePositionRunnable);
                        Toast.makeText(getApplicationContext(), "sending", Toast.LENGTH_LONG).show();
                        PlayScreenHost.sendMessage sendMessage = new PlayScreenHost.sendMessage("0");
                        sendMessage.execute((Void) null);

                        // Runnable runnable = new Runnable() {
                        //@Override
                        //  public void run() {
                        player.pause();
                        playButton.setImageResource(R.drawable.play);
                        //    }
                        //   };
                        //Handler handler = new Handler();
                        //handler.postDelayed(runnable, 3000);
                    } else {
                        if (isStarted) {

                            Toast.makeText(getApplicationContext(), currentFile, Toast.LENGTH_LONG).show();
                         //   String filename=currentFile.substring(currentFile.lastIndexOf("/")+1);

                            PlayScreenHost.sendMessage sendMessage = new PlayScreenHost.sendMessage(String.valueOf(player.getCurrentPosition()));
                            sendMessage.execute((Void) null);
                            player.start();
                            playButton.setImageResource(R.drawable.pause_button);
                            updatePosition();
                        }
                        else {
                            startplay(currentFile);
                        }
                        break;

                    }
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
