package com.tech.thrithvam.partyec;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoryList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    SearchView searchView;
    ListView categoryListView;
    CustomAdapter adapter;
    ArrayList<AsyncTask> asyncTasks=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        categoryListView=(ListView)findViewById(R.id.category_listview);
        if(getIntent().hasExtra("from")){
            switch (getIntent().getExtras().getString("from")){
                case "shopByCategory":
                    getSupportActionBar().setTitle(R.string.shop_by_category);
                    shopByCategory();
                    break;
                case "shopByOccasion":
                    getSupportActionBar().setTitle(R.string.shop_by_occasion);
                    shopByOccasion();
                    break;
                case "offers":
                    getSupportActionBar().setTitle(R.string.offers);
                    offers();
                    break;
            }
        }
        else {
            finish();return;
        }




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Common.NavigationBarHeaderClick(this,navigationView);
    }

    void shopByCategory(){
        //Threading------------------------------------------------------------------------------------------------------
        String webService="api/category/GetMainCategories";
        String postData =  "";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"URL","Name","ID"};//Order Matters. Data in the common.dataArrayList will be in same order
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                adapter=new CustomAdapter(CategoryList.this, common.dataArrayList,"CategoryList");
                categoryListView.setAdapter(adapter);
                categoryListView.setVisibility(View.VISIBLE);
                categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(CategoryList.this,ProductList.class);
                        intent.putExtra("CategoryCode",common.dataArrayList.get(position)[2]);
                        intent.putExtra("CategoryName",common.dataArrayList.get(position)[1]);
                        startActivity(intent);
                    }
                });
            }
        };
        common.AsynchronousThread(CategoryList.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
        asyncTasks.add(common.asyncTask);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }
    void shopByOccasion(){
        //Threading--------------------------------------------------
        String webService="api/category/GetCategoryMainPageItems";
        String postData =  "{\"ID\":\""+getResources().getString(R.string.occasion_code)+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                final ArrayList<String[]> subCategories=new ArrayList<>();
                JSONObject jsonRootObject;
                try {
                    jsonRootObject = new JSONObject(common.json);
                    JSONArray jsonArray =jsonRootObject.optJSONArray("SubCategories");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String[] data = new String[3];
                        data[0] = jsonObject.optString("URL");
                        data[1] = jsonObject.optString("Name");
                        data[2] = jsonObject.optString("ID");
                        subCategories.add(data);
                    }
                    adapter=new CustomAdapter(CategoryList.this, subCategories,"CategoryList");
                    categoryListView.setAdapter(adapter);
                    categoryListView.setVisibility(View.VISIBLE);
                    categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent=new Intent(CategoryList.this,ProductList.class);
                            intent.putExtra("CategoryCode",subCategories.get(position)[2]);
                            intent.putExtra("CategoryName",subCategories.get(position)[1]);
                            startActivity(intent);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        common.AsynchronousThread(CategoryList.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
        asyncTasks.add(common.asyncTask);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }
    void offers(){
        //Threading--------------------------------------------------
        String webService="api/category/GetCategoryMainPageItems";
        String postData =  "{\"ID\":\""+getResources().getString(R.string.offers_code)+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                final ArrayList<String[]> subCategories=new ArrayList<>();
                JSONObject jsonRootObject;
                try {
                    jsonRootObject = new JSONObject(common.json);
                    JSONArray jsonArray =jsonRootObject.optJSONArray("SubCategories");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String[] data = new String[3];
                        data[0] = jsonObject.optString("URL");
                        data[1] = jsonObject.optString("Name");
                        data[2] = jsonObject.optString("ID");
                        subCategories.add(data);
                    }
                    adapter=new CustomAdapter(CategoryList.this, subCategories,"CategoryList");
                    categoryListView.setAdapter(adapter);
                    categoryListView.setVisibility(View.VISIBLE);
                    categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent=new Intent(CategoryList.this,ProductList.class);
                            intent.putExtra("CategoryCode",subCategories.get(position)[2]);
                            intent.putExtra("CategoryName",subCategories.get(position)[1]);
                            startActivity(intent);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        common.AsynchronousThread(CategoryList.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
        asyncTasks.add(common.asyncTask);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            for(int i=0;i<asyncTasks.size();i++){
            asyncTasks.get(i).cancel(true);
            }
            if(isTaskRoot()){
                Intent intent=new Intent(this,Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_n_cart, menu);
        //Searching-------------------
        searchView=(SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Intent searchIntent=new Intent(CategoryList.this,Search.class);
                searchIntent.putExtra("searchkey",searchView.getQuery().toString().trim());
                startActivity(searchIntent);
                finish();
                return true;

            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(adapter!=null){//for searching in category names
                    adapter.getFilter(1).filter(searchView.getQuery().toString().trim());
                    TextView noItems=(TextView)findViewById(R.id.no_items);
                    noItems.setVisibility(categoryListView.getChildCount()>0?View.INVISIBLE:View.VISIBLE);
                }
               return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                TextView noItems=(TextView)findViewById(R.id.no_items);
                noItems.setVisibility(categoryListView.getChildCount()>0?View.INVISIBLE:View.VISIBLE);
                return false;
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_cart) {
            Intent intent=new Intent(this,Cart.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Common.NavigationBarItemClick(this,item);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
