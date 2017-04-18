package com.tech.thrithvam.partyec;

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
