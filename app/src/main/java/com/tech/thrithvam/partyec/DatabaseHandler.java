package com.tech.thrithvam.partyec;


import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    private static final int DATABASE_VERSION = 2;
    // Database Name
    private static final String DATABASE_NAME = "PartyEC.db";
    private SQLiteDatabase db;
    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    private static DatabaseHandler dbInstance = null;
    public static DatabaseHandler getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (dbInstance == null) {
            dbInstance = new DatabaseHandler(context.getApplicationContext());
        }
        return dbInstance;
    }
    // Creating Tables
    // IMPORTANT: if you are changing anything in the below function onCreate(), DO DELETE THE DATABASE file in
    // the emulator or uninstall the application in the phone, to run the application
    @Override
    public void onCreate(SQLiteDatabase db) {
        //---------------Tables----------------------------------
        String CREATE_USER_ACCOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS Customer (CustomerID TEXT PRIMARY KEY,Name TEXT,Email TEXT,Mobile TEXT,Gender TEXT, CustomerImg TEXT);";
        db.execSQL(CREATE_USER_ACCOUNTS_TABLE);
    }
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
                db.execSQL("ALTER TABLE Customer ADD CustomerImg TEXT" );
        // Create tables again
        onCreate(db);
    }
    //--------------------------Customer-----------------------------
//    DatabaseUtils.sqlEscapeString(Denomination)
    void InsertCustomer(String CustomerID, String Name, String Email, String Mobile, String Gender)
    {
        db=this.getWritableDatabase();
        ClearCustomer();
        db.execSQL("INSERT INTO Customer (CustomerID,Name,Email,Mobile,Gender) VALUES ('"+CustomerID+"',"+ DatabaseUtils.sqlEscapeString(Name)+",'"+Email+"','"+Mobile+"','"+Gender+"');");
    }
    private void ClearCustomer()
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM Customer;");
    }
    void UpdateCustomer(String Name,String Mobile)
    {
        db=this.getWritableDatabase();
        db.execSQL("UPDATE Customer SET Name="+DatabaseUtils.sqlEscapeString(Name)+",Mobile='"+Mobile+"';");
    }
    void UpdateCustomerImg(String ImgURL)
    {
        db=this.getWritableDatabase();
        db.execSQL("UPDATE Customer SET CustomerImg="+DatabaseUtils.sqlEscapeString(ImgURL)+";");
    }
    String GetCustomerDetails(String detail)
    {db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT CustomerID,Name,Email,Mobile,Gender,CustomerImg FROM Customer;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            String result=cursor.getString(cursor.getColumnIndex(detail));
            cursor.close();
            return result;
        }
        else {
            cursor.close();
            return null;
        }
    }
   /*
    ArrayList<String[]> GetNotifications()
    {
        db=this.getReadableDatabase();
        ArrayList<String[]> nots=new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT Title,Description,Type,NotDate FROM Notifications ORDER BY NotDate DESC;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            do {
                String[] data = new String[4];
                data[0] = cursor.getString(cursor.getColumnIndex("Title"));
                data[1] = cursor.getString(cursor.getColumnIndex("Description"));
                data[2] = cursor.getString(cursor.getColumnIndex("NotDate"));
                data[3] = cursor.getString(cursor.getColumnIndex("Type"));
                nots.add(data);
            }while (cursor.moveToNext());
            cursor.close();
            //db.close;
            return nots;
        }
        else
        {
            //db.close;
            cursor.close();
            return nots;//empty array list to avoid exception in custom adapter
        }
    }*/
}
