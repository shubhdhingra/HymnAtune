package com.syncbrothers.hymnatune;

/**
 * Created by Shubham on 27-11-2016.
 */

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

public class PlaySolo extends ListActivity {
    private PlaySolo.MediaCursorAdapter mediaAdapter = null;

/*
    ArrayList<String> al;
    ArrayList<String> alname;
    MediaPlayer mp;
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_solo);
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (null != cursor) {
            cursor.moveToFirst();

            mediaAdapter = new PlaySolo.MediaCursorAdapter(this, R.layout.listitem, cursor);
            setListAdapter(mediaAdapter);

        }

    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);

        // Animate the background color of clicked Item
        ColorDrawable[] color = {
                new ColorDrawable(Color.parseColor("#b6c75c")),
                new ColorDrawable(Color.parseColor("#FF0000"))
        };
        TransitionDrawable trans = new TransitionDrawable(color);
        view.setBackground(trans);
        trans.startTransition(1000); // duration 1 seconds

        // Go back to the default background color of Item
        ColorDrawable[] color2 = {
                new ColorDrawable(Color.parseColor("#FF0000")),
                new ColorDrawable(Color.parseColor("#b6c75c"))
        };
        TransitionDrawable trans2 = new TransitionDrawable(color2);
        view.setBackground(trans2);
        trans2.startTransition(1000); // duration 1 seconds

        String currentFile;
        currentFile = (String) view.getTag();
        String filename = currentFile.substring(currentFile.lastIndexOf("/") + 1);
        check(filename);
    }

    private void check(String song){
        //  Toast.makeText(getApplicationContext(),song,Toast.LENGTH_LONG).show();
        //       al=new ArrayList<String>();
        //     alname=new ArrayList<String>();
        String fullpath="";
        String[] STAR = { "*" };
        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        //Intent intent;
        Cursor cursor = getContentResolver().query(allsongsuri, STAR, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    //  String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    //                Toast.makeText(getApplicationContext(), "song name"+ song_name, Toast.LENGTH_SHORT).show();
                    fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    //   al.add(fullpath);
                    String filename1=fullpath.substring(fullpath.lastIndexOf("/")+1);
                    //                  Toast.makeText(getApplicationContext(),filename1,Toast.LENGTH_LONG).show();
                    if(song.equals(filename1))
                    {
                        Toast.makeText(getApplicationContext()," song found" , Toast.LENGTH_SHORT).show();
                              /*   intent=new Intent(Receiver.this,PlayScreen.class);
                                intent.putExtra("currentFile",fullpath);*/
                        break;
                    }
                } while (cursor.moveToNext());
                //   startActivity(intent);
            }
            cursor.close();
        }
//        Toast.makeText(getApplicationContext(),fullpath,Toast.LENGTH_LONG).show();
        Intent intent = new Intent(PlaySolo.this, PlayScreenSolo.class);
        intent.putExtra("currentFile",fullpath);
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

}
