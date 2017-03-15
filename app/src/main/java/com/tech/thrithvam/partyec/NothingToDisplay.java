package com.tech.thrithvam.partyec;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class NothingToDisplay extends AppCompatActivity {
    Bundle extras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nothing_to_display);
        extras=getIntent().getExtras();

        final TextView nothingLabel=(TextView)findViewById(R.id.nothing_label);
        if(getIntent().hasExtra("activityHead")){
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(extras.getString("activityHead"));
            setSupportActionBar(toolbar);
        }
        if(getIntent().hasExtra("msg")){
            nothingLabel.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    nothingLabel.setText(extras.getString("msg"));
                    return true;
                }
            });
        }
    }
    public void back(View view)
    {
        onBackPressed();
    }
}
