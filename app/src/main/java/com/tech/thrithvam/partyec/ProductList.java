package com.tech.thrithvam.partyec;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Collections;

public class ProductList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    LinearLayout initialProductsHorizontal;
    ListView navigationCategoryListView;
    ArrayList<String[]> initialProducts=new ArrayList<>();
    ArrayList<String[]> navigationCategories=new ArrayList<>();
    AVLoadingIndicatorView loadingIndicator;
    ArrayList<String[]> allProducts=new ArrayList<>();
    ArrayList<String[]> filterCategories=new ArrayList<>();
    RelativeLayout productsAndNavigationRelativeView,allProductsRelativeView;
    GridView allProductsGrid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        productsAndNavigationRelativeView=(RelativeLayout)findViewById(R.id.products_and_categories);
        allProductsRelativeView=(RelativeLayout)findViewById(R.id.all_products);
        allProductsRelativeView.setVisibility(View.GONE);

        //horizontal initial products------------------------
        initialProductsHorizontal=(LinearLayout)findViewById(R.id.initial_products_horizontal);

        String[] pData1=new String[2];pData1[0]="Product A";pData1[1]="https://www.partyec.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/b/a/balthazar_3.jpg";
        String[] pData2=new String[2];pData2[0]="Product B";pData2[1]="https://www.partyec.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/l/e/le_sultan-02.jpg";
        String[] pData3=new String[2];pData3[0]="Product C";pData3[1]="https://www.partyec.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/c/h/chocolate-fudge_2.jpg";

        initialProducts.add(pData1);initialProducts.add(pData2);initialProducts.add(pData3);

        for(int i=0;i<initialProducts.size();i++){
            initialProductsHorizontal(i);
        }

        //Navigation Category Listing-----------------------------
        navigationCategoryListView=(ListView)findViewById(R.id.navigation_category_listview);

        String[] nData1=new String[2];nData1[0]="Birthday";nData1[1]="465";
        String[] nData2=new String[2];nData2[0]="Seasonal cakes";nData2[1]="95";
        String[] nData3=new String[2];nData3[0]="Corporate events";nData3[1]="64";

        navigationCategories.add(nData1);navigationCategories.add(nData2);navigationCategories.add(nData3);


        CustomAdapter adapterNavCats=new CustomAdapter(ProductList.this, navigationCategories,"NavigationCategoryList");
        navigationCategoryListView.setAdapter(adapterNavCats);
        loadingIndicator.setVisibility(View.GONE);

        //-------------------------------------------------------------------------------------------------
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        common.NavigationBarHeaderClick(this,navigationView);
    }

    void initialProductsHorizontal(int i){
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View productItem=inflater.inflate(R.layout.item_product, null);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);
        params.setMargins(5,5,5,5);
        productItem.setLayoutParams(params);
        ((TextView)(productItem.findViewById(R.id.product_name))).setText(initialProducts.get(i)[0]);
        common.LoadImage(ProductList.this,(ImageView)(productItem.findViewById(R.id.product_image)),initialProducts.get(i)[1],R.drawable.dim_icon);
        initialProductsHorizontal.addView(productItem);
    }

    public void viewAll(View view){
        navigationCategoryListView.animate()
                .translationY(navigationCategoryListView.getHeight())
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            navigationCategoryListView.setVisibility(View.GONE);
                            loadingIndicator.setVisibility(View.VISIBLE);

                            //All products and filter categories loading---------------------------------

                            allProductsGrid=(GridView) findViewById(R.id.all_products_grid);

                            for(int i=0;i<11;i++){
                            String[] pData1=new String[2];pData1[0]="Product A";pData1[1]="https://www.partyec.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/b/a/balthazar_3.jpg";
                            String[] pData2=new String[2];pData2[0]="Product B";pData2[1]="https://www.partyec.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/l/e/le_sultan-02.jpg";
                            String[] pData3=new String[2];pData3[0]="Product C";pData3[1]="https://www.partyec.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/c/h/chocolate-fudge_2.jpg";

                            allProducts.add(pData1);allProducts.add(pData2);allProducts.add(pData3);
                            }
                            Collections.shuffle(allProducts);

                            CustomAdapter adapterAllProducts=new CustomAdapter(ProductList.this, allProducts,"AllProducts");
                            allProductsGrid.setAdapter(adapterAllProducts);

                            productsAndNavigationRelativeView.setVisibility(View.GONE);
                            allProductsRelativeView.setVisibility(View.VISIBLE);

                        }
                    });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return true;
    }

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        common.NavigationBarItemClick(this,item);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
