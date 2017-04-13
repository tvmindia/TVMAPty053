package com.tech.thrithvam.partyec;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import static android.view.View.GONE;

public class ProductDetails extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    TextView actualPrice;
    String productID="4067";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        //Threading--------------------------------------------------
        String webService="api/product/GetProductDetails";
        String postData =  "{\"ID\":\""+productID+"\"}";
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

                    ((TextView)findViewById(R.id.supplier_name)).setText(jsonRootObject.getString("SupplierName"));

                    ((TextView)findViewById(R.id.short_description)).setText(jsonRootObject.getString("ShortDescription"));

                    if (jsonRootObject.optBoolean("StockAvailable")) {
                        ((TextView) findViewById(R.id.stock_availability)).setText(R.string.in_stock);
                        ((TextView) findViewById(R.id.stock_availability)).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                    else {
                        ((TextView) findViewById(R.id.stock_availability)).setText(R.string.out_of_stock);
                        ((TextView) findViewById(R.id.stock_availability)).setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    }

                    if(jsonRootObject.optBoolean("ShowPrice")) {
                        String priceString = String.format(Locale.US, "%.2f", jsonRootObject.optDouble("BaseSellingPrice")
                                + jsonRootObject.optDouble("PriceDifference")
                                - jsonRootObject.optDouble("DiscountAmount"));
                        ((TextView) findViewById(R.id.price)).setText(getString(R.string.price_display, priceString));
                        if (jsonRootObject.optDouble("DiscountAmount") != 0) {
                            String actualPriceString = String.format(Locale.US, "%.2f", jsonRootObject.optDouble("BaseSellingPrice")
                                    + jsonRootObject.optDouble("PriceDifference"));
                            ((TextView) findViewById(R.id.actual_price)).setText(getString(R.string.price_display, actualPriceString));
                        }
                        else {
                            (findViewById(R.id.actual_price)).setVisibility(GONE);
                        }
                    }
                    else {
                        (findViewById(R.id.price)).setVisibility(GONE);
                        (findViewById(R.id.actual_price)).setVisibility(GONE);
                    }

                    if(jsonRootObject.optBoolean("FreeDelivery"))
                        findViewById(R.id.free_delivery).setVisibility(View.VISIBLE);
                    else
                        findViewById(R.id.free_delivery).setVisibility(GONE);

                    switch (jsonRootObject.optString("ActionType")){
                        case "A":((Button)findViewById(R.id.action_button)).setText(R.string.buy);
                            break;
                        case "B":((Button)findViewById(R.id.action_button)).setText(R.string.book);
                            break;
                        case "Q":((Button)findViewById(R.id.action_button)).setText(R.string.req_quotation);
                            break;
                    }

                    ((WebView)findViewById(R.id.web_view_description)).loadData(jsonRootObject.optString("LongDescription"), "text/html; charset=UTF-8", null);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
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
