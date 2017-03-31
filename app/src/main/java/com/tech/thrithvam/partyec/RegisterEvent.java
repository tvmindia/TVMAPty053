
package com.tech.thrithvam.partyec;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class RegisterEvent extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Common common=new Common();
    EditText eventName,dateTime,noOfPersons,budget,lookingFor,requirements,name,email, phone,message;
    Spinner eventTypeSpinner;
    ArrayList<String> arrayListEventTypes;
    Calendar eventDateTime=Calendar.getInstance();
    ArrayList<String> lookingForItemsAvailable=new ArrayList<>();
    boolean[] lookingForItemsSelectedIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        eventName=(EditText)findViewById(R.id.event_name);
        dateTime=(EditText)findViewById(R.id.date_time);
        noOfPersons=(EditText)findViewById(R.id.no_of_persons);
        budget=(EditText)findViewById(R.id.budget);
        lookingFor=(EditText)findViewById(R.id.looking_for);
        requirements=(EditText)findViewById(R.id.requirements);
        name=(EditText)findViewById(R.id.user_name);
        email=(EditText)findViewById(R.id.user_email);
        phone=(EditText)findViewById(R.id.user_phone);
        message=(EditText)findViewById(R.id.message);

        //---------------------Get Event type items------------------------------
        setEventTypeSpinner();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        common.NavigationBarHeaderClick(RegisterEvent.this,navigationView);
    }
    public void submitRequest(View view){
        if(eventName.getText().toString().length()==0 || !eventName.getText().toString().matches(common.UserNameRegularExpression)){
            eventName.setError(getResources().getString(R.string.give_valid));
            eventName.requestFocus();
        }
        else if(!(eventTypeSpinner.getSelectedItemPosition()>0)){
            Toast.makeText(this, R.string.select_event_type, Toast.LENGTH_SHORT).show();
            eventTypeSpinner.requestFocus();
        }
        else if(dateTime.getText().toString().length()==0){
            dateTime.setError(getResources().getString(R.string.give_valid));
            dateTime.requestFocus();
        }
        else if(noOfPersons.getText().toString().length()==0 || Integer.parseInt(noOfPersons.getText().toString())<1){
            noOfPersons.setError(getResources().getString(R.string.give_valid));
            noOfPersons.requestFocus();
        }
        else if(lookingFor.getText().toString().length()==0){
            lookingFor.setError(getResources().getString(R.string.give_valid));
            lookingFor.requestFocus();
        }
        else if(name.getText().toString().length()==0 || !name.getText().toString().matches(common.UserNameRegularExpression)){
            name.setError(getResources().getString(R.string.give_valid));
            name.requestFocus();
        }
        else if( !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            email.setError(getResources().getString(R.string.give_valid));
            email.requestFocus();
        }
        else if(phone.getText().toString().length()==0){// || !name.getText().toString().matches(common.MobileNumberRegularExpression)){
            phone.setError(getResources().getString(R.string.give_valid));
            phone.requestFocus();
        }
        else{
            AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loading_indicator);
            loadingIndicatorView.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
    }
    void setEventTypeSpinner(){
        arrayListEventTypes=new ArrayList<>();
        arrayListEventTypes.add(getResources().getString(R.string.select_event_type));
        eventTypeSpinner=(Spinner)findViewById(R.id.event_type);
        //Threading--------------------------------------------------
        String webService="api/event/GetEventTypesAndRelatedCategories";
        String postData =  "";
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView) findViewById(R.id.loading_indicator_event_type);
        String[] dataColumns={"ID","Name","RelatedCategoriesCSV"};//Order Matters. Data in the common.dataArrayList will be in same order
        Runnable postThread=new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<common.dataArrayList.size();i++)
                    arrayListEventTypes.add(common.dataArrayList.get(i)[1]);
                ArrayAdapter adapter = new ArrayAdapter<String>(RegisterEvent.this, android.R.layout.simple_spinner_item, arrayListEventTypes){
                    @Override
                    public boolean isEnabled(int position){
                        return position != 0;// Disable the first item from Spinner. First item will be use for hint
                    }
                    @Override
                    public View getDropDownView(int position, View convertView,
                                                ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;
                        if(position == 0){                    // Set the hint text color gray
                            tv.setTextColor(Color.GRAY);
                        }
                        else {
                            tv.setTextColor(Color.BLACK);
                        }
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                eventTypeSpinner.setAdapter(adapter);
                eventTypeSpinner.setVisibility(View.VISIBLE);
                //Looking For items filling-------------------------------
                eventTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(position>0) {
                            String relatedCategories = common.dataArrayList.get(position - 1)[2];//since first item is placeholder in spinner
                            ArrayList<String> lookingForItemsUnderThisType = new ArrayList<>(Arrays.asList(relatedCategories.split("\\s*,\\s*")));
                            setAvailableLookingForItems(lookingForItemsUnderThisType);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        };
        Runnable postFailThread=new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegisterEvent.this, R.string.retrying, Toast.LENGTH_SHORT).show();
                setEventTypeSpinner();
            }
        };
        common.AsynchronousThread(RegisterEvent.this,
                webService,
                postData,
                loadingIndicator,
                dataColumns,
                postThread,
                postFailThread);
    }
    void setAvailableLookingForItems(ArrayList<String> lookingForItemsUnderCurrentType){
        lookingForItemsAvailable.clear();
        lookingFor.setText("");
        if(lookingForItemsUnderCurrentType.size()>0) {
            lookingForItemsAvailable = lookingForItemsUnderCurrentType;
            lookingForItemsSelectedIndex = new boolean[lookingForItemsAvailable.size()];
            Arrays.fill(lookingForItemsSelectedIndex, Boolean.FALSE);//initialize
        }
    }
    public void getEventDateTime(View view){
        dateTime.setError(null);
        final Calendar today = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                eventDateTime=Calendar.getInstance();
                eventDateTime.set(Calendar.YEAR, year);
                eventDateTime.set(Calendar.MONTH, monthOfYear);
                eventDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                //Validation--------------
                if(eventDateTime.before(today)){
                    Toast.makeText(RegisterEvent.this, R.string.give_valid, Toast.LENGTH_SHORT).show();
                    return;
                }

                //Select time-------------------------------
                TimePickerDialog.OnTimeSetListener timeSetListener=new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        eventDateTime.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        eventDateTime.set(Calendar.MINUTE,minute);
                        //Setting display text-------
                        SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy   hh:mm a", Locale.US);
                        dateTime.setText(formatted.format(eventDateTime.getTime()));
                    }
                };
                TimePickerDialog timePickerDialog=new TimePickerDialog(RegisterEvent.this,timeSetListener,today.get(Calendar.HOUR_OF_DAY), today.get(Calendar.MINUTE),false);
                timePickerDialog.setTitle(R.string.select_time_optional);
                timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getResources().getString(R.string.no_time),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                eventDateTime.set(Calendar.HOUR_OF_DAY,0);
                                eventDateTime.set(Calendar.MINUTE,0);
                                //Setting display text-------
                                SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                                dateTime.setText(formatted.format(eventDateTime.getTime()));
                            }
                        });
                timePickerDialog.show();
            }
        };
        new DatePickerDialog(RegisterEvent.this, dateSetListener, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show();
    }
    public void getLookingForItemsFromUser(View view)
    {
        lookingFor.setError(null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.looking_for)
                .setMultiChoiceItems(lookingForItemsAvailable.toArray(new CharSequence[lookingForItemsAvailable.size()]),
                        lookingForItemsSelectedIndex,
                        new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        lookingForItemsSelectedIndex[indexSelected]=isChecked;
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String lookingForString="";
                        for(int i=0;i<lookingForItemsAvailable.size();i++){
                            if(lookingForItemsSelectedIndex[i]){
                                lookingForString+=lookingForItemsAvailable.get(i)+", ";
                            }
                        }
                        if(lookingForString.lastIndexOf(",")>0){
                            lookingForString=lookingForString.substring(0,lookingForString.lastIndexOf(","));
                            lookingFor.setText(lookingForString);
                        }
                        else {
                            lookingFor.setText("");
                        }
                    }
                }).create();
        dialog.show();
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

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        common.NavigationBarItemClick(this,item);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
