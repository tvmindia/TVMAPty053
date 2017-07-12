package com.tech.thrithvam.partyec;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Login extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    EditText emailInput;
    EditText name;
    EditText mob;
    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db=DatabaseHandler.getInstance(this);
        if(db.GetCustomerDetails("CustomerID")!=null){
            Intent profileIntent=new Intent(this,MyProfile.class);
            startActivity(profileIntent);
            finish();
        }
        emailInput =(EditText)findViewById(R.id.email);
        name=(EditText)findViewById(R.id.name);
        mob=(EditText)findViewById(R.id.mob_no);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void emailEnteredClick(final View view){
        emailInput.setText(emailInput.getText().toString().trim());
        if(emailInput.getText().toString().equals("") || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.getText().toString()).matches())
        {   emailInput.setError(getResources().getString(R.string.give_valid));
            return;}
        emailInput.setEnabled(false);
        emailInput.setInputType(InputType.TYPE_NULL);
        view.setVisibility(View.INVISIBLE);
        //Threading------------------------------------------------------------------------------------------------------
        String webService="/api/Customer/GetCustomerVerificationandOTP";
        String postData =  "{\"Email\":\"" + emailInput.getText().toString() + "\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={};//"IsUser","OTP","UserID","Name","Email","Image
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject= null;
                try {
                    jsonObject = new JSONObject(common.json);
                    Boolean isUser=jsonObject.optBoolean("IsUser");
                        UserVerification(jsonObject.getString("CustomerOTP"),isUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Runnable postThreadFailed=new Runnable() {
            @Override
            public void run() {
                    Common.toastMessage(Login.this,R.string.failed_try_again);
                    emailInput.setEnabled(true);
                    emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
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
        name.setText(name.getText().toString().trim());
        mob.setText(mob.getText().toString().trim());
        if(name.getText().toString().length()==0){
            name.setError(getResources().getString(R.string.give_valid));
        }
        else if(mob.getText().toString().length()==0){
            mob.setError(getResources().getString(R.string.give_valid));
        }
        else {
            name.setEnabled(false);name.setInputType(InputType.TYPE_NULL);
            mob.setEnabled(false);mob.setInputType(InputType.TYPE_NULL);
            view.setVisibility(View.GONE);

            inputNewAddress();
        }
    }
    public void UserVerification(final String otp, final Boolean isUser){
        final AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
        alert.setTitle(R.string.enter_otp);
        final EditText otpInput=new EditText(Login.this);
        otpInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        otpInput.setGravity(Gravity.CENTER_HORIZONTAL);
        alert.setView(otpInput);
        alert.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (otpInput.getText().toString().equals("")) {
                    UserVerification(otp,isUser);
                }
                else if(otpInput.getText().toString().equals(otp)){
                    if(isUser){
                        try {
                            JSONObject jsonObject = new JSONObject(common.json);
                            JSONObject customer=jsonObject.optJSONObject("Customer");
                            db.InsertCustomer(customer.optString("ID"),
                                                customer.optString("Name"),
                                                customer.optString("Email"),
                                                customer.optString("Mobile"),
                                                customer.optString("Gender"));
                        Intent intentUser = new Intent(Login.this, MyProfile.class);
                        intentUser.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        finish();
                        startActivity(intentUser);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        LinearLayout signUpForm = (LinearLayout) findViewById(R.id.signup_form);
                        signUpForm.setVisibility(View.VISIBLE);
                        TextView loginDescription = (TextView) findViewById(R.id.login_description);
                        loginDescription.setVisibility(View.GONE);
                    }
                }
                else {
                    Common.toastMessage(Login.this,R.string.otp_incorrect);
                    UserVerification(otp,isUser);
                }
            }
        });
        alert.setCancelable(false);
        alert.show();
    }
    void inputNewAddress(){
        final Common common1=new Common();
        final Common common2=new Common();
        final ArrayList<String> locations=new ArrayList<>();
        final ArrayList<String> countries=new ArrayList<>();
        final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Threading for locations--------------------------------------------------
        String webService="api/customer/GetShippingLocations";
        String postData =  "";
        final ProgressDialog progressDialog=new ProgressDialog(Login.this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);progressDialog.show();
        String[] dataColumns={"ID","Name"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                String[] notSelect={"",getResources().getString(R.string.not_selecting)};
                common1.dataArrayList.add(notSelect);
                for(int i=0;i<common1.dataArrayList.size();i++){
                    locations.add(common1.dataArrayList.get(i)[1]);
                }
                //Threading for countries--------------------------------------------------
                String webService="api/customer/GetCountries";
                String postData =  "";
                String[] dataColumns={"Code","Name"};
                Runnable postThread=new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0;i<common2.dataArrayList.size();i++){
                            countries.add(common2.dataArrayList.get(i)[1]);
                        }

                        //New address alert dialogue box---------------------------------
                        AlertDialog.Builder newAddressDialogue = new AlertDialog.Builder(Login.this);
                        newAddressDialogue.setIcon(R.drawable.user);
                        newAddressDialogue.setTitle(R.string.address);
                        final View newAddressView=inflater.inflate(R.layout.item_address_input, null);
                        ArrayAdapter locationAdapter = new ArrayAdapter<String>(Login.this, android.R.layout.simple_spinner_item, locations);
                        ArrayAdapter countryAdapter = new ArrayAdapter<String>(Login.this, android.R.layout.simple_spinner_item, countries);
                        Spinner locationSpinner=(Spinner) newAddressView.findViewById(R.id.location);
                        Spinner countrySpinner=(Spinner) newAddressView.findViewById(R.id.country);
                        locationSpinner.setAdapter(locationAdapter);
                        countrySpinner.setAdapter(countryAdapter);
                        newAddressDialogue.setView(newAddressView);

                        newAddressDialogue.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               /* name.setEnabled(true);name.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                                mob.setEnabled(true);mob.setInputType(InputType.TYPE_CLASS_PHONE);
                                (findViewById(R.id.signup_button)).setVisibility(View.VISIBLE);*/
                               registerUser(dialog,progressDialog,"");
                                dialog.dismiss();
                            }
                        });

                        newAddressDialogue.setPositiveButton(R.string.ok_button, null);
                        newAddressDialogue.setCancelable(false);
                        AlertDialog getAddress=newAddressDialogue.create();
                        getAddress.setOnShowListener(new DialogInterface.OnShowListener() {

                            @Override
                            public void onShow(final DialogInterface dialog) {
                                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                button.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        //New address--
                                        if(((EditText)newAddressView.findViewById(R.id.first_name)).getText().toString().length()==0){
                                            ((EditText)newAddressView.findViewById(R.id.first_name)).setError(getResources().getString(R.string.give_valid));
                                        }
                                        else if(((EditText)newAddressView.findViewById(R.id.address)).getText().toString().length()==0){
                                            ((EditText)newAddressView.findViewById(R.id.address)).setError(getResources().getString(R.string.give_valid));
                                        }
                                        else if(((EditText)newAddressView.findViewById(R.id.city)).getText().toString().length()==0){
                                            ((EditText)newAddressView.findViewById(R.id.city)).setError(getResources().getString(R.string.give_valid));
                                        }
                                        else if(((EditText)newAddressView.findViewById(R.id.stateprovince)).getText().toString().length()==0){
                                            ((EditText)newAddressView.findViewById(R.id.stateprovince)).setError(getResources().getString(R.string.give_valid));
                                        }
                                        else if(((EditText)newAddressView.findViewById(R.id.contact_no)).getText().toString().length()==0){
                                            ((EditText)newAddressView.findViewById(R.id.contact_no)).setError(getResources().getString(R.string.give_valid));
                                        }
                                        else {
                                            String customerAddressJSON = "\"customerAddress\":{";
                                            customerAddressJSON+="\"ID\":\"" + 0 + "\"," +
                                                    "\"CustomerID\":\"" + 0 + "\"," +
                                                    "\"Prefix\":\"" + ((EditText)newAddressView.findViewById(R.id.prefix)).getText().toString() + "\"," +
                                                    "\"FirstName\":\"" + ((EditText)newAddressView.findViewById(R.id.first_name)).getText().toString() + "\"," +
                                                    "\"MidName\":\"" + ((EditText)newAddressView.findViewById(R.id.mid_name)).getText().toString() + "\"," +
                                                    "\"LastName\":\"" + ((EditText)newAddressView.findViewById(R.id.last_name)).getText().toString() + "\"," +
                                                    "\"Address\":\"" + ((EditText)newAddressView.findViewById(R.id.address)).getText().toString() + "\"," +
                                                    "\"LocationID\":\"" + common1.dataArrayList.get(((Spinner)newAddressView.findViewById(R.id.location)).getSelectedItemPosition())[0] + "\"," +
                                                    "\"City\":\"" + ((EditText)newAddressView.findViewById(R.id.city)).getText().toString() + "\"," +
                                                    "\"CountryCode\":\"" + common2.dataArrayList.get(((Spinner)newAddressView.findViewById(R.id.country)).getSelectedItemPosition())[0] + "\"," +
                                                    "\"StateProvince\":\"" + ((EditText)newAddressView.findViewById(R.id.stateprovince)).getText().toString() + "\"," +
                                                    "\"ContactNo\":\"" + ((EditText)newAddressView.findViewById(R.id.contact_no)).getText().toString() + "\"}" ;
                                            registerUser(dialog, progressDialog,customerAddressJSON);
                                        }
                                    }
                                });
                            }
                        });
                        getAddress.show();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                };
                Runnable postFailThread=new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        name.setEnabled(true);name.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                        mob.setEnabled(true);mob.setInputType(InputType.TYPE_CLASS_PHONE);
                        (findViewById(R.id.signup_button)).setVisibility(View.VISIBLE);
                        Common.toastMessage(Login.this,R.string.some_error_at_server);
                    }
                };
                common2.AsynchronousThread(Login.this,
                        webService,
                        postData,
                        null,
                        dataColumns,
                        postThread,
                        postFailThread);

            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                name.setEnabled(true);name.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                mob.setEnabled(true);mob.setInputType(InputType.TYPE_CLASS_PHONE);
                (findViewById(R.id.signup_button)).setVisibility(View.VISIBLE);
                Common.toastMessage(Login.this,R.string.some_error_at_server);
            }
        };
        common1.AsynchronousThread(Login.this,
                webService,
                postData,
                null,
                dataColumns,
                postThread,
                postFailThread);
    }
    void registerUser(final DialogInterface dialog, final ProgressDialog progressDialog, String customerAddressJSON){
        final Common common3=new Common();
        //Threading--------------------------------------------------
        String webService = "api/Customer/RegisterUser";

        String postData = "{\"Email\":\"" + emailInput.getText().toString()
                + "\",\"Name\":\"" + name.getText().toString()
                + "\",\"Mobile\":\"" + mob.getText().toString()
                + "\",\"Gender\":\"" + (((RadioGroup)findViewById(R.id.gender)).getCheckedRadioButtonId()==R.id.radio_male?"Male":"Female")
                + "\"," + customerAddressJSON
                + "}";
        progressDialog.show();
        String[] dataColumns = {};
        Runnable postThread = new Runnable() {
            @Override
            public void run() {
                //Displaying
                try {
                    JSONObject jsonObject=new JSONObject(common3.json);
                    String customerID=jsonObject.optString("ReturnValues");
                    db.InsertCustomer(customerID,
                            name.getText().toString(),
                            emailInput.getText().toString(),
                            mob.getText().toString(),
                            (((RadioGroup)findViewById(R.id.gender)).getCheckedRadioButtonId()==R.id.radio_male?"Male":"Female"));
                    Intent intentUser = new Intent(Login.this, MyProfile.class);
                    intentUser.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    finish();
                    startActivity(intentUser);
                    dialog.dismiss();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (JSONException e) {
                    Common.toastMessage(Login.this,R.string.some_error_at_server);
                }
            }
        };
        Runnable postFailThread = new Runnable() {
            @Override
            public void run() {
                Common.toastMessage(Login.this,R.string.failed_try_again);
                name.setEnabled(true);name.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                mob.setEnabled(true);mob.setInputType(InputType.TYPE_CLASS_PHONE);
                (findViewById(R.id.signup_button)).setVisibility(View.VISIBLE);
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        };
        common3.AsynchronousThread(Login.this,
                webService,
                postData,
                null,
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Common.NavigationBarItemClick(this,item);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
