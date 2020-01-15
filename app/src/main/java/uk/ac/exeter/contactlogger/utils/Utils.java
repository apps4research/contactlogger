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
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import uk.ac.exeter.contactlogger.R;

public class Utils {

    private static final String TAG = Utils.class.getName();
    // Image directory for Photos
    public static final String PHOTO_ALBUM = "ContactLogger";
    private Context context;

     public static String getReadablePicName(String picName) {
        String[] split = picName.split("_");
        String date = split[1];
        String time = split[2];
        String dateTime = date + time;
        String format = "yyyyMMddHHmmss";

        try {
            return DateFormat.getDateTimeInstance().
                    format(new SimpleDateFormat(format, Locale.US).parse(dateTime));
        } catch (ParseException pe) {
            Log.d(TAG, "Unable to parse date from pic name", pe);
            return picName;
        }
    }

    public static List<String> ReadSDCard() {

        // Creating an empty string list array
        List<String> list = new ArrayList<>();

        File photos_folder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), PHOTO_ALBUM);
        File dir = new File(String.valueOf(photos_folder));

        if (dir.isDirectory()) {
            File[] files = dir.listFiles(jpgFilter);

            if (files.length != 0) {
                //sort files after Last modified date (newest first)
                Arrays.sort(files, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                            return -1;
                        } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                            return +1;
                        } else {
                            return 0;
                        }
                    }
                });

                for (File sfile : files) {
                    if (sfile.length() != 0) {
                        list.add(dir + File.separator + sfile.getName());
                    } else {
                        //deletes any 0 byte temp files created by camera handler abort
                        if (sfile.exists()) sfile.delete();
                    }
                }
            }
        } else {
            Log.d(TAG, "No files in folder");
        }

        return list;
    }

    //filter for jpg files
    static FilenameFilter jpgFilter = new FilenameFilter() {
        public boolean accept(File file, String name) {
            return name.endsWith(".jpg");
        }
    };

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String getDeviceId(Context context) {
        String prefs_name = context.getResources().getString(R.string.PREFS_NAME);
        SharedPreferences settings = context.getSharedPreferences(prefs_name, 0);
        String device_id = settings.getString("userid", "");
        if (device_id.isEmpty()) {
            device_id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }
        return device_id;
    }
}
