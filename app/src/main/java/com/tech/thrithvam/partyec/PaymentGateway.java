package com.tech.thrithvam.partyec;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentGateway extends AppCompatActivity {
String orderID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_gateway);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.payment);
        orderID=getIntent().getExtras().getString("orderID");
        ((TextView)findViewById(R.id.order_id_text)).setText("Order No : " + orderID);
    }
    public void payClick(View view){
         final Common common = new Common();
        //Threading--------------------------------------------------
        String webService = "api/Order/GetPaymentStatus";
        String postData = "{}";
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
        String[] dataColumns = {};
        Runnable postThread = new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.payment)).setVisibility(View.GONE);
                (findViewById(R.id.order_status)).setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonObject=new JSONObject(common.json);
                    Boolean status=jsonObject.optBoolean("Status");
                    String reference=jsonObject.optString("Reference");
                    if(status){
                        ((TextView)findViewById(R.id.order_status_text)).setText("Payment Success\nReference No : "+reference);
                    }
                    else {
                        Toast.makeText(PaymentGateway.this, "Payment is not success", Toast.LENGTH_SHORT).show();
                    }
                    final Common common2 = new Common();
                    //Threading payment status--------------------------------------------------
                    String webService = "api/Order/UpdateOrderPaymentStatus";
                    String postData = "{\"ID\":\""+orderID+"\",\"PayStatusCode\":\""+(status?"1":"2")+"\"}";
                    AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
                    String[] dataColumns = {};
                    Runnable postThread = new Runnable() {
                        @Override
                        public void run() {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(PaymentGateway.this, ListViewsActivity.class);
                                    intent.putExtra("list", "orders");
                                    intent.putExtra("orderid", orderID);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 3000);
                        }
                    };
                    Runnable postFailThread = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PaymentGateway.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                        }
                    };
                    common2.AsynchronousThread(PaymentGateway.this,
                            webService,
                            postData,
                            loadingIndicatorView,
                            dataColumns,
                            postThread,
                            postFailThread);
                } catch (JSONException e) {
                    Toast.makeText(PaymentGateway.this, "Payment is not success", Toast.LENGTH_SHORT).show();
                }
            }
        };
        Runnable postFailThread = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PaymentGateway.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
            }
        };
        common.AsynchronousThread(PaymentGateway.this,
                webService,
                postData,
                loadingIndicatorView,
                dataColumns,
                postThread,
                postFailThread);
    }
}
