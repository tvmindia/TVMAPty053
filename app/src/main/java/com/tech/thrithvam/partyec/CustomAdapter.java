package com.tech.thrithvam.partyec;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

class CustomAdapter extends BaseAdapter{
    private Context adapterContext;
    private static LayoutInflater inflater=null;
    private ArrayList<String[]> objects;
    private String calledFrom;
    private SimpleDateFormat formatted;
    private Calendar cal;
    private int viewType;
    CustomAdapter(Context context, ArrayList<String[]> objects, String calledFrom) {
        // super(context, textViewResourceId, objects);
        initialization(context, objects, calledFrom);
    }
    CustomAdapter(Context context, ArrayList<String[]> objects, String calledFrom,int viewType){//Constructor for products listing
        this.viewType=viewType;
        initialization(context, objects, calledFrom);
    }
    void initialization(Context context, ArrayList<String[]> objects, String calledFrom){
        adapterContext=context;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects=objects;
        this.filteredObjects=objects;
        this.calledFrom=calledFrom;
        formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        cal= Calendar.getInstance();
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

    private class Holder {
        //Category list---------------------
        TextView categoryName;
        ImageView categoryImage;
        //Navigation Category List----------
        TextView navCatName, itemsCount;
        //All products list-----------------
        TextView productName,actualPrice,stockAvailability,supplierName;
        ImageView productImage,stickerImage;
        //Product Reviews-------------------
        TextView customerName, reviewDate, review,isApproved;
        MaterialRatingBar ratingReview;
        ImageView customerImage;
        //Address-------------------------
        TextView address, location, city, state, country, contactNo, setDefault, select,remove, edit;
        LinearLayout unwantedControls;
        //WishList-------------------------
        TextView daysCount, price;
        //Bookings----------------------------
        TextView bookingNo, RequiredDate, Status, BookingDate;
        //Quotations-------------------------
        TextView quotationNo, quotationDate;
        //xCart(now used for other purposes)--------------------
        TextView shipping, quantity, attributes;
        //Order----------------------------------
        TextView orderNo, orderDate, orderStatus, totalAmount, taxAmount;
        //Event Requests-------------------------
        TextView eventReqNo, eventType, eventTitle, eventDateTime, eventTime, lookingFor, requirementSpec, message, noOfPersons, budget, eventRequestStatus, adminRemarks, totalPrice, totalTaxAmt, totalDiscountAmt;
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
                Common.LoadImage(adapterContext,
                                    holder.categoryImage,
                                    adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[0],
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
                holder.navCatName.setText(filteredObjects.get(position)[1]);
                holder.itemsCount.setText(filteredObjects.get(position)[2].equals("null")?"":("("+filteredObjects.get(position)[2])+")");
                break;
            //--------------------------for All products list items------------------
            case "AllProducts":
                if (convertView == null) {
                    holder = new Holder();
                    if(viewType==0)
                        convertView = inflater.inflate(R.layout.item_product_grid, null);
                    else if(viewType==1)
                        convertView = inflater.inflate(R.layout.item_product_horizontal, null);
                    else
                        convertView = inflater.inflate(R.layout.item_product_single, null);
                    holder.productName = (TextView) convertView.findViewById(R.id.product_name);
                    holder.productImage=(ImageView) convertView.findViewById(R.id.product_image);
                    holder.stickerImage=(ImageView) convertView.findViewById(R.id.sticker);
                    holder.totalPrice=(TextView)convertView.findViewById(R.id.total_price);
                    holder.actualPrice=(TextView)convertView.findViewById(R.id.actual_price);
                    holder.stockAvailability=(TextView)convertView.findViewById(R.id.stock_availability);
                    holder.supplierName=(TextView)convertView.findViewById(R.id.supplier_name);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.productName.setText(filteredObjects.get(position)[0]);
                Common.LoadImage(adapterContext,
                        holder.productImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[1],
                        R.drawable.dim_icon);
                Common.LoadImage(adapterContext,
                        holder.stickerImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[3],
                        0);
                if(!(filteredObjects.get(position)[4].equals("null")) && Double.parseDouble(filteredObjects.get(position)[4])!=0) {
                    holder.totalPrice.setVisibility(View.VISIBLE);
                    holder.actualPrice.setVisibility(View.VISIBLE);
                    //price
                    holder.totalPrice.setText(adapterContext.getResources().getString(R.string.price_display, (filteredObjects.get(position)[4]).equals("null") || (filteredObjects.get(position)[4].equals("0.0")) ? "" : filteredObjects.get(position)[4]));
                    //stock availability
                    if(!(filteredObjects.get(position)[6].equals("null"))){
                        if(Boolean.parseBoolean(filteredObjects.get(position)[6])){
                            holder.stockAvailability.setText(R.string.in_stock);
                            holder.stockAvailability.setTextColor(adapterContext.getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            holder.stockAvailability.setText(R.string.out_of_stock);
                            holder.stockAvailability.setTextColor(adapterContext.getResources().getColor(android.R.color.holo_red_light));
                        }
                    }
                    else {
                        holder.stockAvailability.setText("");
                    }
                    //discount
                    if (!(filteredObjects.get(position)[5].equals("null")) && !(Double.parseDouble(filteredObjects.get(position)[5]) == 0)) {
                        holder.actualPrice.setPaintFlags(holder.actualPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        String actualPriceString = String.format(Locale.US, "%.2f",
                                ((filteredObjects.get(position)[4]).equals("null") ? 0.0 : Double.parseDouble(filteredObjects.get(position)[4]))
                                        +
                                        ((filteredObjects.get(position)[5]).equals("null") ? 0.0 : Double.parseDouble(filteredObjects.get(position)[5])));
                        holder.actualPrice.setText(adapterContext.getResources().getString(R.string.price_display, actualPriceString));
                    } else {
                        holder.actualPrice.setText("");
                    }
                }
                else {
                    holder.totalPrice.setVisibility(View.GONE);
                    holder.actualPrice.setVisibility(View.GONE);
                    holder.stockAvailability.setText("");
                }
                holder.supplierName.setText(filteredObjects.get(position)[7].equals("null")?"":filteredObjects.get(position)[7]);
                break;
            //--------------------------for reviews list items------------------
            case "ReviewList":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_review, null);
                    holder.customerName = (TextView) convertView.findViewById(R.id.customer_name);
                    holder.customerImage=(ImageView) convertView.findViewById(R.id.customer_image);
                    holder.reviewDate = (TextView) convertView.findViewById(R.id.date);
                    holder.review = (TextView) convertView.findViewById(R.id.review);
                    holder.ratingReview = (MaterialRatingBar) convertView.findViewById(R.id.avg_rating_bar);
                    holder.isApproved=(TextView)convertView.findViewById(R.id.is_approved);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.isApproved.setVisibility(View.GONE);
                holder.customerName.setText(filteredObjects.get(position)[1]);
                Common.LoadImage(adapterContext,
                        holder.customerImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[2],
                        R.drawable.user);
                holder.reviewDate.setText(filteredObjects.get(position)[4]);
                holder.review.setText(filteredObjects.get(position)[5]);
                holder.ratingReview.setRating(Float.parseFloat(filteredObjects.get(position)[3]));
                break;
            //--------------------------for related products list items------------------
            case "RelatedItemsList":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_product_horizontal, null);
                    holder.productName = (TextView) convertView.findViewById(R.id.product_name);
                    holder.productImage=(ImageView) convertView.findViewById(R.id.product_image);
                    holder.stickerImage=(ImageView) convertView.findViewById(R.id.sticker);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.productName.setText(filteredObjects.get(position)[1]);
                Common.LoadImage(adapterContext,
                        holder.productImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[2],
                        R.drawable.dim_icon);
                Common.LoadImage(adapterContext,
                        holder.stickerImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[3],
                        0);
                break;
            //-------------------------for product images slider-----------------------------
            case "ProductImages":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_product_images, null);
                    holder.productImage=(ImageView) convertView.findViewById(R.id.image);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                Common.LoadImage(adapterContext,
                        holder.productImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[1],
                        R.drawable.dim_icon);
                break;
            //----------------------------------for address list------------------------------
            case "Address":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_address, null);
                    holder.customerName=(TextView) convertView.findViewById(R.id.name);
                    holder.address=(TextView) convertView.findViewById(R.id.address);
                    holder.location=(TextView) convertView.findViewById(R.id.location);
                    holder.city=(TextView) convertView.findViewById(R.id.city);
                    holder.state=(TextView) convertView.findViewById(R.id.stateprovince);
                    holder.country=(TextView) convertView.findViewById(R.id.country);
                    holder.contactNo=(TextView) convertView.findViewById(R.id.contact_no);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                String name=(filteredObjects.get(position)[1].equals("null")?"":filteredObjects.get(position)[1])+   " "
                        +   (filteredObjects.get(position)[2].equals("null")?"":filteredObjects.get(position)[2])+   " "
                        +   (filteredObjects.get(position)[3].equals("null")?"":filteredObjects.get(position)[3])+   " "
                        +   (filteredObjects.get(position)[4].equals("null")?"":filteredObjects.get(position)[4]);
                holder.customerName.setText(name);
                holder.address.setText(filteredObjects.get(position)[5].equals("null")?"":filteredObjects.get(position)[5]);
                holder.location.setText(filteredObjects.get(position)[6].equals("null")?"-":filteredObjects.get(position)[6]);
                holder.city.setText(filteredObjects.get(position)[7].equals("null")?"-":filteredObjects.get(position)[7]);
                holder.state.setText(filteredObjects.get(position)[8].equals("null")?"-":filteredObjects.get(position)[8]);
                String country="";
                try {
                    JSONObject jsonObjectCountry=new JSONObject(filteredObjects.get(position)[9]);
                    country=jsonObjectCountry.getString("Name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                holder.country.setText(country.equals("null")?"-":country);
                holder.contactNo.setText(filteredObjects.get(position)[10].equals("null")?"-":filteredObjects.get(position)[10]);
                break;
            case "AddressManagement":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_address_edit, null);
                    holder.customerName=(TextView) convertView.findViewById(R.id.name);
                    holder.address=(TextView) convertView.findViewById(R.id.address);
                    holder.location=(TextView) convertView.findViewById(R.id.location);
                    holder.city=(TextView) convertView.findViewById(R.id.city);
                    holder.state=(TextView) convertView.findViewById(R.id.stateprovince);
                    holder.country=(TextView) convertView.findViewById(R.id.country);
                    holder.contactNo=(TextView) convertView.findViewById(R.id.contact_no);
                    holder.setDefault=(TextView) convertView.findViewById(R.id.select_default);
                    holder.remove=(TextView)convertView.findViewById(R.id.remove);
                    holder.edit=(TextView)convertView.findViewById(R.id.edit);
                    holder.unwantedControls=(LinearLayout)convertView.findViewById(R.id.address_select_controls);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.unwantedControls.setVisibility(View.GONE);
                String name1=(filteredObjects.get(position)[1].equals("null")?"":filteredObjects.get(position)[1])+   " "
                        +   (filteredObjects.get(position)[2].equals("null")?"":filteredObjects.get(position)[2])+   " "
                        +   (filteredObjects.get(position)[3].equals("null")?"":filteredObjects.get(position)[3])+   " "
                        +   (filteredObjects.get(position)[4].equals("null")?"":filteredObjects.get(position)[4]);
                holder.customerName.setText(name1);
                holder.address.setText(filteredObjects.get(position)[5].equals("null")?"":filteredObjects.get(position)[5]);
                holder.location.setText(filteredObjects.get(position)[6].equals("null")?"-":filteredObjects.get(position)[6]);
                holder.city.setText(filteredObjects.get(position)[7].equals("null")?"-":filteredObjects.get(position)[7]);
                holder.state.setText(filteredObjects.get(position)[8].equals("null")?"-":filteredObjects.get(position)[8]);
                String country1="";
                try {
                    JSONObject jsonObjectCountry=new JSONObject(filteredObjects.get(position)[9]);
                    country1=jsonObjectCountry.getString("Name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                holder.country.setText(country1.equals("null")?"-":country1);
                holder.contactNo.setText(filteredObjects.get(position)[10].equals("null")?"-":filteredObjects.get(position)[10]);
                holder.setDefault.setTag(filteredObjects.get(position)[0]);
                holder.remove.setTag(filteredObjects.get(position)[0]);
                holder.edit.setTag(filteredObjects.get(position)[0]);
                if(filteredObjects.get(position)[12].equals("true")){
                    holder.setDefault.setText(adapterContext.getResources().getString(R.string.default_address));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.setDefault.setTextColor(adapterContext.getColor(R.color.secondary_text));
                    }
                    else {
                        holder.setDefault.setTextColor(adapterContext.getResources().getColor(R.color.secondary_text));
                    }
                    holder.remove.setVisibility(View.GONE);
                }
                else {
                    holder.setDefault.setText(adapterContext.getResources().getString(R.string.select_default));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.setDefault.setTextColor(adapterContext.getColor(R.color.colorAccent));
                    }
                    else {
                        holder.setDefault.setTextColor(adapterContext.getResources().getColor(R.color.colorAccent));
                    }
                    holder.remove.setVisibility(View.VISIBLE);
                }
                break;
            case "AddressSelection":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_address_edit, null);
                    holder.customerName=(TextView) convertView.findViewById(R.id.name);
                    holder.address=(TextView) convertView.findViewById(R.id.address);
                    holder.location=(TextView) convertView.findViewById(R.id.location);
                    holder.city=(TextView) convertView.findViewById(R.id.city);
                    holder.state=(TextView) convertView.findViewById(R.id.stateprovince);
                    holder.country=(TextView) convertView.findViewById(R.id.country);
                    holder.contactNo=(TextView) convertView.findViewById(R.id.contact_no);
                    holder.select=(TextView) convertView.findViewById(R.id.select_address);
                    holder.edit=(TextView)convertView.findViewById(R.id.edit2);
                    holder.unwantedControls=(LinearLayout)convertView.findViewById(R.id.address_edit_controls);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.unwantedControls.setVisibility(View.GONE);
                String name2=(filteredObjects.get(position)[1].equals("null")?"":filteredObjects.get(position)[1])+   " "
                        +   (filteredObjects.get(position)[2].equals("null")?"":filteredObjects.get(position)[2])+   " "
                        +   (filteredObjects.get(position)[3].equals("null")?"":filteredObjects.get(position)[3])+   " "
                        +   (filteredObjects.get(position)[4].equals("null")?"":filteredObjects.get(position)[4]);
                holder.customerName.setText(name2);
                holder.address.setText(filteredObjects.get(position)[5].equals("null")?"":filteredObjects.get(position)[5]);
                holder.location.setText(filteredObjects.get(position)[6].equals("null")?"-":filteredObjects.get(position)[6]);
                holder.city.setText(filteredObjects.get(position)[7].equals("null")?"-":filteredObjects.get(position)[7]);
                holder.state.setText(filteredObjects.get(position)[8].equals("null")?"-":filteredObjects.get(position)[8]);
                String country2="";
                try {
                    JSONObject jsonObjectCountry=new JSONObject(filteredObjects.get(position)[9]);
                    country2=jsonObjectCountry.getString("Name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                holder.country.setText(country2.equals("null")?"-":country2);
                holder.contactNo.setText(filteredObjects.get(position)[10].equals("null")?"-":filteredObjects.get(position)[10]);
                holder.select.setTag(filteredObjects.get(position)[0]);
                holder.edit.setTag(filteredObjects.get(position)[0]);
                break;
            //----------------------------------  WishList  ------------------------------
            case "WishList":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_wishlist, null);
                    holder.productImage=(ImageView) convertView.findViewById(R.id.product_image);
                    holder.productName=(TextView)convertView.findViewById(R.id.product_name);
                    holder.daysCount =(TextView)convertView.findViewById(R.id.days_count);
                    holder.price =(TextView)convertView.findViewById(R.id.price);
                    holder.stickerImage=(ImageView)convertView.findViewById(R.id.sticker);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                Common.LoadImage(adapterContext,
                        holder.productImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[2],
                        R.drawable.dim_icon);
                holder.productName.setText(filteredObjects.get(position)[1].equals("null")?"":filteredObjects.get(position)[1]);
                Common.LoadImage(adapterContext,
                        holder.stickerImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[5],
                        0);
                holder.daysCount.setText(filteredObjects.get(position)[3].equals("null")?"":adapterContext.getResources().getString(R.string.wishlist_days,filteredObjects.get(position)[3]));
                holder.price.setText(filteredObjects.get(position)[4].equals("null")?"":adapterContext.getResources().getString(R.string.price_display,filteredObjects.get(position)[4]));
                break;
            //----------------------------------  Bookings  ------------------------------
            case "Bookings":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_bookings, null);
                    holder.productImage=(ImageView) convertView.findViewById(R.id.product_image);
                    holder.bookingNo=(TextView)convertView.findViewById(R.id.booking_No);
                    holder.productName=(TextView)convertView.findViewById(R.id.product_name);
                    holder.RequiredDate =(TextView)convertView.findViewById(R.id.required_Date);
                    holder.BookingDate =(TextView)convertView.findViewById(R.id.booking_Date);
                    holder.Status =(TextView)convertView.findViewById(R.id.booking_status);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                Common.LoadImage(adapterContext,
                        holder.productImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[6],
                        R.drawable.dim_icon);
                holder.productName.setText(filteredObjects.get(position)[5].equals("null")?"":filteredObjects.get(position)[5]);
                holder.bookingNo.setText(filteredObjects.get(position)[0].equals("null")?"":adapterContext.getResources().getString(R.string.booking_no,filteredObjects.get(position)[0]));
                holder.RequiredDate.setText(filteredObjects.get(position)[2].equals("null")?"":adapterContext.getResources().getString(R.string.required_dates,filteredObjects.get(position)[2]));
                holder.BookingDate.setText(filteredObjects.get(position)[3].equals("null")?"":adapterContext.getResources().getString(R.string.booking_dates,filteredObjects.get(position)[3]));
                holder.Status.setText(filteredObjects.get(position)[4].equals("null")?"":adapterContext.getResources().getString(R.string.booking_status,filteredObjects.get(position)[4]));
                break;
            //----------------------------------  Quotations  ------------------------------
            case "Quotations":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_quotations, null);
                    holder.productImage=(ImageView) convertView.findViewById(R.id.product_image);
                    holder.quotationNo=(TextView)convertView.findViewById(R.id.quotation_No);
                    holder.productName=(TextView)convertView.findViewById(R.id.product_name);
                    holder.RequiredDate =(TextView)convertView.findViewById(R.id.required_Date);
                    holder.quotationDate =(TextView)convertView.findViewById(R.id.quotation_Date);
                    holder.Status =(TextView)convertView.findViewById(R.id.quotation_status);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                Common.LoadImage(adapterContext,
                        holder.productImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[6],
                        R.drawable.dim_icon);
                holder.productName.setText(filteredObjects.get(position)[5].equals("null")?"":filteredObjects.get(position)[5]);
                holder.quotationNo.setText(filteredObjects.get(position)[0].equals("null")?"":adapterContext.getResources().getString(R.string.quotation_no,filteredObjects.get(position)[0]));
                holder.RequiredDate.setText(filteredObjects.get(position)[2].equals("null")?"":adapterContext.getResources().getString(R.string.required_dates,filteredObjects.get(position)[2]));
                holder.quotationDate.setText(filteredObjects.get(position)[3].equals("null")?"":adapterContext.getResources().getString(R.string.quotation_dates,filteredObjects.get(position)[3]));
                holder.Status.setText(filteredObjects.get(position)[4].equals("null")?"":adapterContext.getResources().getString(R.string.quotation_status,filteredObjects.get(position)[4]));
                break;
            //---------------------------------------- Orders --------------------------------------
            case "Orders":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_orders, null);
                    holder.orderNo = (TextView) convertView.findViewById(R.id.order_no);
                    holder.orderDate=(TextView) convertView.findViewById(R.id.order_date);
                    holder.orderStatus=(TextView) convertView.findViewById(R.id.order_status);
                    holder.totalAmount=(TextView) convertView.findViewById(R.id.order_amount);

                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.orderNo.setText(filteredObjects.get(position)[0].equals("null")?"":adapterContext.getResources().getString(R.string.order_no,filteredObjects.get(position)[0]));
                holder.orderDate.setText(filteredObjects.get(position)[1].equals("null")?"":adapterContext.getResources().getString(R.string.order_dates,filteredObjects.get(position)[1]));
                holder.orderStatus.setText(filteredObjects.get(position)[2].equals("null")?"":adapterContext.getResources().getString(R.string.order_status,filteredObjects.get(position)[2]));
                holder.totalAmount.setText(filteredObjects.get(position)[3].equals("null")?"":adapterContext.getResources().getString(R.string.order_total,filteredObjects.get(position)[3]));
                break;
            case "OrderDetails":

                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_order_details, null);
                    holder.productName = (TextView) convertView.findViewById(R.id.product_name);
                    holder.quantity=(TextView) convertView.findViewById(R.id.quantity);
                    holder.price=(TextView) convertView.findViewById(R.id.price);
                    holder.shipping=(TextView) convertView.findViewById(R.id.shipping);
                    holder.taxAmount=(TextView) convertView.findViewById(R.id.tax_amount);
                    holder.totalAmount=(TextView) convertView.findViewById(R.id.total_amount);
                    holder.attributes=(TextView) convertView.findViewById(R.id.attributes);
                    holder.productImage=(ImageView) convertView.findViewById(R.id.product_image);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.productName.setText(filteredObjects.get(position)[1].equals("null")?"":filteredObjects.get(position)[1]);
                holder.attributes.setText(filteredObjects.get(position)[2]);
                holder.quantity.setText(adapterContext.getResources().getString(R.string.quantity_display,filteredObjects.get(position)[3].equals("null")?"-":filteredObjects.get(position)[3]));
                holder.price.setText(adapterContext.getResources().getString(R.string.price_display_2,filteredObjects.get(position)[4].equals("null")?"-":filteredObjects.get(position)[4]));
                holder.shipping.setText(adapterContext.getResources().getString(R.string.shipping_charge,filteredObjects.get(position)[5].equals("null")?"-":filteredObjects.get(position)[5]));
                holder.taxAmount.setText(adapterContext.getResources().getString(R.string.tax_amount,filteredObjects.get(position)[6].equals("null")?"-":filteredObjects.get(position)[6]));
                holder.totalAmount.setText(filteredObjects.get(position)[7].equals("null")?"":adapterContext.getResources().getString(R.string.item_total,filteredObjects.get(position)[7]));
                Common.LoadImage(adapterContext,
                        holder.productImage,
                        adapterContext.getResources().getString(R.string.url)+filteredObjects.get(position)[8],
                        R.drawable.dim_icon);
                break;
            //--------------------------for event request history list items------------------
            case "EventRequests":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_event_request, null);
                    holder.eventReqNo = (TextView) convertView.findViewById(R.id.event_req_no);
                    holder.eventType=(TextView) convertView.findViewById(R.id.event_type);
                    holder.eventTitle=(TextView) convertView.findViewById(R.id.event_name);
                    holder.eventDateTime=(TextView) convertView.findViewById(R.id.event_date);
                    holder.eventTime=(TextView) convertView.findViewById(R.id.event_time);
                    holder.lookingFor=(TextView) convertView.findViewById(R.id.looking_for);
                    holder.requirementSpec=(TextView) convertView.findViewById(R.id.req_specification);
                    holder.message=(TextView) convertView.findViewById(R.id.message);
                    holder.noOfPersons=(TextView) convertView.findViewById(R.id.no_of_persons);
                    holder.budget=(TextView) convertView.findViewById(R.id.budget);
                    holder.eventRequestStatus=(TextView) convertView.findViewById(R.id.request_status);
                    holder.adminRemarks=(TextView) convertView.findViewById(R.id.admin_remark);
                    holder.totalPrice=(TextView) convertView.findViewById(R.id.total_price);
                    holder.totalTaxAmt=(TextView) convertView.findViewById(R.id.tax_amount);
                    holder.totalDiscountAmt=(TextView) convertView.findViewById(R.id.discount_amount);
                    holder.totalAmount=(TextView) convertView.findViewById(R.id.total_amount);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.eventReqNo.setText(filteredObjects.get(position)[1]);
                holder.eventType.setText(filteredObjects.get(position)[2]);
                holder.eventTitle.setText(filteredObjects.get(position)[3]);
                holder.eventDateTime.setText(filteredObjects.get(position)[4]);
                holder.eventTime.setText(filteredObjects.get(position)[5].equals("12:00:00 AM")||filteredObjects.get(position)[5].equals("null")?"":filteredObjects.get(position)[5]);

                holder.lookingFor.setText(filteredObjects.get(position)[6].equals("null")?"":filteredObjects.get(position)[6]);
                if(holder.lookingFor.getText().equals("")) holder.lookingFor.setVisibility(View.GONE); else holder.lookingFor.setVisibility(View.VISIBLE);

                holder.requirementSpec.setText(filteredObjects.get(position)[7].equals("null")?"":filteredObjects.get(position)[7]);
                if(holder.requirementSpec.getText().equals("")) holder.requirementSpec.setVisibility(View.GONE); else holder.requirementSpec.setVisibility(View.VISIBLE);

                holder.message.setText(filteredObjects.get(position)[8].equals("null")?"":filteredObjects.get(position)[8]);
                if(holder.message.getText().equals("")) holder.message.setVisibility(View.GONE); else holder.message.setVisibility(View.VISIBLE);

                holder.noOfPersons.setText(filteredObjects.get(position)[9].equals("null")?"":adapterContext.getResources().getString(R.string.no_of_persons_value,filteredObjects.get(position)[9]));
                holder.budget.setText(filteredObjects.get(position)[10].equals("0.0")||filteredObjects.get(position)[10].equals("null")?"":adapterContext.getResources().getString(R.string.budget_value,filteredObjects.get(position)[10]));
                holder.eventRequestStatus.setText(filteredObjects.get(position)[11].equals("null")?"":filteredObjects.get(position)[11]);

                holder.adminRemarks.setText(filteredObjects.get(position)[12].equals("null")?"":filteredObjects.get(position)[12]);
                if(holder.adminRemarks.getText().equals("")) holder.adminRemarks.setVisibility(View.GONE); else holder.adminRemarks.setVisibility(View.VISIBLE);

                holder.totalPrice.setText(filteredObjects.get(position)[14].equals("null")?"":adapterContext.getResources().getString(R.string.total_price,filteredObjects.get(position)[14]));
                holder.totalTaxAmt.setText(filteredObjects.get(position)[15].equals("null")?"":adapterContext.getResources().getString(R.string.tax_amount,filteredObjects.get(position)[15]));
                holder.totalDiscountAmt.setText(filteredObjects.get(position)[16].equals("null")?"":adapterContext.getResources().getString(R.string.total_discount,filteredObjects.get(position)[16]));
                String totalAmountValue= String.format(Locale.US, "%.2f",
                                    Double.parseDouble(filteredObjects.get(position)[14].equals("null")?"0.0":filteredObjects.get(position)[14])
                                    + Double.parseDouble(filteredObjects.get(position)[15].equals("null")?"0.0":filteredObjects.get(position)[15])
                                    - Double.parseDouble(filteredObjects.get(position)[16].equals("null")?"0.0":filteredObjects.get(position)[16]));
                holder.totalAmount.setText(adapterContext.getResources().getString(R.string.total_amount,totalAmountValue));
                break;
            //--------------------------for reviews list items------------------
            case "CustomerReviewList"://{"ID","Review","ReviewCreatedDate","IsApproved"};
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.item_review, null);
                    holder.customerName = (TextView) convertView.findViewById(R.id.customer_name);
                    holder.customerImage=(ImageView) convertView.findViewById(R.id.customer_image);
                    holder.reviewDate = (TextView) convertView.findViewById(R.id.date);
                    holder.review = (TextView) convertView.findViewById(R.id.review);
                    holder.ratingReview = (MaterialRatingBar) convertView.findViewById(R.id.avg_rating_bar);
                    holder.isApproved=(TextView)convertView.findViewById(R.id.is_approved);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.customerName.setVisibility(View.GONE);
                holder.customerImage.setVisibility(View.GONE);
                holder.reviewDate.setText(filteredObjects.get(position)[2]);
                holder.review.setText(filteredObjects.get(position)[1]);
                holder.ratingReview.setVisibility(View.GONE);
                holder.isApproved.setVisibility(View.VISIBLE);
                if(Boolean.parseBoolean(filteredObjects.get(position)[3]))
                {
                    holder.isApproved.setText(adapterContext.getResources().getString(R.string.approved));
                }
                else {
                    holder.isApproved.setText(adapterContext.getResources().getString(R.string.approval_pending));
                }
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
