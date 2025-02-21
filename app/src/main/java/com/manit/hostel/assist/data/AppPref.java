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
    private static final String DEVICE_ID = "device_id";
    private static final String AUTH_TOKEN = "auth_token";
    private static final String USERNAME = "username";
    private static final String PHONE_NUMBER = "phone_number";

    public static void setSelectedHostel(HomeActivity c, String string) {
        c.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(SELECTED_HOSTEL, string).apply();
    }
    public static String getSelectedHostel(HomeActivity c) {
        return c.getSharedPreferences(APREF, MODE_PRIVATE).getString(SELECTED_HOSTEL, "");
    }

    public static void setDeviceId(Context context, String deviceId) {
        context.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(DEVICE_ID, deviceId).apply();
    }
    public static String getDeviceId(Context context) {
        return context.getSharedPreferences(APREF, MODE_PRIVATE).getString(DEVICE_ID, "");
    }

    public static void setAuthToken(Context context, String token) {
        context.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(AUTH_TOKEN, token).apply();
    }
    public static String getAuthToken(Context context) {
        return context.getSharedPreferences(APREF, MODE_PRIVATE).getString(AUTH_TOKEN, "");
    }

    public static void setUsername(Context context, String username) {
        context.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(USERNAME, username).apply();
    }
    public static String getUsername(Context context) {
        return context.getSharedPreferences(APREF, MODE_PRIVATE).getString(USERNAME, "guard");
    }

    public static void setPhoneNumber(Context context, String phoneNumber) {
        context.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(PHONE_NUMBER, phoneNumber).apply();
    }
    public static String getPhoneNumber(Context context) {
        return context.getSharedPreferences(APREF, MODE_PRIVATE).getString(PHONE_NUMBER, "");
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
