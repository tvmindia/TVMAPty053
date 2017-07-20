package com.tech.thrithvam.partyec;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.wang.avi.AVLoadingIndicatorView;

public class ContactUs extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    EditText name,email,phone,comment;
    String Name,Email,Phone,Comments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        name=(EditText)findViewById(R.id.name);
        email=(EditText)findViewById(R.id.email);
        phone=(EditText)findViewById(R.id.phone);
        comment=(EditText)findViewById(R.id.comment);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Common.NavigationBarHeaderClick(this,navigationView);
    }
    public void submitRequest(View view){
        if(name.getText().toString().length()==0 || !name.getText().toString().matches(common.UserNameRegularExpression)){
            name.setError(getResources().getString(R.string.give_valid));
            name.requestFocus();
        }
        else if( !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            email.setError(getResources().getString(R.string.give_valid));
            email.requestFocus();
        }
        else if(comment.getText().toString().length()==0){
            comment.setError(getResources().getString(R.string.give_valid));
            comment.requestFocus();
        }
        else{
            AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
            loadingIndicatorView.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
            sendContactUs();

        }
    }


    void sendContactUs(){
        Name= name.getText().toString();
        Email= email.getText().toString();
        Phone= phone.getText().toString();
        Comments= comment.getText().toString();
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/SendContactUsEmail";
        String postData =  "{\"Name\":\""+Name+"\",\"Email\":\""+Email+"\",\"Phone\":\""+Phone+"\",\"Comments\":\""+Comments+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                Common.toastMessage(ContactUs.this,R.string.contact_us_email);
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                Common.toastMessage(ContactUs.this,R.string.some_error_at_server);
                (findViewById(R.id.btn_send)).setVisibility(View.VISIBLE);
            }
        };
        common.AsynchronousThread(ContactUs.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postFailThread
                );
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Common.NavigationBarItemClick(this,item);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
