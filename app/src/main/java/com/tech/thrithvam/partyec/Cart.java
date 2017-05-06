package com.tech.thrithvam.partyec;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
