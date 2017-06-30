package com.tech.thrithvam.partyec;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ManageAddresses extends AppCompatActivity {
    DatabaseHandler db;
    String customerID;
    LayoutInflater inflater;
    String from="";
    String SHIPPING_ADDRESS_ID="";
    String BILLING_ADDRESS_ID="";
    ListView addressList;
    ArrayList<AsyncTask> asyncTasks=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_addresses);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.manage_addresses);
        db=DatabaseHandler.getInstance(this);
        if(db.GetCustomerDetails("CustomerID")==null) {
            Intent loginIntent=new Intent(this,Login.class);
            Common.toastMessage(ManageAddresses.this,R.string.please_login);
            startActivity(loginIntent);
            finish();
            return;
        }
        customerID=db.GetCustomerDetails("CustomerID");
        addressList=(ListView)findViewById(R.id.address_list_view);
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        getCustomerAddresses();
        if(getIntent().getExtras().getString("from").equals("my_profile")){
            from="my_profile";
            addressList.setSelector(new StateListDrawable());
        }
        else {
            from="cart";
            SHIPPING_ADDRESS_ID=getIntent().getExtras().getString("shipping_address_id");
            BILLING_ADDRESS_ID=getIntent().getExtras().getString("billing_address_id");
        }
    }
    ArrayList<CustomerAddress> addresses;
    void getCustomerAddresses(){
        addresses=new ArrayList<>();
        final Common common=new Common();
        //Threading--------------------------------------------------
        String webService="api/customer/GetCustomerAddress";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
        String[] dataColumns={"ID",//0
                "Prefix",//1
                "FirstName",//2
                "MidName",//3
                "LastName",//4
                "Address",//5
                "Location",//6
                "City",//7
                "StateProvince",//8
                "country",//9
                "ContactNo",//10
                "BillDefaultYN",//11
                "ShipDefaultYN",//12
                "LocationID"//13
        };
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter customAdapter;
                if(from.equals("my_profile")){
                    customAdapter=new CustomAdapter(ManageAddresses.this,common.dataArrayList,"AddressManagement");
                }
                else {
                    customAdapter=new CustomAdapter(ManageAddresses.this,common.dataArrayList,"AddressSelection");
                }
                addressList.setAdapter(customAdapter);
                (findViewById(R.id.no_address_label)).setVisibility(View.GONE);
                addressList.setVisibility(View.VISIBLE);
                for (int i=0;i<common.dataArrayList.size();i++){
                    CustomerAddress customerAddress=new CustomerAddress();
                    customerAddress.ID=(common.dataArrayList.get(i)[0].equals("null")?"":common.dataArrayList.get(i)[0]);
                    customerAddress.CustomerID=customerID;
                    customerAddress.Prefix=(common.dataArrayList.get(i)[1].equals("null")?"":common.dataArrayList.get(i)[1]);
                    customerAddress.FirstName=(common.dataArrayList.get(i)[2].equals("null")?"":common.dataArrayList.get(i)[2]);
                    customerAddress.MidName=(common.dataArrayList.get(i)[3].equals("null")?"":common.dataArrayList.get(i)[3]);
                    customerAddress.LastName=(common.dataArrayList.get(i)[4].equals("null")?"":common.dataArrayList.get(i)[4]);
                    customerAddress.Address=(common.dataArrayList.get(i)[5].equals("null")?"":common.dataArrayList.get(i)[5]);
                    customerAddress.LocationID=(common.dataArrayList.get(i)[13].equals("null")?"":common.dataArrayList.get(i)[13]);
                    customerAddress.City=(common.dataArrayList.get(i)[7].equals("null")?"":common.dataArrayList.get(i)[7]);
                    String countryCode="";
                    try {
                        JSONObject jsonObjectCountry=new JSONObject(common.dataArrayList.get(i)[9]);
                        countryCode=jsonObjectCountry.getString("Code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    customerAddress.CountryCode=countryCode;
                    customerAddress.StateProvince=(common.dataArrayList.get(i)[8].equals("null")?"":common.dataArrayList.get(i)[8]);
                    customerAddress.ContactNo=(common.dataArrayList.get(i)[10].equals("null")?"":common.dataArrayList.get(i)[10]);
                    addresses.add(customerAddress);
                }
                //If cart functions
              /*  if(from.equals("cart")){
                    for (int i=0;i<addresses.size();i++){
                        if(addresses.get(i).ID.equals(SHIPPING_ADDRESS_ID)){
//                            View addressView=addressList.getChildAt(i);
//                            (addressView.findViewById(R.id.select_address)).setVisibility(View.GONE);
                            addressList.setItemChecked(i,true);
                            break;
                        }
                        else if(addresses.get(i).ID.equals(BILLING_ADDRESS_ID)){
                            break;
                        }
                    }
                }*/
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject= null;
                try {
                    jsonObject = new JSONObject(common.json);
                    String message=jsonObject.optString("Message");
                    if(message.equals("No items")) {
                        //No address for this customer
                        (findViewById(R.id.no_address_label)).setVisibility(View.VISIBLE);
                        addressList.setVisibility(View.GONE);
                    }
                    else {
                        Common.toastMessage(ManageAddresses.this,R.string.some_error_at_server);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        common.AsynchronousThread(ManageAddresses.this,
                webService,
                postData,
                loadingIndicatorView,
                dataColumns,
                postThread,
                postFailThread);
        asyncTasks.add(common.asyncTask);
    }
    public void setDefault(View view){
        if(((TextView)view).getText().toString().equals(getResources().getString(R.string.default_address))){
            return;
        }
        Common common=new Common();
        //Threading--------------------------------------------------
        String webService="api/customer/SetDefaultAddress";
        String postData =  "{\"ID\":\""+view.getTag()+"\",\"CustomerID\":\""+db.GetCustomerDetails("CustomerID")+"\"}";
        String[] dataColumns={};
        final ProgressDialog progressDialog=new ProgressDialog(ManageAddresses.this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);progressDialog.show();
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                getCustomerAddresses();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                Common.toastMessage(ManageAddresses.this,R.string.some_error_at_server);
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        };
        common.AsynchronousThread(ManageAddresses.this,
                webService,
                postData,
                null,
                dataColumns,
                postThread,
                postFailThread);
    }
    public void removeAddress(final View view){
        new AlertDialog.Builder(ManageAddresses.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle(R.string.exit)
                .setMessage(R.string.remove_address_q)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                                Common common=new Common();
                                //Threading--------------------------------------------------
                                String webService="api/customer/DeleteCustomerAddress";
                                String postData =  "{\"ID\":\""+view.getTag()+"\",\"CustomerID\":\""+db.GetCustomerDetails("CustomerID")+"\"}";
                                String[] dataColumns={};
                                final ProgressDialog progressDialog=new ProgressDialog(ManageAddresses.this);
                                progressDialog.setMessage(getResources().getString(R.string.please_wait));
                                progressDialog.setCancelable(false);progressDialog.show();
                                Runnable postThread=new Runnable() {
                                    @Override
                                    public void run() {
                                        getCustomerAddresses();
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    }
                                };
                                Runnable postFailThread=new Runnable() {
                                    @Override
                                    public void run() {
                                        Common.toastMessage(ManageAddresses.this,R.string.some_error_at_server);
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    }
                                };
                                common.AsynchronousThread(ManageAddresses.this,
                                        webService,
                                        postData,
                                        null,
                                        dataColumns,
                                        postThread,
                                        postFailThread);
                    }
                }).setNegativeButton(R.string.no, null).show();
    }
    String addressID;
    public void editAddress(final View view){
        final Common common1=new Common();
        final Common common2=new Common();
        final ArrayList<String> locations=new ArrayList<>();
        final ArrayList<String> countries=new ArrayList<>();
        //Threading for locations--------------------------------------------------
        String webService="api/customer/GetShippingLocations";
        String postData =  "";
        final ProgressDialog progressDialog=new ProgressDialog(ManageAddresses.this);
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
                        AlertDialog.Builder newAddressDialogue = new AlertDialog.Builder(ManageAddresses.this);
                        newAddressDialogue.setIcon(R.drawable.user);
                        newAddressDialogue.setTitle(R.string.new_address);
                        final View newAddressView=inflater.inflate(R.layout.item_address_input, null);
                        ArrayAdapter locationAdapter = new ArrayAdapter<String>(ManageAddresses.this, android.R.layout.simple_spinner_item, locations);
                        ArrayAdapter countryAdapter = new ArrayAdapter<String>(ManageAddresses.this, android.R.layout.simple_spinner_item, countries);
                        Spinner locationSpinner=(Spinner) newAddressView.findViewById(R.id.location);
                        Spinner countrySpinner=(Spinner) newAddressView.findViewById(R.id.country);
                        locationSpinner.setAdapter(locationAdapter);
                        countrySpinner.setAdapter(countryAdapter);
                        //set values
                        addressID="0";
                        for (int i=0;i<addresses.size();i++){
                            if (view.getTag().equals(addresses.get(i).ID)){
                                int locationPosition=0;
                                int countryPosition=0;
                                for (int j=0;j<common1.dataArrayList.size();j++){
                                    if(common1.dataArrayList.get(j)[0].equals(addresses.get(i).LocationID)){
                                        locationPosition=j;
                                        break;
                                    }
                                }
                                for (int j=0;j<common2.dataArrayList.size();j++){
                                    if(common2.dataArrayList.get(j)[0].equals(addresses.get(i).CountryCode)){
                                        countryPosition=j;
                                        break;
                                    }
                                }

                                ((EditText)newAddressView.findViewById(R.id.prefix)).setText(addresses.get(i).Prefix);
                                ((EditText)newAddressView.findViewById(R.id.first_name)).setText(addresses.get(i).FirstName);
                                ((EditText)newAddressView.findViewById(R.id.mid_name)).setText(addresses.get(i).MidName);
                                ((EditText)newAddressView.findViewById(R.id.last_name)).setText(addresses.get(i).LastName);
                                ((EditText)newAddressView.findViewById(R.id.address)).setText(addresses.get(i).Address);
                                ((Spinner)newAddressView.findViewById(R.id.location)).setSelection(locationPosition);
                                ((EditText)newAddressView.findViewById(R.id.city)).setText(addresses.get(i).City);
                                ((Spinner)newAddressView.findViewById(R.id.country)).setSelection(countryPosition);
                                ((EditText)newAddressView.findViewById(R.id.stateprovince)).setText(addresses.get(i).StateProvince);
                                ((EditText)newAddressView.findViewById(R.id.contact_no)).setText(addresses.get(i).ContactNo);

                                addressID=addresses.get(i).ID;
                                break;
                            }
                        }



                        newAddressDialogue.setView(newAddressView);

                        newAddressDialogue.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        newAddressDialogue.setPositiveButton(R.string.ok_button, null);
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
                                            final Common common3=new Common();
                                            //Threading--------------------------------------------------
                                            String webService = "api/Customer/InsertUpdateCustomerAddress";
                                            String customerAddressJSON = "\"customerAddress\":{";
                                            customerAddressJSON+="\"ID\":\"" + addressID + "\"," +
                                                    "\"CustomerID\":\"" + customerID + "\"," +
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
                                            String postData = "{\"ID\":\"" + customerID
                                                    + "\"," + customerAddressJSON
                                                    + "}";
                                            progressDialog.show();
                                            String[] dataColumns = {"ReturnValues"};
                                            Runnable postThread = new Runnable() {
                                                @Override
                                                public void run() {
                                                        getCustomerAddresses();
                                                        dialog.dismiss();
                                                        if (progressDialog.isShowing())
                                                            progressDialog.dismiss();

                                                }
                                            };
                                            Runnable postFailThread = new Runnable() {
                                                @Override
                                                public void run() {
                                                    Common.toastMessage(ManageAddresses.this,R.string.some_error_at_server);
                                                    if (progressDialog.isShowing())
                                                        progressDialog.dismiss();
                                                }
                                            };
                                            common3.AsynchronousThread(ManageAddresses.this,
                                                    webService,
                                                    postData,
                                                    null,
                                                    dataColumns,
                                                    postThread,
                                                    postFailThread);
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
                        Common.toastMessage(ManageAddresses.this,R.string.some_error_at_server);
                    }
                };
                common2.AsynchronousThread(ManageAddresses.this,
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
                Common.toastMessage(ManageAddresses.this,R.string.some_error_at_server);
            }
        };
        common1.AsynchronousThread(ManageAddresses.this,
                webService,
                postData,
                null,
                dataColumns,
                postThread,
                postFailThread);
    }
    public void selectAddress(View view){
        if(SHIPPING_ADDRESS_ID.equals("")){
            SHIPPING_ADDRESS_ID=view.getTag().toString();
        }
        else if(BILLING_ADDRESS_ID.equals("")){
            BILLING_ADDRESS_ID=view.getTag().toString();
        }
        Intent cartIntent=new Intent(ManageAddresses.this,Cart.class);
        cartIntent.putExtra("shipping_address_id",SHIPPING_ADDRESS_ID);
        cartIntent.putExtra("billing_address_id",BILLING_ADDRESS_ID);
        cartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cartIntent);
    }
    private class CustomerAddress{
        String ID="";
        String CustomerID="";
        String Prefix="";
        String FirstName="";
        String MidName="";
        String LastName="";
        String Address="";
        String LocationID="";
        String City="";
        String CountryCode="";
        String StateProvince="";
        String ContactNo="";
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.address_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Filter menu------------------------
        if (id == R.id.menu_add_address) {
            inputNewAddress();
        }
        return super.onOptionsItemSelected(item);
    }
    void inputNewAddress(){
        final Common common1=new Common();
        final Common common2=new Common();
        final ArrayList<String> locations=new ArrayList<>();
        final ArrayList<String> countries=new ArrayList<>();
        //Threading for locations--------------------------------------------------
        String webService="api/customer/GetShippingLocations";
        String postData =  "";
        final ProgressDialog progressDialog=new ProgressDialog(ManageAddresses.this);
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
                        AlertDialog.Builder newAddressDialogue = new AlertDialog.Builder(ManageAddresses.this);
                        newAddressDialogue.setIcon(R.drawable.user);
                        newAddressDialogue.setTitle(R.string.new_address);
                        final View newAddressView=inflater.inflate(R.layout.item_address_input, null);
                        ArrayAdapter locationAdapter = new ArrayAdapter<String>(ManageAddresses.this, android.R.layout.simple_spinner_item, locations);
                        ArrayAdapter countryAdapter = new ArrayAdapter<String>(ManageAddresses.this, android.R.layout.simple_spinner_item, countries);
                        Spinner locationSpinner=(Spinner) newAddressView.findViewById(R.id.location);
                        Spinner countrySpinner=(Spinner) newAddressView.findViewById(R.id.country);
                        locationSpinner.setAdapter(locationAdapter);
                        countrySpinner.setAdapter(countryAdapter);
                        newAddressDialogue.setView(newAddressView);

                        newAddressDialogue.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        newAddressDialogue.setPositiveButton(R.string.ok_button, null);
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
                                            final Common common3=new Common();
                                            //Threading--------------------------------------------------
                                            String webService = "api/Customer/InsertUpdateCustomerAddress";
                                            String customerAddressJSON = "\"customerAddress\":{";
                                            customerAddressJSON+="\"ID\":\"" + 0 + "\"," + //0 for new address insertion in repository function
                                                    "\"CustomerID\":\"" + customerID + "\"," +
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
                                            String postData = "{\"ID\":\"" + customerID
                                                    + "\"," + customerAddressJSON
                                                    + "}";
                                            progressDialog.show();
                                            String[] dataColumns = {"ReturnValues"};
                                            Runnable postThread = new Runnable() {
                                                @Override
                                                public void run() {
                                                    getCustomerAddresses();
                                                    dialog.dismiss();
                                                    if (progressDialog.isShowing())
                                                        progressDialog.dismiss();
                                                }
                                            };
                                            Runnable postFailThread = new Runnable() {
                                                @Override
                                                public void run() {
                                                    Common.toastMessage(ManageAddresses.this,R.string.some_error_at_server);
                                                    if (progressDialog.isShowing())
                                                        progressDialog.dismiss();
                                                }
                                            };
                                            common3.AsynchronousThread(ManageAddresses.this,
                                                    webService,
                                                    postData,
                                                    null,
                                                    dataColumns,
                                                    postThread,
                                                    postFailThread);
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
                        Common.toastMessage(ManageAddresses.this,R.string.some_error_at_server);
                    }
                };
                common2.AsynchronousThread(ManageAddresses.this,
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
                Common.toastMessage(ManageAddresses.this,R.string.some_error_at_server);
            }
        };
        common1.AsynchronousThread(ManageAddresses.this,
                webService,
                postData,
                null,
                dataColumns,
                postThread,
                postFailThread);
    }
    @Override
    public void onBackPressed() {
        for(int i=0;i<asyncTasks.size();i++){
            asyncTasks.get(i).cancel(true);
        }
        super.onBackPressed();
    }
}
