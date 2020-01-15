package uk.ac.exeter.contactlogger.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by apps4research on 2015-11-12.
 */
public class DBAdapter {

    // Database
    private static final String TAG = "DBAdapter";
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "contact_logger.sqlite";
    private static final String TABLE_NAME = "contacts";

    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_PHONE_ID = "phone_id";
    private static final String KEY_CON_TIME = "date_time";
    private static final String KEY_CON_TYPE = "type";
    private static final String KEY_CON_OTHER_TYPE = "other_type";
    private static final String KEY_CON_RELATE = "relation";
    private static final String KEY_CON_OTHER_REL = "other_rel";
    private static final String KEY_CON_TYPICAL = "typical";
    private static final String KEY_CON_DUR = "duration";
    private static final String KEY_CON_STAT = "status";
    private static final String KEY_CON_EXP = "experience";
    private static final String KEY_CON_GEN = "gender";
    private static final String KEY_CON_AGE = "age";
    private static final String KEY_CON_ATT = "attitude";
    private static final String KEY_LOC_LAT = "latitude";
    private static final String KEY_LOC_LNG = "longitude";
    private static final String KEY_LOC_ACC = "accuracy";
    private static final String KEY_CORR_LOC_LAT = "corr_lat";
    private static final String KEY_CORR_LOC_LNG = "corr_lon";
    private static final String KEY_IOLUX = "io_lux";
    private static final String KEY_IOMAG = "io_mag";

    // Create Table
    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_PHONE_ID + " TEXT,"
                    + KEY_CON_TIME + " TEXT,"
                    + KEY_CON_TYPE + " INTEGER,"
                    + KEY_CON_OTHER_TYPE + " TEXT,"
                    + KEY_CON_RELATE + " INTEGER,"
                    + KEY_CON_OTHER_REL + " TEXT,"
                    + KEY_CON_TYPICAL + " INTEGER,"
                    + KEY_CON_DUR + " INTEGER,"
                    + KEY_CON_STAT + " INTEGER,"
                    + KEY_CON_EXP + " INTEGER,"
                    + KEY_CON_GEN + " INTEGER,"
                    + KEY_CON_AGE + " INTEGER,"
                    + KEY_CON_ATT + " INTEGER,"
                    + KEY_LOC_LAT + " DOUBLE,"
                    + KEY_LOC_LNG + " DOUBLE,"
                    + KEY_LOC_ACC + " FLOAT,"
                    + KEY_CORR_LOC_LAT + " DOUBLE,"
                    + KEY_CORR_LOC_LNG + " DOUBLE,"
                    + KEY_IOLUX + " DOUBLE,"
                    + KEY_IOMAG + " DOUBLE"
                    + ")";

    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        // Upgrading database
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    // opens the database
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    // closes the database
    public void close() {
        DBHelper.close();
    }

    // Add new contact
    public long insertContact(String phoneid, String datetime, int type, String type_other,
                              int relation, String rel_other, int typical, int duration, int status,
                              int experience, int gender, int age, int attitude, double lat,
                              double lng, float accuracy, double corr_lat, double corr_lng,
                              int iolux, int iomag) {
        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_ID, phoneid); // Phone id
        values.put(KEY_CON_TIME, datetime); // Datetime
        values.put(KEY_CON_TYPE, type); // Setting / Type
        values.put(KEY_CON_OTHER_TYPE, type_other); // Setting / Type other
        values.put(KEY_CON_RELATE, relation); // Relationship
        values.put(KEY_CON_OTHER_REL, rel_other); // Relationship other
        values.put(KEY_CON_TYPICAL, typical); // Typicality
        values.put(KEY_CON_DUR, duration); // Duration
        values.put(KEY_CON_STAT, status); // Relative Status
        values.put(KEY_CON_EXP, experience); // Experience
        values.put(KEY_CON_GEN, gender); // Gender
        values.put(KEY_CON_AGE, age); // Age
        values.put(KEY_CON_ATT, attitude); // Attiutde
        values.put(KEY_LOC_LAT, lat); // Location - Latitude
        values.put(KEY_LOC_LNG, lng); // Location - Longitude
        values.put(KEY_LOC_ACC, accuracy); // Location - Accuracy
        values.put(KEY_CORR_LOC_LAT, corr_lat); // Location - Corrected Latitude
        values.put(KEY_CORR_LOC_LNG, corr_lng); // Location - Corrected Longitude
        values.put(KEY_IOLUX, iolux); // Lux
        values.put(KEY_IOMAG, iomag); // Magnetic

        // Insert new row
        return db.insert(TABLE_NAME, null, values);
    }

    // updates location of contact
    public boolean updateContactLocation(long rowId, double corrlat, double corrlng) {
        ContentValues values = new ContentValues();
        values.put(KEY_CORR_LOC_LAT, corrlat); // Location - Corrected Latitude
        values.put(KEY_CORR_LOC_LNG, corrlng); // Location - Corrected Longitude
        return db.update(TABLE_NAME, values, KEY_ID + "=" + rowId, null) > 0;
    }

    // Get log count
    public int getDBCount() {
        int count = 0;
        String countQuery = "SELECT id FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && !cursor.isClosed()) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    // Get daily log count
    public int getDailyCount(String today) {
        int count = 0;
        //count number of contacts today
        String countQuery = "SELECT id FROM " + TABLE_NAME + " WHERE " + KEY_CON_TIME + " like '" + today + "%'";
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && !cursor.isClosed()) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }
}