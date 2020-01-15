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

package uk.ac.exeter.contactlogger;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;

import uk.ac.exeter.contactlogger.dialogs.SharedPrefsDialog;
import uk.ac.exeter.contactlogger.utils.DBAdapter;
import uk.ac.exeter.contactlogger.utils.Utils;
import uk.ac.exeter.contactlogger.utils.compressHandler;

public class AdminActivity extends Activity {

    private static final String TAG = AdminActivity.class.getName();
    public static final String DATA_DIR = "ContactLogger_data";
    public static final String MB_TILES = "offline_map.mbtiles";
    private static final Integer green = 0xFF00897B;
    private static final Integer red = 0xFFFF0000;

    private String device_id, mapfilename;
    private String slash_char = "";
    private TextView task_msg, map_status_msg, map_local_status_msg;
    private Button btn_delete, btn_local_delete, btn_import, btn_local_import;
    private ProgressBar mProgressBar;
    private int dl_progress, len, import_type;
    private int count = 0;
    private long total = 0;
    private File importDir, dbFile, dbExportFile, mapImportFile, mapExportFile;
    private Object[] photo_array;
    private EditText userid;
    private Toast toast;
    private SharedPreferences settings;
    private FragmentManager fm = getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String prefs_name = getResources().getString(R.string.PREFS_NAME);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_layout);

        //get number of shared preferences
        settings = getSharedPreferences(prefs_name, 0);
        int num_prefs = settings.getAll().size();

        //set/get EditText of userid
        userid = (EditText) findViewById(R.id.user_id);
        device_id = Utils.getDeviceId(this);
        userid.setText(device_id, TextView.BufferType.EDITABLE);
        userid.setSelection(userid.getText().length()); //set cursor to end

        //get photos and number
        photo_array = Utils.ReadSDCard().toArray();
        TextView filesize_photos = (TextView) findViewById(R.id.photos_info_val);
        if (!photos_filesize().isEmpty()) slash_char = getString(R.string.slash);
        filesize_photos.setText(photo_array.length + slash_char + photos_filesize());

        //get number of entries in db
        DBAdapter db = new DBAdapter(this);
        db.open();
        int num_contacts = db.getDBCount();
        db.close();

        //directories and paths
        importDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + DATA_DIR);
        dbFile = new File(getDatabasePath(DBAdapter.DATABASE_NAME).toString()); //database folder
        dbExportFile = new File(importDir, device_id + ".sqlite");
        mapExportFile = new File(getDatabasePath(MB_TILES).toString()); //database folder
        mapImportFile = new File(importDir, MB_TILES);

        //if import directory does not exist, create it
        if (!importDir.exists()) importDir.mkdirs();

        mProgressBar = (ProgressBar) findViewById(R.id.dwl_progress);

        //number of database and shared preferences entries
        final TextView num_entries = (TextView) findViewById(R.id.num_entries);
        final TextView num_pref_entries = (TextView) findViewById(R.id.num_pref_entries);
        task_msg = (TextView) findViewById(R.id.task_msg);
        map_status_msg = (TextView) findViewById(R.id.map_status_msg);
        map_local_status_msg = (TextView) findViewById(R.id.map_local_status_msg);
        num_entries.setText(String.valueOf(num_contacts));
        num_pref_entries.setText(String.valueOf(num_prefs));

        btn_import = (Button) findViewById(R.id.import_map);
        btn_delete = (Button) findViewById(R.id.del_map);
        btn_local_import = (Button) findViewById(R.id.import_local_map);
        btn_local_delete = (Button) findViewById(R.id.del_local_map);

        //check to see if mbtiles has already been downloaded
        if (mapImportFile.exists()) {
            btn_import.setEnabled(true);
        }

        //check to see if there is an mbfiles file in the
        // import folder for local import
        for (File file : importDir.listFiles()) {
            if (file.isFile()) {
                if (file.getName().endsWith(".mbtiles")) {
                    count++;
                    mapfilename = file.getName();
                }
            }
            if (count == 1) {
                // file is available for import, but hasn't been imported yet
                btn_local_import.setEnabled(true);
                map_local_status_msg.setTextColor(green);
                map_local_status_msg.setText(getString(R.string.map_found) + mapfilename);
            } else {
                // no map to import found, and no map imported yet
                map_local_status_msg.setTextColor(red);
                map_local_status_msg.setText(getString(R.string.map_not_found));
            }
        }

        //check to see if we already have an imported map db
        if (mapExportFile.exists()) {
            btn_delete.setEnabled(true);
            btn_local_delete.setEnabled(true);
            map_status_msg.setTextColor(green);
            map_status_msg.setText(getString(R.string.map_exists));
            map_local_status_msg.setTextColor(green);
            map_local_status_msg.setText(getString(R.string.map_exists));
        } else if (mapImportFile.exists() && !mapExportFile.exists()) {
            // file is available for import, but hasn't been imported yet
            map_status_msg.setTextColor(red);
            map_status_msg.setText(getString(R.string.map_import_ready));
        } else {
            map_status_msg.setTextColor(red);
            map_status_msg.setText(getString(R.string.map_nodwl_yet));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.logout:
                // logout by deleting shared pref
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("admin_login", false).apply();

                //return to main activity
                Intent launchMainActivity = new Intent(this, MainActivity.class);
                this.finish();
                startActivity(launchMainActivity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void backupDB(View view) {
        if (dbFile.exists()) {
            new exportDB().execute();
        }
    }

    public void importMap(View view) {
        if (mapImportFile.exists()) {
            import_type = 0; //remote
            new importMBTiles().execute();
        }
    }

    public void importLocalMap(View view) {
        if (!mapfilename.isEmpty()) {
            import_type = 1; //local
            mapImportFile = new File(importDir, mapfilename);
            new importMBTiles().execute();
        }
    }

    public void deleteMapDB(View view) {
        if (mapExportFile.exists()) {
            boolean del_status = mapExportFile.delete();
            if (del_status) {
                task_msg.setTextColor(red);
                task_msg.setText(getString(R.string.off_map_deleted));
                map_status_msg.setTextColor(red);
                map_status_msg.setText(getString(R.string.no_off_map));
                map_local_status_msg.setTextColor(red);
                map_local_status_msg.setText(getString(R.string.map_not_found));
                btn_delete.setEnabled(false);
                btn_local_delete.setEnabled(false);
            } else {
                task_msg.setTextColor(red);
                task_msg.setText(getString(R.string.del_map_error));
            }
        }
    }

    public void clearSharedPrefs(View view) {
        //get shared preferences and delete them
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().apply();

        //show toast message
        toast = Toast.makeText(this, getString(R.string.delete_ok), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 160);
        toast.show();
        recreate();
    }

    public void showSharedPrefs(View view) {
        SharedPrefsDialog SharedPrefsFormFragment = new SharedPrefsDialog();
        SharedPrefsFormFragment.show(fm, TAG);
    }

    public void clearDB(View view) {
        // delete the file
        if (deleteDatabase(DBAdapter.DATABASE_NAME)) {
            toast = Toast.makeText(this, getString(R.string.delete_ok), Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(this, getString(R.string.delete_fail), Toast.LENGTH_LONG);
        }
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 160);
        toast.show();
        recreate();
    }

    public void downloadMap(View view) {

        EditText download_url = (EditText) findViewById(R.id.map_dwl_url);
        String download_uri = download_url.getText().toString().trim();
        Uri Download_Uri = Uri.parse(download_uri);

        boolean status = importDir.exists() || importDir.mkdir();

        if (status) {

            //check if file already exists, if so delete it
            if (mapImportFile.exists()) {
                mapImportFile.delete();
            }

            if (!download_uri.trim().isEmpty()) {
                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                request.setAllowedOverRoaming(false);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                request.setTitle(getString(R.string.dwl_title));
                request.setDescription(getString(R.string.dwl_desc));
                request.setDestinationInExternalPublicDir(DATA_DIR, MB_TILES);

                final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                final long downloadId = manager.enqueue(request);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean downloading = true;
                        while (downloading) {
                            DownloadManager.Query q = new DownloadManager.Query();
                            q.setFilterById(downloadId);

                            Cursor cursor = manager.query(q);
                            cursor.moveToFirst();
                            int bytes_downloaded = cursor.getInt(cursor
                                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            int bytes_total = cursor.getInt(cursor.getColumnIndex(
                                    DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                            if (cursor.getInt(cursor.getColumnIndex(
                                    DownloadManager.COLUMN_STATUS)) == DownloadManager.
                                    STATUS_SUCCESSFUL) {
                                downloading = false;
                            }

                            dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(dl_progress);
                                    task_msg.setTextColor(green);
                                    task_msg.setText(String.valueOf(dl_progress) + getString(R.string.percent));
                                    if (dl_progress > 0 && dl_progress < 100) {
                                        map_status_msg.setTextColor(green);
                                        map_status_msg.setText(getString(R.string.downloading_map));
                                    } else if (dl_progress == 100 && mapImportFile.exists()) {
                                        btn_import.setEnabled(true);
                                        map_status_msg.setTextColor(red);
                                        map_status_msg.setText(getString(R.string.map_import_ready));
                                    }
                                }
                            });
                            cursor.close();
                        }
                    }
                }).start();
            } else {
                //url error
                task_msg.setTextColor(red);
                task_msg.setText(getString(R.string.map_url_error));
            }
        }
    }

    private class importMBTiles extends AsyncTask<String, Integer, Boolean> {

        protected void onPreExecute() {
            mProgressBar.setProgress(0);
            if (import_type == 0) {
                map_status_msg.setTextColor(green);
                map_status_msg.setText(getString(R.string.map_importing));
            } else {
                map_local_status_msg.setTextColor(green);
                map_local_status_msg.setText(getString(R.string.map_importing));
            }
            super.onPreExecute();
        }

        protected Boolean doInBackground(final String... args) {
            long fileLength = mapImportFile.length();
            try {
                InputStream in = new FileInputStream(mapImportFile);
                OutputStream out = new FileOutputStream(mapExportFile);
                byte[] buf = new byte[1024];
                while ((len = in.read(buf)) > 0) {
                    total += len;
                    out.write(buf, 0, len);
                    int progress = (int) (total * 100 / fileLength);
                    publishProgress(progress);
                }
                out.flush();
                out.close();
                in.close();
                return true;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressBar.setProgress(progress[0]);
            task_msg.setTextColor(green);
            task_msg.setText(String.valueOf(progress[0]) + getString(R.string.percent));
        }

        protected void onPostExecute(final Boolean success) {
            if (success) {
                task_msg.setTextColor(green);
                task_msg.setText(getString(R.string.end_import));
                mProgressBar.setProgress(0);

                // after successful import, delete downloaded file
                if (mapImportFile.exists()) {
                    mapImportFile.delete();
                    btn_import.setEnabled(false);
                    btn_local_import.setEnabled(false);
                }

                if (import_type == 0) {
                    btn_delete.setEnabled(true);
                    map_status_msg.setTextColor(green);
                    map_status_msg.setText(getString(R.string.map_exists));
                } else {
                    btn_local_delete.setEnabled(true);
                    map_local_status_msg.setTextColor(green);
                    map_local_status_msg.setText(getString(R.string.map_exists));
                }
            }
        }
    }

    private class exportDB extends AsyncTask<String, Integer, Boolean> {

        protected void onPreExecute() {
            mProgressBar.setProgress(0);
            super.onPreExecute();
        }

        protected Boolean doInBackground(final String... args) {
            long fileLength = dbFile.length();

            try {
                InputStream in = new FileInputStream(dbFile);
                OutputStream out = new FileOutputStream(dbExportFile);
                byte[] buf = new byte[1024];
                while ((len = in.read(buf)) > 0) {
                    total += len;
                    out.write(buf, 0, len);
                    int progress = (int) (total * 100 / fileLength);
                    publishProgress(progress);
                }
                out.flush();
                out.close();
                in.close();
                return true;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressBar.setProgress(progress[0]);
            task_msg.setTextColor(green);
            task_msg.setText(String.valueOf(progress[0]) + getString(R.string.percent));
        }

        protected void onPostExecute(final Boolean success) {
            if (success) {
                task_msg.setTextColor(green);
                task_msg.setText(getString(R.string.end_export));
                mProgressBar.setProgress(0);
            }
        }
    }

    public void deleteAllPhotos(View view) {
        int count = 0;
        boolean files_deleted = false;
        ImageLoader imageLoader = ImageLoader.getInstance();
        //Object[] photo_array = Utils.ReadSDCard().toArray(); //cast string list array to object array
        for (Object photo : photo_array) {
            count++;
            String imageUri = String.valueOf(photo);

            //delete image from memory cache and disk cache (if it was used)
            DiskCacheUtils.removeFromCache(imageUri, imageLoader.getDiskCache());
            MemoryCacheUtils.removeFromCache(imageUri, imageLoader.getMemoryCache());

            //delete file from storage
            File file = new File(imageUri);
            files_deleted = file.delete();
        }

        if (files_deleted) {
            toast = Toast.makeText(this, count + getString(R.string.admin_photos_deleted), Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(this, getString(R.string.admin_nothing_deleted), Toast.LENGTH_LONG);
        }

        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 160);
        toast.show();
        recreate();
    }

    public void zipAllPhotos(View view) {
        if (photo_array.length != 0) {
            String zipFile_path = importDir + File.separator + device_id + ".zip";
            String[] stringArray = Arrays.copyOf(photo_array, photo_array.length, String[].class);
            compressHandler compress = new compressHandler(stringArray, zipFile_path);
            compress.zip();
            File zipFile = new File(zipFile_path);
            if (zipFile.exists()) {
                toast = Toast.makeText(this, getString(R.string.zipped_ok), Toast.LENGTH_LONG);
            } else {
                toast = Toast.makeText(this, getString(R.string.zipped_failed), Toast.LENGTH_LONG);
            }
        } else {
            toast = Toast.makeText(this, getString(R.string.no_photos_backup), Toast.LENGTH_LONG);
        }
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 160);
        toast.show();
    }

    public String photos_filesize() {
        long result = 0;
        if (photo_array.length != 0) {
            for (Object photo : photo_array) {
                File file = new File(photo.toString());
                result += file.length();
            }
            return readableFileSize(result);
        } else {
            return "";
        }
    }

    private static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public void saveUserID(View view){
        SharedPreferences.Editor editor = settings.edit();
        String userid_to_save = userid.getText().toString().trim();
        if (!userid_to_save.isEmpty()) {
            editor.putString("userid", userid_to_save).apply();
            toast = Toast.makeText(this, getString(R.string.saved_userid), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 160);
            toast.show();
        }
        recreate();
    }
}
