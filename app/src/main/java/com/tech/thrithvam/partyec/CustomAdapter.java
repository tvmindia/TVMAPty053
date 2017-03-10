package com.tech.thrithvam.partyec;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

class CustomAdapter extends BaseAdapter{
    private Context adapterContext;
    private static LayoutInflater inflater=null;
    private ArrayList<String[]> objects;
    private String calledFrom;
    private SimpleDateFormat formatted;
    private Calendar cal;
    private Common common;
    CustomAdapter(Context context, ArrayList<String[]> objects, String calledFrom) {
        // super(context, textViewResourceId, objects);
        adapterContext=context;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects=objects;
        this.filteredObjects=objects;
        this.calledFrom=calledFrom;
        formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        cal= Calendar.getInstance();
        common=new Common();
    }
    @Override
    public int getCount() {
        return filteredObjects.size();
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
        //Navigation Category List---------
        TextView navCatName,itemsCount;
        //All products list----------------
        TextView productName;
        ImageView productImage;
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
                holder.categoryName.setText(filteredObjects.get(position)[1]);
                common.LoadImage(adapterContext,
                                    holder.categoryImage,
                                    filteredObjects.get(position)[0],
                                    R.drawable.dim_icon);
                break;
            //--------------------------for navigation category list items------------------
            case "NavigationCategoryList":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_nav_category, null);
                    holder.navCatName = (TextView) convertView.findViewById(R.id.nav_cat_name);
                    holder.itemsCount=(TextView) convertView.findViewById(R.id.items_count);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.navCatName.setText(filteredObjects.get(position)[0]);
                holder.itemsCount.setText(filteredObjects.get(position)[1]);
                break;
            //--------------------------for All products list items------------------
            case "AllProducts":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_product, null);
                    holder.productName = (TextView) convertView.findViewById(R.id.product_name);
                    holder.productImage=(ImageView) convertView.findViewById(R.id.product_image);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.productName.setText(filteredObjects.get(position)[0]);
                common.LoadImage(adapterContext,
                        holder.productImage,
                        filteredObjects.get(position)[1],
                        R.drawable.dim_icon);
                break;
            default:
                break;
        }
        return convertView;
    }

    //Filtering--------------------------------------
    private ItemFilter mFilter = new ItemFilter();
    private ArrayList<String[]> filteredObjects;
    private int dataItemPosition;
    Filter getFilter(int dataItem) {
        dataItemPosition=dataItem;
        return mFilter;
    }
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            int count = objects.size();
            final ArrayList<String[]> filteredList = new ArrayList<String[]>(count);

            for (int i = 0; i < count; i++) {
                if (objects.get(i)[dataItemPosition].toLowerCase().contains(filterString)) {
                    filteredList.add(objects.get(i));
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredObjects = (ArrayList<String[]>) results.values;
            notifyDataSetChanged();
        }
    }
}
