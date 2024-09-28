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

public class MariaDBConnection {


    private static final String BASE_URL = "http://4.186.57.254/API/";
    String URL_SUFFIX_GET_LIST_OF_HOSTEL_NAMES = "get_list_of_hostel_names.php";
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
                callback.onResponse(response);
            }else{
                callback.onErrorResponse(parentJSONObject.getString("message"));
            }
        } catch (JSONException e) {
            callback.onErrorResponse("Error : " + e.getMessage());
        }

    }
    public void fetchEntryExitList(Callback callback){
        final StringRequest mStringRequest = new StringRequest(Request.Method.GET, BASE_URL+"/API/fetch_latest_entries.php", (Response.Listener<String>) response -> callback.onResponse(response), (Response.ErrorListener) error -> callback.onErrorResponse("Error : " + error.getMessage()));
        mQueue.add(mStringRequest);
    }


    public interface Callback {
        void onResponse(String result);
        void onErrorResponse(String error);
    }

}
