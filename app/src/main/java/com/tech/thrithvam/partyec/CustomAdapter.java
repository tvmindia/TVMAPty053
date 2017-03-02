package com.tech.thrithvam.partyec;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CustomAdapter extends BaseAdapter{
    private Context adapterContext;
    private static LayoutInflater inflater=null;
    private ArrayList<String[]> objects;
    private String calledFrom;
    private SimpleDateFormat formatted;
    private Calendar cal;
    Common common;
    public CustomAdapter(Context context, ArrayList<String[]> objects, String calledFrom) {
        // super(context, textViewResourceId, objects);
        adapterContext=context;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects=objects;
        this.calledFrom=calledFrom;
        formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        cal= Calendar.getInstance();
        common=new Common();
    }
    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder
    {
        //Category list---------------------
        TextView categoryName;
        ImageView categoryImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        final int fPos=position;
        switch (calledFrom) {
            //--------------------------for category list items------------------
            case "CategoryList":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_category, null);
                    holder.categoryName = (TextView) convertView.findViewById(R.id.category_name);
                    holder.categoryImage=(ImageView)convertView.findViewById(R.id.category_image);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.categoryName.setText(objects.get(position)[1]);
                common.LoadImage(adapterContext,
                                    holder.categoryImage,
                                    objects.get(position)[0],
                                    R.drawable.dim_icon);
                break;
            default:
                break;
        }
        return convertView;
    }
}
