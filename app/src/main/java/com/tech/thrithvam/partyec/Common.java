package com.tech.thrithvam.partyec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

class Common {
    //Constants-----------------------
    String MobileNumberRegularExpression = "^[0-9]*$";
    String UserNameRegularExpression="^[a-zA-Z\\. ]+$";                 //^[a-z0-9_-]{3,15}$

    //To load navigation panel menu items and their clicks--------------------------
    void NavigationBarItemClick(Context context, MenuItem item){
        int id = item.getItemId();

        if (id == R.id.nav_shop_by_category) {
            Intent intent=new Intent(context,CategoryList.class);
            context.startActivity(intent);
        } else if (id == R.id.nav_register_event) {
            Intent intent=new Intent(context,RegisterEvent.class);
            context.startActivity(intent);
        } else if (id == R.id.nav_contact_us) {
            Intent intent=new Intent(context,ContactUs.class);
            context.startActivity(intent);
        }/* else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

    }
    void NavigationBarHeaderClick(final Context context, NavigationView navigationView){
        navigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, Login.class));
            }
        });
    }

    //To load image from a url------------------------------------------------------
    void LoadImage(Context context,ImageView imageView, String imageURL, int failImage){
        if(!imageURL.equals("null")){
            Glide.with(context)
                    .load(imageURL)//adapterContext.getResources().getString(R.string.url) +imageURL.substring(imageURL.indexOf("img")))
                    .fitCenter()
                    .thumbnail(0.1f)
                    .error(failImage)
                    .into(imageView);
        }
        else{
            Glide.with(context)
                    .load(failImage)
                    .fitCenter()
                    .into(imageView);
        }
    }

    //Threading: to load data from a server----------------------------------------
    ArrayList<String[]> dataArrayList=new ArrayList<>();
    String json="";
    void AsynchronousThread(final Context context,
                            final String webService,
                            final String postData,
                            final AVLoadingIndicatorView loadingIndicator,
                            final String[] dataColumns,
                            final Runnable postThread,
                            final Runnable postFailThread){

        class GetDataFromServer extends AsyncTask<Void , Void, Void> {
            private int status;
            private StringBuilder sb;
            private String strJson;
            private String msg;
            private boolean pass=false;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dataArrayList.clear();
                if(loadingIndicator!=null) loadingIndicator.setVisibility(View.VISIBLE);
                //----------encrypting ---------------------------
                // usernameString=cryptography.Encrypt(usernameString);
            }
            @Override
            protected Void doInBackground(Void... arg0) {
                String url =context.getResources().getString(R.string.url) + webService;
                HttpURLConnection c = null;
                try {
                    URL u = new URL(url);
                    c = (HttpURLConnection) u.openConnection();
                    c.setRequestMethod("POST");
                    c.setRequestProperty("Content-type", "application/json");
                    c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                    c.setDoInput(true);
                    c.setDoOutput(true);
                    c.setUseCaches(false);
                    c.setConnectTimeout(10000);
                    c.setReadTimeout(10000);
                    DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                    status = c.getResponseCode();
                    switch (status) {
                        case 200:
                        case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                            sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            br.close();
                            int a=sb.indexOf("{");
                            int b=sb.lastIndexOf("}");
                            strJson=sb.substring(a, b + 1);
                            //   strJson=cryptography.Decrypt(strJson);
                            strJson=/*"{\"JSON\":[" +*/ strJson.replace("\\\"","\"").replace("\\\\","\\") /*+ "]}"*/;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    msg=ex.getMessage();
                } finally {
                    if (c != null) {
                        try {
                            c.disconnect();
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            msg=ex.getMessage();
                        }
                    }
                }
                if(strJson!=null)
                {try {
                    JSONObject jsonRootObject = new JSONObject(strJson);
                    pass = jsonRootObject.optBoolean("Result");
                    if(pass){
                        JSONArray jsonArray = jsonRootObject.optJSONArray("Records");
                        if(jsonArray!=null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String[] data = new String[dataColumns.length];
                                for (int j = 0; j < dataColumns.length; j++) {
                                    data[j] = jsonObject.optString(dataColumns[j]);
                                }
                                dataArrayList.add(data);
                            }
                        }
                        else {
                            json=jsonRootObject.optString("Records");
                        }
                    }
                    else {
                        msg=jsonRootObject.optString("Message");
                    }
                } catch (Exception ex) {
                    msg=ex.getMessage();
                }}
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if(loadingIndicator!=null) loadingIndicator.setVisibility(View.GONE);
                if(!pass) {
                    if(postFailThread==null){
                        Intent noItemsIntent=new Intent(context,NothingToDisplay.class);
                        noItemsIntent.putExtra("msg",msg);
                        noItemsIntent.putExtra("activityHead","PartyEC");
                        context.startActivity(noItemsIntent);
                        ((Activity)context).finish();
                    }
                    else {
                        postFailThread.run();
                    }
                }
                else {
                    postThread.run();
                }
            }
        }

        new GetDataFromServer().execute();
    }
}
