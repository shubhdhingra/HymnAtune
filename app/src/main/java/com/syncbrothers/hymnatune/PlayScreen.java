package com.syncbrothers.hymnatune;

/**
 * Created by Shubham on 16-11-2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
//import android.widget.SeekBar;
import android.widget.TextView;

import com.jesusm.holocircleseekbar.lib.HoloCircleSeekBar;

import java.io.IOException;
import java.math.BigDecimal;

import static android.R.attr.value;

public class PlayScreen extends Activity {
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

        Cursor cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
        if(null!=cursor)
        {
            cursor.moveToFirst();
            playButton.setOnClickListener(onButtonClick);
         //   prevButton.setOnClickListener(onButtonClick);
           // nextButton.setOnClickListener(onButtonClick);

        }
        Intent CurrentFileIntent = getIntent();
        String currentFile = CurrentFileIntent.getExtras().getString("currentFile");
        startplay(currentFile);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(updatePositionRunnable);
        player.stop();
        player.reset();
        player.release();

        player=null;
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
        catch(IllegalStateException  e)
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
