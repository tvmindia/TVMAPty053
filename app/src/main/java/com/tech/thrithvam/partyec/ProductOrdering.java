package com.tech.thrithvam.partyec;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.Inflater;

import static android.view.View.GONE;

public class ProductOrdering extends AppCompatActivity {
    Common common=new Common();
    String productID="";
    LayoutInflater inflater;

    //Product detail attributes
    ArrayList<ProductDetails> productDetailsArrayList=new ArrayList<>();
    ArrayList<Spinner> spinners = new ArrayList<>();

    //Order attributes
    ArrayList<Attributes> orderAttributesArrayList=new ArrayList<>();
    ArrayList<View> orderAttributesUserInputs=new ArrayList<>();

    boolean showPrice=false;
    Double baseSellingPrice=0.0;
    String selectedProductDetailID;
    Boolean inStock=true;

    CustomerAddress customerAddress;
    String customerID;
    String actionType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_ordering);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Order: "+getIntent().getExtras().getString("productName",""));
        productID=getIntent().getExtras().getString("productID");
        customerID="1009";//TODO change
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        (findViewById(R.id.price_n_stock)).setVisibility(View.GONE);
        TextView actualPrice=(TextView)findViewById(R.id.actual_price);
        actualPrice.setPaintFlags(actualPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        actionType=getIntent().getExtras().getString("actionType");
        if(!actionType.equals("A")){//Not a buyable product
            (findViewById(R.id.price_n_stock)).setVisibility(GONE);
            if(actionType.equals("Q")||actionType.equals("B")){//Quotable or Bookable product
                (findViewById(R.id.quote_book_options)).setVisibility(View.VISIBLE);

                final EditText requiredDate=(EditText)findViewById(R.id.required_date);
                requiredDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar today = Calendar.getInstance();
                        final Calendar selectedDate=Calendar.getInstance();
                        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                selectedDate.set(Calendar.YEAR, year);
                                selectedDate.set(Calendar.MONTH, monthOfYear);
                                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                //Validation--------------
                                if(selectedDate.before(today)){
                                    Toast.makeText(ProductOrdering.this, R.string.give_valid, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                //Setting display text-------
                                SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                                requiredDate.setText(formatted.format(selectedDate.getTime()));
                            }
                        };
                        new DatePickerDialog(ProductOrdering.this, dateSetListener, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });
                if(actionType.equals("B")){
                    getCustomerAddress();
                }
            }
        }
        else {
            (findViewById(R.id.quote_book_options)).setVisibility(GONE);
        }
        getProductDetailsForOrder();
    }
    void getProductDetailsForOrder(){
        (findViewById(R.id.proceed_button)).setVisibility(View.INVISIBLE);
        //Threading--------------------------------------------------
        String webService="api/product/GetProductDetailsForOrder";
        String postData =  "{\"ID\":\""+productID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                JSONObject jsonRootObject;
                try {
                    jsonRootObject = new JSONObject(common.json);
                    //Product base details
                    showPrice=jsonRootObject.optBoolean("ShowPrice");
                    baseSellingPrice=jsonRootObject.optDouble("BaseSellingPrice");
                    //Free delivery
                    if(jsonRootObject.optBoolean("FreeDelivery"))
                        findViewById(R.id.free_delivery).setVisibility(View.VISIBLE);
                    else
                        findViewById(R.id.free_delivery).setVisibility(GONE);

                    //Product details for product attributes
                    JSONArray productDetails =jsonRootObject.optJSONArray("ProductDetails");
                    for (int i = 0; i < productDetails.length(); i++) {
                        ProductDetails productDetailsObj=new ProductDetails();

                        JSONObject jsonObject = productDetails.getJSONObject(i);
                        productDetailsObj.ID=jsonObject.optString("ID");
                        productDetailsObj.PriceDifference=(jsonObject.optString("PriceDifference").equals("null")?0:jsonObject.optDouble("PriceDifference"));
                        productDetailsObj.DiscountAmount=(jsonObject.optString("DiscountAmount").equals("null")?0:jsonObject.optDouble("DiscountAmount"));
                        productDetailsObj.stockAvailable=jsonObject.optBoolean("StockAvailable");
                        productDetailsObj.quantity=jsonObject.optInt("Qty");

                        JSONArray productAttributes=jsonObject.optJSONArray("ProductAttributes");
                        if(productAttributes!=null) {
                            productDetailsObj.productAttributes = new ArrayList<>();
                            for (int j = 0; j < productAttributes.length(); j++) {
                                JSONObject proAttriObj = productAttributes.getJSONObject(j);
                                Attributes prodDetAttr = new Attributes();
                                prodDetAttr.Name = proAttriObj.optString("Name");
                                prodDetAttr.Caption = proAttriObj.optString("Caption");
                                prodDetAttr.Value = proAttriObj.optString("Value");
                                prodDetAttr.DataType = proAttriObj.optString("DataType");
                                productDetailsObj.productAttributes.add(prodDetAttr);
                            }
                            productDetailsArrayList.add(productDetailsObj);
                        }
                    }

                    //OrderAttributes
                    JSONArray orderAttributes=jsonRootObject.optJSONArray("OrderAttributes");
                    if(orderAttributes!=null){
                        for (int i = 0; i < orderAttributes.length(); i++) {
                            JSONObject jsonObj = orderAttributes.getJSONObject(i);
                            Attributes attributeObj = new Attributes();
                            attributeObj.Name = jsonObj.optString("Name");
                            attributeObj.Caption = jsonObj.optString("Caption");
                            attributeObj.Value = jsonObj.optString("Value");
                            attributeObj.DataType = jsonObj.optString("DataType");
                            orderAttributesArrayList.add(attributeObj);
                        }
                    }

                    if(productDetailsArrayList.get(0).productAttributes.size() ==0 && orderAttributesArrayList.size()==0){
                        (findViewById(R.id.select_options)).setVisibility(GONE);
                    }
                    else {
                        (findViewById(R.id.select_options)).setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setupUserControls();
                (findViewById(R.id.proceed_button)).setVisibility(View.VISIBLE);
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.select_options)).setVisibility(GONE);
            }
        };
        common.AsynchronousThread(ProductOrdering.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postFailThread);
    }
    void setupUserControls(){
        //Attributes
        LinearLayout attributesLinear=(LinearLayout)findViewById(R.id.prod_attributes_linear);
        //-------------------arrange user controls---------------------
        //Product Attributes
        if(productDetailsArrayList.size()!=0) {
            for (int i = 0; i < productDetailsArrayList.get(0).productAttributes.size(); i++) {
                TextView label = new TextView(ProductOrdering.this);
                label.setText(productDetailsArrayList.get(0).productAttributes.get(i).Caption);
                label.setPadding(5, 5, 5, 0);
                attributesLinear.addView(label);
                Spinner spinner = new Spinner(ProductOrdering.this);
                attributesLinear.addView(spinner);
                spinners.add(spinner);
            }
            //setup values
            for (int j = 0; j < productDetailsArrayList.get(0).productAttributes.size(); j++) {
                ArrayList<String> arrayList = new ArrayList<>();
                for (int i = 0; i < productDetailsArrayList.size(); i++) {
                    if (!arrayList.contains(productDetailsArrayList.get(i).productAttributes.get(j).Value)) {
                        arrayList.add(productDetailsArrayList.get(i).productAttributes.get(j).Value);
                    }
                }
                ArrayAdapter adapter = new ArrayAdapter<String>(ProductOrdering.this, android.R.layout.simple_spinner_item, arrayList);
                spinners.get(j).setAdapter(adapter);
            }
            for (int i = 0; i < spinners.size(); i++) {
                final int Fi = i;
                spinners.get(i).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (Fi + 1 < spinners.size()) {
                            //setup values

                        ArrayList<String> arrayList = new ArrayList<>();
                            for (int j = 0; j < productDetailsArrayList.size(); j++) {
                                Boolean flag1 = true;
                                for (int k = 0; k <= Fi; k++) {
                                        if(!(productDetailsArrayList.get(j).productAttributes.get(k).Value
                                                .equals(spinners.get(k).getSelectedItem().toString()))){
                                            flag1=false;
                                            break;
                                        }
                                }
                                if(flag1){
                                    if (!arrayList.contains(productDetailsArrayList.get(j).productAttributes.get(Fi + 1).Value)) {
                                        arrayList.add(productDetailsArrayList.get(j).productAttributes.get(Fi + 1).Value);
                                    }
                                }
                            }
                            ArrayAdapter adapter = new ArrayAdapter<String>(ProductOrdering.this, android.R.layout.simple_spinner_item, arrayList);
                            spinners.get(Fi + 1).setAdapter(adapter);
                        }
                        //Checking price and stock
                        if(!actionType.equals("Q")){ //Quotable product doesn't show price and stock info
                            for (int j = 0; j < productDetailsArrayList.size(); j++) {
                                Boolean flag=true;
                                for (int k=0;k<productDetailsArrayList.get(j).productAttributes.size();k++){
                                    if(productDetailsArrayList.get(j).productAttributes.get(k).Value
                                        .equals(spinners.get(k).getSelectedItem().toString())){
                                    }
                                    else {
                                        flag=false;
                                        break;
                                    }
                                }
                                if(flag){
                                    //display changes
                                    if(productDetailsArrayList.get(j).stockAvailable){
                                        if(productDetailsArrayList.get(j).quantity>0){
                                            inStock=true;
                                        }
                                        else {
                                            inStock=false;
                                        }
                                    }
                                    else {
                                        inStock=false;
                                    }
                                    calculatePrice(productDetailsArrayList.get(j).PriceDifference,productDetailsArrayList.get(j).DiscountAmount);
                                    selectedProductDetailID=productDetailsArrayList.get(j).ID;
                                }
                            }
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }
        //Order attributes
        if(orderAttributesArrayList.size()!=0){
            for (int i=0;i<orderAttributesArrayList.size();i++){
                TextView label = new TextView(ProductOrdering.this);
                label.setText(orderAttributesArrayList.get(i).Caption);
                label.setPadding(5, 5, 5, 0);
                attributesLinear.addView(label);

                switch (orderAttributesArrayList.get(i).DataType){
                    case "C":
                        Spinner spinner=new Spinner(ProductOrdering.this);
                        ArrayList<String> arrayListForSpinner = new ArrayList<>(Arrays.asList(orderAttributesArrayList.get(i).Value.split("\\s*,\\s*")));
                        ArrayAdapter adapter = new ArrayAdapter<String>(ProductOrdering.this, android.R.layout.simple_spinner_item, arrayListForSpinner);
                        spinner.setAdapter(adapter);

                        orderAttributesUserInputs.add(spinner);
                        attributesLinear.addView(spinner);
                        break;
                    case "S":
                        EditText stringText=new EditText(ProductOrdering.this);
                        stringText.setPadding(5,0,5,5);

                        orderAttributesUserInputs.add(stringText);
                        attributesLinear.addView(stringText);
                        break;
                    case "N":
                        EditText stringNumber=new EditText(ProductOrdering.this);
                        stringNumber.setPadding(5,0,5,5);
                        stringNumber.setInputType(InputType.TYPE_CLASS_NUMBER);

                        orderAttributesUserInputs.add(stringNumber);
                        attributesLinear.addView(stringNumber);
                        break;
                    case "D":
                        final EditText date=new EditText(ProductOrdering.this);
                        date.setHint(R.string.select_date);
                        date.setPadding(5,0,5,5);
                        date.setFocusable(false);
                        date.setTextSize(15);
                        date.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Calendar today = Calendar.getInstance();
                                final Calendar selectedDate=Calendar.getInstance();
                                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        selectedDate.set(Calendar.YEAR, year);
                                        selectedDate.set(Calendar.MONTH, monthOfYear);
                                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                        //Validation--------------
                                        if(selectedDate.before(today)){
                                            Toast.makeText(ProductOrdering.this, R.string.give_valid, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        //Setting display text-------
                                        SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                                        date.setText(formatted.format(selectedDate.getTime()));
                                    }
                                };
                                new DatePickerDialog(ProductOrdering.this, dateSetListener, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show();
                            }
                        });

                        orderAttributesUserInputs.add(date);
                        attributesLinear.addView(date);
                        break;
                }
            }
        }
    }
    String price="";
    void calculatePrice(Double priceDifference, Double discountAmount){
        (findViewById(R.id.price_n_stock)).setVisibility(View.VISIBLE);
        if(showPrice) {
            String priceString = String.format(Locale.US, "%.2f",
                    baseSellingPrice
                    + priceDifference
                    - discountAmount);
            ((TextView) findViewById(R.id.price)).setText(getString(R.string.price_display, priceString));
            price=priceString;
            if (discountAmount != 0) {
                String actualPriceString = String.format(Locale.US, "%.2f",
                        baseSellingPrice
                        + priceDifference);
                ((TextView) findViewById(R.id.actual_price)).setText(getString(R.string.price_display, actualPriceString));
            }
            else {
                (findViewById(R.id.actual_price)).setVisibility(GONE);
            }
        }
        else {
            (findViewById(R.id.price)).setVisibility(GONE);
            (findViewById(R.id.actual_price)).setVisibility(GONE);
        }
        if (inStock) {
            ((TextView) findViewById(R.id.stock_availability)).setText(R.string.in_stock);
            ((TextView) findViewById(R.id.stock_availability)).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        else {
            ((TextView) findViewById(R.id.stock_availability)).setText(R.string.out_of_stock);
            ((TextView) findViewById(R.id.stock_availability)).setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
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
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
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
                                final Common common1=new Common();
                                //Threading--------------------------------------------------
                                String webService="api/customer/GetCustomerAddress";
                                String postData =  "{\"CustomerID\":\""+customerID+"\"}";
                                final ProgressDialog progressDialog=new ProgressDialog(ProductOrdering.this);
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
                                        AlertDialog.Builder selectAddressAlert = new AlertDialog.Builder(ProductOrdering.this);
                                        selectAddressAlert.setIcon(R.drawable.user);
                                        selectAddressAlert.setTitle(R.string.select_address);
                                        CustomAdapter customAdapter=new CustomAdapter(ProductOrdering.this,common1.dataArrayList,"Address");
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
                                            }
                                        });
                                        selectAddressAlert.setPositiveButton(R.string.new_address,  new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                inputNewAddress();
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
                                        Toast.makeText(ProductOrdering.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    }
                                };
                                common1.AsynchronousThread(ProductOrdering.this,
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
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.customer_address)).setVisibility(GONE);
            }
        };
        common.AsynchronousThread(ProductOrdering.this,
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
        final ProgressDialog progressDialog=new ProgressDialog(ProductOrdering.this);
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
                        AlertDialog.Builder newAddressDialogue = new AlertDialog.Builder(ProductOrdering.this);
                        newAddressDialogue.setIcon(R.drawable.user);
                        newAddressDialogue.setTitle(R.string.new_address);
                        final View newAddressView=inflater.inflate(R.layout.item_address_input, null);
                        ArrayAdapter locationAdapter = new ArrayAdapter<String>(ProductOrdering.this, android.R.layout.simple_spinner_item, locations);
                        ArrayAdapter countryAdapter = new ArrayAdapter<String>(ProductOrdering.this, android.R.layout.simple_spinner_item, countries);
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
                                                    "\"CustomerID\":\"" + customerAddress.CustomerID + "\"," +
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
                                                    + "}";//Replace with customer id TODO
                                            progressDialog.show();
                                            String[] dataColumns = {"ReturnValues"};
                                            Runnable postThread = new Runnable() {
                                                @Override
                                                public void run() {
                                                    //Displaying
                                                    try {
                                                        JSONObject jsonObject=new JSONObject(common3.json);
                                                        String addressID=jsonObject.optString("ReturnValues");

                                                    setAddressDisplayAndObject(addressView,
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
                                                        Toast.makeText(ProductOrdering.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            };
                                            Runnable postFailThread = new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(ProductOrdering.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                                                    if (progressDialog.isShowing())
                                                        progressDialog.dismiss();
                                                }
                                            };
                                            common3.AsynchronousThread(ProductOrdering.this,
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
                        Toast.makeText(ProductOrdering.this,R.string.some_error_at_server,Toast.LENGTH_SHORT).show();
                    }
                };
                common2.AsynchronousThread(ProductOrdering.this,
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
                Toast.makeText(ProductOrdering.this,R.string.some_error_at_server,Toast.LENGTH_SHORT).show();
            }
        };
        common1.AsynchronousThread(ProductOrdering.this,
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
    private class ProductDetails{
        String ID;
        Double PriceDifference=0.0,DiscountAmount=0.0;
        Boolean stockAvailable;
        int quantity;
        ArrayList<Attributes> productAttributes;
    }
    private class Attributes
    {
    String Name;
    String Caption;
    String Value;
    String DataType;
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
    public void proceedClick(final View view){
        if(actionType.equals("A")) {//------------------------------------------Buying--------------------------------------

            view.setVisibility(GONE);
            //----------------------JSON Making---------------------------
            String attributeValuesJSON = "\"AttributeValues\":[";

            //Product detail attributes------------
            String productDetailAttributeJson=getProductDetailAttributeValuesFromSpinners();
            attributeValuesJSON+=productDetailAttributeJson;
            //OrderAttributes
            for (int i = 0; i < orderAttributesArrayList.size(); i++) {
                String attributeJsonObject = "{" +
                        "\"Name\":\"" + orderAttributesArrayList.get(i).Name + "\"," +
                        "\"Caption\":\"" + orderAttributesArrayList.get(i).Caption + "\"," +
                        "\"Value\":\"" + ((orderAttributesArrayList.get(i).DataType.equals("C")) ? (((Spinner) orderAttributesUserInputs.get(i)).getSelectedItem().toString()) : (((EditText) orderAttributesUserInputs.get(i)).getText().toString())) + "\"," +
                        "\"DataType\":\"" + orderAttributesArrayList.get(i).DataType + "\"," +
                        "\"Isconfigurable\":\"false\"" +
                        "}";
                attributeValuesJSON += attributeJsonObject + ",";
            }
            if (attributeValuesJSON.lastIndexOf(",") > 0) {
                attributeValuesJSON = attributeValuesJSON.substring(0, attributeValuesJSON.lastIndexOf(","));
            }
            attributeValuesJSON += "]";


            //Threading--------------------------------------------------
            String webService = "api/Order/AddProductToCart";
            String postData = "{\"ProductID\":\"" + productID
                    + "\",\"CustomerID\":\"" + customerID
                    + "\",\"Price\":\"" + price
                    + "\",\"Qty\":\"" + 1
                    + "\"," + attributeValuesJSON
                    + "}";//Replace with customer id TODO
            AVLoadingIndicatorView loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.loading_indicator_proceed);
            String[] dataColumns = {};
            Runnable postThread = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ProductOrdering.this, R.string.success, Toast.LENGTH_SHORT).show();//TODO navigate
                    Intent clearIntent=new Intent(ProductOrdering.this,Home.class);
                    clearIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(clearIntent);
                    finish();
                }
            };
            Runnable postFailThread = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ProductOrdering.this, R.string.failed, Toast.LENGTH_SHORT).show();
                    view.setVisibility(View.VISIBLE);
                }
            };
            common.AsynchronousThread(ProductOrdering.this,
                    webService,
                    postData,
                    loadingIndicator,
                    dataColumns,
                    postThread,
                    postFailThread);
        }
        else if(actionType.equals("B")) {//--------------------------------------Booking--------------------------------------
            //Validation----
            EditText requiredDate = (EditText) findViewById(R.id.required_date);
            if (requiredDate.getText().toString().equals("")) {
                requiredDate.setError(getResources().getString(R.string.give_valid));
                requiredDate.requestFocus();
                return;
            }

            view.setVisibility(GONE);
            //----------------------JSON Making---------------------------
            String attributeValuesJSON = "\"AttributeValues\":[";

            //Product detail attributes------------
            String productDetailAttributeJson=getProductDetailAttributeValuesFromSpinners();
            attributeValuesJSON+=productDetailAttributeJson;
            //OrderAttributes
            for (int i = 0; i < orderAttributesArrayList.size(); i++) {
                String attributeJsonObject = "{" +
                        "\"Name\":\"" + orderAttributesArrayList.get(i).Name + "\"," +
                        "\"Caption\":\"" + orderAttributesArrayList.get(i).Caption + "\"," +
                        "\"Value\":\"" + ((orderAttributesArrayList.get(i).DataType.equals("C")) ? (((Spinner) orderAttributesUserInputs.get(i)).getSelectedItem().toString()) : (((EditText) orderAttributesUserInputs.get(i)).getText().toString())) + "\"," +
                        "\"DataType\":\"" + orderAttributesArrayList.get(i).DataType + "\"," +
                        "\"Isconfigurable\":\"false\"" +
                        "}";
                attributeValuesJSON += attributeJsonObject + ",";
            }
            if (attributeValuesJSON.lastIndexOf(",") > 0) {
                attributeValuesJSON = attributeValuesJSON.substring(0, attributeValuesJSON.lastIndexOf(","));
            }
            attributeValuesJSON += "]";

            //Customer Address-----------------
            String customerAddressJSON = "\"CustomerAddress\":{";
            customerAddressJSON+="\"ID\":\"" + customerAddress.ID + "\"," +
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
                                    "\"ContactNo\":\"" + customerAddress.ContactNo + "\"}" ;

            //Threading--------------------------------------------------
            String webService = "api/Order/InsertBookings";
            String postData = "{\"ProductID\":\"" + productID
                    + "\",\"CustomerID\":\"" + customerID
                    + "\",\"RequiredDate\":\"" + requiredDate.getText().toString()
                    + "\",\"SourceIP\":\"" + getLocalIpAddress()
                    + "\",\"Message\":\"" + ((EditText) findViewById(R.id.quote_message)).getText().toString()
                    + "\",\"Price\":\"" + price
                    + "\",\"Qty\":\"" + 1
                    + "\"," + attributeValuesJSON
                    + "," + customerAddressJSON
                    + "}";//Replace with customer id TODO
            AVLoadingIndicatorView loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.loading_indicator_proceed);
            String[] dataColumns = {};
            Runnable postThread = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ProductOrdering.this, R.string.success, Toast.LENGTH_SHORT).show();//TODO navigate to bookings
                    Intent clearIntent=new Intent(ProductOrdering.this,Home.class);
                    clearIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(clearIntent);
                    finish();
                }
            };
            Runnable postFailThread = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ProductOrdering.this, R.string.failed, Toast.LENGTH_SHORT).show();
                    view.setVisibility(View.VISIBLE);
                }
            };
            common.AsynchronousThread(ProductOrdering.this,
                    webService,
                    postData,
                    loadingIndicator,
                    dataColumns,
                    postThread,
                    postFailThread);
        }
        else if(actionType.equals("Q")) {//--------------------------------Quotation-------------------------------------------------
            //Validation----
            EditText requiredDate = (EditText) findViewById(R.id.required_date);
            if (requiredDate.getText().toString().equals("")) {
                requiredDate.setError(getResources().getString(R.string.give_valid));
                requiredDate.requestFocus();
                return;
            }

            view.setVisibility(GONE);
            //----------------------JSON Making---------------------------
            String attributeValuesJSON = "\"AttributeValues\":[";

            //Product detail attributes------------
            String productDetailAttributeJson=getProductDetailAttributeValuesFromSpinners();
            attributeValuesJSON+=productDetailAttributeJson;
            //OrderAttributes
            for (int i = 0; i < orderAttributesArrayList.size(); i++) {
                String attributeJsonObject = "{" +
                        "\"Name\":\"" + orderAttributesArrayList.get(i).Name + "\"," +
                        "\"Caption\":\"" + orderAttributesArrayList.get(i).Caption + "\"," +
                        "\"Value\":\"" + ((orderAttributesArrayList.get(i).DataType.equals("C")) ? (((Spinner) orderAttributesUserInputs.get(i)).getSelectedItem().toString()) : (((EditText) orderAttributesUserInputs.get(i)).getText().toString())) + "\"," +
                        "\"DataType\":\"" + orderAttributesArrayList.get(i).DataType + "\"," +
                        "\"Isconfigurable\":\"false\"" +
                        "}";
                attributeValuesJSON += attributeJsonObject + ",";
            }
            if (attributeValuesJSON.lastIndexOf(",") > 0) {
                attributeValuesJSON = attributeValuesJSON.substring(0, attributeValuesJSON.lastIndexOf(","));
            }
            attributeValuesJSON += "]";


            //Threading--------------------------------------------------
            String webService = "api/Order/InsertQuotations";
            String postData = "{\"ProductID\":\"" + productID
                    + "\",\"CustomerID\":\"" + customerID
                    + "\",\"RequiredDate\":\"" + requiredDate.getText().toString()
                    + "\",\"SourceIP\":\"" + getLocalIpAddress()
                    + "\",\"Message\":\"" + ((EditText) findViewById(R.id.quote_message)).getText().toString()
                    + "\"," + attributeValuesJSON
                    + "}";//Replace with customer id TODO
            AVLoadingIndicatorView loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.loading_indicator_proceed);
            String[] dataColumns = {};
            Runnable postThread = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ProductOrdering.this, R.string.success, Toast.LENGTH_SHORT).show();//TODO navigate to quotations
                    Intent clearIntent=new Intent(ProductOrdering.this,Home.class);
                    clearIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(clearIntent);
                    finish();
                }
            };
            Runnable postFailThread = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ProductOrdering.this, R.string.failed, Toast.LENGTH_SHORT).show();
                    view.setVisibility(View.VISIBLE);
                }
            };
            common.AsynchronousThread(ProductOrdering.this,
                    webService,
                    postData,
                    loadingIndicator,
                    dataColumns,
                    postThread,
                    postFailThread);
        }
    }
    String getProductDetailAttributeValuesFromSpinners(){
        String json="";
        for (int i = 0; i < spinners.size(); i++) {
            String attributeJsonObject = "{" +
                    "\"Name\":\"" + productDetailsArrayList.get(0).productAttributes.get(i).Name + "\"," +
                    "\"Caption\":\"" + productDetailsArrayList.get(0).productAttributes.get(i).Caption + "\"," +
                    "\"Value\":\"" + spinners.get(i).getSelectedItem().toString() + "\"," +
                    "\"DataType\":\"" + productDetailsArrayList.get(0).productAttributes.get(i).DataType + "\"," +
                    "\"Isconfigurable\":\"false\"" +
                    "}";
            json += attributeJsonObject + ",";
        }
        return json;
    }
}
