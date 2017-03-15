package com.tech.thrithvam.partyec;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

public class Login extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    EditText mobileInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mobileInput=(EditText)findViewById(R.id.mob_no);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void mobileEnteredClick(final View view){
        mobileInput.setText(mobileInput.getText().toString().trim());
        mobileInput.setEnabled(false);mobileInput.setInputType(InputType.TYPE_NULL);
        view.setVisibility(View.INVISIBLE);
        //Threading------------------------------------------------------------------------------------------------------
        String webService="Webservices/document.asmx/userLogin";
        String postData =  "{\"mobile\":\"" + mobileInput.getText().toString() + "\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"IsUser","OTP","UserID","Name","Email","Image"};//Order Matters. Data in the common.dataArrayList will be in same order
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                if(common.dataArrayList.get(0)[0].equals("True")){
                    UserVerification(common.dataArrayList.get(0)[1]);
                }
                else {
                    LinearLayout signUpForm=(LinearLayout)findViewById(R.id.signup_form);
                    signUpForm.setVisibility(View.VISIBLE);
                    TextView loginDescription=(TextView)findViewById(R.id.login_description);
                    loginDescription.setVisibility(View.GONE);
                }
            }
        };
        Runnable postThreadFailed=new Runnable() {
            @Override
            public void run() {
                    Toast.makeText(Login.this, "Failed attempt. Please try again", Toast.LENGTH_SHORT).show();
                    mobileInput.setEnabled(true);mobileInput.setInputType(InputType.TYPE_CLASS_PHONE);
                    view.setVisibility(View.VISIBLE);
            }
        };
        common.AsynchronousThread(Login.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postThreadFailed);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }
    public void signUp(final View view){
        final EditText name=(EditText)findViewById(R.id.name);
        name.setEnabled(false);name.setInputType(InputType.TYPE_NULL);
        final EditText email=(EditText)findViewById(R.id.email);
        email.setEnabled(false);email.setInputType(InputType.TYPE_NULL);
        view.setVisibility(View.GONE);
        //Threading------------------------------------------------------------------------------------------------------
        String webService="Webservices/document.asmx/userRegistration";
        String postData =  "{\"mobile\":\"" + mobileInput.getText().toString() + "\",\"name\":\"" + name.getText().toString()+ "\",\"email\":\"" + email.getText().toString()+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator2);
        String[] dataColumns={"OTP"};//Order Matters. Data in the common.dataArrayList will be in same order
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Login.this,"Success",Toast.LENGTH_LONG).show();
                UserVerification(common.dataArrayList.get(0)[0]);
            }
        };
        Runnable postThreadFailed=new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Login.this, "Failed attempt. Please try again", Toast.LENGTH_SHORT).show();
                name.setEnabled(true);name.setInputType(InputType.TYPE_CLASS_PHONE);
                email.setEnabled(true);email.setInputType(InputType.TYPE_CLASS_PHONE);
                view.setVisibility(View.VISIBLE);
            }
        };
        common.AsynchronousThread(Login.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postThreadFailed);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }
    public void UserVerification(final String otp){
        Toast.makeText(Login.this,otp,Toast.LENGTH_LONG).show();
        AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
        alert.setTitle(R.string.enter_otp);
        final EditText otpInput=new EditText(Login.this);
        otpInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        otpInput.setGravity(Gravity.CENTER_HORIZONTAL);
        alert.setView(otpInput);
        alert.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (otpInput.getText().toString().equals("")) {
                    UserVerification(otp);
                }
                else if(otpInput.getText().toString().equals(otp)){
                    Toast.makeText(Login.this,"Matches",Toast.LENGTH_LONG).show();

                  /*  new UserActivation().execute();
                    db.UserLogin(userID,adres);*/
                    Intent intentUser = new Intent(Login.this, MyProfile.class);
                    finish();
                    startActivity(intentUser);
                }
                else {
                    Toast.makeText(Login.this,"Not Matching",Toast.LENGTH_LONG).show();
                    UserVerification(otp);
                }
            }
        });
        alert.setCancelable(false);
        alert.show();
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
