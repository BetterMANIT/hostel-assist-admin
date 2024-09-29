package com.manit.hostel.assist.data;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.manit.hostel.assist.HomeActivity;

import org.jetbrains.annotations.Contract;

public class AppPref {
    private static final String APREF = "apref";
    private static final String SELECTED_HOSTEL = "selected_hostel";

    public static void setSelectedHostel(HomeActivity c, String string) {
        c.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(SELECTED_HOSTEL, string).apply();
    }
    public static String getSelectedHostel(HomeActivity c) {
        return c.getSharedPreferences(APREF, MODE_PRIVATE).getString(SELECTED_HOSTEL, "");
    }


    private static final String KEY_SHARED_PREFERENCES_TABLE_MANAGER = "table_manager";
    @Contract("_, _ -> !null")
    public static long getLastTimeTableFetchedUNIXTimestamp(Context mContext, String table_name){
       return mContext.getSharedPreferences(KEY_SHARED_PREFERENCES_TABLE_MANAGER, MODE_PRIVATE).getLong(table_name, 0);
    }
    public static void setLastTimeTableFetchedUNIXTimestamp(@NonNull Context mContext, String table_name, long last_update){
         mContext.getSharedPreferences(KEY_SHARED_PREFERENCES_TABLE_MANAGER, MODE_PRIVATE).edit().putLong(table_name, last_update).apply();
        Log.d(AppPref.class.getSimpleName(), "Table name : " + table_name + ", last update : " + last_update);
    }

}
