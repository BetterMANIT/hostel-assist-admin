package com.manit.hostel.assist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputLayout;
import com.manit.hostel.assist.data.AppPref;
import com.manit.hostel.assist.database.MariaDBConnection;
import com.manit.hostel.assist.databinding.ActivityHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    @NonNull ActivityHomeBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        addClickLogic();


        final MariaDBConnection.Callback mCallback =new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                Log.d(HomeActivity.this.toString(), "onResponse: " + result);
                try{
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    setupHostelSpinner(jsonArrayToList(dataArray));
                    addClickLogicToViewEntries();
                } catch (JSONException e) {
                    onErrorResponse(e.getMessage());
                }
            }

            @Override
            public void onErrorResponse(String error) {
                Log.d(HomeActivity.this.toString(), "onError: " + error);

            }


        };
        new MariaDBConnection(this).get_list_of_hostel_names(mCallback);

        lb.date.setText(getDateFormated());
    }

    @NonNull
    public static List<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            list.add(jsonArray.getString(i));
        }
        return list;
    }


    private void addClickLogicToViewEntries() {
        lb.classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(HomeActivity.this.toString(), "onItemSelected");
                lb.viewEntries.setAlpha(1);
                AppPref.setSelectedHostel(HomeActivity.this, parent.getItemAtPosition(position).toString());
                lb.viewEntries.setEnabled(true);
                lb.viewEntries.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ViewEnteryActivity.class)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                lb.viewEntries.setEnabled(false);
                lb.viewEntries.setAlpha(0.5f);
            }
        });
    }

    private void addClickLogic() {
        lb.backbtn.setOnClickListener(v -> finish());
    }

    private String getDateFormated() {
        //get todays date in dd-mm-yyyy format
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }
    private void setupHostelSpinner(List<String> classes) {
        TextInputLayout mTextInputLayout = findViewById(R.id.spinner_hostel_select_text_input_layout);
        mTextInputLayout.setHint("Select Hostel");
        mTextInputLayout.setEnabled(true);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classes);
        lb.classSpinner.setAdapter(adapter);
    }


}