package com.tech.thrithvam.partyec;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.Locale;

import static android.view.View.GONE;

public class Cart extends AppCompatActivity {
String customerID;
    CustomerAddress customerAddress;
    LayoutInflater inflater;
    Double totalPrice =0.0,totalShipping=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.cart);
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customerID="1009";//TODO replace with customer ID
        loadCart();
    }
    void loadCart(){
        final Common common=new Common();
        final ListView cartListView=(ListView) findViewById(R.id.listview);


        (findViewById(R.id.cart_scrollview)).setVisibility(View.GONE);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerShoppingCart";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        String[] dataColumns={"ID",//0
                "ProductID",//1
                "ProductName",//2
                "ImageURL",//3
                "AttributeValues",//4
                "Qty",//5
                "Price",//6
                "ShippingCharge"//7
                };
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                //Attributes parsing
                String attributesString="";
                for (int i=0;i<common.dataArrayList.size();i++){
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
                    totalPrice +=Double.parseDouble(common.dataArrayList.get(i)[6].equals("null")?"0":common.dataArrayList.get(i)[6]);
                    totalShipping+=Double.parseDouble(common.dataArrayList.get(i)[7].equals("null")?"0":common.dataArrayList.get(i)[7]);
                }
                Double totalAmount=totalPrice+totalShipping;
                ((TextView)findViewById(R.id.total_price)).setText(getString(R.string.total_price,String.format(Locale.US, "%.2f", totalPrice)));
                ((TextView)findViewById(R.id.total_shipping)).setText(getString(R.string.total_shipping,String.format(Locale.US, "%.2f",totalShipping)));
                ((TextView)findViewById(R.id.total_amount)).setText(getString(R.string.total_amount,String.format(Locale.US, "%.2f",totalAmount)));
                (findViewById(R.id.total_amount_card_view)).setVisibility(View.VISIBLE);
              //Load customer address-----------------------------------
                getCustomerAddress();
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
    //Address--------------------------------------------------------------------------------------------------------------------
    View addressView;
    void getCustomerAddress(){
        final Common common=new Common();
        customerAddress=new CustomerAddress();
        addressView=inflater.inflate(R.layout.item_address, null);
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

                        (findViewById(R.id.change_address)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder selectAddressAlert = new AlertDialog.Builder(Cart.this);
                                selectAddressAlert.setIcon(R.drawable.user);
                                selectAddressAlert.setTitle(R.string.select_address);
                                CustomAdapter customAdapter=new CustomAdapter(Cart.this,common.dataArrayList,"Address");
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
                                                common.dataArrayList.get(which)[0],
                                                common.dataArrayList.get(which)[1],
                                                common.dataArrayList.get(which)[2],
                                                common.dataArrayList.get(which)[3],
                                                common.dataArrayList.get(which)[4],
                                                common.dataArrayList.get(which)[5],
                                                common.dataArrayList.get(which)[6],
                                                common.dataArrayList.get(which)[7],
                                                common.dataArrayList.get(which)[8],
                                                common.dataArrayList.get(which)[9],
                                                common.dataArrayList.get(which)[10],
                                                common.dataArrayList.get(which)[13]
                                        );
                                    }
                                });
                                selectAddressAlert.setPositiveButton(R.string.new_address,  new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        inputNewAddress();
                                    }
                                });
                                selectAddressAlert.show();
                            }
                        });
                        break;
                    }
                }
                findViewById(R.id.customer_address).setVisibility(View.VISIBLE);
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
    void inputNewAddress(){
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
                                            setAddressDisplayAndObject(addressView,
                                                    "null",
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
                                            dialog.dismiss();
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
    void setAddressDisplayAndObject(View addressView,String ID,String Prefix,String FirstName,String MidName,String LastName,String Address,String Location,String City,String StateProvince,String countryJson,String ContactNo,String LocationID){
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


        customerAddress.ID=(ID.equals("null")?"":ID);
        customerAddress.CustomerID=customerID;
        customerAddress.Prefix=(Prefix.equals("null")?"":Prefix);
        customerAddress.FirstName=(FirstName.equals("null")?"":FirstName);
        customerAddress.MidName=(MidName.equals("null")?"":MidName);
        customerAddress.LastName=(LastName.equals("null")?"":LastName);
        customerAddress.Address=(Address.equals("null")?"":Address);
        customerAddress.LocationID=(LocationID.equals("null")?"":LocationID);
        customerAddress.City=(City.equals("null")?"":City);
        customerAddress.CountryCode=countryCode;
        customerAddress.StateProvince=(StateProvince.equals("null")?"":StateProvince);
        customerAddress.ContactNo=(ContactNo.equals("null")?"":ContactNo);
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
}
