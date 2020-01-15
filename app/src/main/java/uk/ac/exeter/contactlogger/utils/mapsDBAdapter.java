package uk.ac.exeter.contactlogger.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import uk.ac.exeter.contactlogger.AdminActivity;

/**
 * Created by apps4research on 2015-11-12.
 */
public class mapsDBAdapter {

    private static final String TAG = mapsDBAdapter.class.getName();
    private static final Integer DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "tiles";

    private static Context ctx;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public mapsDBAdapter(Context context) {
        ctx = context;
        DBHelper = new DatabaseHelper(ctx);
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
