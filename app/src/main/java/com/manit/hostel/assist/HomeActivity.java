package com.manit.hostel.assist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

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
import java.util.Calendar;
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
        addDatesToDateSelectionSpinner();

        final MariaDBConnection.Callback mCallback =new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                Log.d(HomeActivity.this.toString(), "onResponse: " + result);
                try{
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    setupHostelSpinner(jsonArrayToList(dataArray));
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
        setUpViewEntryButton();
        lb.date.setText(getDateFormated());

    }

    private void setUpViewEntryButton() {
        final AutoCompleteTextView mDateSelectionAutoCompleteTextView = findViewById(R.id.spinner_date_selection);
        final     AutoCompleteTextView mHostelSelectionAutoCompleteTextView = findViewById(R.id.hostel_selection_spinner);

        final String mTableName =  mDateSelectionAutoCompleteTextView.getText().toString().replace("-","") +
                mHostelSelectionAutoCompleteTextView.getText().toString();
        Log.d(HomeActivity.this.toString(), "Hostel name : " + mHostelSelectionAutoCompleteTextView.getText().toString());
        final Intent mViewEntryActivity = new Intent(getApplicationContext(), ViewEnteryActivity.class);
        Log.d(HomeActivity.this.toString(), "table name : " + mTableName);
        mViewEntryActivity.putExtra(ViewEnteryActivity.INTENT_KEY_TABLE_NAME, mTableName);
        lb.viewEntries.setOnClickListener(v -> startActivity(mViewEntryActivity));

    }


    private void addDatesToDateSelectionSpinner() {

        final AutoCompleteTextView mDateSelectionAutoCompleteTextView = findViewById(R.id.spinner_date_selection);

//        mDateSelectionAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
//            Log.d(HomeActivity.this.toString(), "onItemSelected");
//            lb.viewEntries.setAlpha(1);
//            AppPref.setSelectedHostel(HomeActivity.this, parent.getItemAtPosition(position).toString());
//            lb.viewEntries.setEnabled(true);
//            lb.viewEntries.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ViewEnteryActivity.class)));
//        });
//        TextInputLayout mTextInputLayout = findViewById(R.id.spinner_hostel_select_text_input_layout);
//        mTextInputLayout.setHint("Select Hostel");
//        mTextInputLayout.setEnabled(true);

        List<String> dateList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        // Get the current date
        Calendar calendar = Calendar.getInstance();

        // Add 7 dates starting from today
        for (int i = 0; i < 7; i++) {
            dateList.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DATE, -1);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, dateList);
        mDateSelectionAutoCompleteTextView.setAdapter(adapter);
        mDateSelectionAutoCompleteTextView.setText(dateList.get(0), false); // false to not show dropdown

//        mDateSelectionAutoCompleteTextView.setSelection(1);

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
        final TextInputLayout mTextInputLayout = findViewById(R.id.spinner_hostel_select_text_input_layout);
        mTextInputLayout.setHint("Select Hostel");
        mTextInputLayout.setEnabled(true);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classes);
        final AutoCompleteTextView mHostelSelectionSpinnerAutoCompleteTextView = findViewById(R.id.hostel_selection_spinner);
        mHostelSelectionSpinnerAutoCompleteTextView.setAdapter(adapter);
//        mHostelSelectionSpinnerAutoCompleteTextView.showDropDown();
        mHostelSelectionSpinnerAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(HomeActivity.this.toString(), "onItemSelected");
            lb.viewEntries.setAlpha(1);
            AppPref.setSelectedHostel(HomeActivity.this, parent.getItemAtPosition(position).toString());
            lb.viewEntries.setEnabled(true);
        });
    }


}