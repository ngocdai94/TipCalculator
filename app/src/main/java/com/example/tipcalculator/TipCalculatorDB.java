package com.example.tipcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TipCalculatorDB {
    /***** DATABASE TABLE - Could Be In a Separate File ********/
    // database constants
    public static final String DB_NAME = "tiplist.db";
    public static final int    DB_VERSION = 1;

    // bill list table constants
    public static final String BILL_TABLE = "bill";

    public static final String BILL_ID = "_id";
    public static final int    BILL_ID_COL = 0;

    public static final String BILL_DATE = "bill_date";
    public static final int    BILL_DATE_COL = 1;

    public static final String BILL_AMOUNT = "bill_amount";
    public static final int    BILL_AMOUNT_COL = 2;

    public static final String TIP_PERCENT = "tip_percent";
    public static final int    TIP_PERCENT_COL = 3;

    // CREATE and DROP TABLE statements
    public static final String CREATE_BILL_TABLE =
            "CREATE TABLE " + BILL_TABLE  + " (" +
            BILL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BILL_DATE       + " INTEGER NOT NULL, " +
            BILL_AMOUNT     + " REAL    NOT NULL, " +
            TIP_PERCENT     + " REAL    NOT NULL);";

    public static final String DROP_BILL_TABLE =
            "DROP TABLE IF EXISTS " + BILL_TABLE;
    /**************************** END *****************************/

    /*** INNER CLASS - DBHelper - Could Be in A Separate File *****/
    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context,
                        String name,
                        SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create tables
            db.execSQL(CREATE_BILL_TABLE);

            // insert default bill list
            db.execSQL("INSERT INTO bill VALUES (1, 2016, 40.60, .10)");
            db.execSQL("INSERT INTO bill VALUES (2, 2017, 50.60, .20)");
            db.execSQL("INSERT INTO bill VALUES (3, 2018, 60.60, .30)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("Bill list", "Upgrading db from version "
                    + oldVersion + " to " + newVersion);

            db.execSQL(TipCalculatorDB.DROP_BILL_TABLE);
            onCreate(db);
        }
    }
    /**************************** END *****************************/

    /********************** TipCalculatorDB Class  ****************/
    // database and database helper objects
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    // helper variable
    private long lastRowID;
    private String lastSaveDate;

    // constructor
    public TipCalculatorDB(Context context) {
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
        lastRowID = 0;
    }

    // private methods
    private void openReadableDB() {
        db = dbHelper.getReadableDatabase();
    }

    private void openWriteableDB() {
        db = dbHelper.getWritableDatabase();
    }

    private void closeDB() {
        if (db != null)
            db.close();
    }

    // public method
    public ArrayList<Tip> getTips() {
        // Instantiate the Tip array objects to get each bill row in the bill table
        ArrayList<Tip> tips = new ArrayList<Tip>();

        /**
         * Open Database file and get cursor ready to query through the database
         * When finish, end cursor and close the database to prevent memory leak.
         *
         * Finally, return the array of Tips to TipCalculatorActivity class.
         * */
        openReadableDB();
        Cursor cursor = db.query(BILL_TABLE,null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Tip tip = getTaskFromCursor(cursor);
            tips.add(tip);
        }
        if (cursor != null) cursor.close();
        closeDB();

        return tips;
    }

    public float getAverageTipPercent(){
        // Instantiate variable avgTip and array tipList to get avgTip from Database
        String avgTip = "";
        ArrayList<String> tipList = new ArrayList<String>();

        /**
         * Use rawQuery method to use average function easier.
         * tipColumn is an alias column of TIP_PERCENT
         *
         * Finally, return avgTip to TipCalculatorActivity class.
         * */
        openReadableDB();
        Cursor cursor = db.rawQuery( "select (AVG(" + TIP_PERCENT + ")) AS tipColumn from "+ BILL_TABLE, null );
        while (cursor.moveToNext()) {
            tipList.add(cursor.getString(cursor.getColumnIndex("tipColumn")));
        }
        if (cursor != null) cursor.close();
        closeDB();

        // Assign avgTip then return to TipCalculatorActivity class
        for (String t: tipList) {
            avgTip = t;
        }
        
        return  Float.parseFloat(avgTip);
    }

    public long saveTip(Tip tip){
        // Don't need to the BILL_ID since the id is auto increment
        ContentValues cv = new ContentValues();
        cv.put(BILL_DATE, tip.getBillDate());
        cv.put(BILL_AMOUNT, tip.getBillAmount());
        cv.put(TIP_PERCENT, tip.getTipPercent());

        openWriteableDB();
        long rowID = db.insert(BILL_TABLE, null, cv);
        lastRowID = rowID;
        closeDB();

        // Format Date
        Date dateTime = Calendar.getInstance().getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("MMM dd YYYY HH:mm:ss");
        lastSaveDate = fmt.format(dateTime);

        return rowID;
    }

    public String getLastSaveDate(){
        return lastSaveDate;
    }

    public Tip getLastTip() {
        String where = BILL_ID + "= ?";
        String[] whereArgs = {Long.toString(lastRowID)};

        openReadableDB();
        Cursor cursor = db.query(BILL_TABLE, null, where, whereArgs, null, null, null);
        cursor.moveToFirst();
        Tip tip = getTaskFromCursor(cursor);
        if (cursor != null) cursor.close();
        this.closeDB();

        return tip;
    }

    private Tip getTaskFromCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0){
            return null;
        }
        else {
            try {
                Tip tip = new Tip(
                        cursor.getInt(BILL_ID_COL),
                        cursor.getInt(BILL_DATE_COL),
                        cursor.getFloat(BILL_AMOUNT_COL),
                        cursor.getFloat(TIP_PERCENT_COL));
                return tip;
            }
            catch(Exception e) {
                return null;
            }
        }
    }
    /**************************** END *****************************/
}
