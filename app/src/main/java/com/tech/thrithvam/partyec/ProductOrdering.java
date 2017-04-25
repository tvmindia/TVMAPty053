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
import android.widget.EditText;
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
                    ///TO BE DELETED

                    common.json="{\"ID\":2042,\"Name\":\"samsung LED 4567\",\"SKU\":\"samsung LED 2356\",\"Enabled\":true,\"Unit\":\"number\",\"URL\":null,\"ActionType\":\"A\",\"SupplierID\":1,\"suppliers\":null,\"ManufacturerID\":3,\"manufacturers\":null,\"ProductType\":\"C\",\"AttributeSetID\":2019,\"AttributeSetName\":\"TV\",\"AttributeSets\":null,\"FreeDelivery\":false,\"TotalQty\":0,\"productDetailhdf\":null,\"CostPrice\":0.00,\"BaseSellingPrice\":250.00,\"ShowPrice\":false,\"TaxClass\":null,\"DiscountAmount\":null,\"DiscountStartDate\":null,\"DiscountEndDate\":null,\"ProdutMainImageUpload\":null,\"OtherImagesUpload\":null,\"ProductStickerUpload\":null,\"StickerID\":null,\"ImageID\":0,\"ImageURL\":null,\"ProductDetID\":0,\"MainImage\":false,\"IDSet\":null,\"ShortDescription\":\"sdfdsfds\",\"LongDescription\":\"dsfdsfdsghjhkjhkhj\",\"StockAvailable\":true,\"Qty\":null,\"OutOfStockAlertQty\":null,\"HeaderTags\":\"34,23,45,23,45,12\",\"StickerURL\":null,\"LinkID\":0,\"CategoryID\":0,\"PositionNo\":0.0,\"logDetails\":null,\"ProductDetails\":[{\"ID\":27,\"ProductID\":2042,\"Qty\":5400,\"OutOfStockAlertQty\":570,\"PriceDifference\":null,\"DetailTags\":null,\"Enabled\":true,\"StockAvailable\":true,\"DefaultOption\":false,\"DiscountAmount\":50.00,\"DiscountStartDate\":\"2017-03-05T00:00:00\",\"DiscountEndDate\":\"2017-03-14T00:00:00\",\"logDetails\":{\"CreatedBy\":\"Albert Thomson\",\"CreatedDate\":\"2017-03-24T17:16:00\",\"UpdatedBy\":null,\"UpdatedDate\":null},\"ProductAttributes\":[{\"Name\":\"ScreenSize\",\"Caption\":\"Screen Size\",\"Value\":\"\",\"DataType\":\"C\",\"Isconfigurable\":true},{\"Name\":\"WallMoutable\",\"Caption\":\"WallMoutable\",\"Value\":\"\",\"DataType\":\"C\",\"Isconfigurable\":true}],\"ProductDetailImages\":[],\"ProductName\":\"samsung LED 4567\",\"BaseSellingPrice\":250.00,\"ActualPrice\":null},{\"ID\":28,\"ProductID\":2042,\"Qty\":5400,\"OutOfStockAlertQty\":570,\"PriceDifference\":54321.00,\"DetailTags\":null,\"Enabled\":false,\"StockAvailable\":true,\"DefaultOption\":false,\"DiscountAmount\":50.00,\"DiscountStartDate\":\"2017-03-05T00:00:00\",\"DiscountEndDate\":\"2017-03-14T00:00:00\",\"logDetails\":{\"CreatedBy\":\"Albert Thomson\",\"CreatedDate\":\"2017-03-24T17:16:00\",\"UpdatedBy\":null,\"UpdatedDate\":null},\"ProductAttributes\":[{\"Name\":\"ScreenSize\",\"Caption\":\"Screen Size\",\"Value\":\"20\",\"DataType\":\"C\",\"Isconfigurable\":true},{\"Name\":\"WallMoutable\",\"Caption\":\"WallMoutable\",\"Value\":\"Yes\",\"DataType\":\"C\",\"Isconfigurable\":true}],\"ProductDetailImages\":[],\"ProductName\":\"samsung LED 4567\",\"BaseSellingPrice\":250.00,\"ActualPrice\":54571.00},{\"ID\":29,\"ProductID\":2042,\"Qty\":77,\"OutOfStockAlertQty\":65,\"PriceDifference\":506.00,\"DetailTags\":null,\"Enabled\":false,\"StockAvailable\":true,\"DefaultOption\":false,\"DiscountAmount\":50.00,\"DiscountStartDate\":\"2017-03-29T00:00:00\",\"DiscountEndDate\":\"2017-03-12T00:00:00\",\"logDetails\":{\"CreatedBy\":\"Albert Thomson\",\"CreatedDate\":\"2017-03-24T17:29:00\",\"UpdatedBy\":null,\"UpdatedDate\":null},\"ProductAttributes\":[{\"Name\":\"ScreenSize\",\"Caption\":\"Screen Size\",\"Value\":\"20\",\"DataType\":\"C\",\"Isconfigurable\":true},{\"Name\":\"WallMoutable\",\"Caption\":\"WallMoutable\",\"Value\":\"Yes\",\"DataType\":\"C\",\"Isconfigurable\":true}],\"ProductDetailImages\":[],\"ProductName\":\"samsung LED 4567\",\"BaseSellingPrice\":250.00,\"ActualPrice\":756.00},{\"ID\":30,\"ProductID\":2042,\"Qty\":77,\"OutOfStockAlertQty\":65,\"PriceDifference\":23.00,\"DetailTags\":null,\"Enabled\":false,\"StockAvailable\":true,\"DefaultOption\":false,\"DiscountAmount\":50.00,\"DiscountStartDate\":\"2017-03-29T00:00:00\",\"DiscountEndDate\":\"2017-03-12T00:00:00\",\"logDetails\":{\"CreatedBy\":\"Albert Thomson\",\"CreatedDate\":\"2017-03-24T17:30:00\",\"UpdatedBy\":null,\"UpdatedDate\":null},\"ProductAttributes\":[{\"Name\":\"ScreenSize\",\"Caption\":\"Screen Size\",\"Value\":\"20\",\"DataType\":\"C\",\"Isconfigurable\":true},{\"Name\":\"WallMoutable\",\"Caption\":\"WallMoutable\",\"Value\":\"Yes\",\"DataType\":\"C\",\"Isconfigurable\":true}],\"ProductDetailImages\":[],\"ProductName\":\"samsung LED 4567\",\"BaseSellingPrice\":250.00,\"ActualPrice\":273.00},{\"ID\":31,\"ProductID\":2042,\"Qty\":23,\"OutOfStockAlertQty\":45,\"PriceDifference\":456.00,\"DetailTags\":null,\"Enabled\":false,\"StockAvailable\":true,\"DefaultOption\":false,\"DiscountAmount\":50.00,\"DiscountStartDate\":\"2017-03-29T00:00:00\",\"DiscountEndDate\":\"2017-04-06T00:00:00\",\"logDetails\":{\"CreatedBy\":\"Albert Thomson\",\"CreatedDate\":\"2017-03-24T17:33:00\",\"UpdatedBy\":null,\"UpdatedDate\":null},\"ProductAttributes\":[{\"Name\":\"ScreenSize\",\"Caption\":\"Screen Size\",\"Value\":\"32\",\"DataType\":\"C\",\"Isconfigurable\":true},{\"Name\":\"WallMoutable\",\"Caption\":\"WallMoutable\",\"Value\":\"No\",\"DataType\":\"C\",\"Isconfigurable\":true}],\"ProductDetailImages\":[],\"ProductName\":\"samsung LED 4567\",\"BaseSellingPrice\":250.00,\"ActualPrice\":706.00},{\"ID\":32,\"ProductID\":2042,\"Qty\":435,\"OutOfStockAlertQty\":3443,\"PriceDifference\":654.00,\"DetailTags\":null,\"Enabled\":false,\"StockAvailable\":true,\"DefaultOption\":false,\"DiscountAmount\":50.00,\"DiscountStartDate\":\"2017-03-29T00:00:00\",\"DiscountEndDate\":\"2017-03-19T00:00:00\",\"logDetails\":{\"CreatedBy\":\"Albert Thomson\",\"CreatedDate\":\"2017-03-24T17:41:00\",\"UpdatedBy\":null,\"UpdatedDate\":null},\"ProductAttributes\":[{\"Name\":\"ScreenSize\",\"Caption\":\"Screen Size\",\"Value\":\"20\",\"DataType\":\"C\",\"Isconfigurable\":true},{\"Name\":\"WallMoutable\",\"Caption\":\"WallMoutable\",\"Value\":\"No\",\"DataType\":\"C\",\"Isconfigurable\":true}],\"ProductDetailImages\":[],\"ProductName\":\"samsung LED 4567\",\"BaseSellingPrice\":250.00,\"ActualPrice\":904.00},{\"ID\":34,\"ProductID\":2042,\"Qty\":45,\"OutOfStockAlertQty\":34,\"PriceDifference\":454.00,\"DetailTags\":null,\"Enabled\":false,\"StockAvailable\":true,\"DefaultOption\":false,\"DiscountAmount\":50.00,\"DiscountStartDate\":\"2017-03-28T00:00:00\",\"DiscountEndDate\":\"2017-03-22T00:00:00\",\"logDetails\":{\"CreatedBy\":\"Albert Thomson\",\"CreatedDate\":\"2017-03-27T12:21:00\",\"UpdatedBy\":null,\"UpdatedDate\":null},\"ProductAttributes\":[{\"Name\":\"ScreenSize\",\"Caption\":\"Screen Size\",\"Value\":\"20\",\"DataType\":\"C\",\"Isconfigurable\":true},{\"Name\":\"WallMoutable\",\"Caption\":\"WallMoutable\",\"Value\":\"Yes\",\"DataType\":\"C\",\"Isconfigurable\":true}],\"ProductDetailImages\":[],\"ProductName\":\"samsung LED 4567\",\"BaseSellingPrice\":250.00,\"ActualPrice\":704.00}],\"ProductDetailObj\":null,\"IDList\":null,\"ProductOtherAttributes\":[{\"Name\":\"NoOfUSB\",\"Caption\":\"NoOfUSB\",\"Value\":\"6\",\"DataType\":\"S\",\"Isconfigurable\":false}],\"OrderAttributes\":[{\"Name\":\"numorder\",\"Caption\":\"Numerical Order Attribute\",\"Value\":null,\"DataType\":\"N\",\"Isconfigurable\":false},{\"Name\":\"color\",\"Caption\":\"Color selection\",\"Value\":null,\"DataType\":\"C\",\"Isconfigurable\":false},{\"Name\":\"Message\",\"Caption\":\"Message\",\"Value\":null,\"DataType\":\"S\",\"Isconfigurable\":false},{\"Name\":\"DeliveryDate\",\"Caption\":\"DeliveryDate\",\"Value\":null,\"DataType\":\"D\",\"Isconfigurable\":false}],\"RatingAttributes\":[{\"Name\":\"vfm\",\"Caption\":\"value for money\",\"Value\":null,\"DataType\":\"N\",\"Isconfigurable\":false}],\"SupplierName\":\"Navya Bakerys\"}";

                    ///
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
                        EditText stringText=new EditText(ProductOrdering.this);
                        stringText.setPadding(5,0,5,5);

                        orderAttributesUserInputs.add(stringText);
                        attributesLinear.addView(stringText);
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
