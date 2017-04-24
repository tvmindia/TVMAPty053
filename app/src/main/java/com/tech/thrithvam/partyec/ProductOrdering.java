package com.tech.thrithvam.partyec;

import android.app.DatePickerDialog;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ProductOrdering extends AppCompatActivity {
    Common common=new Common();
    String productID="";
    ArrayList<ProductDetails> productDetailsArrayList=new ArrayList<>();
    ArrayList<Attributes> orderAttributesArrayList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_ordering);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Order: "+getIntent().getExtras().getString("productName"));
        productID=getIntent().getExtras().getString("productID");
        getProductDetailsForOrder();
    }
    void getProductDetailsForOrder(){
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
                    //Product details for product attributes
                    JSONArray productDetails =jsonRootObject.optJSONArray("ProductDetails");
                    for (int i = 0; i < productDetails.length(); i++) {
                        ProductDetails productDetailsObj=new ProductDetails();

                        JSONObject jsonObject = productDetails.getJSONObject(i);
                        productDetailsObj.ID=jsonObject.optString("ID");

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

                    //OtherAttributes
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setupUserControls();
            }
        };
        common.AsynchronousThread(ProductOrdering.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void setupUserControls(){
        //Attributes
        LinearLayout attributesLinear=(LinearLayout)findViewById(R.id.prod_attributes_linear);
        //arrange user controls
        //Product Attributes
        if(productDetailsArrayList.size()!=0) {
            final ArrayList<Spinner> spinners = new ArrayList<>();
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
            ArrayList<View> orderAttributesUserInputs=new ArrayList<>();
            for (int i=0;i<orderAttributesArrayList.size();i++){
                TextView label = new TextView(ProductOrdering.this);
                label.setText(orderAttributesArrayList.get(i).Caption);
                label.setPadding(5, 5, 5, 0);
                attributesLinear.addView(label);

                switch (orderAttributesArrayList.get(i).DataType){
                    case "C":
                        break;
                    case "S":
                        break;
                    case "N":
                        break;
                    case "D":
                        final TextView date=new TextView(ProductOrdering.this);
                        date.setText(R.string.select_date);
                        date.setPadding(5,0,5,5);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            date.setTextColor(getColor(R.color.colorAccent));
                        }
                        else {
                            date.setTextColor(getResources().getColor(R.color.colorAccent));
                        }
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
    private class ProductDetails{
        String ID;
        ArrayList<Attributes> productAttributes;
    }
    private class Attributes
    {
    String Name;
    String Caption;
    String Value;
    String DataType;
    }
}
