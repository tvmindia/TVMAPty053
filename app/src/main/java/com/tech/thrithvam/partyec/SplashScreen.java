package com.tech.thrithvam.partyec;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.google.firebase.messaging.FirebaseMessaging;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class SplashScreen extends AppCompatActivity {
    SharedPreferences mPrefs;
    String welcomeScreenShownPref = "welcomeScreenShownPartyEC";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        FirebaseMessaging.getInstance().subscribeToTopic("common");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean welcomeScreenShown = mPrefs.getBoolean(welcomeScreenShownPref, false);
        if (!welcomeScreenShown) {
        final ImageView myView = (ImageView) findViewById(R.id.test);

        // Android native animator
        myView.post(new Runnable(){

            @Override
            public void run()
            {


                // get the center for the clipping circle
                int cx = (myView.getLeft() + myView.getRight()) / 2;
                int cy = (myView.getTop() + myView.getBottom()) / 2;

                // get the final radius for the clipping circle
                int dx = Math.max(cx, myView.getWidth() - cx);
                int dy = Math.max(cy, myView.getHeight() - cy);
                float finalRadius = (float) Math.hypot(dx, dy);
        SupportAnimator animator =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(2500);
        animator.start();

            }
        });


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreen.this, Home.class));
                    finish();
                }
            }, 3000);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(welcomeScreenShownPref, true);
            editor.apply();
        }
        else {
            startActivity(new Intent(SplashScreen.this, Home.class));
            finish();
        }
    }
}
