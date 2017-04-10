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
import android.view.Gravity;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static android.view.View.GONE;

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
        allProductsRelativeView.setVisibility(GONE);

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
                if(initialProducts.size()==0) (findViewById(R.id.view_all)).setVisibility(GONE);
                else (findViewById(R.id.view_all)).setVisibility(View.VISIBLE);
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
        (productItem.findViewById(R.id.dim_icon)).setVisibility(GONE);
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
        view.setEnabled(false);
        menu.setGroupVisible(R.id.search_n_filter,true);
        navigationCategoryListView.animate()
                .translationX(navigationCategoryListView.getWidth())
                .alpha(0.0f)
                .setDuration(700)
                .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            navigationCategoryListView.setVisibility(GONE);
                            loadingIndicator.setVisibility(View.VISIBLE);

                            //All products and filter categories loading---------------------------------
                            allProductsGrid=(GridView) findViewById(R.id.all_products_grid);

                            //Threading--------------------------------------------------
                            String webService="api/category/GetProductsOfCategory";
                            String postData =  "{\"ID\":\""+getIntent().getExtras().getString("CategoryCode")+"\"}";
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
                                            allProducts.add(data);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if(allProducts.size()==0) (findViewById(R.id.no_items)).setVisibility(View.VISIBLE);
                                    else (findViewById(R.id.no_items)).setVisibility(GONE);
                                    CustomAdapter adapterAllProducts=new CustomAdapter(ProductList.this, allProducts,"AllProducts",0);
                                    allProductsGrid.setAdapter(adapterAllProducts);
                                    productsAndNavigationRelativeView.setVisibility(GONE);
                                    allProductsRelativeView.setVisibility(View.VISIBLE);
                                    //Displaying filters-------
                                    setFilterMenu();
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
                    });
    }

    ArrayList<CheckBox> filterCheckBoxes=new ArrayList<>();
    void setFilterMenu(){
        LinearLayout filterMenuLinear=(LinearLayout)findViewById(R.id.filter_menu_linear);
        JSONObject jsonRootObject;
        try {
            jsonRootObject = new JSONObject(common.json);
            JSONArray jsonArray =jsonRootObject.optJSONArray("Filters");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String[] data = new String[2];
                data[0] = jsonObject.optString("ID");
                data[1] = jsonObject.optString("Name");
                filterCategories.add(data);
            }
            //FilterMenu
            for(int i=0;i<filterCategories.size();i++){
                CheckBox checkBox= new CheckBox(ProductList.this);
                checkBox.setText(filterCategories.get(i)[1]);
                filterCheckBoxes.add(checkBox);
                filterMenuLinear.addView(checkBox);
            }
            appliedFiltersBoolean=new boolean[filterCheckBoxes.size()];
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Apply Button
        if(filterCategories.size()>0){
            TextView filterApplyButton= new TextView(ProductList.this);
            filterApplyButton.setText(R.string.apply);
            filterApplyButton.setBackgroundResource(R.drawable.button);
            filterApplyButton.setTextColor(Color.WHITE);
            filterApplyButton.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            params.setMargins(0,0,0,5);
            params.gravity = Gravity.END;
            filterApplyButton.setPadding(7,3,7,3);
            filterApplyButton.setLayoutParams(params);
            filterApplyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterProducts();
                }
            });
            filterMenuLinear.addView(filterApplyButton);
        }
        //Divider
        if(filterCategories.size()>0 && navigationCategories.size()>0) {
            View divider = new View(this);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(lp);
            divider.setBackgroundColor(Color.GRAY);
            divider.setPadding(0, 10, 0, 7);
            filterMenuLinear.addView(divider);
        }

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
    boolean[] appliedFiltersBoolean;
    void filterProducts(){
        filterMenu.setVisibility(GONE);
        (findViewById(R.id.no_items)).setVisibility(View.GONE);
        allProductsGrid.setVisibility(View.VISIBLE);
        //Threading--------------------------------------------------
        String webService="api/category/GetProductsByFiltering";

        String filterCategoryCodes="";
        String filtersName="";
        Arrays.fill(appliedFiltersBoolean,false);
        for(int i=0;i<filterCheckBoxes.size();i++)
        {
            if(filterCheckBoxes.get(i).isChecked()){
                filterCategoryCodes+=filterCategories.get(i)[0]+",";
                filtersName+=filterCategories.get(i)[1]+",";
                appliedFiltersBoolean[i]=true;
            }
        }

        TextView filterNames=(TextView)findViewById(R.id.filter_names);
        if(filterCategoryCodes.equals("")){//No filters applied
            CustomAdapter adapterAllProducts=new CustomAdapter(ProductList.this, allProducts,"AllProducts",viewState);
            allProductsGrid.setAdapter(adapterAllProducts);
            filterNames.setVisibility(GONE);
            return;
        }
        filtersName=filtersName.substring(0,filtersName.lastIndexOf(","));
        filterNames.setText(filtersName);
        filterNames.setVisibility(View.VISIBLE);


        String postData =  "{\"filterCriteriaCSV\":\""+filterCategoryCodes+"\"}";
        String[] dataColumns={"Name","ImageURL","ID"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapterAllProducts=new CustomAdapter(ProductList.this, common.dataArrayList,"AllProducts",viewState);
                allProductsGrid.setAdapter(adapterAllProducts);
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.no_items)).setVisibility(View.VISIBLE);
                allProductsGrid.setVisibility(View.GONE);
            }
        };
        common.AsynchronousThread(ProductList.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postFailThread);
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
                filterMenu.setVisibility(GONE);
                if(appliedFiltersBoolean!=null) {
                    for (int i = 0; i < appliedFiltersBoolean.length; i++) {
                        if (appliedFiltersBoolean[i]) {
                            filterCheckBoxes.get(i).setChecked(true);
                        }
                    }
                }
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
                        !outRect2.contains((int)event.getRawX(), (int)event.getRawY())) {
                    filterMenu.setVisibility(GONE);
                    menu.findItem(R.id.filter).getIcon().clearColorFilter();
                    if (appliedFiltersBoolean != null) {
                        for (int i = 0; i < appliedFiltersBoolean.length; i++) {
                            if (appliedFiltersBoolean[i]) {
                                filterCheckBoxes.get(i).setChecked(true);
                            }
                        }
                    }
                }
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
