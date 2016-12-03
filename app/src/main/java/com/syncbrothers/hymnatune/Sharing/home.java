package com.syncbrothers.hymnatune.Sharing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.syncbrothers.hymnatune.R;

public class home extends Activity {
    Button b,h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        button_list();
    }
    public void button_list(){
        b=(Button)findViewById(R.id.b1);
        b.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(home.this,Page2.class);
                        startActivity(i);
                    }
                }
        );
        h=(Button)findViewById(R.id.help);
        h.getLayoutParams().width= 80;
        h.getLayoutParams().height=80;
        h.setBackgroundResource(R.drawable.help_img);
        h.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i=new Intent("com.syncbrothers.hymnatune.Sharing.help");
                        startActivity(i);
                    }
                }
        );
    }
}
