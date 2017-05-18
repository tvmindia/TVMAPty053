package com.tech.thrithvam.partyec;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import static android.view.View.GONE;

public class ProductDetails extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    DatabaseHandler db;
    TextView actualPrice;
    String productID;
    String productName;
    String attributeSetID;
    Boolean isFav=false;
    String actionType;
    String customerID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        productID=getIntent().getExtras().getString("productID");
        db=DatabaseHandler.getInstance(ProductDetails.this);
        customerID=db.GetCustomerDetails("CustomerID");
        actualPrice=(TextView)findViewById(R.id.actual_price);
        actualPrice.setPaintFlags(actualPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        loadProductDetails();
//------------------------------------------------------------------------------------------------------------------
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        common.NavigationBarHeaderClick(ProductDetails.this,navigationView);
    }
    void loadProductDetails(){
        (findViewById(R.id.product_details_scroll_view)).setVisibility(GONE);
        //Threading--------------------------------------------------
        String webService="api/product/GetProductDetails";
        String postData =  "{\"ID\":\""+productID+"\",\"CustomerID\":\""+customerID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.product_details_scroll_view).setVisibility(View.VISIBLE);
                JSONObject jsonRootObject;
                try {
                    jsonRootObject = new JSONObject(common.json);

                    getSupportActionBar().setTitle(jsonRootObject.getString("Name"));
                    ((TextView)findViewById(R.id.product_name)).setText(jsonRootObject.getString("Name"));
                    productName=jsonRootObject.getString("Name");

                    ((TextView)findViewById(R.id.supplier_name)).setText(jsonRootObject.getString("SupplierName").equals("null")?"-":jsonRootObject.getString("SupplierName"));

                    ((TextView)findViewById(R.id.short_description)).setText(jsonRootObject.getString("ShortDescription").equals("null")?"-":jsonRootObject.getString("ShortDescription"));

                    switch (jsonRootObject.optString("ActionType")){
                        case "A":((Button)findViewById(R.id.action_button)).setText(R.string.buy);
                            actionType="A";
                            break;
                        case "B":((Button)findViewById(R.id.action_button)).setText(R.string.book);
                            actionType="B";
                            break;
                        case "Q":((Button)findViewById(R.id.action_button)).setText(R.string.req_quotation);
                            actionType="Q";
                            break;
                    }

                    if(actionType.equals("A")) {// product that can buy
                        Button proceed=(Button)findViewById(R.id.action_button);
                        if (!jsonRootObject.optString("StockAvailable").equals("null")) {
                            if (jsonRootObject.optBoolean("StockAvailable")) {
                                ((TextView) findViewById(R.id.stock_availability)).setText(R.string.in_stock);
                                ((TextView) findViewById(R.id.stock_availability)).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                proceed.setEnabled(true);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    proceed.setBackgroundColor(getColor(R.color.colorPrimary));
                                }
                                else {
                                    proceed.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                }
                            } else {
                                ((TextView) findViewById(R.id.stock_availability)).setText(R.string.out_of_stock);
                                ((TextView) findViewById(R.id.stock_availability)).setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                proceed.setEnabled(false);
                                proceed.setBackgroundColor(Color.GRAY);
                            }
                        }

                        if (jsonRootObject.optBoolean("ShowPrice") && jsonRootObject.optDouble("BaseSellingPrice") != 0) {
                            String priceString = String.format(Locale.US, "%.2f",
                                    jsonRootObject.optDouble("BaseSellingPrice")
                                            + (jsonRootObject.optString("PriceDifference").equals("null") ? 0 : jsonRootObject.optDouble("PriceDifference"))
                                            - (jsonRootObject.optString("DiscountAmount").equals("null") ? 0 : jsonRootObject.optDouble("DiscountAmount")));
                            ((TextView) findViewById(R.id.price)).setText(getString(R.string.price_display, priceString));
                            if (jsonRootObject.optDouble("DiscountAmount") != 0) {
                                String actualPriceString = String.format(Locale.US, "%.2f",
                                        jsonRootObject.optDouble("BaseSellingPrice")
                                                + (jsonRootObject.optString("PriceDifference").equals("null") ? 0 : jsonRootObject.optDouble("PriceDifference")));
                                ((TextView) findViewById(R.id.actual_price)).setText(getString(R.string.price_display, actualPriceString));
                            } else {
                                (findViewById(R.id.actual_price)).setVisibility(GONE);
                            }
                        } else {
                            (findViewById(R.id.price)).setVisibility(GONE);
                            (findViewById(R.id.actual_price)).setVisibility(GONE);
                        }

                        if (jsonRootObject.optBoolean("FreeDelivery"))
                            findViewById(R.id.free_delivery).setVisibility(View.VISIBLE);
                        else
                            findViewById(R.id.free_delivery).setVisibility(GONE);
                    }
                    else {
                        (findViewById(R.id.price_n_stock)).setVisibility(GONE);
                        (findViewById(R.id.add_to_cart)).setVisibility(GONE);
                    }


                    if(!jsonRootObject.optString("LongDescription").equals("null")) {
                        ((WebView) findViewById(R.id.web_view_description)).loadData(jsonRootObject.optString("LongDescription"), "text/html; charset=UTF-8", null);
                    }
                    else {
                        (findViewById(R.id.web_view_description)).setVisibility(GONE);
                    }
                    attributeSetID=jsonRootObject.optString("AttributeSetID");

                    if(!jsonRootObject.optString("IsFav").equals("null")){
                        isFav=jsonRootObject.optBoolean("IsFav");
                        if(isFav){
                            ((ImageView)findViewById(R.id.is_fav_image)).setImageResource(R.drawable.wishlist_filled);
                        }
                        else {
                            ((ImageView)findViewById(R.id.is_fav_image)).setImageResource(R.drawable.wishlist);
                        }
                    }

                    JSONArray productOtherAttributes=jsonRootObject.optJSONArray("ProductOtherAttributes");
                    LinearLayout otherAttributes=(LinearLayout)findViewById(R.id.other_attributes_linear);
                    if(productOtherAttributes!=null){
                        for(int i=0;i<productOtherAttributes.length();i++){
                            JSONObject jsonObject=productOtherAttributes.getJSONObject(i);
                            String productAttribute=jsonObject.optString("Caption")+" : "+(jsonObject.optString("Value").equals("null")?"-":jsonObject.optString("Value"));
                            TextView attributeText=new TextView(ProductDetails.this);
                            attributeText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            attributeText.setText(productAttribute);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                attributeText.setTextColor(getColor(R.color.primary_text));
                            }
                            else {
                                attributeText.setTextColor(getResources().getColor(R.color.primary_text));
                            }
                            attributeText.setTextSize(14);
                            otherAttributes.addView(attributeText);
                        }
                    }
                    else {
                        otherAttributes.setVisibility(GONE);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                (findViewById(R.id.product_details_scroll_view)).setVisibility(View.VISIBLE);
                //Product Images Loading------------------
                loadProductImages();
                //Product rating loading--------------------
                loadProductRatings();
            }
        };
        common.AsynchronousThread(ProductDetails.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadProductImages(){
        final Common common=new Common();
        final AdapterViewFlipper imageSlides=(AdapterViewFlipper)findViewById(R.id.product_images);
        final ImageView nextImageButton=(ImageView)findViewById(R.id.next_image);
        final ImageView previousImageButton=(ImageView)findViewById(R.id.previous_image);
        final ArrayList<String[]> productImages=new ArrayList<>();
        //Threading--------------------------------------------------
        String webService="api/product/GetProductImages";
        String postData =  "{\"ID\":\""+productID+"\"}";
        String[] dataColumns={};
        AVLoadingIndicatorView imageLoadingIndicator=(AVLoadingIndicatorView)findViewById(R.id.image_loading);
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                JSONObject jsonRootObject;
                try {
                    jsonRootObject= new JSONObject(common.json);
                    JSONArray jsonArray =jsonRootObject.optJSONArray("Images");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String[] data = new String[2];
                        data[0] = jsonObject.optString("ID");
                        data[1] = jsonObject.optString("URL");
                        productImages.add(data);
                    }
                    CustomAdapter imagesAdapter=new CustomAdapter(ProductDetails.this,productImages,"ProductImages");
                    imageSlides.setAdapter(imagesAdapter);
                    imageSlides.setFlipInterval(2000);
                    imageSlides.startFlipping();
                    //Next and Previous buttons
                    if(productImages.size()>1){
                        nextImageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageSlides.stopFlipping();
                                imageSlides.showNext();
                            }
                        });
                        previousImageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageSlides.stopFlipping();
                                imageSlides.showPrevious();
                            }
                        });
                    }
                    else {
                        nextImageButton.setVisibility(GONE);
                        previousImageButton.setVisibility(GONE);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                imageSlides.setVisibility(GONE);
                nextImageButton.setVisibility(GONE);
                previousImageButton.setVisibility(GONE);
            }
        };
        common.AsynchronousThread(ProductDetails.this,
                webService,
                postData,
                imageLoadingIndicator,
                dataColumns,
                postThread,
                postFailThread);
    }
    void loadProductRatings(){
        final LinearLayout productRatingLinear=(LinearLayout)findViewById(R.id.ratings_linear);
        final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Threading--------------------------------------------------
        String webService="api/product/GetProductRatings";
        String postData =  "{\"ID\":\""+productID+"\",\"AttributeSetID\":\""+attributeSetID+"\"}";
        String[] dataColumns={};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                JSONObject jsonRootObject;
                JSONArray firstJSONArray;
                try {
                    firstJSONArray= new JSONArray(common.json);
                    jsonRootObject = firstJSONArray.getJSONObject(0);
                    JSONArray ratings=jsonRootObject.optJSONArray("ProductRatingAttributes");
                    float sum=0;int ratables=0;
                    for (int i=0;i<ratings.length();i++){
                        JSONObject jsonObject = ratings.getJSONObject(i);
                        View ratingBar=inflater.inflate(R.layout.item_rating_bar, null);
                        ((TextView)ratingBar.findViewById(R.id.rating_label)).setText(jsonObject.optString("Caption"));
                        ((RatingBar)ratingBar.findViewById(R.id.rating_bar)).setRating(Float.parseFloat(jsonObject.optString("Value")));
                        sum+=Float.parseFloat(jsonObject.optString("Value"));
                        ratables++;
                        LayerDrawable stars = (LayerDrawable) ((RatingBar)ratingBar.findViewById(R.id.rating_bar)).getProgressDrawable();
                        stars.getDrawable(2).setColorFilter(Color.parseColor("#FFF9DB01"), PorterDuff.Mode.SRC_ATOP);
                        productRatingLinear.addView(ratingBar);
                    }
                    if(ratables>0) {
                        float avg = sum / ratables;
                        ((RatingBar)findViewById(R.id.avg_rating_bar)).setRating(avg);
                        LayerDrawable stars = (LayerDrawable) ((RatingBar)findViewById(R.id.avg_rating_bar)).getProgressDrawable();
                        stars.getDrawable(2).setColorFilter(Color.parseColor("#FFF9DB01"), PorterDuff.Mode.SRC_ATOP);
                        findViewById(R.id.avg_rating_bar).setVisibility(View.VISIBLE);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                //Reviews
                loadProductReviews();
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                //do nothing
                //Load reviews
                loadProductReviews();
            }
        };
        common.AsynchronousThread(ProductDetails.this,
                webService,
                postData,
                null,
                dataColumns,
                postThread,
                postFailThread);
    }
    void loadProductReviews(){
        final LinearLayout productReviewsLinear=(LinearLayout)findViewById(R.id.reviews_linear);
        final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //divider
        View divider = new View(ProductDetails.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(15,0,15,0);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(Color.GRAY);
        divider.setPadding(0, 5, 0, 5);
        productReviewsLinear.addView(divider);
        //Threading--------------------------------------------------
        String webService="api/product/GetProductReviews";
        String postData =  "{\"ProductID\":\""+productID+"\",\"count\":\""+"5"+"\"}";
        String[] dataColumns={"ID","CustomerName","ImageUrl","AvgRating","ReviewCreatedDate","Review"};
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator_ball_pulse);
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                    for (int i=0;i<common.dataArrayList.size();i++) {
                        View review = inflater.inflate(R.layout.item_review, null);
                        ((TextView)review.findViewById(R.id.customer_name)).setText(common.dataArrayList.get(i)[1]);
                        ((TextView)review.findViewById(R.id.date)).setText(common.dataArrayList.get(i)[4]);
                        ((TextView)review.findViewById(R.id.review)).setText(common.dataArrayList.get(i)[5]);
                        ((RatingBar)review.findViewById(R.id.avg_rating_bar)).setRating(Float.parseFloat(common.dataArrayList.get(i)[3]));
                        LayerDrawable stars = (LayerDrawable) ((RatingBar)review.findViewById(R.id.avg_rating_bar)).getProgressDrawable();
                        stars.getDrawable(2).setColorFilter(Color.parseColor("#FFF9DB01"), PorterDuff.Mode.SRC_ATOP);
                        common.LoadImage(ProductDetails.this,
                                ((ImageView)review.findViewById(R.id.customer_image)),
                                common.dataArrayList.get(i)[2],
                                R.drawable.user);
                        productReviewsLinear.addView(review);
                        //divider
                        View divider = new View(ProductDetails.this);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        lp.setMargins(15,0,15,0);
                        divider.setLayoutParams(lp);
                        divider.setBackgroundColor(Color.GRAY);
//                        divider.setPadding(0, 5, 0, 5);
                        productReviewsLinear.addView(divider);
                    }
                (findViewById(R.id.view_all_reviews)).setVisibility(View.VISIBLE);
                (findViewById(R.id.view_all_reviews)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(ProductDetails.this, ListViewsActivity.class);
                        intent.putExtra("list","reviews");
                        intent.putExtra("productID",productID);
                        intent.putExtra("productName",productName);
                        startActivity(intent);
                    }
                });
                //Removing label if no reviews and ratings
                if(((LinearLayout)findViewById(R.id.ratings_linear)).getChildCount()==0 && ((LinearLayout)findViewById(R.id.reviews_linear)).getChildCount()<=1){
                    (findViewById(R.id.rating_reviews_label)).setVisibility(GONE);
                }

                //Load related products
                loadRelatedProducts();
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                //do nothing
                //Removing label if no reviews and ratings
                if(((LinearLayout)findViewById(R.id.ratings_linear)).getChildCount()==0 && ((LinearLayout)findViewById(R.id.reviews_linear)).getChildCount()<=1){
                    (findViewById(R.id.rating_reviews_label)).setVisibility(GONE);
                }

                //Load related products
                loadRelatedProducts();
            }
        };
        common.AsynchronousThread(ProductDetails.this,
                webService,
                postData,
                loadingIndicatorView,
                dataColumns,
                postThread,
                postFailThread);
    }
    void loadRelatedProducts(){
        final LinearLayout relatedLinear=(LinearLayout)findViewById(R.id.related_products_horizontal);
        //Threading--------------------------------------------------
        String webService="api/product/GetRelatedProducts";
        String postData =  "{\"ID\":\""+productID+"\",\"count\":\""+"3"+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator_ball_pulse2);
        String[] dataColumns={"ID","Name","ImageURL"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {

                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                for(int i=0;i<common.dataArrayList.size();i++) {
                    View productItem = inflater.inflate(R.layout.item_product_grid, null);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                    params.setMargins(5, 5, 5, 5);
                    productItem.setLayoutParams(params);
                    ((TextView) (productItem.findViewById(R.id.product_name))).setText(common.dataArrayList.get(i)[1]);
                    ((TextView) (productItem.findViewById(R.id.product_name))).setMaxLines(1);
                    ((TextView) (productItem.findViewById(R.id.product_name))).setEllipsize(TextUtils.TruncateAt.END);
                    ImageView relatedProductImage=(ImageView) productItem.findViewById(R.id.product_image);
                    relatedProductImage.getLayoutParams().height = 120;
                    common.LoadImage(ProductDetails.this, relatedProductImage ,getResources().getString(R.string.url)+common.dataArrayList.get(i)[2], R.drawable.dim_icon);
                    (productItem.findViewById(R.id.dim_icon)).setVisibility(GONE);
                    final int Fi=i;
                    productItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(ProductDetails.this,ProductDetails.class);
                            intent.putExtra("productID",common.dataArrayList.get(Fi)[0]);
                            startActivity(intent);
                            finish();
                        }
                    });
                    relatedLinear.addView(productItem);
                }
                (findViewById(R.id.view_all_related_items)).setVisibility(View.VISIBLE);
                (findViewById(R.id.view_all_related_items)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(ProductDetails.this, ListViewsActivity.class);
                        intent.putExtra("list","relatedItems");
                        intent.putExtra("productID",productID);
                        intent.putExtra("productName",productName);
                        startActivity(intent);
                    }
                });
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                //do nothing
                (findViewById(R.id.related_products_label)).setVisibility(GONE);
                relatedLinear.setVisibility(GONE);
            }
        };
        common.AsynchronousThread(ProductDetails.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postFailThread);
    }
    public void buyProduct(View view){
        if(db.GetCustomerDetails("CustomerID")!=null) {
            Intent intent = new Intent(ProductDetails.this, ProductOrdering.class);
            intent.putExtra("productID", productID);
            intent.putExtra("productName", productName);
            intent.putExtra("cartORbuy", "buy");
            intent.putExtra("actionType", actionType);
            startActivity(intent);
        }
        else {
            Intent loginIntent=new Intent(this,Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
        }
    }
    public void addToCart(View view){
        if(db.GetCustomerDetails("CustomerID")!=null) {
            Intent intent = new Intent(ProductDetails.this, ProductOrdering.class);
            intent.putExtra("productID", productID);
            intent.putExtra("productName", productName);
            intent.putExtra("cartORbuy", "cart");
            startActivity(intent);
        }
        else {
            Intent loginIntent = new Intent(this, Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
        }
    }
    public void toggleWishlist(View view){
        if(db.GetCustomerDetails("CustomerID")!=null) {
            if (isFav) {
                isFav = false;
                ((ImageView) view).setImageResource(R.drawable.wishlist);
            } else {
                isFav = true;
                ((ImageView) view).setImageResource(R.drawable.wishlist_filled);
            }
            final Common common = new Common();
            //Threading--------------------------------------------------
            String webService = "api/product/UpdateWishlist";
            String postData = "{\"CustomerID\":\"" + customerID + "\",\"ProductID\":\"" + productID + "\"}";
            String[] dataColumns = {};
            Runnable postThread = new Runnable() {
                @Override
                public void run() {
                    if (isFav) {
                        Toast.makeText(ProductDetails.this, R.string.added_to_wishlist, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetails.this, R.string.removed_from_wishlist, Toast.LENGTH_SHORT).show();
                    }
                }
            };
            Runnable postFailThread = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ProductDetails.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                }
            };
            common.AsynchronousThread(ProductDetails.this,
                    webService,
                    postData,
                    null,
                    dataColumns,
                    postThread,
                    postFailThread);
        }
        else {
            Intent loginIntent = new Intent(this, Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        common.NavigationBarItemClick(this,item);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
