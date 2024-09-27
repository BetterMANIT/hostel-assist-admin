package com.manit.hostel.assist.database;

import android.telecom.Call;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MariaDBConnection {


    private static final String BASE_URL = "http://4.186.57.254";
    String url = "data";


    private RequestQueue mQueue;
    private AppCompatActivity mAppCompatActivity;
    public MariaDBConnection(AppCompatActivity mAppCompatActivity){
        mQueue = Volley.newRequestQueue(mAppCompatActivity);
        this.mAppCompatActivity = mAppCompatActivity;

    }


    public void fetchEntryExitList(Callback callback){
        StringRequest mStringRequest = new StringRequest(Request.Method.GET, BASE_URL+"/API/fetch_latest_entries.php", (Response.Listener<String>) response -> callback.onResponse(response), (Response.ErrorListener) error -> callback.onErrorResponse(error));
        mQueue.add(mStringRequest);
    }


    public interface Callback {
        void onResponse(String result);
        void onErrorResponse(VolleyError error);
    }

}
