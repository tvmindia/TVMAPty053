package com.tech.thrithvam.partyec;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
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
        final Common common=new Common();
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
                CustomAdapter customAdapter=new CustomAdapter(ManageAddresses.this,common.dataArrayList,"AddressManagement");
                ListView addressList=(ListView)findViewById(R.id.address_list_view);
                addressList.setAdapter(customAdapter);

            }
        };
        common.AsynchronousThread(ManageAddresses.this,
                webService,
                postData,
                loadingIndicatorView,
                dataColumns,
                postThread,
                null);

    }
    public void setDefault(View view){
        if(((TextView)view).getText().toString().equals(getResources().getString(R.string.default_address))){
            return;
        }
        Common common=new Common();
        //Threading--------------------------------------------------
        String webService="api/customer/SetDefaultAddress";
        String postData =  "{\"ID\":\""+view.getTag()+"\",\"CustomerID\":\""+db.GetCustomerDetails("CustomerID")+"\"}";
        String[] dataColumns={};
        final ProgressDialog progressDialog=new ProgressDialog(ManageAddresses.this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);progressDialog.show();
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                getCustomerAddresses();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ManageAddresses.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        };
        common.AsynchronousThread(ManageAddresses.this,
                webService,
                postData,
                null,
                dataColumns,
                postThread,
                postFailThread);
    }
    public void removeAddress(final View view){
        new AlertDialog.Builder(ManageAddresses.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle(R.string.exit)
                .setMessage(R.string.remove_item_q)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                                Common common=new Common();
                                //Threading--------------------------------------------------
                                String webService="api/customer/DeleteCustomerAddress";
                                String postData =  "{\"ID\":\""+view.getTag()+"\",\"CustomerID\":\""+db.GetCustomerDetails("CustomerID")+"\"}";
                                String[] dataColumns={};
                                final ProgressDialog progressDialog=new ProgressDialog(ManageAddresses.this);
                                progressDialog.setMessage(getResources().getString(R.string.please_wait));
                                progressDialog.setCancelable(false);progressDialog.show();
                                Runnable postThread=new Runnable() {
                                    @Override
                                    public void run() {
                                        getCustomerAddresses();
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    }
                                };
                                Runnable postFailThread=new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ManageAddresses.this, R.string.some_error_at_server, Toast.LENGTH_SHORT).show();
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    }
                                };
                                common.AsynchronousThread(ManageAddresses.this,
                                        webService,
                                        postData,
                                        null,
                                        dataColumns,
                                        postThread,
                                        postFailThread);
                    }
                }).setNegativeButton(R.string.no, null).show();
    }
}
