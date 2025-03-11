package com.manit.hostel.assist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.manit.hostel.assist.data.AppPref;
import com.manit.hostel.assist.database.MariaDBConnection;
import com.manit.hostel.assist.databinding.ActivityHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    @NonNull
    ActivityHomeBinding lb;

    @Override
    protected void onResume() {
        super.onResume();
        getAndUpdateListOfPurposeInSpinner();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        addClickLogic();
        addDatesToDateSelectionSpinner();
        addCreateCategoryButton();
        initDateSelector();
        getAndUpdateListOfPurposeInSpinner();
        setUpViewEntryButton();

    }

    private void initDateSelector() {
        TextInputLayout dateInputLayout = findViewById(R.id.spinner_date_selection_text_input_layout);
        AutoCompleteTextView dateSelection = findViewById(R.id.date_picker_text);
        setTodaysDate(dateSelection);

        dateSelection.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String formattedDateT = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    year, month + 1, day);
            dateSelection.setText(formattedDateT);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDay);
                        dateSelection.setText(formattedDate);
                    },
                    year, month, day  // Pre-selected today
            );

            // Disable future dates
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });


    }

    private static void setTodaysDate(AutoCompleteTextView dateSelection) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String formattedDateT = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                year, month + 1, day);
        dateSelection.setText(formattedDateT);
    }

    private void getAndUpdateListOfPurposeInSpinner() {
        new MariaDBConnection(this).get_list_of_category_name_with_table_name_with_hostel_name(new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    hostel_names_parent_with_table_with_category_child = jsonResponse.getJSONObject("data");
                    setupHostelSpinner(jsonArrayToList(hostel_names_parent_with_table_with_category_child.names()));
                } catch (Exception e) {
                    onErrorResponse(e.getMessage());
                }
                Log.d(HomeActivity.class.getSimpleName(), "result : " + result);
            }

            @Override
            public void onErrorResponse(String error) {
                Toast.makeText(HomeActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                Log.d(HomeActivity.class.getSimpleName(), error);
            }
        });

    }


    private JSONObject hostel_names_parent_with_table_with_category_child;


    private void addCreateCategoryButton() {
        lb.createCategoryButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CategoryCreationActivity.class)));
    }

    private void setUpViewEntryButton() {
        lb.viewEntries.setOnClickListener(v -> {
            final AutoCompleteTextView mHostelSelectionAutoCompleteTextView = findViewById(R.id.hostel_selection_spinner);

            if (mHostelSelectionAutoCompleteTextView.getText().toString().equals("")) {
                mHostelSelectionAutoCompleteTextView.showDropDown();
                Toast.makeText(HomeActivity.this, getString(R.string.select_a_hostel), Toast.LENGTH_LONG).show();
                return;
            }
            Log.d(HomeActivity.this.toString(), "Hostel name : " + mHostelSelectionAutoCompleteTextView.getText().toString());
            final Intent mViewEntryActivity = new Intent(getApplicationContext(), ViewEntryActivity.class);
            mViewEntryActivity.putExtra(ViewEntryActivity.INTENT_TABLE_NAME, view_entries_table_name);
            mViewEntryActivity.putExtra(ViewEntryActivity.INTENT_PURPOSE, view_entries_purpose_name);
            AutoCompleteTextView datePickerText = findViewById(R.id.date_picker_text);
            mViewEntryActivity.putExtra(ViewEntryActivity.INTENT_KEY_DATE, datePickerText.getText().toString());
            mViewEntryActivity.putExtra(ViewEntryActivity.INTENT_KEY_HOSTEL_NAME, mHostelSelectionAutoCompleteTextView.getText().toString());
            startActivity(mViewEntryActivity);
        });

    }


    private void addDatesToDateSelectionSpinner() {

    }

    @NonNull
    public static List<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }


    private void addClickLogic() {
        lb.backbtn.setOnClickListener(v -> finish());
    }

    private String view_entries_table_name = null, view_entries_purpose_name;

    private void setPurposeSelector(final List<String> mPurposeList, final List<String> mTableNameList) {
        final TextInputLayout mTextInputLayout = findViewById(R.id.spinner_to_category_selection_text_input_layout);
        mTextInputLayout.setHint("Select Purpose");
        mTextInputLayout.setEnabled(true);


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mPurposeList);
        final AutoCompleteTextView mPurposeSelectionAutoTextView = findViewById(R.id.spinner_to_category_selection_autocomplete_textview);
        mPurposeSelectionAutoTextView.setText("");
        mPurposeSelectionAutoTextView.setAdapter(adapter);
        mPurposeSelectionAutoTextView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(HomeActivity.this.toString(), "onItemSelected");
            lb.viewEntries.setAlpha(1);
            lb.viewEntries.setEnabled(true);
            AppPref.setSelectedHostel(HomeActivity.this, parent.getItemAtPosition(position).toString());
            lb.viewEntries.setEnabled(true);
            view_entries_table_name = mTableNameList.get(position);
            view_entries_purpose_name = mPurposeList.get(position);
        });
        mPurposeSelectionAutoTextView.showDropDown();
    }


    private void setupHostelSpinner(List<String> classes) {
        final TextInputLayout mTextInputLayout = findViewById(R.id.spinner_hostel_select_text_input_layout);
        mTextInputLayout.setHint("Select Hostel");
        mTextInputLayout.setEnabled(true);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classes);
        final AutoCompleteTextView mHostelSelectionSpinnerAutoCompleteTextView = findViewById(R.id.hostel_selection_spinner);
        mHostelSelectionSpinnerAutoCompleteTextView.setAdapter(adapter);
        mHostelSelectionSpinnerAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(HomeActivity.this.toString(), "onItemSelected");

            try {

                List<String> mPurposeList = new ArrayList<>();
                List<String> mTableNameList = new ArrayList<>();

                final JSONArray table_name_with_purpose = hostel_names_parent_with_table_with_category_child.getJSONArray(mHostelSelectionSpinnerAutoCompleteTextView.getText().toString());
                for (int i = 0; i < table_name_with_purpose.length(); i++) {
                    JSONObject tableObject = table_name_with_purpose.getJSONObject(i);
                    String purpose = tableObject.getString("purpose");
                    String table_name = tableObject.getString("table_name");
                    Log.d(HomeActivity.class.getSimpleName(), "purpose : " + purpose + " table name : " + table_name);

                    mTableNameList.add(table_name);
                    mPurposeList.add(purpose);
                }

//                Iterator<String> keys = table_name_with_purpose.keys();
//                final List<String> purpose = new ArrayList<>();
//                final List<String> table_name = new ArrayList<>();
//
//                while (keys.hasNext()) {
//                    purpose.add(keys.next());
//                    String key = keys.next();
//                    table_name.add(table_name_with_purpose.getString(key));
//                    // Do something with key and value
//                }

                setPurposeSelector(mPurposeList, mTableNameList);


            } catch (Exception e) {
                Toast.makeText(HomeActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
//            lb.viewEntries.setAlpha(1);
//            lb.viewEntries.setEnabled(true);
//            AppPref.setSelectedHostel(HomeActivity.this, parent.getItemAtPosition(position).toString());
//            lb.viewEntries.setEnabled(true);
        });
    }


}