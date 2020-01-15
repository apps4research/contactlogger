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

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import uk.ac.exeter.contactlogger.AdminActivity;

public class mapsDBAdapter {

    private static final String TAG = mapsDBAdapter.class.getName();
    private static final Integer DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "tiles";

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public mapsDBAdapter(Context context) {
        DBHelper = new DatabaseHelper(context);
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, AdminActivity.MB_TILES, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
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
    public mapsDBAdapter open() throws SQLException {
        db = DBHelper.getReadableDatabase();
        return this;
    }

    //Get Tile in Database
    public Cursor getTile(int row, int column, int zoom) {
        return db.query(TABLE_NAME, new String[]{"tile_data"}, "tile_row = ? AND tile_column = ? AND zoom_level = ?",
                new String[]{Integer.toString(column), Integer.toString(row), Integer.toString(zoom)}, null, null, null);
    }

    // Get Bounds present in db
    public Cursor getBoundsMap() {
        return db.query("metadata", new String[]{"value"}, "name like \"bounds\"", null, null, null, null);
    }

    // get Min Zoom present in db
    public Cursor getMinZoom() {
        return db.query("metadata", new String[]{"value"}, "name like \"minzoom\"", null, null, null, null);
    }

    // get Max Zoom present in db
    public Cursor getMaxZoom() {
        return db.query("metadata", new String[]{"value"}, "name like \"maxzoom\"", null, null, null, null);
    }

    // closes the database
    public void close() {
        DBHelper.close();
    }

}
