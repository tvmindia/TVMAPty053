package com.tech.thrithvam.partyec;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

public class ManageAddresses extends AppCompatActivity {
    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_addresse);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.manage_addresses);
        db=DatabaseHandler.getInstance(this);
        getCustomerAddresses();
    }
    void getCustomerAddresses(){
        final Common common1=new Common();
        //Threading--------------------------------------------------
        String webService="api/customer/GetCustomerAddress";
        String postData =  "{\"CustomerID\":\""+db.GetCustomerDetails("CustomerID")+"\"}";
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
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
                CustomAdapter customAdapter=new CustomAdapter(ManageAddresses.this,common1.dataArrayList,"AddressManagement");
                ListView addressList=(ListView)findViewById(R.id.address_list_view);
                addressList.setAdapter(customAdapter);

            }
        };
        common1.AsynchronousThread(ManageAddresses.this,
                webService,
                postData,
                loadingIndicatorView,
                dataColumns,
                postThread,
                null);

    }
}
