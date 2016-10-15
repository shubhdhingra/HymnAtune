package com.syncbrothers.hymnatune;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;



/**
 * Created by Shubham on 08-10-2016.
 */

public class Play_Screen extends Activity implements SeekBar.OnSeekBarChangeListener {
  //  private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;

    private TextView selectedFile = null;
    private MediaPlayer player = null;
    private ImageButton playButton = null;
    private ImageButton prevButton = null;
    private ImageButton nextButton = null;

    private boolean isStarted = true;
    private String currentFile = "";

    //Seekbar variables
    private SeekBar seekBar = null;
   private int seekMax;
   private static int songEnded = 0;
    boolean mBroadcastIsRegistered;

    boolean mBufferBroadcastIsRegistered;
    private ProgressDialog pdBuff = null;

    //Declare broadcast action  and intent
    public static final String BROADCAST_SEEKBAR = "com.example.shubham.hymnattune.sendseekbar";
    Intent intent;


     Intent serviceIntent;
    private boolean isOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_play);
        try {

            Intent intent1 = getIntent();
            serviceIntent = new Intent(this, myPlayService.class);

            String currentFile = intent1.getExtras().getString("currentFile");

            //set up seekbar intent for broadcasting new position to service
            //intent = new Intent(BROADCAST_SEEKBAR);
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);

            initViews();
            setListeners(cursor);
            startPlay(currentFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();

        }

    }

    private void initViews(){
        selectedFile = (TextView) (findViewById(R.id.selectedfile));
        seekBar = (SeekBar) (findViewById(R.id.seekbar));
        playButton = (ImageButton) (findViewById(R.id.play));
        prevButton = (ImageButton) (findViewById(R.id.prev));
        nextButton = (ImageButton) (findViewById(R.id.next));
        player = new MediaPlayer();
    }

    private void setListeners(Cursor cursor){
        if (null != cursor) {
            playButton.setOnClickListener(onButtonClick);
            prevButton.setOnClickListener(onButtonClick);
            nextButton.setOnClickListener(onButtonClick);
            seekBar.setOnSeekBarChangeListener(this);
        }
    }

   private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent serviceIntent) {
     //       updateUI(serviceIntent);
        }
    };

    private void updateUI(Intent serviceIntent) {
        String counter = serviceIntent.getStringExtra("counter");
        String mediamax = serviceIntent.getStringExtra("mediamax");
        String strSongEnded = serviceIntent.getStringExtra("song_ended");
        int seekProgress = Integer.parseInt(counter);
        seekMax = Integer.parseInt(mediamax);
        songEnded = Integer.parseInt(strSongEnded);
        seekBar.setMax(seekMax);
        seekBar.setProgress(seekProgress);
        if (songEnded == 1) {
            playButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }
    //End of seek bar update code


    private void startPlay(String file) {
        Log.i("Selected: ", file);
//        checkConnectivity();
//        if (isOnline) {
           // stopPlay();

            //split file
            selectedFile.setText(file);
            serviceIntent.putExtra("currentFile", file);
            try {
                startService(serviceIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            //register receiver for seekbar
            registerReceiver(broadcastReceiver, new IntentFilter(myPlayService.BROADCAST_ACTION));

            mBroadcastIsRegistered = true;

        /*} else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Network Not Connected...");
            alertDialog.setMessage("Please connect to a network or try again!!!");
            alertDialog.setButton(1, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog.setIcon(android.R.drawable.arrow_down_float);
            playButton.setImageResource(android.R.drawable.ic_media_play);
            alertDialog.show();
        }
        */
    }

    private void stopPlay() {
        //Unregister broadcats receiver for seek bar
        if (mBroadcastIsRegistered) {
          try {
                unregisterReceiver(broadcastReceiver);
                mBroadcastIsRegistered = false;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        try {
            stopService(serviceIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        isStarted = false;
    }

    private View.OnClickListener onButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play: {
                    if (player.isPlaying()) {
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                        stopPlay();
                        } else {
                        if (isStarted) {
                            player.start();
                            playButton.setImageResource(android.R.drawable.ic_media_pause);

                        } else {
                            startPlay(currentFile);
                        }
                    }
                    break;
                }
                case R.id.next: {
                    int seekto = player.getCurrentPosition() + STEP_VALUE;

                    if (seekto > player.getDuration())
                        seekto = player.getDuration();
                    player.pause();
                    player.seekTo(seekto);
                    player.start();

                    break;
                }
                case R.id.prev: {
                    int seekto = player.getCurrentPosition() - STEP_VALUE;
                    if (seekto < 0)
                        seekto = 0;

                    player.pause();
                    player.seekTo(seekto);
                    player.start();

                    break;
                }
            }
        }

    };

    //Handle progress dialog for buffering

    private void showPD(Intent bufferIntent) {
        String bufferValue = bufferIntent.getStringExtra("buffering");
        int bufferIntValue = Integer.parseInt(bufferValue);
        switch (bufferIntValue) {
            case 0:
                if (pdBuff != null) {
                    pdBuff.dismiss();
                }
                break;
            case 1:
                BufferDialogue();
                break;
            case 2:
                playButton.setImageResource(android.R.drawable.ic_media_play);
                break;
        }
    }

    //progress dialogue
    private void BufferDialogue() {
        pdBuff = ProgressDialog.show(Play_Screen.this, "Buffering...", "Acquiring Song...", true);
    }

    //Set up broadcast receiver
    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showPD(intent);
        }
    };

    /*private void checkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting() ||
                cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting()) {
            isOnline = true;
        } else
            isOnline = false;
    }*/

    @Override
    protected void onPause() {
        //Unregister broadcast receiver
        if (mBufferBroadcastIsRegistered) {
            try {
                unregisterReceiver(broadcastBufferReceiver);
                mBufferBroadcastIsRegistered = false;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        //Unregister seekbr broadcast receiver
        if (mBroadcastIsRegistered) {
            try {
                unregisterReceiver(broadcastReceiver);
                mBroadcastIsRegistered = false;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        super.onPause();
    }

    @Override
    protected void onResume() {

        //Register broadcast receiver

        if (!mBufferBroadcastIsRegistered) {
            registerReceiver(broadcastBufferReceiver, new IntentFilter(myPlayService.BROADCAST_BUFFER));
            mBufferBroadcastIsRegistered = true;
        }

        //Register seekar broadcast receiver

        if (!mBroadcastIsRegistered) {
            registerReceiver(broadcastReceiver, new IntentFilter(myPlayService.BROADCAST_ACTION));
            mBroadcastIsRegistered = true;
        }
        super.onResume();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int seekPos = seekBar.getProgress();
            intent.putExtra("seekpos", seekPos);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}