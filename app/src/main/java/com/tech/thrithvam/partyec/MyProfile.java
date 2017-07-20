package com.tech.thrithvam.partyec;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MyProfile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    DatabaseHandler db;

    File imageFile;
    ImageView customerImage;
    Boolean isFromCamera=false;
    final int PHOTO_FROM_CAMERA=555;
    final int PHOTO_FROM_GALLERY=444;
    Uri imageUri;
    NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //-----------------------------------------------------------------------------
        db=DatabaseHandler.getInstance(this);
        if(db.GetCustomerDetails("CustomerID")==null){
            Intent loginIntent=new Intent(this,Login.class);
            Common.toastMessage(MyProfile.this,R.string.please_login);
            startActivity(loginIntent);
            finish();
            return;
        }
        else {
            ((TextView)findViewById(R.id.name)).setText(db.GetCustomerDetails("Name"));
            ((TextView)findViewById(R.id.mob_no)).setText(db.GetCustomerDetails("Mobile"));
            ((TextView)findViewById(R.id.email)).setText(db.GetCustomerDetails("Email"));

            FirebaseMessaging.getInstance().subscribeToTopic(db.GetCustomerDetails("CustomerID"));

            //CustomerImage
            customerImage=(ImageView)findViewById(R.id.user_image);
            Common.LoadImage(MyProfile.this,
                    customerImage,
                    getResources().getString(R.string.url)+db.GetCustomerDetails("CustomerImg"),
                    R.drawable.user);
            customerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {/*
                    final CharSequence[] items = {getResources().getString(R.string.take_photo), getResources().getString(R.string.choose_from_galley), getResources().getString(R.string.cancel)};
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyProfile.this);
//                    builder.setTitle(getResources().getString(R.string.cust));
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (items[item].equals(getResources().getString(R.string.take_photo))) {
                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                File dir = getDir("directory", Context.MODE_PRIVATE);
                                imageFile = new File(dir,  "Pic.jpg");
                                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                        Uri.fromFile(imageFile));
                                imageUri = Uri.fromFile(imageFile);
                                startActivityForResult(intent, PHOTO_FROM_CAMERA);
                            } else if (items[item].equals(getResources().getString(R.string.choose_from_galley))) {*/
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent, "Select File"), PHOTO_FROM_GALLERY);
                        /*    } else if (items[item].equals(getResources().getString(R.string.cancel))) {
                                finish();
                            }
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();*/
                }
            });
        }
        //-----------------------------------------------------------------------------
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Common.NavigationBarHeaderClick(MyProfile.this,navigationView);
    }

    public void editProfile(View view){
        //Display changes-------
        (findViewById(R.id.customer_details_linear)).setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        (findViewById(R.id.customer_edit_linear)).setVisibility(View.VISIBLE);
        ((EditText)findViewById(R.id.name_edit)).setText(((TextView)findViewById(R.id.name)).getText().toString());
        ((EditText)findViewById(R.id.mob_no_edit)).setText(((TextView)findViewById(R.id.mob_no)).getText().toString());
        ((EditText)findViewById(R.id.email_edit)).setText(((TextView)findViewById(R.id.email)).getText().toString());
    }
    public void proceedClickForUpdate(final View view){
        view.setVisibility(View.GONE);
        final EditText name=(EditText)findViewById(R.id.name_edit);
        final EditText mob=(EditText)findViewById(R.id.mob_no_edit);
        if(name.getText().length()==0||mob.getText().length()==0){
            Common.toastMessage(MyProfile.this,R.string.give_valid);
            return;
        }
        //Threading------------------------------------------------------------------------------------------------------
        String webService="/api/Customer/UpdateUser";
        String postData = "{\"ID\":\""+db.GetCustomerDetails("CustomerID")+"\",\"Name\":\"" + name.getText().toString() +"\",\"Mobile\":\""+mob.getText().toString()+ "\"}";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator_proceed);
        String[] dataColumns={};
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                db.UpdateCustomer(name.getText().toString(),mob.getText().toString());
                Intent profileIntent=new Intent(MyProfile.this,MyProfile.class);
                startActivity(profileIntent);
                finish();
            }
        };
        Runnable postThreadFailed=new Runnable() {
            @Override
            public void run() {
                Common.toastMessage(MyProfile.this,R.string.some_error_at_server);
                view.setVisibility(View.VISIBLE);
            }
        };
        common.AsynchronousThread(MyProfile.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postThreadFailed);
    }
    public void manageAddresses(View view){
        Intent addressIntent=new Intent(this,ManageAddresses.class);
        addressIntent.putExtra("from","my_profile");
        startActivity(addressIntent);
    }
    public void cartClick(View view){
        Intent intent=new Intent(MyProfile.this,Cart.class);
        startActivity(intent);
    }
    public void wishlistClick(View view){
        Intent intent=new Intent (MyProfile.this,ListViewsActivity.class);
        intent.putExtra("list","wishlist");
        startActivity(intent);
    }
    public void ordersClick(View view){
        Intent intent=new Intent (MyProfile.this,ListViewsActivity.class);
        intent.putExtra("list","orders");
        startActivity(intent);
    }
    public void historyClick(View view){
        Intent intent=new Intent (MyProfile.this,ListViewsActivity.class);
        intent.putExtra("list","history");
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap;
        if (resultCode == RESULT_OK)
        {
            if(requestCode == PHOTO_FROM_CAMERA){
                if (((imageFile.length() / 1024) / 1024) > 5) {
                    Toast.makeText(MyProfile.this, "Please make the image file smaller size", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    /*bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    customerImage.setImageBitmap(bitmap);*/
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);

                        customerImage.setImageBitmap(bitmap);
                        Toast.makeText(this, selectedImage.toString(),
                                Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();

                    }
                    isFromCamera=true;
                }


            }
            else if(requestCode == PHOTO_FROM_GALLERY) {
                if (isOnline()) {
                    Uri selectedImageUri = data.getData();
                    String[] projection = {MediaStore.MediaColumns.DATA};
                    CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
                    Cursor cursor = cursorLoader.loadInBackground();
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();
                    File imageFileTemp = new File(cursor.getString(column_index));
                    if (((imageFileTemp.length() / 1024) / 1024) > 5) {
                        Toast.makeText(MyProfile.this, "Please make the image file smaller size", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        imageFile = imageFileTemp;
                        String selectedImagePath = cursor.getString(column_index);
                        bitmap = BitmapFactory.decodeFile(selectedImagePath);
                        customerImage.setImageBitmap(bitmap);
                    }
                    cursor.close();
                    Upload();
                }
                else {
                    Common.toastMessage(MyProfile.this,R.string.network_off_alert);
                }
            }
        }
    }


    public void Upload() {
            FileInputStream fStream = null;
            if (imageFile != null) {
                try {
                    fStream = new FileInputStream(imageFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            final FileUpload hfu = new FileUpload(MyProfile.this, getResources().getString(R.string.url) + "/api/Customer/UploadProfileImage",
                                            fStream,
                                            imageFile.getName(),
                                            db.GetCustomerDetails("CustomerID"));

            Runnable postUpload=new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject=new JSONObject(hfu.responseString);
                        String msg=jsonObject.optString("Message");
                        db.UpdateCustomerImg(jsonObject.optString("FilePath"));
                        Common.toastMessage(MyProfile.this,msg);
                        Common.NavigationBarHeaderClick(MyProfile.this,navigationView);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            hfu.UploadFileFn(postUpload);
    }

    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(isTaskRoot()){
                Intent intent=new Intent(this,Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else {
                super.onBackPressed();
            }
        }
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
