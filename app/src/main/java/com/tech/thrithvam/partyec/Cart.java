package com.tech.thrithvam.partyec;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import static android.view.View.GONE;

public class Cart extends AppCompatActivity {
    DatabaseHandler db;
    String customerID;
    CustomerAddress customerAddress;
    CustomerAddress billingAddress;
    LayoutInflater inflater;
    Double totalPrice =0.0,totalShipping=0.0;Double totalAmount=0.0;
    String locationID="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.cart);
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        db=DatabaseHandler.getInstance(Cart.this);
        if(db.GetCustomerDetails("CustomerID")==null) {
            Intent loginIntent=new Intent(this,Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
            return;
        }
        customerID=db.GetCustomerDetails("CustomerID");
        //Load customer's shopping cart-------------------------
        loadCart();
        //Load customer address-----------------------------------
        getCustomerAddress();
    }
    void loadCart(){
        final Common common=new Common();
        final ListView cartListView=(ListView) findViewById(R.id.listview);
        (findViewById(R.id.cart_scrollview)).setVisibility(View.GONE);
        //Threading-------------------------------------------------------------------------
        String webService="api/Order/GetCustomerCart";
        String postData;
        if(locationID.equals("")){
            postData =  "{\"CustomerID\":\""+customerID+"\"}";
        }
        else {
            postData =  "{\"CustomerID\":\""+customerID+"\",\"LocationID\":\""+locationID+"\"}";
        }

        String[] dataColumns={"ID",//0
                "ProductID",//1
                "ProductName",//2
                "ImageURL",//3
                "AttributeValues",//4
                "Qty",//5
                "CurrentPrice",//6
                "ShippingCharge",//7
                "StockAvailableYN"//8
                };
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                //Attributes parsing
                for (int i=0;i<common.dataArrayList.size();i++){
                    String attributesString="";
                    try {
                        JSONArray jsonArray=new JSONArray(common.dataArrayList.get(i)[4]);
                        if(jsonArray.length()!=0){
                            for (int j=0;j<jsonArray.length();j++){
                                JSONObject attribute=jsonArray.getJSONObject(j);
                                attributesString+=attribute.optString("Caption")+" : "+attribute.optString("Value")+"\n";
                            }
                            if (attributesString.lastIndexOf("\n") > 0) {
                                attributesString = attributesString.substring(0, attributesString.lastIndexOf("\n"));
                                common.dataArrayList.get(i)[4]=attributesString;
                            }
                        }
                        else {
                            common.dataArrayList.get(i)[4]="";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //cart list
                CustomAdapter adapter=new CustomAdapter(Cart.this, common.dataArrayList,"Cart");
                cartListView.setAdapter(adapter);
                cartListView.setSelector(android.R.color.transparent);
                int desiredWidth = View.MeasureSpec.makeMeasureSpec(cartListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
                int totalHeight = 0;
                View view = null;
                for (int i = 0; i < cartListView.getCount(); i++) {
                    view = adapter.getView(i, view, cartListView);
                    if (i == 0)
                        view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

                    view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                    totalHeight += view.getMeasuredHeight();
                }
                ViewGroup.LayoutParams params = cartListView.getLayoutParams();
                params.height = totalHeight + (cartListView.getDividerHeight() * (adapter.getCount() - 1));
                cartListView.setLayoutParams(params);
              /*  cartListView.setOnTouchListener(new View.OnTouchListener() {
                    // Setting on Touch Listener for handling the touch inside ScrollView
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // Disallow the touch request for parent scroll on touch of child view
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });*/
              (findViewById(R.id.cart_scrollview)).setVisibility(View.VISIBLE);
              //Totaling-----------------------------------
                for(int i=0;i<common.dataArrayList.size();i++){
                    if(common.dataArrayList.get(i)[8].equals("true")) {
                        totalPrice += Double.parseDouble(common.dataArrayList.get(i)[6].equals("null") ? "0" : common.dataArrayList.get(i)[6]);
                        totalShipping += Double.parseDouble(common.dataArrayList.get(i)[7].equals("null") ? "0" : common.dataArrayList.get(i)[7]);
                    }
                }
                totalAmount=totalPrice+totalShipping;
                ((TextView)findViewById(R.id.total_price)).setText(getString(R.string.total_price,String.format(Locale.US, "%.2f", totalPrice)));
                ((TextView)findViewById(R.id.total_shipping)).setText(getString(R.string.total_shipping,String.format(Locale.US, "%.2f",totalShipping)));
                ((TextView)findViewById(R.id.total_amount)).setText(getString(R.string.total_amount,String.format(Locale.US, "%.2f",totalAmount)));
                (findViewById(R.id.total_amount_card_view)).setVisibility(View.VISIBLE);
            }
        };
        common.AsynchronousThread(Cart.this,
                webService,
                postData,
                loadingIndicatorView,
                dataColumns,
                postThread,
                null);
    }
    public void removeFromCart(final View view){
        new AlertDialog.Builder(Cart.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle(R.string.exit)
                .setMessage(R.string.remove_item_q)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Common common=new Common();
                        final ProgressDialog progressDialog=new ProgressDialog(Cart.this);
                        progressDialog.setMessage(getResources().getString(R.string.please_wait));
                        progressDialog.setCancelable(false);progressDialog.show();
                        //Threading--------------------------------------------------
                        String webService="api/order/RemoveProductFromCart";
                        String postData =  "{\"ID\":\""+(String) view.getTag()+"\"}";
                        String[] dataColumns={};
                        Runnable postThread=new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                                //Refresh Cart----------------------
                                totalShipping=0.0;totalPrice=0.0;
                                loadCart();
                            }
                        };
                        Runnable postFailThread=new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                                Toast.makeText(Cart.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                            }
                        };
                        common.AsynchronousThread(Cart.this,
                                webService,
                                postData,
                                null,
                                dataColumns,
                                postThread,
                                postFailThread);
                    }
                }).setNegativeButton(R.string.no, null).show();
    }
    //Address--------------------------------------------------------------------------------------------------------------------
    View addressView,billingAddressView;
    void getCustomerAddress(){
        final Common common=new Common();
        customerAddress=new CustomerAddress();
        billingAddress=new CustomerAddress();
        addressView=inflater.inflate(R.layout.item_address, null);
        billingAddressView=inflater.inflate(R.layout.item_address, null);
        ((LinearLayout)findViewById(R.id.customer_address_linear)).addView(addressView);
        //Threading--------------------------------------------------
        String webService="api/customer/GetCustomerAddress";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator2);
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
                for (int i=0;i<common.dataArrayList.size();i++){
                    if(common.dataArrayList.get(i)[11].equals("true")){
                        setAddressDisplayAndObject(addressView,
                                customerAddress,
                                common.dataArrayList.get(i)[0],
                                common.dataArrayList.get(i)[1],
                                common.dataArrayList.get(i)[2],
                                common.dataArrayList.get(i)[3],
                                common.dataArrayList.get(i)[4],
                                common.dataArrayList.get(i)[5],
                                common.dataArrayList.get(i)[6],
                                common.dataArrayList.get(i)[7],
                                common.dataArrayList.get(i)[8],
                                common.dataArrayList.get(i)[9],
                                common.dataArrayList.get(i)[10],
                                common.dataArrayList.get(i)[13]
                        );
                        //By default
                        billingAddress.ID=customerAddress.ID;
                        billingAddress.CustomerID=customerAddress.CustomerID;
                        billingAddress.Prefix=customerAddress.Prefix;
                        billingAddress.FirstName=customerAddress.FirstName;
                        billingAddress.MidName=customerAddress.MidName;
                        billingAddress.LastName=customerAddress.LastName;
                        billingAddress.Address=customerAddress.Address;
                        billingAddress.LocationID=customerAddress.LocationID;
                        billingAddress.City=customerAddress.City;
                        billingAddress.CountryCode=customerAddress.CountryCode;
                        billingAddress.StateProvince=customerAddress.StateProvince;
                        billingAddress.ContactNo=customerAddress.ContactNo;

                        locationID=customerAddress.LocationID;

                        (findViewById(R.id.change_address)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Common common1=new Common();
                                //Threading--------------------------------------------------
                                String webService="api/customer/GetCustomerAddress";
                                String postData =  "{\"CustomerID\":\""+customerID+"\"}";
                                final ProgressDialog progressDialog=new ProgressDialog(Cart.this);
                                progressDialog.setMessage(getResources().getString(R.string.please_wait));
                                progressDialog.setCancelable(false);progressDialog.show();
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
                                        AlertDialog.Builder selectAddressAlert = new AlertDialog.Builder(Cart.this);
                                        selectAddressAlert.setIcon(R.drawable.user);
                                        selectAddressAlert.setTitle(R.string.select_address);
                                        CustomAdapter customAdapter=new CustomAdapter(Cart.this,common1.dataArrayList,"Address");
                                        selectAddressAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        selectAddressAlert.setAdapter(customAdapter, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                setAddressDisplayAndObject(addressView,
                                                        customerAddress,
                                                        common1.dataArrayList.get(which)[0],
                                                        common1.dataArrayList.get(which)[1],
                                                        common1.dataArrayList.get(which)[2],
                                                        common1.dataArrayList.get(which)[3],
                                                        common1.dataArrayList.get(which)[4],
                                                        common1.dataArrayList.get(which)[5],
                                                        common1.dataArrayList.get(which)[6],
                                                        common1.dataArrayList.get(which)[7],
                                                        common1.dataArrayList.get(which)[8],
                                                        common1.dataArrayList.get(which)[9],
                                                        common1.dataArrayList.get(which)[10],
                                                        common1.dataArrayList.get(which)[13]
                                                );
                                                //Refresh Cart----------------------
                                                locationID=customerAddress.LocationID;
                                                totalShipping=0.0;totalPrice=0.0;
                                                loadCart();
                                            }
                                        });
                                        selectAddressAlert.setPositiveButton(R.string.new_address,  new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                inputNewAddress(addressView,customerAddress);
                                            }
                                        });
                                        selectAddressAlert.show();
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    }
                                };
                                Runnable postFailThread=new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Cart.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    }
                                };
                                common1.AsynchronousThread(Cart.this,
                                        webService,
                                        postData,
                                        null,
                                        dataColumns,
                                        postThread,
                                        postFailThread);
                            }
                        });
                        break;
                    }
                }
                findViewById(R.id.customer_address).setVisibility(View.VISIBLE);
                //Billing address---------------
                findViewById(R.id.billing_address).setVisibility(View.VISIBLE);
                (findViewById(R.id.change_billing_address)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Common common2=new Common();
                        //Threading--------------------------------------------------
                        String webService="api/customer/GetCustomerAddress";
                        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
                        final ProgressDialog progressDialog=new ProgressDialog(Cart.this);
                        progressDialog.setMessage(getResources().getString(R.string.please_wait));
                        progressDialog.setCancelable(false);progressDialog.show();
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
                                    AlertDialog.Builder selectAddressAlert = new AlertDialog.Builder(Cart.this);
                                    selectAddressAlert.setIcon(R.drawable.user);
                                    selectAddressAlert.setTitle(R.string.select_address);
                                    CustomAdapter customAdapter=new CustomAdapter(Cart.this,common2.dataArrayList,"Address");
                                    selectAddressAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    selectAddressAlert.setAdapter(customAdapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            setAddressDisplayAndObject(billingAddressView,
                                                    billingAddress,
                                                    common2.dataArrayList.get(which)[0],
                                                    common2.dataArrayList.get(which)[1],
                                                    common2.dataArrayList.get(which)[2],
                                                    common2.dataArrayList.get(which)[3],
                                                    common2.dataArrayList.get(which)[4],
                                                    common2.dataArrayList.get(which)[5],
                                                    common2.dataArrayList.get(which)[6],
                                                    common2.dataArrayList.get(which)[7],
                                                    common2.dataArrayList.get(which)[8],
                                                    common2.dataArrayList.get(which)[9],
                                                    common2.dataArrayList.get(which)[10],
                                                    common2.dataArrayList.get(which)[13]
                                            );
                                            //selecting an address
                                            (findViewById(R.id.same_address_label)).setVisibility(GONE);
                                            ((LinearLayout)findViewById(R.id.billing_address_linear)).removeView(billingAddressView);
                                            ((LinearLayout)findViewById(R.id.billing_address_linear)).addView(billingAddressView);
                                        }
                                    });
                                    selectAddressAlert.setPositiveButton(R.string.new_address,  new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            inputNewAddress(billingAddressView,billingAddress);
                                            //selecting an address
                                            (findViewById(R.id.same_address_label)).setVisibility(GONE);
                                            ((LinearLayout)findViewById(R.id.billing_address_linear)).removeView(billingAddressView);
                                            ((LinearLayout)findViewById(R.id.billing_address_linear)).addView(billingAddressView);
                                        }
                                    });
                                    selectAddressAlert.show();
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }
                        };
                        Runnable postFailThread=new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Cart.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }
                        };
                        common2.AsynchronousThread(Cart.this,
                                webService,
                                postData,
                                null,
                                dataColumns,
                                postThread,
                                postFailThread);
                    }
                });

            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.customer_address)).setVisibility(GONE);
            }
        };
        common.AsynchronousThread(Cart.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postFailThread);
    }
    void inputNewAddress(final View targetView, final CustomerAddress targetAddress){
        final Common common1=new Common();
        final Common common2=new Common();
        final ArrayList<String> locations=new ArrayList<>();
        final ArrayList<String> countries=new ArrayList<>();
        //Threading for locations--------------------------------------------------
        String webService="api/customer/GetShippingLocations";
        String postData =  "";
        final ProgressDialog progressDialog=new ProgressDialog(Cart.this);
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
                        AlertDialog.Builder newAddressDialogue = new AlertDialog.Builder(Cart.this);
                        newAddressDialogue.setIcon(R.drawable.user);
                        newAddressDialogue.setTitle(R.string.new_address);
                        final View newAddressView=inflater.inflate(R.layout.item_address_input, null);
                        ArrayAdapter locationAdapter = new ArrayAdapter<String>(Cart.this, android.R.layout.simple_spinner_item, locations);
                        ArrayAdapter countryAdapter = new ArrayAdapter<String>(Cart.this, android.R.layout.simple_spinner_item, countries);
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
                                                    "\"CustomerID\":\"" + targetAddress.CustomerID + "\"," +
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
                                                    //Displaying
                                                    try {
                                                        JSONObject jsonObject=new JSONObject(common3.json);
                                                        String addressID=jsonObject.optString("ReturnValues");

                                                        setAddressDisplayAndObject(targetView,
                                                                targetAddress,
                                                                addressID,
                                                                ((EditText)newAddressView.findViewById(R.id.prefix)).getText().toString(),
                                                                ((EditText)newAddressView.findViewById(R.id.first_name)).getText().toString(),
                                                                ((EditText)newAddressView.findViewById(R.id.mid_name)).getText().toString(),
                                                                ((EditText)newAddressView.findViewById(R.id.last_name)).getText().toString(),
                                                                ((EditText)newAddressView.findViewById(R.id.address)).getText().toString(),
                                                                ((Spinner)newAddressView.findViewById(R.id.location)).getSelectedItem().toString(),
                                                                ((EditText)newAddressView.findViewById(R.id.city)).getText().toString(),
                                                                ((EditText)newAddressView.findViewById(R.id.stateprovince)).getText().toString(),
                                                                "{\"Code\":\""+common2.dataArrayList.get(((Spinner)newAddressView.findViewById(R.id.country)).getSelectedItemPosition())[0]+"\",\"Name\":\""+common2.dataArrayList.get(((Spinner)newAddressView.findViewById(R.id.country)).getSelectedItemPosition())[1]+"\"}",
                                                                ((EditText)newAddressView.findViewById(R.id.contact_no)).getText().toString(),
                                                                common1.dataArrayList.get(((Spinner)newAddressView.findViewById(R.id.location)).getSelectedItemPosition())[0]
                                                        );
                                                        //Adding to address arraylist;
                                                        dialog.dismiss();
                                                        if (progressDialog.isShowing())
                                                            progressDialog.dismiss();
                                                    } catch (JSONException e) {
                                                        Toast.makeText(Cart.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            };
                                            Runnable postFailThread = new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(Cart.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                                                    if (progressDialog.isShowing())
                                                        progressDialog.dismiss();
                                                }
                                            };
                                            common3.AsynchronousThread(Cart.this,
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
                        Toast.makeText(Cart.this,R.string.some_error_at_server,Toast.LENGTH_SHORT).show();
                    }
                };
                common2.AsynchronousThread(Cart.this,
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
                Toast.makeText(Cart.this,R.string.some_error_at_server,Toast.LENGTH_SHORT).show();
            }
        };
        common1.AsynchronousThread(Cart.this,
                webService,
                postData,
                null,
                dataColumns,
                postThread,
                postFailThread);
    }
    void setAddressDisplayAndObject(View addressView, CustomerAddress addressObject, String ID, String Prefix, String FirstName, String MidName, String LastName, String Address, String Location, String City, String StateProvince, String countryJson, String ContactNo, String LocationID){
        String name=(Prefix.equals("null")?"":Prefix)           +   " "
                +   (FirstName.equals("null")?"":FirstName)     +   " "
                +   (MidName.equals("null")?"":MidName)         +   " "
                +   (LastName.equals("null")?"":LastName);
        ((TextView)addressView.findViewById(R.id.name)).setText(name);
        ((TextView)addressView.findViewById(R.id.address)).setText(Address.equals("null")?"":Address);
        ((TextView)addressView.findViewById(R.id.location)).setText(Location.equals("null")?"-":Location);
        ((TextView)addressView.findViewById(R.id.city)).setText(City.equals("null")?"-":City);
        ((TextView)addressView.findViewById(R.id.stateprovince)).setText(StateProvince.equals("null")?"-":StateProvince);
        String country="";
        String countryCode="";
        try {
            JSONObject jsonObjectCountry=new JSONObject(countryJson);
            country=jsonObjectCountry.getString("Name");
            countryCode=jsonObjectCountry.getString("Code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((TextView)addressView.findViewById(R.id.country)).setText(country.equals("null")?"-":country);
        ((TextView)addressView.findViewById(R.id.contact_no)).setText(ContactNo.equals("null")?"-":ContactNo);


        addressObject.ID=(ID.equals("null")?"":ID);
        addressObject.CustomerID=customerID;
        addressObject.Prefix=(Prefix.equals("null")?"":Prefix);
        addressObject.FirstName=(FirstName.equals("null")?"":FirstName);
        addressObject.MidName=(MidName.equals("null")?"":MidName);
        addressObject.LastName=(LastName.equals("null")?"":LastName);
        addressObject.Address=(Address.equals("null")?"":Address);
        addressObject.LocationID=(LocationID.equals("null")?"":LocationID);
        addressObject.City=(City.equals("null")?"":City);
        addressObject.CountryCode=countryCode;
        addressObject.StateProvince=(StateProvince.equals("null")?"":StateProvince);
        addressObject.ContactNo=(ContactNo.equals("null")?"":ContactNo);
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
    public void proceedClick(final View view) {
        if(locationID.equals("")||locationID.equals("0")){
            Toast.makeText(this, "Please select shipping address having location", Toast.LENGTH_SHORT).show();
            return;
        }
        if(totalAmount==0){
            Toast.makeText(this, R.string.no_items_available, Toast.LENGTH_SHORT).show();
            return;
        }
        final Common common=new Common();
        view.setVisibility(GONE);

        //Customer Address-----------------
        String customerShippingAddressJSON = "\"CustomerShippingAddress\":{";
        customerShippingAddressJSON += "\"ID\":\"" + customerAddress.ID + "\"," +
                "\"CustomerID\":\"" + customerAddress.CustomerID + "\"," +
                "\"Prefix\":\"" + customerAddress.Prefix + "\"," +
                "\"FirstName\":\"" + customerAddress.FirstName + "\"," +
                "\"MidName\":\"" + customerAddress.MidName + "\"," +
                "\"LastName\":\"" + customerAddress.LastName + "\"," +
                "\"Address\":\"" + customerAddress.Address + "\"," +
                "\"LocationID\":\"" + customerAddress.LocationID + "\"," +
                "\"City\":\"" + customerAddress.City + "\"," +
                "\"CountryCode\":\"" + customerAddress.CountryCode + "\"," +
                "\"StateProvince\":\"" + customerAddress.StateProvince + "\"," +
                "\"ContactNo\":\"" + customerAddress.ContactNo + "\"}";

        String customerBillingAddressJSON = "\"CustomerBillAddress\":{";
        customerBillingAddressJSON += "\"ID\":\"" + billingAddress.ID + "\"," +
                "\"CustomerID\":\"" + billingAddress.CustomerID + "\"," +
                "\"Prefix\":\"" + billingAddress.Prefix + "\"," +
                "\"FirstName\":\"" + billingAddress.FirstName + "\"," +
                "\"MidName\":\"" + billingAddress.MidName + "\"," +
                "\"LastName\":\"" + billingAddress.LastName + "\"," +
                "\"Address\":\"" + billingAddress.Address + "\"," +
                "\"LocationID\":\"" + billingAddress.LocationID + "\"," +
                "\"City\":\"" + billingAddress.City + "\"," +
                "\"CountryCode\":\"" + billingAddress.CountryCode + "\"," +
                "\"StateProvince\":\"" + billingAddress.StateProvince + "\"," +
                "\"ContactNo\":\"" + billingAddress.ContactNo + "\"}";

        //Threading--------------------------------------------------
        String webService = "api/Order/InsertOrder";
        String postData = "{\"CustomerID\":\"" + customerID
                + "\",\"shippingLocationID\":\"" + locationID
                + "\",\"PaymentType\":\"" + "COD"
                + "\",\"PaymentStatus\":\"" + "0"
                + "\",\"SourceIP\":\"" + getLocalIpAddress()
                + "\"," + customerShippingAddressJSON
                + "," + customerBillingAddressJSON
                + "}";
        final ProgressDialog progressDialog=new ProgressDialog(Cart.this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);progressDialog.show();
        String[] dataColumns = {};
        Runnable postThread = new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject= null;
                try {
                    jsonObject = new JSONObject(common.json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String orderID=jsonObject.optString("ReturnValues");
                Intent clearIntent = new Intent(Cart.this, PaymentGateway.class);
                clearIntent.putExtra("orderID",orderID);
                clearIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(clearIntent);
                finish();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        };
        Runnable postFailThread = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Cart.this, R.string.failed, Toast.LENGTH_SHORT).show();
                view.setVisibility(View.VISIBLE);
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        };
        common.AsynchronousThread(Cart.this,
                webService,
                postData,
                null,
                dataColumns,
                postThread,
                postFailThread);
    }
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        return ip;
                    }
                }
            }

        } catch (SocketException ex) {
            Log.e("", ex.toString());
        }
        return null;
    }
}
