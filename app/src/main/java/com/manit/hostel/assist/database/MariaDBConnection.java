package com.manit.hostel.assist.database;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.manit.hostel.assist.data.AppPref;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MariaDBConnection {

    private static String BASE_URL;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;
    String URL_SUFFIX_GET_LIST_OF_HOSTEL_NAMES = "guard/get_list_of_hostel_names.php";
    String URL_SUFFIX_FETCH_ALL_ENTRIES_BY_TABLE_NAME = "guard/fetch_all_entries_by_table_name.php";
    String URL_SUFFIX_GET_STUDENT_INFO = "get_student_info.php";
    String URL_SUFFIX_ADD_STUDENT_ENTRY = "open_new_entry.php";
    String URL_SUFFIX_GET_LIST_OF_CATEGORY_NAME_WITH_TABLE_NAME_WITH_HOSTEL_NAME = "/guard/get_list_of_table_name_with_purposes_with_hostel_name.php";
    String URL_SUFFIX_CREATE_ENTRY_CATEGORY_IN_A_HOSTEL = "guard/create_entry_purpose_in_a_hostel.php";
    String URL_SUFFIX_CLOSE_STUDENT_ENTRY = "close_already_existing_entry.php";
    String URL_SUFFIX_GET_PURPOSE_BY_HOSTEL_NAME = "student/get_purposes_by_hostel_name.php";
    String URL_SUFFIX_CHECK_IF_NEW_UPDATE_IN_DB_TABLE = "guard/check_if_new_update_in_db_table.php";


    final String KEY_SUCCESS_STATUS_CODE = "success";
    final String KEY_ERROR_STATUS_CODE = "error";

    private final RequestQueue mQueue;
    private final AppCompatActivity mAppCompatActivity;

    public MariaDBConnection(AppCompatActivity mAppCompatActivity) {
        mQueue = Volley.newRequestQueue(mAppCompatActivity);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        this.mAppCompatActivity = mAppCompatActivity;
        BASE_URL = mFirebaseRemoteConfig.getString("BASE_URL") + "/API/";
    }

    public void get_list_of_hostel_names(Callback callback) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + URL_SUFFIX_GET_LIST_OF_HOSTEL_NAMES;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);
        StringRequest mStringRequest = getmStringRequest(callback, BASE_URL_PLUS_SUFFIX, Request.Method.GET);
        mQueue.add(mStringRequest);
    }

    void handleCallBackResponse(Callback callback, String response) {
        try {
            final JSONObject parentJSONObject = new JSONObject(response);
            String status = parentJSONObject.getString("status");
            if (status.equals(KEY_SUCCESS_STATUS_CODE)) {
                Log.d(MariaDBConnection.class.getSimpleName(), response);
                callback.onResponse(response);
            } else {
                Log.d(MariaDBConnection.class.getSimpleName(), response);
                callback.onErrorResponse(parentJSONObject.getString("message"));
            }
        } catch (JSONException e) {
            Log.d(MariaDBConnection.class.getSimpleName(), response);
            callback.onErrorResponse("Error : " + e.getMessage());
        }

    }

    public void fetchEntryExitList(Callback callback, String table_name, String purpose, String dateSelected) {
        String BASE_URL_PLUS_SUFFIX = BASE_URL + URL_SUFFIX_FETCH_ALL_ENTRIES_BY_TABLE_NAME + "?table_name=" + table_name + "&fromDate=" + dateSelected + "&toDate=" + dateSelected;
        if (purpose != null) BASE_URL_PLUS_SUFFIX += "&purpose=" + purpose;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);
        final StringRequest mStringRequest = getmStringRequest(callback, BASE_URL_PLUS_SUFFIX, Request.Method.GET);
        mQueue.add(mStringRequest);
    }

    public void get_student_info(Callback callback, String scholar_no) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + URL_SUFFIX_GET_STUDENT_INFO + "?scholar_no=" + scholar_no;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);
        final StringRequest mStringRequest = getmStringRequest(callback, BASE_URL_PLUS_SUFFIX, Request.Method.GET);
        mQueue.add(mStringRequest);
    }


    public void check_if_new_update_in_table(Callback callback, String table_name, String purpose) {

//        final RequestFuture<String> future = RequestFuture.newFuture();

        String BASE_URL_PLUS_SUFFIX = BASE_URL + URL_SUFFIX_CHECK_IF_NEW_UPDATE_IN_DB_TABLE + "?table_name=" + table_name + "&last_update=" + AppPref.getLastTimeTableFetchedUNIXTimestamp(mAppCompatActivity, table_name);
        if (purpose != null) BASE_URL_PLUS_SUFFIX += "&purpose=" + purpose;
        Log.e(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);

//        final StringRequest mStringRequests = new StringRequest(Request.Method.GET, BASE_URL_PLUS_SUFFIX, future, future);

        final StringRequest mStringRequest = getmStringRequest(callback, BASE_URL_PLUS_SUFFIX, Request.Method.GET);
        mQueue.add(mStringRequest);
    }

    public void get_list_of_category_name_with_table_name_with_hostel_name(Callback callback) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + URL_SUFFIX_GET_LIST_OF_CATEGORY_NAME_WITH_TABLE_NAME_WITH_HOSTEL_NAME;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);

        final StringRequest mStringRequest = getmStringRequest(callback, BASE_URL_PLUS_SUFFIX, Request.Method.POST);
        mQueue.add(mStringRequest);
    }

    public void add_entry_student(long scholar_no, String name, String room_no, String photo_url, String phone_no, String section, String hostel_name, String purpose, String table_name, String created_by, Callback callback) {

        Log.d(MariaDBConnection.class.getSimpleName(), "add_entry_student TABLE_NAME: " + table_name);

        final String BASE_URL_PLUS_SUFFIX = BASE_URL + URL_SUFFIX_ADD_STUDENT_ENTRY;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);

        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, (Response.Listener<String>) response -> handleCallBackResponse(callback, response), (Response.ErrorListener) error -> callback.onErrorResponse("Error : " + error.getMessage())) {

            // Override getParams() to send POST parameters
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("scholar_no", String.valueOf(scholar_no));
                params.put("name", name);
                params.put("room_no", String.valueOf(room_no));
                params.put("photo_url", photo_url);
                params.put("phone_no", String.valueOf(phone_no));
                params.put("section", section);
                params.put("hostel_name", hostel_name);
                params.put("purpose", purpose);
                params.put("table_name", table_name);
                params.put("created_by", created_by);
                Log.d(MariaDBConnection.class.getSimpleName(), "add_entry_student 2 TABLE_NAME: " + table_name);

                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("device-id", AppPref.getDeviceId(mAppCompatActivity));
                headers.put("token", AppPref.getAuthToken(mAppCompatActivity));
                headers.put("username", AppPref.getUsername(mAppCompatActivity));
                return headers;
            }
        };

        mQueue.add(mStringRequest);
    }


    public void get_category_details_by_hostel_name(Callback callback, String hostel_name) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + URL_SUFFIX_GET_PURPOSE_BY_HOSTEL_NAME + "?hostel_name=" + hostel_name;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);
        final StringRequest mStringRequest = getmStringRequest(callback, BASE_URL_PLUS_SUFFIX, Request.Method.GET);
        mQueue.add(mStringRequest);
    }

    private @NonNull StringRequest getmStringRequest(Callback callback, String BASE_URL_PLUS_SUFFIX, int type) {
        return new StringRequest(type, BASE_URL_PLUS_SUFFIX, (Response.Listener<String>) response -> handleCallBackResponse(callback, response), (Response.ErrorListener) error -> callback.onErrorResponse("Error : " + error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("device-id", AppPref.getDeviceId(mAppCompatActivity));
                headers.put("token", AppPref.getAuthToken(mAppCompatActivity));
                headers.put("username", AppPref.getUsername(mAppCompatActivity));
                return headers;
            }
        };
    }

    public void create_new_category_in_a_hostel(String category_name, String constant_table_name, String variable_table_name_suffix, String hostel_name, String created_by, Callback callback) {

        Log.d("create_new_category_in_a_hostel", "variable_table_name_suffix : " + variable_table_name_suffix);

        final String BASE_URL_PLUS_SUFFIX = BASE_URL + URL_SUFFIX_CREATE_ENTRY_CATEGORY_IN_A_HOSTEL;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);

        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, (Response.Listener<String>) response -> handleCallBackResponse(callback, response), (Response.ErrorListener) error -> callback.onErrorResponse("Error : " + error.getMessage())) {

            // Override getParams() to send POST parameters
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("constant_table_name", constant_table_name);
                params.put("variable_table_name_suffix", variable_table_name_suffix);
                params.put("hostel_name", hostel_name);
                params.put("created_by", created_by);
                params.put("purpose", category_name);
                return params;
            }

            @NonNull
            @Contract(pure = true)
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("device-id", AppPref.getDeviceId(mAppCompatActivity));
                headers.put("token", AppPref.getAuthToken(mAppCompatActivity));
                headers.put("username", AppPref.getUsername(mAppCompatActivity));
                return headers;
            }
        };

        mQueue.add(mStringRequest);
    }


    public void close_entry_student(String scholar_no, Callback callback) {

        final String BASE_URL_PLUS_SUFFIX = BASE_URL + URL_SUFFIX_CLOSE_STUDENT_ENTRY;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);

        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, (Response.Listener<String>) response -> handleCallBackResponse(callback, response), (Response.ErrorListener) error -> callback.onErrorResponse("Error : " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("scholar_no", String.valueOf(scholar_no));
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("device-id", AppPref.getDeviceId(mAppCompatActivity));
                headers.put("token", AppPref.getAuthToken(mAppCompatActivity));
                headers.put("username", AppPref.getUsername(mAppCompatActivity));
                return headers;
            }
        };

        mQueue.add(mStringRequest);


    }

    public String getPhotoUrl(String scholarno) {
        return BASE_URL+"/student/photos/"+scholarno+".png";
    }

    public interface Callback {
        void onResponse(String result);

        void onErrorResponse(String error);
    }

}
