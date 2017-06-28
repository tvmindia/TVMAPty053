package com.tech.thrithvam.partyec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ListViewsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    DatabaseHandler db;
    String customerID;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_views);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db=DatabaseHandler.getInstance(ListViewsActivity.this);
        customerID=db.GetCustomerDetails("CustomerID");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        listView=(ListView)findViewById(R.id.listview);
        switch (getIntent().getExtras().getString("list")){
            case "reviews":
                loadProductReviews();
                break;
            case "relatedItems":
                loadRelatedProducts();
                break;
            case "wishlist":
                loadWishlistProducts();
                break;
            case "bookings":
                loadBookings();
                break;
            case "quotations":
                loadQuotations();
                break;
            case "orders":
                loadOrder();
                break;
            case "ordersDetails":
                loadOrderDetails(getIntent().getExtras().getString("ID"));
                break;
            case "history":
                loadOrderHistory();
                break;
            case "event_requests":
                loadEventRequests();
                break;
            case "customer_reviews":
                loadCustomerProductReviews();
                break;
            default:
                finish();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Common.NavigationBarHeaderClick(ListViewsActivity.this,navigationView);
    }
    void loadProductReviews(){
        getSupportActionBar().setTitle("Reviews: "+getIntent().getExtras().getString("productName"));
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/product/GetProductReviews";
        String postData =  "{\"ProductID\":\""+getIntent().getExtras().getString("productID")+"\",\"count\":\""+"-1"+"\"}";
        String[] dataColumns={"ID","CustomerName","ImageUrl","AvgRating","ReviewCreatedDate","Review"};
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"ReviewList");
                listView.setAdapter(adapter);
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicatorView,
                dataColumns,
                postThread,
                null);
    }
    void loadRelatedProducts(){
        getSupportActionBar().setTitle("Related Items: "+getIntent().getExtras().getString("productName"));
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/product/GetRelatedProducts";
        String postData =  "{\"ID\":\""+getIntent().getExtras().getString("productID")+"\",\"count\":\""+"-1"+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"ID","Name","ImageURL","StickerURL"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"RelatedItemsList");
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(ListViewsActivity.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[0]);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadWishlistProducts(){
        if(customerID==null) {
            Intent loginIntent=new Intent(this,Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
            return;
        }
        getSupportActionBar().setTitle("WishList Items ");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerWishlist";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"ProductID","ProductName","ImageURL","DaysinWL","Price","StickerURL"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"WishList");
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(ListViewsActivity.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[0]);
                        startActivity(intent);
                    }
                });
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadBookings(){
        if(customerID==null) {
            Intent loginIntent=new Intent(this,Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
            return;
        }
        getSupportActionBar().setTitle("Bookings ");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerBookings";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"BookingNo","ProductID","RequiredDate","BookingDate","StatusText","ProductName","ImageUrl"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"Bookings");
                listView.setAdapter(adapter);
                /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(ListViewsActivity.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[1]);
                        startActivity(intent);
                    }
                });*/
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadQuotations(){
        if(customerID==null) {
            Intent loginIntent=new Intent(this,Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
            return;
        }
        getSupportActionBar().setTitle("Quotations");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerQuotations";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"QuotationNo","ProductID","RequiredDate","QuotationDate","StatusText","ProductName","ImageUrl"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"Quotations");
                listView.setAdapter(adapter);
               /* listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(ListViewsActivity.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[1]);
                        startActivity(intent);
                    }
                });*/
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadOrder(){
        if(customerID==null) {
            Intent loginIntent=new Intent(this,Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
            return;
        }
        getSupportActionBar().setTitle("Orders");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerOrders";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"OrderNo","OrderDate","OrderStatus","TotalOrderAmt","ID"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"Orders");
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent (ListViewsActivity.this,ListViewsActivity.class);
                        intent.putExtra("list","ordersDetails");
                        intent.putExtra("ID",common.dataArrayList.get(position)[4]);
                        intent.putExtra("OrderNo",common.dataArrayList.get(position)[0]);
                        intent.putExtra("OrderDate",common.dataArrayList.get(position)[1]);
                        intent.putExtra("OrderStatus",common.dataArrayList.get(position)[2]);
                        intent.putExtra("TotalOrderAmt",common.dataArrayList.get(position)[3]);
                        startActivity(intent);
                    }
                });

                //if from order placement
                if(getIntent().hasExtra("orderid")){
                    for(int i=0;i<common.dataArrayList.size();i++){
                        if(common.dataArrayList.get(i)[4].equals(getIntent().getExtras().getString("orderid"))){
                            listView.performItemClick(
                                    listView.getAdapter().getView(i, null, null),
                                    i,
                                    listView.getAdapter().getItemId(i));
                        }
                    }
                }
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadOrderDetails(String ID ){
        getSupportActionBar().setTitle("Order Details");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerOrderDetails";
        String postData =  "{\"OrderID\":\""+ID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        final String[] dataColumns={"OrderDetailID","ProductName","AttributeValues","Qty","Price","ShippingAmt","TaxAmt","SubTotal","ImageUrl","ProductID"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CardView OrderDetail=(CardView) findViewById(R.id.order_Detail_CardView);
                TextView orderNo=(TextView)  findViewById(R.id.order_No);
                TextView orderDate=(TextView)  findViewById(R.id.order_Date);
                TextView orderStatus=(TextView) findViewById(R.id.order_Status);
                TextView totalAmount=(TextView) findViewById(R.id.order_Amount);
                OrderDetail.setVisibility(View.VISIBLE);
                orderNo.setText(getResources().getString(R.string.order_no,getIntent().getExtras().getString("OrderNo")));
                orderDate.setText(getResources().getString(R.string.order_dates,getIntent().getExtras().getString("OrderDate")));
                orderStatus.setText(getResources().getString(R.string.order_status,getIntent().getExtras().getString("OrderStatus")));
                //orderStatus.setText(getIntent().getExtras().getString("OrderStatus"));
                totalAmount.setText(getResources().getString(R.string.order_total,getIntent().getExtras().getString("TotalOrderAmt")));

                //Attributes parsing
                for (int i=0;i<common.dataArrayList.size();i++){
                    String attributesString="";
                    try {
                        JSONArray jsonArray=new JSONArray(common.dataArrayList.get(i)[2]);
                        if(jsonArray.length()!=0){
                            for (int j=0;j<jsonArray.length();j++){
                                JSONObject attribute=jsonArray.getJSONObject(j);
                                attributesString+=attribute.optString("Caption")+" : "+attribute.optString("Value")+"\n";
                            }
                            if (attributesString.lastIndexOf("\n") > 0) {
                                attributesString = attributesString.substring(0, attributesString.lastIndexOf("\n"));
                                common.dataArrayList.get(i)[2]=attributesString;
                            }
                        }
                        else {
                            common.dataArrayList.get(i)[2]="";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"OrderDetails");
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(ListViewsActivity.this,ProductDetails.class);
                        intent.putExtra("productID",common.dataArrayList.get(position)[9]);
                        startActivity(intent);
                    }
                });
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadOrderHistory(){
        if(customerID==null) {
            Intent loginIntent=new Intent(this,Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
            return;
        }
        getSupportActionBar().setTitle("Orders History");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/Customer/GetCustomerOrdersHistory";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"OrderNo","OrderDate","OrderStatus","TotalOrderAmt","ID"};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"Orders");
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent (ListViewsActivity.this,ListViewsActivity.class);
                        intent.putExtra("list","ordersDetails");
                        intent.putExtra("ID",common.dataArrayList.get(position)[4]);
                        intent.putExtra("OrderNo",common.dataArrayList.get(position)[0]);
                        intent.putExtra("OrderDate",common.dataArrayList.get(position)[1]);
                        intent.putExtra("OrderStatus",common.dataArrayList.get(position)[2]);
                        intent.putExtra("TotalOrderAmt",common.dataArrayList.get(position)[3]);
                        startActivity(intent);
                    }
                });

                //if from order placement
                if(getIntent().hasExtra("orderid")){
                    for(int i=0;i<common.dataArrayList.size();i++){
                        if(common.dataArrayList.get(i)[4].equals(getIntent().getExtras().getString("orderid"))){
                            listView.performItemClick(
                                    listView.getAdapter().getView(i, null, null),
                                    i,
                                    listView.getAdapter().getItemId(i));
                        }
                    }
                }
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadEventRequests(){
        if(customerID==null) {
            Intent loginIntent=new Intent(this,Login.class);
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            startActivity(loginIntent);
            finish();
            return;
        }
        getSupportActionBar().setTitle("Event Requests");
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/event/GetEventRequestStatus";
        String postData =  "{\"CustomerID\":\""+customerID+"\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        String[] dataColumns={"ID",//0
                "EventReqNo",//1
                "EventType",//2
                "EventTitle",//3
                "EventDateTime",//4
                "EventTime",//5
                "LookingFor",//6
                "RequirementSpec",//7
                "Message",//8
                "NoOfPersons",//9
                "Budget",//10
                "EventDesc",//11
                "AdminRemarks",//12
                "EventStatus",//13
                "TotalAmt",//14
                "TotalTaxAmt",//15
                "TotalDiscountAmt"//16
                };
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"EventRequests");
                listView.setAdapter(adapter);
                listView.setDivider(null);
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                null);
    }
    void loadCustomerProductReviews(){
        if(customerID==null){
            finish();return;
        }
        getSupportActionBar().setTitle("Reviews: "+getIntent().getExtras().getString("productName"));
        listView.setSelector(android.R.color.transparent);
        //Threading-------------------------------------------------------------------------
        String webService="api/product/GetCustomerProductReview";
        String postData =  "{\"ProductID\":\""+getIntent().getExtras().getString("productID")+"\",\"CustomerID\":\""+customerID+"\"}";
        String[] dataColumns={"ID","Review","ReviewCreatedDate","IsApproved"};
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                CustomAdapter adapter=new CustomAdapter(ListViewsActivity.this, common.dataArrayList,"CustomerReviewList");
                listView.setAdapter(adapter);
            }
        };
        common.AsynchronousThread(ListViewsActivity.this,
                webService,
                postData,
                loadingIndicatorView,
                dataColumns,
                postThread,
                null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_cart) {
            Intent intent=new Intent(this,Cart.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Common.NavigationBarItemClick(this,item);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
