package com.tech.thrithvam.partyec;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.view.View.GONE;

public class ProductOrdering extends AppCompatActivity {
    Common common=new Common();
    String productID="2042";
    ArrayList<ProductDetails> productDetailsArrayList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_ordering);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Order: "+getIntent().getExtras().getString("productName"));
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
                    JSONArray productDetails =jsonRootObject.optJSONArray("ProductDetails");
                    for (int i = 0; i < productDetails.length(); i++) {
                        ProductDetails productDetailsObj=new ProductDetails();

                        JSONObject jsonObject = productDetails.getJSONObject(i);
                        productDetailsObj.ID=jsonObject.optString("ID");

                        JSONArray productAttributes=jsonObject.optJSONArray("ProductAttributes");
                        productDetailsObj.productAttributes=new ArrayList<>();
                        for (int j=0;j<productAttributes.length();j++){
                            JSONObject proAttriObj=productAttributes.getJSONObject(j);
                            ProductAttributes prodDetAttr=new ProductAttributes();
                            prodDetAttr.Name=proAttriObj.optString("Name");
                            prodDetAttr.Caption=proAttriObj.optString("Caption");
                            prodDetAttr.Value=proAttriObj.optString("Value");
                            prodDetAttr.DataType=proAttriObj.optString("DataType");
                            productDetailsObj.productAttributes.add(prodDetAttr);
                        }
                        productDetailsArrayList.add(productDetailsObj);
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
        //ProductAttributes
        LinearLayout attributesLinear=(LinearLayout)findViewById(R.id.prod_attributes_linear);
        //arrange user controls
        final ArrayList<Spinner> spinners=new ArrayList<>();
        for(int i=0;i<productDetailsArrayList.get(0).productAttributes.size();i++){
            TextView label=new TextView(ProductOrdering.this);
            label.setText(productDetailsArrayList.get(0).productAttributes.get(i).Caption);
            label.setPadding(5,5,5,0);
            attributesLinear.addView(label);
            Spinner spinner=new Spinner(ProductOrdering.this);
            attributesLinear.addView(spinner);
            spinners.add(spinner);
        }
        //setup values
        for(int j=0;j<productDetailsArrayList.get(0).productAttributes.size();j++){
            ArrayList<String> arrayList=new ArrayList<>();
            for(int i=0;i<productDetailsArrayList.size();i++){
                if(!arrayList.contains(productDetailsArrayList.get(i).productAttributes.get(j).Value)){
                    arrayList.add(productDetailsArrayList.get(i).productAttributes.get(j).Value);
                }
            }
            ArrayAdapter adapter = new ArrayAdapter<String>(ProductOrdering.this, android.R.layout.simple_spinner_item, arrayList);
            spinners.get(j).setAdapter(adapter);
        }
        for(int i=0;i<spinners.size();i++){
            final int Fi=i;
            spinners.get(i).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(Fi+1<spinners.size()){
                        //setup values
                            ArrayList<String> arrayList=new ArrayList<>();
                            for(int i=0;i<productDetailsArrayList.size();i++){
                                if(productDetailsArrayList.get(i).productAttributes.get(Fi).Value.equals(spinners.get(Fi).getSelectedItem().toString())){
                                        if(!arrayList.contains(productDetailsArrayList.get(i).productAttributes.get(Fi+1).Value)){
                                            arrayList.add(productDetailsArrayList.get(i).productAttributes.get(Fi+1).Value);
                                        }
                                }
                            }
                            ArrayAdapter adapter = new ArrayAdapter<String>(ProductOrdering.this, android.R.layout.simple_spinner_item, arrayList);
                            spinners.get(Fi+1).setAdapter(adapter);

                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }
    class ProductDetails{
        String ID;
        ArrayList<ProductAttributes> productAttributes;
    }
    class ProductAttributes
    {
    String Name;
    String Caption;
    String Value;
    String DataType;
    }
}
