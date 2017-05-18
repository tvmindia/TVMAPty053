package com.tech.thrithvam.partyec;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

public class MyProfile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //-----------------------------------------------------------------------------
        db=DatabaseHandler.getInstance(this);
        if(db.GetCustomerDetails("CustomerID")==null){
            Intent loginIntent=new Intent(this,Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
            return;
        }
        else {
            ((TextView)findViewById(R.id.name)).setText(db.GetCustomerDetails("Name"));
            ((TextView)findViewById(R.id.mob_no)).setText(db.GetCustomerDetails("Mobile"));
            ((TextView)findViewById(R.id.email)).setText(db.GetCustomerDetails("Email"));
        }
        //-----------------------------------------------------------------------------
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void editProfile(View view){
        //Display changes-------
        (findViewById(R.id.customer_details_linear)).setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        (findViewById(R.id.customer_edit_linear)).setVisibility(View.VISIBLE);
        ((EditText)findViewById(R.id.name_edit)).setText(((TextView)findViewById(R.id.name)).getText().toString());
        ((EditText)findViewById(R.id.mob_no_edit)).setText(((TextView)findViewById(R.id.mob_no)).getText().toString());
        ((EditText)findViewById(R.id.email_edit)).setText(((TextView)findViewById(R.id.email)).getText().toString());
    }
    public void proceedClickForUpdate(final View view){
        view.setVisibility(View.GONE);
        final EditText name=(EditText)findViewById(R.id.name_edit);
        final EditText mob=(EditText)findViewById(R.id.mob_no_edit);
        if(name.getText().length()==0||mob.getText().length()==0){
            Toast.makeText(this, R.string.give_valid, Toast.LENGTH_SHORT).show();
            return;
        }
        //Threading------------------------------------------------------------------------------------------------------
        String webService="/api/Customer/UpdateUser";
        String postData = "{\"ID\":\""+db.GetCustomerDetails("CustomerID")+"\",\"Name\":\"" + name.getText().toString() +"\",\"Mobile\":\""+mob.getText().toString()+ "\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator_proceed);
        String[] dataColumns={};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                db.UpdateCustomer(name.getText().toString(),mob.getText().toString());
                Intent profileIntent=new Intent(MyProfile.this,MyProfile.class);
                startActivity(profileIntent);
                finish();
            }
        };
        Runnable postThreadFailed=new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyProfile.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                view.setVisibility(View.VISIBLE);
            }
        };
        common.AsynchronousThread(MyProfile.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postThreadFailed);
    }
    public void manageAddresses(View view){
        Intent addressIntent=new Intent(this,ManageAddresses.class);
        startActivity(addressIntent);
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
