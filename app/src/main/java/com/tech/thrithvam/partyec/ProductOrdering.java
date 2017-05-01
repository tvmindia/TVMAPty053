package com.tech.thrithvam.partyec;

import android.app.DatePickerDialog;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_ordering);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Order: "+getIntent().getExtras().getString("productName",""));
        productID=getIntent().getExtras().getString("productID");
        (findViewById(R.id.price_n_stock)).setVisibility(View.GONE);
        TextView actualPrice=(TextView)findViewById(R.id.actual_price);
        actualPrice.setPaintFlags(actualPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        if(!getIntent().getExtras().getString("actionType").equals("A")){//Not a buyable product
            (findViewById(R.id.price_n_stock)).setVisibility(GONE);
            if(getIntent().getExtras().getString("actionType").equals("Q")||getIntent().getExtras().getString("actionType").equals("B")){//Quotable or Bookable product
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
                if(getIntent().getExtras().getString("actionType").equals("B")){
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
                            for (int i = 0; i < productDetailsArrayList.size(); i++) {
                                if (productDetailsArrayList.get(i).productAttributes.get(Fi).Value.equals(spinners.get(Fi).getSelectedItem().toString())) {
                                    if (!arrayList.contains(productDetailsArrayList.get(i).productAttributes.get(Fi + 1).Value)) {
                                        arrayList.add(productDetailsArrayList.get(i).productAttributes.get(Fi + 1).Value);
                                    }
                                    //display changes
                                    if(productDetailsArrayList.get(i).stockAvailable){
                                        if(productDetailsArrayList.get(i).quantity>0){
                                            inStock=true;
                                        }
                                        else {
                                            inStock=false;
                                        }
                                    }
                                    else {
                                        inStock=false;
                                    }
                                    calculatePrice(productDetailsArrayList.get(i).PriceDifference,productDetailsArrayList.get(i).DiscountAmount);
                                    selectedProductDetailID=productDetailsArrayList.get(i).ID;
                                }
                            }
                            ArrayAdapter adapter = new ArrayAdapter<String>(ProductOrdering.this, android.R.layout.simple_spinner_item, arrayList);
                            spinners.get(Fi + 1).setAdapter(adapter);
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
    void calculatePrice(Double priceDifference, Double discountAmount){
        (findViewById(R.id.price_n_stock)).setVisibility(View.VISIBLE);
        if(showPrice) {
            String priceString = String.format(Locale.US, "%.2f",
                    baseSellingPrice
                    + priceDifference
                    - discountAmount);
            ((TextView) findViewById(R.id.price)).setText(getString(R.string.price_display, priceString));
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
    int selectedAddress;
    void getCustomerAddress(){
        final Common common=new Common();
        //Threading--------------------------------------------------
        String webService="api/customer/GetCustomerAddress";
        String postData =  "{\"CustomerID\":\""+1009+"\"}";
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
                "ShipDefaultYN"//12
                };
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<common.dataArrayList.size();i++){
                    if(common.dataArrayList.get(i)[11].equals("true")){
                        selectedAddress=i;
                        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final View addressView=inflater.inflate(R.layout.item_address, null);
                        String name=(common.dataArrayList.get(i)[1].equals("null")?"":common.dataArrayList.get(i)[1])
                                +   (common.dataArrayList.get(i)[2].equals("null")?"":common.dataArrayList.get(i)[2])
                                +   (common.dataArrayList.get(i)[3].equals("null")?"":common.dataArrayList.get(i)[3])
                                +   (common.dataArrayList.get(i)[4].equals("null")?"":common.dataArrayList.get(i)[4]);
                        ((TextView)addressView.findViewById(R.id.name)).setText(name);
                        ((TextView)addressView.findViewById(R.id.address)).setText(common.dataArrayList.get(i)[5].equals("null")?"":common.dataArrayList.get(i)[5]);
                        ((TextView)addressView.findViewById(R.id.location)).setText(common.dataArrayList.get(i)[6].equals("null")?"-":common.dataArrayList.get(i)[6]);
                        ((TextView)addressView.findViewById(R.id.city)).setText(common.dataArrayList.get(i)[7].equals("null")?"-":common.dataArrayList.get(i)[7]);
                        ((TextView)addressView.findViewById(R.id.stateprovince)).setText(common.dataArrayList.get(i)[8].equals("null")?"-":common.dataArrayList.get(i)[8]);
                        String country="";
                        try {
                            JSONObject jsonObjectCountry=new JSONObject(common.dataArrayList.get(i)[9]);
                            country=jsonObjectCountry.getString("Name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ((TextView)addressView.findViewById(R.id.country)).setText(country.equals("null")?"-":country);
                        ((TextView)addressView.findViewById(R.id.contact_no)).setText(common.dataArrayList.get(i)[10].equals("null")?"-":common.dataArrayList.get(i)[10]);
                        ((LinearLayout)findViewById(R.id.customer_address_linear)).addView(addressView);
                         final int Fi=i;
                        (findViewById(R.id.change_address)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builderSingle = new AlertDialog.Builder(ProductOrdering.this);
                                builderSingle.setIcon(R.drawable.user);
                                builderSingle.setTitle("Select address");
                                CustomAdapter customAdapter=new CustomAdapter(ProductOrdering.this,common.dataArrayList,"Address");
                                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builderSingle.setAdapter(customAdapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String name=(common.dataArrayList.get(which)[1].equals("null")?"":common.dataArrayList.get(which)[1])
                                                +   (common.dataArrayList.get(which)[2].equals("null")?"":common.dataArrayList.get(which)[2])
                                                +   (common.dataArrayList.get(which)[3].equals("null")?"":common.dataArrayList.get(which)[3])
                                                +   (common.dataArrayList.get(which)[4].equals("null")?"":common.dataArrayList.get(which)[4]);
                                        ((TextView)addressView.findViewById(R.id.name)).setText(name);
                                        ((TextView)addressView.findViewById(R.id.address)).setText(common.dataArrayList.get(which)[5].equals("null")?"":common.dataArrayList.get(which)[5]);
                                        ((TextView)addressView.findViewById(R.id.location)).setText(common.dataArrayList.get(which)[6].equals("null")?"-":common.dataArrayList.get(which)[6]);
                                        ((TextView)addressView.findViewById(R.id.city)).setText(common.dataArrayList.get(which)[7].equals("null")?"-":common.dataArrayList.get(which)[7]);
                                        ((TextView)addressView.findViewById(R.id.stateprovince)).setText(common.dataArrayList.get(which)[8].equals("null")?"-":common.dataArrayList.get(which)[8]);
                                        String country="";
                                        try {
                                            JSONObject jsonObjectCountry=new JSONObject(common.dataArrayList.get(which)[9]);
                                            country=jsonObjectCountry.getString("Name");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        ((TextView)addressView.findViewById(R.id.country)).setText(country.equals("null")?"-":country);
                                        ((TextView)addressView.findViewById(R.id.contact_no)).setText(common.dataArrayList.get(which)[10].equals("null")?"-":common.dataArrayList.get(which)[10]);
                                    }
                                });
                                builderSingle.show();
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
    public void proceedClick(final View view){
        if(getIntent().getExtras().getString("actionType").equals("Q")) {
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
                    + "\",\"CustomerID\":\"" + 1010
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
