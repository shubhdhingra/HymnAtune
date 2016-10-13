package com.example.syncbrothers.hymnattune;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by Shubham on 10-10-2016.
 */

public class myPlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {
    private MediaPlayer player = new MediaPlayer();
    private String currentFile;

    //set up the notification id
    private static final int NOTIFICATION_ID = 1;

    private boolean isPausedInCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;


    //Variables for seekbar processing

    String sentSeekPos;
    int intSeekPos;
    int mediaPosition;
    int mediaMax;   // total length of the song
    private final Handler handler = new Handler();    // handler is used when u have to do multi threading operations
    private static int songEnded;
    public static final String BROADCAST_ACTION = "com.example.shubham.hymnattune.seekprogress";

    //Set Up broadcast identifier and Intent
    public static final String BROADCAST_BUFFER = "com.example.shubham.hymnattune.broadcastbuffer";
    Intent bufferIntent;
    Intent seekIntent;

    //Declare headsetSwitch variable
    private int headsetSwitch = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v(TAG, "Creating Service");
//Instantiate bufferIntent to communicate with Activity for progress dialogue
        bufferIntent = new Intent(BROADCAST_BUFFER);

        //set up intent for seekbar broadcast
       // seekIntent = new Intent(BROADCAST_ACTION);

     //   player.setOnSeekCompleteListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnPreparedListener(this);
        player.setOnBufferingUpdateListener(this);
      //  player.setOnSeekCompleteListener(this);
        player.setOnInfoListener(this);
        player.reset();

        //Register headset receiver
        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        //set up receiver for seekbar change
        registerReceiver(broadcastReceiver, new IntentFilter(SecondActivity.BROADCAST_SEEKBAR));


        //Manage incoming  phone calls during playback . pause media player on incomge.
        //resume on hang up.

        //Get the telephony manager

        Log.v(TAG, "Starting Telephony");
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Log.v(TAG, "Starting Listener");
        phoneStateListener = new PhoneStateListener() {

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {

                Log.v(TAG, "Starting CallStateChange");
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (player != null) {
                            pauseMedia();
                            isPausedInCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (player != null) {
                            if (isPausedInCall) {
                                isPausedInCall = false;
                                playMedia();
                            }
                        }
                        break;
                }
            }
        };

        //Register the listener in the telephony manager

        telephonyManager.listen(phoneStateListener, phoneStateListener.LISTEN_CALL_STATE);

        //Insert notification start
//        initNotification();

        currentFile = intent.getExtras().getString("currentFile");
        player.stop();
        player.reset();
        if (!player.isPlaying()) {
            try {
                player.setDataSource(currentFile);

                //Send message to Activity  to display progress dialogue
                sendBufferingBroadCast();

                //prepare media player
                player.prepareAsync();

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setupHandler();

        return START_STICKY;
    }

    //Send seekbar info to activity
    private void setupHandler() {
        handler.removeCallbacks(sendUpdatestoUI);
        handler.postDelayed(sendUpdatestoUI, 1000);  //1sec
    }

    private Runnable sendUpdatestoUI = new Runnable() {
        @Override
        public void run() {
            LogMediaPosition();
            handler.postDelayed(this, 1000); //2seconds
        }
    };

    private void LogMediaPosition() {
        if (player.isPlaying()) {
            mediaPosition = player.getCurrentPosition();
            mediaMax = player.getDuration();
            seekIntent.putExtra("counter", String.valueOf(mediaPosition));
            seekIntent.putExtra("mediaMax", String.valueOf(mediaMax));
            seekIntent.putExtra("song_ended", String.valueOf(songEnded));
            sendBroadcast(seekIntent);
        }
    }

    //Receive seekbar position if it has been changes by the user in the activity
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekPos(intent);
        }
    };

    //Update seek position from Activity
    public void updateSeekPos(Intent intent) {
        int seekPos = intent.getIntExtra("seekpos", 0);
        if (player.isPlaying()) {
            handler.removeCallbacks(sendUpdatestoUI);
            player.seekTo(seekPos);
            setupHandler();
        }
    }

//End of seek bar code


    //If headset gets unplugged ,stop music and service
    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        private boolean headsetConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (headsetConnected && intent.getIntExtra("state", 0) == 0) {
                    headsetConnected = false;
                    headsetSwitch = 0;
                } else if (!headsetConnected && intent.getIntExtra("state", 0) == 1) {
                    headsetConnected = true;
                    headsetSwitch = 1;
                }
            }
            switch (headsetSwitch) {
                case (0):
                    headsetDisconnected();
                    break;
                case (1):
                    break;
            }
        }
    };

    private void headsetDisconnected() {
        stopMedia();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
                player.reset();
            }
            player.release();
            player = null;
        }

        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, phoneStateListener.LISTEN_NONE);
        }
        //Cancel the notification
  //      cancelNotification();

        //Unregister seekbar receiver
        unregisterReceiver(broadcastReceiver);

        //Unregister headset receiver
        unregisterReceiver(headsetReceiver);

        //Stop the seekbar handler  from sending updates to UI
        handler.removeCallbacks(sendUpdatestoUI);

        //Service ends,need to tell activity to display Play Button
        resetButtonPlayStopBroadcast();
    }

    //Send a message to Activity that audio is being prepared  and buffering started
    private void sendBufferingBroadCast() {
        bufferIntent.putExtra("buffering", "1");
        sendBroadcast(bufferIntent);
    }

    //Send a message to Activity that audio is prepared  and ready to start playing
    private void sendBufferCompleteBroadcast() {
        bufferIntent.putExtra("buffering", "0");
        sendBroadcast(bufferIntent);
    }

    //Send a message to Activity to reset the play button
    private void resetButtonPlayStopBroadcast() {
        bufferIntent.putExtra("buffering", "2");
        sendBroadcast(bufferIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        //Send a message to Activity to end progress dialogue
        sendBufferCompleteBroadcast();
        playMedia();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK" + extra, Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(this, "MEDIA_ERROR_SERVER_DIED" + extra, Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this, "MEDIA_ERROR_UNKNOWN" + extra, Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (!player.isPlaying()) {
            playMedia();
        }
    }

    public void playMedia() {
        if (!player.isPlaying()) {
            player.start();
        }
    }

    public void pauseMedia() {
        if (player.isPlaying()) {
            player.pause();
        }
    }

    public void stopMedia() {
        if (!player.isPlaying()) {
            player.stop();
        }
    }
/*
    private void initNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        Notification.Builder builder = new Notification.Builder(myPlayService.this);
        int icon = android.R.drawable.btn_radio;
        CharSequence tickerText = "Music In Service";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        Context context = getApplicationContext();
        CharSequence contentTitle = "Music In Service App";
        CharSequence contentText = "Listening to music";
        Intent notificationIntent = new Intent(this, SecondActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        builder.setSmallIcon(icon).setContentTitle(contentTitle).setContentText(contentText).setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        notificationManager.cancel(NOTIFICATION_ID);
    }
*/
}
