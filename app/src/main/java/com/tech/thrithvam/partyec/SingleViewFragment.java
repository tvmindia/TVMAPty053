package com.tech.thrithvam.partyec;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.senab.photoview.PhotoView;


public class SingleViewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_image_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String resourceURL = getArguments().getString("image");
        PhotoView imageView = (PhotoView) getView().findViewById(R.id.image);
        Common.LoadImage(getContext(),
                imageView,
                resourceURL,
                R.drawable.dim_icon);
    }
}
