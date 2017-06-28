package com.tech.thrithvam.partyec;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import uk.co.senab.photoview.PhotoView;

public class ImageViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        Common common=new Common();
        Common.LoadImage(ImageViewer.this,
                (PhotoView)findViewById(R.id.image),
                getIntent().getExtras().getString("imageUrl"),
                R.drawable.dim_icon);
    }
}
