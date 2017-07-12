package com.tech.thrithvam.partyec;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import java.util.ArrayList;

public class ImageViewer extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        int position = getIntent().getIntExtra("position", 0);
        ArrayList<String> temp=getIntent().getStringArrayListExtra("imageURLs");

        ViewPager pager = (ViewPager) findViewById(R.id.image_slider);
        pager.setAdapter(new ImagesPagerAdapter(
                getSupportFragmentManager(), this,temp));
        pager.setCurrentItem(position);
    }
}
