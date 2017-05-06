package com.tech.thrithvam.partyec;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wang.avi.AVLoadingIndicatorView;

public class ListViewsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_views);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        listView=(ListView)findViewById(R.id.listview);
        switch (getIntent().getExtras().getString("list")){
            case "reviews":
                loadProductReviews();
                break;
            case "relatedItems":
                loadRelatedProducts();
                break;
            case "wishlist":
                loadWishlistProducts();
                break;
            case "bookings":
                loadBookings();
                break;
            case "quotations":
                loadQuotations();
                break;
            default:
                finish();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        common.NavigationBarHeaderClick(ListViewsActivity.this,navigationView);
    }
    void loadProductReviews(){
        getSupportActionBar().setTitle("Reviews: "+getIntent().getExtras().getString("productName"));
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/product/GetProductReviews";
        String postData =  "{\"ProductID\":\""+getIntent().getExtras().getString("productID")+"\",\"count\":\""+"-1"+"\"}";
        String[] dataColumns={"ID","CustomerName","ImageUrl","AvgRating","ReviewCreatedDate","Review"};
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"ReviewList");
                listView.setAdapter(adapter);
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicatorView,
                dataColumns,
                postThread,
                null);
    }
    void loadRelatedProducts(){
        getSupportActionBar().setTitle("Related Items: "+getIntent().getExtras().getString("productName"));
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/product/GetRelatedProducts";
        String postData =  "{\"ID\":\""+getIntent().getExtras().getString("productID")+"\",\"count\":\""+"-1"+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"ID","Name","ImageURL"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"RelatedItemsList");
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(ListViewsActivity.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[0]);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadWishlistProducts(){
        getSupportActionBar().setTitle("WishList Items ");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerWishlist";
        String postData =  "{\"CustomerID\":\""+1009+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"ProductID","ProductName","ImageURL","DaysinWL","Price"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"WishList");
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(ListViewsActivity.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[0]);
                        startActivity(intent);
                    }
                });
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadBookings(){
        getSupportActionBar().setTitle("Bookings ");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerBookings";
        String postData =  "{\"CustomerID\":\""+1009+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"BookingNo","ProductID","RequiredDate","BookingDate","StatusText","ProductName","ImageUrl"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"Bookings");
                listView.setAdapter(adapter);
                /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(ListViewsActivity.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[1]);
                        startActivity(intent);
                    }
                });*/
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadQuotations(){
        getSupportActionBar().setTitle("Quotations");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerQuotations";
        String postData =  "{\"CustomerID\":\""+1009+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"QuotationNo","ProductID","RequiredDate","QuotationDate","StatusText","ProductName","ImageUrl"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"Quotations");
                listView.setAdapter(adapter);
               /* listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(ListViewsActivity.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[1]);
                        startActivity(intent);
                    }
                });*/
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
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
