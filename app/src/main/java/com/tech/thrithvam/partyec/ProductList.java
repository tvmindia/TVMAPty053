package com.tech.thrithvam.partyec;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    CardView filterMenu;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getExtras().getString("CategoryName"));
        loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        productsAndNavigationRelativeView=(RelativeLayout)findViewById(R.id.products_and_categories);
        allProductsRelativeView=(RelativeLayout)findViewById(R.id.all_products);
        filterMenu=(CardView)findViewById(R.id.filter_menu_card);
        allProductsRelativeView.setVisibility(View.GONE);

        //horizontal initial products------------------------
        initialProductsHorizontal=(LinearLayout)findViewById(R.id.initial_products_horizontal);
        getTopProductsFromServer();


        //Navigation Category Listing-----------------------------
        navigationCategoryListView=(ListView)findViewById(R.id.navigation_category_listview);


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

    void getTopProductsFromServer(){
        //Threading--------------------------------------------------
        String webService="api/category/GetCategoryMainPageItems";
        String postData =  "{\"ID\":\""+getIntent().getExtras().getString("CategoryCode")+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                JSONObject jsonRootObject;
                try {
                    jsonRootObject = new JSONObject(common.json);
                    JSONArray jsonArray =jsonRootObject.optJSONArray("Products");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String[] data = new String[3];
                        data[0] = jsonObject.optString("Name");
                        data[1] = jsonObject.optString("ImageURL");
                        data[2] = jsonObject.optString("ID");
                        initialProducts.add(data);
                        initialProductsHorizontal(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(initialProducts.size()==0) (findViewById(R.id.view_all)).setVisibility(View.GONE);
                //Displaying sub categories-------
                getSubCategories();
            }
        };
        common.AsynchronousThread(ProductList.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void initialProductsHorizontal(int i){
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View productItem=inflater.inflate(R.layout.item_product_grid, null);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);
        params.setMargins(5,5,5,5);
        productItem.setLayoutParams(params);
        ((TextView)(productItem.findViewById(R.id.product_name))).setText(initialProducts.get(i)[0]);
        common.LoadImage(ProductList.this,(ImageView)(productItem.findViewById(R.id.product_image)),initialProducts.get(i)[1],R.drawable.dim_icon);
        (productItem.findViewById(R.id.dim_icon)).setVisibility(View.GONE);
        initialProductsHorizontal.addView(productItem);
    }

    void getSubCategories(){
        JSONObject jsonRootObject;
        try {
            jsonRootObject = new JSONObject(common.json);
            JSONArray jsonArray =jsonRootObject.optJSONArray("SubCategories");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String[] data = new String[3];
                data[0] = jsonObject.optString("ID");
                data[1] = jsonObject.optString("Name");
                data[2] = jsonObject.optString("ChildrenCount");
                navigationCategories.add(data);
            }
            CustomAdapter adapterNavCats=new CustomAdapter(ProductList.this, navigationCategories,"NavigationCategoryList");
            navigationCategoryListView.setAdapter(adapterNavCats);
            navigationCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent=new Intent(ProductList.this,ProductList.class);
                    intent.putExtra("CategoryCode",navigationCategories.get(position)[0]);
                    intent.putExtra("CategoryName",navigationCategories.get(position)[1]);
                    startActivity(intent);
                    finish();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void viewAll(View view){
        menu.setGroupVisible(R.id.search_n_filter,true);
        navigationCategoryListView.animate()
                .translationX(navigationCategoryListView.getWidth())
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

                            CustomAdapter adapterAllProducts=new CustomAdapter(ProductList.this, allProducts,"AllProducts",0);
                            allProductsGrid.setAdapter(adapterAllProducts);

                            productsAndNavigationRelativeView.setVisibility(View.GONE);
                            allProductsRelativeView.setVisibility(View.VISIBLE);

                        }
                    });

        //Filter menu------------------------------------------------------------------
        LinearLayout filterMenuLinear=(LinearLayout)findViewById(R.id.filter_menu_linear);

        String[] fData1=new String[2];fData1[0]="Chocolate Flavour";fData1[1]="12";
        String[] fData2=new String[2];fData2[0]="Cheese";fData2[1]="13";
        String[] fData3=new String[2];fData3[0]="Honey";fData3[1]="14";

        filterCategories.add(fData1);filterCategories.add(fData2);filterCategories.add(fData3);

        for(int i=0;i<filterCategories.size();i++){
            CheckBox checkBox= new CheckBox(ProductList.this);
            checkBox.setText(filterCategories.get(i)[0]);
            filterMenuLinear.addView(checkBox);
        }
        //Divider
        View divider = new View(this);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(Color.GRAY);
        divider.setPadding(0,7,0,7);
        filterMenuLinear.addView(divider);

        //navigation categories
        for(int i=0;i<navigationCategories.size();i++){
            TextView textView= new TextView(ProductList.this);
            textView.setText(navigationCategories.get(i)[1]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.setTextColor(getResources().getColor(R.color.colorAccent,null));
            }
            else {
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
            }
            textView.setPadding(7,10,7,10);
            final int final_i=i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(ProductList.this,ProductList.class);
                    intent.putExtra("CategoryCode",navigationCategories.get(final_i)[0]);
                    intent.putExtra("CategoryName",navigationCategories.get(final_i)[1]);
                    startActivity(intent);
                    finish();
                }
            });
            filterMenuLinear.addView(textView);
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
    //------------------------------------------Action bar menu---------------------------------------
    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_category, menu);
        this.menu=menu;
        menu.setGroupVisible(R.id.search_n_filter,false);
        return true;
    }

    int viewState=0;//  0=view grid : 1=view horizontal : 2=view single
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Filter menu------------------------
        if (id == R.id.filter) {
            if(filterMenu.getVisibility()==View.VISIBLE){
                item.getIcon().clearColorFilter();
                filterMenu.setVisibility(View.GONE);
            }
            else {
                item.getIcon().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                filterMenu.setVisibility(View.VISIBLE);
            }
            return true;
        }
        //Changing views---------------------
        else if(id==R.id.change_view){
            if(viewState==0){
                item.setIcon(R.drawable.view_single);
                viewState++;
                CustomAdapter adapterAllProducts=new CustomAdapter(ProductList.this, allProducts,"AllProducts",1);
                allProductsGrid.setNumColumns(1);
                allProductsGrid.setAdapter(adapterAllProducts);
            }
            else if(viewState==1){
                item.setIcon(R.drawable.view_grid);
                viewState++;
                CustomAdapter adapterAllProducts=new CustomAdapter(ProductList.this, allProducts,"AllProducts",2);
                allProductsGrid.setNumColumns(1);
                allProductsGrid.setAdapter(adapterAllProducts);
            }
            else if(viewState==2){
                item.setIcon(R.drawable.view_horizontal);
                viewState=0;
                CustomAdapter adapterAllProducts=new CustomAdapter(ProductList.this, allProducts,"AllProducts",0);
                allProductsGrid.setNumColumns(2);
                allProductsGrid.setAdapter(adapterAllProducts);
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (filterMenu.getVisibility()==View.VISIBLE) {
                Rect outRect = new Rect();
                filterMenu.getGlobalVisibleRect(outRect);
                Rect outRect2=new Rect();
                toolbar.getGlobalVisibleRect(outRect2);
                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY())
                        &&
                        !outRect2.contains((int)event.getRawX(), (int)event.getRawY()))
                    filterMenu.setVisibility(View.GONE);
            }
        }
        return super.dispatchTouchEvent(event);
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
