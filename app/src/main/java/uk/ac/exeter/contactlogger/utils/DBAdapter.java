/*
 * Copyright (C) 2016 Tina Keil (apps4research) & Miriam Koschate-Reis.
 * All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.exeter.contactlogger.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

    // Database
    private static final String TAG = "DBAdapter";
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "contact_logger.sqlite";
    private static final String TABLE_NAME1 = "contacts";
    private static final String TABLE_NAME2 = "items";

    // Table Column names for contacts
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
    private static final String KEY_LOC_LAT = "latitude";
    private static final String KEY_LOC_LNG = "longitude";
    private static final String KEY_LOC_ACC = "accuracy";
    private static final String KEY_CORR_LOC_LAT = "corr_lat";
    private static final String KEY_CORR_LOC_LNG = "corr_lon";

    // Table column names for items
    private static final String KEY_ITEM1 = "item1";
    private static final String KEY_ITEM2 = "item2";
    private static final String KEY_ITEM3 = "item3";

    // Create Table contacts
    private static final String CREATE_TABLE1 =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME1 + "("
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
                    + KEY_LOC_LAT + " DOUBLE,"
                    + KEY_LOC_LNG + " DOUBLE,"
                    + KEY_LOC_ACC + " FLOAT,"
                    + KEY_CORR_LOC_LAT + " DOUBLE,"
                    + KEY_CORR_LOC_LNG + " DOUBLE"
                    + ")";

    private static final String CREATE_TABLE2 =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME2 + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_PHONE_ID + " TEXT,"
                    + KEY_CON_TIME + " TEXT,"
                    + KEY_ITEM1 + " INTEGER,"
                    + KEY_ITEM2 + " INTEGER,"
                    + KEY_ITEM3 + " INTEGER"
                    + ")";

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context context) {
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE1);
                db.execSQL(CREATE_TABLE2);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        // Upgrading database
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME1);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
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
                              int experience, int gender, int age, double lat, double lng,
                              float accuracy, double corr_lat, double corr_lng) {
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
        values.put(KEY_LOC_LAT, lat); // Location - Latitude
        values.put(KEY_LOC_LNG, lng); // Location - Longitude
        values.put(KEY_LOC_ACC, accuracy); // Location - Accuracy
        values.put(KEY_CORR_LOC_LAT, corr_lat); // Location - Corrected Latitude
        values.put(KEY_CORR_LOC_LNG, corr_lng); // Location - Corrected Longitude

        // Insert new row
        return db.insert(TABLE_NAME1, null, values);
    }

    // Add new items
    public long insertItems(String phoneid, String datetime, int item1, int item2, int item3) {
        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_ID, phoneid); // Phone id
        values.put(KEY_CON_TIME, datetime); // Datetime
        values.put(KEY_ITEM1, item1); // Item1
        values.put(KEY_ITEM2, item2); // Item2
        values.put(KEY_ITEM3, item3); // Item3

        // Insert new row
        return db.insert(TABLE_NAME2, null, values);
    }

    // updates location of contact
    public boolean updateContactLocation(long rowId, double corrlat, double corrlng) {
        ContentValues values = new ContentValues();
        values.put(KEY_CORR_LOC_LAT, corrlat); // Location - Corrected Latitude
        values.put(KEY_CORR_LOC_LNG, corrlng); // Location - Corrected Longitude
        return db.update(TABLE_NAME1, values, KEY_ID + "=" + rowId, null) > 0;
    }

    // Get log count
    public int getDBCount() {
        int count = 0;
        String countQuery = "SELECT id FROM " + TABLE_NAME1;
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
        String countQuery = "SELECT id FROM " + TABLE_NAME1 + " WHERE " + KEY_CON_TIME + " like '" + today + "%'";
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && !cursor.isClosed()) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }
}
