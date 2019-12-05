package org.ptindia.jithvar.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.ptindia.jithvar.handler.DataBaseHandler;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Created by Arvindo on 31-03-2017.
 * Company KinG
 * email at support@towardtheinfinity.com
 */

public class DataBaseApp extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "jithwar";
    private static final int DATABASE_VERSION = 1;

    private static final String REGISTRATION_TABLE = "REGISTRATION_TABLE";
    private static final String CONTACT_TABLE = "CONTACT_TABLE";
    private static final String TRACKING_TABLE = "TRACKING_TABLE";
    private static final String TRACKING_E_D_TB = "TRACKING_E_D_TB";
    private static final String ON_OFF_TB = "ON_OFF_TB";

    private static final String KEY_PRIMARY_ID = "PRIMARY_ID";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_STATUS = "USER_STATUS";
    private static final String KEY_CONTACT = "USER_CONTACT";

    private static final String KEY_LATITUDE = "LATITUDE";
    private static final String KEY_LONGITUDE = "LONGITUDE";
    private static final String KEY_LOCATION = "LOCATION";
    private static final String KEY_DATE_TIME = "DATE_TIME";

    private static final String KEY_TRACING_E_D = "IS_TRACING_ENABLE";

    private Semaphore semaphore = new Semaphore(1);


    public DataBaseApp(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_REGISTRATION_TB = "CREATE TABLE " + REGISTRATION_TABLE + "(" +
                KEY_PRIMARY_ID +" INTEGER PRIMARY KEY," +
                KEY_USER_ID + " VARCHAR(20)," +
                KEY_STATUS + " VARCHAR(10)" + ")";
        db.execSQL(CREATE_REGISTRATION_TB);


        String CREATE_CONTACT_TB = "CREATE TABLE " + CONTACT_TABLE + "(" +
                KEY_PRIMARY_ID +" INTEGER PRIMARY KEY," +
                KEY_USER_ID + " VARCHAR(20)," +
                KEY_CONTACT + " VARCHAR(10)" + ")";
        db.execSQL(CREATE_CONTACT_TB);

        String CREATE_TRACING_ED_TB = "CREATE TABLE " + TRACKING_E_D_TB + "(" +
                KEY_PRIMARY_ID +" INTEGER PRIMARY KEY," +
                KEY_TRACING_E_D + " VARCHAR(10)" +
                ")";
        db.execSQL(CREATE_TRACING_ED_TB);


        String CREATE_TAACKING_TB = "CREATE TABLE " + TRACKING_TABLE + "(" +
                KEY_PRIMARY_ID +" INTEGER PRIMARY KEY," +
                KEY_USER_ID + " VARCHAR(20)," +
                KEY_LATITUDE + " VARCHAR(90)," +
                KEY_LONGITUDE + " VARCHAR(90)," +
                KEY_LOCATION + " VARCHAR(200)," +
                KEY_DATE_TIME + " DATETIME DEFAULT" + " CURRENT_TIMESTAMP " +
                ")";
        db.execSQL(CREATE_TAACKING_TB);

        String CREATE_ON_OFF_TB = "CREATE TABLE " + ON_OFF_TB + "(" +
                KEY_PRIMARY_ID +" INTEGER PRIMARY KEY," +
                KEY_USER_ID + " VARCHAR(20)," +
                KEY_STATUS + " VARCHAR(3)," +
                KEY_DATE_TIME + " DATETIME DEFAULT" + " CURRENT_TIMESTAMP " +
                      ")";
        db.execSQL(CREATE_ON_OFF_TB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(db);
    }

    public void insertRegistrationTB(DataBaseHandler handler) throws SQLException{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, handler.getUserId());
        values.put(KEY_STATUS, handler.getStatus());
        db.insert(REGISTRATION_TABLE, null, values);
        db.close();
    }

    public void insertContactTB(DataBaseHandler handler) throws SQLException{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, handler.getUserId());
        values.put(KEY_CONTACT, handler.getContact());
        db.insert(CONTACT_TABLE, null, values);
        db.close();
    }

    public void insertTrackingED_TB(DataBaseHandler handler) throws SQLException{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TRACING_E_D, handler.getTracking_TD());
        db.insert(TRACKING_E_D_TB, null, values);
        db.close();
    }

    public void insertTrackingTb(DataBaseHandler handler) throws SQLException{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, handler.getUserId());
        values.put(KEY_LATITUDE, handler.getLatitude());
        values.put(KEY_LONGITUDE, handler.getLongitude());
        values.put(KEY_LOCATION, handler.getLocation());
        db.insert(TRACKING_TABLE, null, values);
        db.close();
    }

    public void insertONffTb(DataBaseHandler handler) throws SQLException{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, handler.getUserId());
        values.put(KEY_STATUS, handler.getStatus());
        db.insert(ON_OFF_TB, null, values);
        db.close();
    }


    public String getPhoneNum(String userId) throws SQLException{
        String num = "";
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + KEY_CONTACT + " from " + CONTACT_TABLE +
                " where " + KEY_USER_ID + " = " + userId +
                " ORDER BY " +  KEY_PRIMARY_ID + " desc LIMIT 1";
        Cursor c = db.rawQuery(query, null);
        if(c != null && c.moveToFirst()){
            num = c.getString(0);
            c.close();
        }

        return num;
    }

    public String getTrueUserId() throws SQLException{
        String id = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + KEY_USER_ID  + ", " + KEY_STATUS +
                " from " + REGISTRATION_TABLE +
                " ORDER BY " +  KEY_PRIMARY_ID + " desc LIMIT 1";
        Cursor c = db.rawQuery(query, null);
        if(c != null && c.moveToFirst()){
            if(c.getString(1).equals("true")) {
                id = c.getString(0);
            }
            c.close();
        }

        return id;
    }

    public boolean registrationOk() throws SQLException{
        boolean ok = false;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + KEY_USER_ID  + ", " + KEY_STATUS +
                " from " + REGISTRATION_TABLE +
                " ORDER BY " +  KEY_PRIMARY_ID + " desc LIMIT 1";
        Cursor c = db.rawQuery(query, null);
        if(c != null && c.moveToFirst()){
            if(c.getString(1).equals("true")) {
                ok = true;
            }
            c.close();
        }
        db.close();

        return ok;
    }

    public synchronized ArrayList<String[] > trackingData() throws SQLException{
        ArrayList<String[]> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + KEY_LATITUDE  + ", " + KEY_LONGITUDE +
                ", " + KEY_DATE_TIME + ", " + KEY_LOCATION +
                " from " + TRACKING_TABLE +
                " ORDER BY " +  KEY_PRIMARY_ID + " asc limit 100";
        Cursor c = db.rawQuery(query, null);
        while (c != null && c.moveToNext()){
            String[] data = new String[4];
            data[0] = c.getString(0);
            data[1] = c.getString(1);
            data[2] = c.getString(2);
            data[3] = c.getString(3);
            list.add(data);
        }
        if (c != null && !c.isClosed()){
            c.close();
        }
        db.close();

        return list;
    }

    public boolean isSqlEmpty() throws SQLException{
        boolean ok = false;
//        try {
//            semaphore.acquire();
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "select " + KEY_PRIMARY_ID +
                    " from " + TRACKING_TABLE +
                    " ORDER BY " + KEY_PRIMARY_ID + " desc LIMIT 1";
            Cursor c = db.rawQuery(query, null);
            if (c != null && c.moveToFirst()) {
                ok = true;
                c.close();
            }
            db.close();
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        finally {
//            semaphore.release();
//        }

        return ok;
    }

    public boolean isTrackingEnable() throws SQLException{
        boolean ok = false;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + KEY_TRACING_E_D +
                " from " + TRACKING_E_D_TB +
                " ORDER BY " +  KEY_PRIMARY_ID + " desc LIMIT 1";
        Cursor c = db.rawQuery(query, null);
        if(c != null && c.moveToFirst()){
            if(c.getString(0).equals("true")) {
                ok = true;
            }
            c.close();
        }
        db.close();

        return ok;
    }

    public boolean isTrackingTbEmpty() throws SQLException{
        boolean ok = true;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + KEY_PRIMARY_ID +
                " from " + TRACKING_E_D_TB +
                " ORDER BY " +  KEY_PRIMARY_ID + " desc LIMIT 1";
        Cursor c = db.rawQuery(query, null);
        if(c != null && c.moveToFirst()){
            ok = false;
            c.close();
        }
        db.close();

        return ok;
    }

    public synchronized void deleteTrakingData() throws SQLException{
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TRACKING_TABLE + " where " +
                KEY_PRIMARY_ID + " IN( select " + KEY_PRIMARY_ID + " from ( select " +
                KEY_PRIMARY_ID + " from " + TRACKING_TABLE +
                " ORDER BY " +  KEY_PRIMARY_ID + " asc limit 100)" +
                " x)");
        db.close();
    }

    public synchronized ArrayList<String[]> onOFFData() throws SQLException{
        ArrayList<String[]> list = new ArrayList<>();
        try {
            semaphore.acquire();
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "select " +
                    KEY_STATUS + ", " + KEY_DATE_TIME +
                    " from " + ON_OFF_TB +
                    " ORDER BY " + KEY_PRIMARY_ID + " asc LIMIT 100";
            Cursor c = db.rawQuery(query, null);
            while (c != null && c.moveToNext()) {
                String[] strings = new String[2];
                strings[0] = c.getString(0);
                strings[1] = c.getString(1);
                list.add(strings);
            }
            if (c != null && !c.isClosed()) {
                c.close();
            }
            db.close();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            semaphore.release();
        }

        return list;
    }

    public synchronized void deleteONOFF() throws SQLException{
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + ON_OFF_TB + " where " +
                KEY_PRIMARY_ID + " IN( select " + KEY_PRIMARY_ID + " from ( select " +
                KEY_PRIMARY_ID + " from " + ON_OFF_TB +
                " ORDER BY " +  KEY_PRIMARY_ID + " asc limit 100)" +
                " x)");
        db.close();
    }


}
