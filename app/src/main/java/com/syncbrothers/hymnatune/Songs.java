package com.syncbrothers.hymnatune;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.math.BigDecimal;

/**
 * Created by Shubham on 12-10-2016.
 */

public class Songs extends ListActivity {

    private MediaCursorAdapter mediaAdapter = null;
    private String currentFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs);

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
*/      currentFile = (String) view.getTag();
//      Intent intent = new Intent(Songs.this, PlayScreen.class);
//      intent.putExtra("currentFile", currentFile);
//      startActivity(intent);
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

