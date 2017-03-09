package com.tech.thrithvam.partyec;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ProductList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    LinearLayout initialProductsHorizontal;
    ArrayList<String[]> initialProducts=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //horizontal initial products------------------------
        initialProductsHorizontal=(LinearLayout)findViewById(R.id.initial_products_horizontal);

        String[] pData1=new String[2];pData1[0]="Product A";pData1[1]="https://www.partyec.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/b/a/balthazar_3.jpg";
        String[] pData2=new String[2];pData2[0]="Product B";pData2[1]="https://www.partyec.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/l/e/le_sultan-02.jpg";
        String[] pData3=new String[2];pData3[0]="Product C";pData3[1]="https://www.partyec.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/c/h/chocolate-fudge_2.jpg";

        initialProducts.add(pData1);initialProducts.add(pData2);initialProducts.add(pData3);

        for(int i=0;i<initialProducts.size();i++){
            initialProductsHorizontal(i);
        }
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
        productItem.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1.0f));
        ((TextView)(productItem.findViewById(R.id.product_name))).setText(initialProducts.get(i)[0]);
        common.LoadImage(ProductList.this,(ImageView)(productItem.findViewById(R.id.product_image)),initialProducts.get(i)[1],R.drawable.dim_icon);
        initialProductsHorizontal.addView(productItem);
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
