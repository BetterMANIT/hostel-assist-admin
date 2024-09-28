package com.manit.hostel.assist.database;

import android.telecom.Call;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MariaDBConnection {


    private static final String BASE_URL = "http://4.186.57.254/API/";
    String URL_SUFFIX_GET_LIST_OF_HOSTEL_NAMES = "get_list_of_hostel_names.php";
    String URL_SUFFIX_FETCH_ALL_ENTRIES_BY_TABLE_NAME = "fetch_all_entries_by_table_name.php";
    String URL_SUFFIX_GET_STUDENT_INFO = "get_student_info.php";
    String URL_SUFFIX_ADD_STUDENT_ENTRY = "open_new_entry.php";
    String URL_SUFFIX_CLOSE_STUDENT_ENTRY = "close_already_existing_entry.php";

    final String KEY_SUCCESS_STATUS_CODE = "success";
    final String KEY_ERROR_STATUS_CODE = "error";

    private final RequestQueue mQueue;
    private AppCompatActivity mAppCompatActivity;
    public MariaDBConnection(AppCompatActivity mAppCompatActivity){
        mQueue = Volley.newRequestQueue(mAppCompatActivity);
        this.mAppCompatActivity = mAppCompatActivity;

    }

    public void get_list_of_hostel_names(Callback callback){
        // No GET or POST Params required
        final String BASE_URL_PLUS_SUFFIX = BASE_URL+URL_SUFFIX_GET_LIST_OF_HOSTEL_NAMES;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);
        StringRequest mStringRequest = new StringRequest(Request.Method.GET, BASE_URL_PLUS_SUFFIX , (Response.Listener<String>) response -> {handleCallBackResponse(callback, response);}, (Response.ErrorListener) error -> {callback.onErrorResponse("Error : " + error.getMessage());});
        mQueue.add(mStringRequest);
    }

    void handleCallBackResponse(Callback callback, String response){
        try {
            final JSONObject parentJSONObject = new JSONObject(response);
            String status = parentJSONObject.getString("status");
            if(status.equals(KEY_SUCCESS_STATUS_CODE)){
                Log.d(MariaDBConnection.class.getSimpleName(), response );
                callback.onResponse(response);
            }else{
                Log.d(MariaDBConnection.class.getSimpleName(), response );
                callback.onErrorResponse(parentJSONObject.getString("message"));
            }
        } catch (JSONException e) {
            Log.d(MariaDBConnection.class.getSimpleName(), response );
            callback.onErrorResponse("Error : " + e.getMessage());
        }

    }
    public void fetchEntryExitList(Callback callback, String table_name){
        final String BASE_URL_PLUS_SUFFIX = BASE_URL+URL_SUFFIX_FETCH_ALL_ENTRIES_BY_TABLE_NAME+"?table_name="+table_name;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);
        final StringRequest mStringRequest = new StringRequest(Request.Method.GET,BASE_URL_PLUS_SUFFIX , (Response.Listener<String>) response -> callback.onResponse(response), (Response.ErrorListener) error -> callback.onErrorResponse("Error : " + error.getMessage()));
        mQueue.add(mStringRequest);
    }

    public void get_student_info(Callback callback, String scholar_no){
        final String BASE_URL_PLUS_SUFFIX = BASE_URL+URL_SUFFIX_GET_STUDENT_INFO+"?scholar_no="+scholar_no;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);
        final StringRequest mStringRequest = new StringRequest(Request.Method.GET,BASE_URL_PLUS_SUFFIX , (Response.Listener<String>) response -> {handleCallBackResponse(callback, response);}, (Response.ErrorListener) error -> callback.onErrorResponse("Error : " + error.getMessage()));
        mQueue.add(mStringRequest);
    }



    public void add_entry_student(long scholar_no,
                                  String name,
                                  int room_no,
                                  String photo_url, int phone_no,
                                  String section,
                                  String hostel_name,
                                  Callback callback){

        final String BASE_URL_PLUS_SUFFIX = BASE_URL+URL_SUFFIX_ADD_STUDENT_ENTRY;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);

        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX,
                (Response.Listener<String>) response -> handleCallBackResponse(callback, response),
                (Response.ErrorListener) error -> callback.onErrorResponse("Error : " + error.getMessage())) {

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
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";  // Proper content type for sending form data
            }
        };

        mQueue.add(mStringRequest);


    }

    public void close_entry_student(String scholar_no, Callback callback){

        final String BASE_URL_PLUS_SUFFIX = BASE_URL+URL_SUFFIX_CLOSE_STUDENT_ENTRY;
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to : " + BASE_URL_PLUS_SUFFIX);

        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX,
                (Response.Listener<String>) response -> handleCallBackResponse(callback, response),
                (Response.ErrorListener) error -> callback.onErrorResponse("Error : " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("scholar_no", String.valueOf(scholar_no));
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";  // Proper content type for sending form data
            }
        };

        mQueue.add(mStringRequest);


    }

    public interface Callback {
        void onResponse(String result);
        void onErrorResponse(String error);
    }

}
