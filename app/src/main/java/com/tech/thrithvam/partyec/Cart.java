package com.tech.thrithvam.partyec;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.wang.avi.AVLoadingIndicatorView;

public class Cart extends AppCompatActivity {
String customerID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.cart);
        customerID="1009";//TODO replace with customer ID
        loadCart();
    }
    void loadCart(){
        final Common common=new Common();
        final ListView cartListView=(ListView) findViewById(R.id.listview);
        cartListView.setSelector(android.R.color.transparent);
        (findViewById(R.id.cart_scrollview)).setVisibility(View.GONE);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerShoppingCart";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        String[] dataColumns={"ID","ProductID","ProductName","ImageURL","AttributeValues","Qty","Price","ShippingCharge"};
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.cart_scrollview)).setVisibility(View.VISIBLE);
                CustomAdapter adapter=new CustomAdapter(Cart.this, common.dataArrayList,"Cart");
                cartListView.setAdapter(adapter);
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
}
