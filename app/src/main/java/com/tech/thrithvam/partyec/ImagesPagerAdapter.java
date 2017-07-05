package com.tech.thrithvam.partyec;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;


class ImagesPagerAdapter extends FragmentPagerAdapter{

    public Context context;
    private ArrayList<String> imageURLs=new ArrayList<>();

    ImagesPagerAdapter(FragmentManager fragmentManager, Context context, ArrayList<String> imageURLs) {
        super(fragmentManager);
        this.context=context;
        this.imageURLs=imageURLs;
    }

    @Override
    public Fragment getItem(int i) {
        Bundle args = new Bundle();
        args.putString("image", imageURLs.get(i));

        SingleViewFragment fragment = new SingleViewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return imageURLs.size();
    }
}
