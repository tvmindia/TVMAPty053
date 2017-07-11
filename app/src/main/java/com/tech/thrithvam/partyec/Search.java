package com.tech.thrithvam.partyec;

import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

public class Search extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SearchView searchView;
    ArrayList<AsyncTask> asyncTasks=new ArrayList<>();
    TextView enterToSearch;
    TextView noItems;
    GridView allProductsGrid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        enterToSearch=(TextView)findViewById(R.id.enter_to_search);
        noItems=(TextView)findViewById(R.id.no_items);
        allProductsGrid=(GridView) findViewById(R.id.all_products_grid);
        loadProducts(getIntent().getExtras().getString("searchkey"));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Common.NavigationBarHeaderClick(this,navigationView);
    }
    CustomAdapter adapterAllProducts;
    public void loadProducts(String searchKeys){
        noItems.setVisibility(View.GONE);
        enterToSearch.setVisibility(View.GONE);
        searchKeys=searchKeys.trim();
        if(searchKeys.length()==0)  return;
        searchKeys=searchKeys.replace(" ",",");
        //Threading--------------------------------------------------
        final Common common=new Common();
        String webService="api/product/ProductsGloablSearching";
        String postData =  "{\"filterCriteriaCSV\":\""+searchKeys+"\"}";
        AVLoadingIndicatorView loadingIndicator=(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        allProductsGrid.setVisibility(View.GONE);
        String[] dataColumns={"Name","ImageURL","ID","StickerURL","TotalPrice","DiscountAmount","StockAvailable","SupplierName"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                allProductsGrid.setVisibility(View.VISIBLE);
                adapterAllProducts=new CustomAdapter(Search.this, common.dataArrayList,"AllProducts",0);
                allProductsGrid.setAdapter(adapterAllProducts);
                allProductsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(Search.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[2]);
                        startActivity(intent);
                    }
                });
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                noItems.setVisibility(View.VISIBLE);
                enterToSearch.setVisibility(View.GONE);
            }
        };
        common.AsynchronousThread(Search.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postFailThread);
        asyncTasks.add(common.asyncTask);
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
            super.onBackPressed();
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
                loadProducts(searchView.getQuery().toString().trim());
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(adapterAllProducts!=null){
                    adapterAllProducts.getFilter(0).filter(searchView.getQuery().toString().trim());
                    if(allProductsGrid.getChildCount()>0){
                        enterToSearch.setVisibility(View.INVISIBLE);
                        noItems.setVisibility(View.GONE);
                    }
                    else{
                        enterToSearch.setVisibility(View.VISIBLE);
                        noItems.setVisibility(View.GONE);
                    }
                }
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if(allProductsGrid.getChildCount()>0){
                    enterToSearch.setVisibility(View.INVISIBLE);
                    noItems.setVisibility(View.GONE);
                }
                else{
                    enterToSearch.setVisibility(View.VISIBLE);
                    noItems.setVisibility(View.GONE);
                }
                return false;
            }
        });
        searchView.setQuery(getIntent().getExtras().getString("searchkey"),false);
        searchView.setIconifiedByDefault(false);
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
