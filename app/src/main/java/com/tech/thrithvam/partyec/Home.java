package com.tech.thrithvam.partyec;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
Common common=new Common();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        common.NavigationBarHeaderClick(this,navigationView);
    }
    public void registerEvent(View view){
        Intent intent=new Intent(this,RegisterEvent.class);
        startActivity(intent);
    }
    public void shopByCategory(View view){
        Intent intent=new Intent(this,CategoryList.class);
        intent.putExtra("from","shopByCategory");
        startActivity(intent);
    }
    public void shopByOccasion(View view){
        Intent intent=new Intent(this,CategoryList.class);
        intent.putExtra("from","shopByOccasion");
        startActivity(intent);
    }
    public void offers(View view){
        Intent intent=new Intent(this,CategoryList.class);
        intent.putExtra("from","offers");
        startActivity(intent);
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
    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        this.menu=menu;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            menu.getItem(0).getIcon().setColorFilter(getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }else {
            menu.getItem(0).getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }
        return true;
    }

    @Override
    protected void onResume() {//To avoid recoloring of menu icon
        super.onResume();
        if(menu!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                menu.getItem(0).getIcon().setColorFilter(getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            } else {
                menu.getItem(0).getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Filter menu------------------------
        if (id == R.id.menu_cart) {
            Intent intent=new Intent(this,Cart.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Common common=new Common();
        common.NavigationBarItemClick(this,item);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
