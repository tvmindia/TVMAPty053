package com.tech.thrithvam.partyec;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class FileUpload implements Runnable{
    private URL connectURL;
    Context context;
    private int responseCode;
    private String responseString;
    private String customerID;
    private String fileName="";
    private FileInputStream fileInputStream = null;

    FileUpload(Context context, String urlString, FileInputStream fStream, String fileName, String customerID){
        try{
            this.context=context;
            connectURL = new URL(urlString);
            fileInputStream = fStream;
            this.fileName=fileName;
            this.customerID=customerID;
        }catch(Exception ex){
              Log.i("FileUpload", "URL Malformatted");
        }
    }

    void Sending(){
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String Tag="fSnd";
        try
        {
            //         Log.e(Tag,"Starting Http File Sending to URL");

            // Open a HTTP connection to the URL
            HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();

            // Allow Inputs
            conn.setDoInput(true);

            // Allow Outputs
            conn.setDoOutput(true);

            // Don't use a cached copy.
            conn.setUseCaches(false);

            // Use a post method.
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            String basicAuth = "Basic " + Base64.encodeToString("partyec@tvm-2017:".getBytes(), Base64.NO_WRAP);
            conn.setRequestProperty ("Authorization", basicAuth);


            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"CustomerID\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(customerID);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);


            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\"" + lineEnd);
            dos.writeBytes(lineEnd);

            //    Log.e(Tag,"Headers are written");

            if(fileInputStream!=null) {
                // create a buffer of maximum size
                int bytesAvailable = fileInputStream.available();

                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // close streams
                fileInputStream.close();
            }
            dos.flush();

            Log.e(Tag, "File Sent, Response: " + String.valueOf(conn.getResponseCode()));

            InputStream is = conn.getInputStream();

            // retrieve the response from server
            int ch;

            StringBuffer b =new StringBuffer();
            while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
            String s=b.toString();
            Log.i("Response",s);

            //extracting message
            int start=s.indexOf("{");
            int end=s.lastIndexOf("}");
            responseString=s.substring(start,end+1).replace("\\\"","\"").replace("\\\\","\\");
            responseCode =conn.getResponseCode();
            dos.close();
        } catch (Exception ioe)
        {
            // Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
        }
    }

    @Override
    public void run() {

    }


    void UploadFileFn(){
        new UploadFile().execute();
    }

    public class UploadFile extends AsyncTask<Void , Void, Void> {
        ProgressDialog pDialog=new ProgressDialog(context);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(context.getResources().getString(R.string.please_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Sending();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
          /*  new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                    .setMessage(responseString)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(responseCode ==200){
                                ((Activity)context).finish();
                            }
                        }
                    }).setCancelable(false).show();*/
            try {
                JSONObject jsonObject=new JSONObject(responseString);
                String msg=jsonObject.optString("Message");
                Common.toastMessage(context,msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}