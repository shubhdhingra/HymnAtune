package com.syncbrothers.hymnatune.Sharing;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.syncbrothers.hymnatune.R;

public class help extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        ImageAdapter adapter = new ImageAdapter(this);
        viewPager.setAdapter(adapter);
    }

}

