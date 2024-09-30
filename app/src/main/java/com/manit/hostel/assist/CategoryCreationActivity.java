package com.manit.hostel.assist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.manit.hostel.assist.data.AppPref;
import com.manit.hostel.assist.database.MariaDBConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CategoryCreationActivity extends AppCompatActivity {

    private Button mCreateCategoryButton;
    private TextInputEditText mMYSQLConstantTableName,mMYSQLVariableTableName, mCategoryNameEditText;
    private TextInputLayout mHostelSpinnerTextInputLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_category);
//        addMYSQLDBExpandable();
        addCreateCategoryButton();
        fetchListOfHostels();


        addCategoryPurposeEditText();

    }

    private void addCreateCategoryButton() {
        this.mCreateCategoryButton = findViewById(R.id.buttonCreateCategory);
        mCreateCategoryButton.setOnClickListener(v -> {
            final AutoCompleteTextView mHostelSelectionSpinnerAutoCompleteTextView = findViewById(R.id.hostel_selection_spinner);
            if(mHostelSelectionSpinnerAutoCompleteTextView.getText() == null){
                mHostelSelectionSpinnerAutoCompleteTextView.showDropDown();
                Toast.makeText(CategoryCreationActivity.this, "Select a hostel", Toast.LENGTH_SHORT).show();
                return;
            }
            if(mCategoryNameEditText.getEditableText()==null || mCategoryNameEditText.getEditableText().toString().isEmpty()){
                mCategoryNameEditText.requestFocus();
                Toast.makeText(CategoryCreationActivity.this, "Please enter category name", Toast.LENGTH_SHORT).show();
                return;
            }
            if(mMYSQLConstantTableName.getEditableText()==null || mMYSQLConstantTableName.getEditableText().toString().isEmpty()){
                mMYSQLConstantTableName.requestFocus();
                Toast.makeText(CategoryCreationActivity.this, "Please enter table name", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog pd = new ProgressDialog(CategoryCreationActivity.this);
            pd.setMessage("Creating category");
            pd.show();

            new MariaDBConnection(CategoryCreationActivity.this)
                    .create_new_category_in_a_hostel(mCategoryNameEditText.getEditableText().toString(), mMYSQLConstantTableName.getEditableText().toString(),
                            mMYSQLVariableTableName.getEditableText().toString() ,
                            mHostelSelectionSpinnerAutoCompleteTextView.getEditableText().toString(),
                            AppPref.getUsername(CategoryCreationActivity.this),
                            new MariaDBConnection.Callback() {
                                @Override
                                public void onResponse(String result) {
                                    Toast.makeText(CategoryCreationActivity.this, "Category creation is successful", Toast.LENGTH_SHORT).show();
                                    Log.d(CategoryCreationActivity.class.getSimpleName(), result);
                                    pd.dismiss();
                                }

                                @Override
                                public void onErrorResponse(String error) {
                                    Toast.makeText(CategoryCreationActivity.this, "Error in category creation : " + error, Toast.LENGTH_SHORT).show();
                                    Log.d(CategoryCreationActivity.class.getSimpleName(), error);
                                    pd.dismiss();
                                }
                            });
        });
    }

    private void addCategoryPurposeEditText() {
         this.mMYSQLConstantTableName = findViewById(R.id.editTextMYSQLDBTableName);
         this.mMYSQLVariableTableName = findViewById(R.id.editTextMYSQLDBVariableTableNameSuffix);
         this.mCategoryNameEditText = findViewById(R.id.editTextScholarNumber);
        mCategoryNameEditText.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        char c = source.charAt(i);
                        if (!Character.isLetterOrDigit(c) && c != ' ' && c != '_') {
                            Toast.makeText(this, "Special characters are not allowed", Toast.LENGTH_SHORT).show();
                            return "";
                        }
                    }
                    return null;
                }
        });
        mCategoryNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s!=null && !s.toString().isEmpty()) {
                    mCreateCategoryButton.setAlpha(1);
                    mCreateCategoryButton.setEnabled(true);
                    mMYSQLConstantTableName.setText(s.toString().replaceAll(" ", "_"));
                }else{
                    mCreateCategoryButton.setAlpha(0.5F);
                    mCreateCategoryButton.setEnabled(false);
                }
            }
        });
    }

//    private void addMYSQLDBExpandable() {
//        ExpandableLayout mExpandableLayout = findViewById(R.id.mysql_db_info_expandable);
//        mExpandableLayout.expand();
//    }

    void fetchListOfHostels(){
        final MariaDBConnection.Callback mCallback = new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                Log.d(CategoryCreationActivity.this.toString(), "onResponse: " + result);
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
                Log.d(CategoryCreationActivity.this.toString(), "onError: " + error);

            }
        };
        new MariaDBConnection(this).get_list_of_hostel_names(mCallback);

    }

    private void setupHostelSpinner(List<String> classes) {

        this. mHostelSpinnerTextInputLayout = findViewById(R.id.spinner_hostel_select_text_input_layout);
        mHostelSpinnerTextInputLayout.setHint("Select Hostel");
        mHostelSpinnerTextInputLayout.setEnabled(true);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classes);
        final AutoCompleteTextView mHostelSelectionSpinnerAutoCompleteTextView = findViewById(R.id.hostel_selection_spinner);
        mHostelSelectionSpinnerAutoCompleteTextView.setAdapter(adapter);
//        mHostelSelectionSpinnerAutoCompleteTextView.showDropDown();
//        mHostelSelectionSpinnerAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
//            Log.d(HomeActivity.this.toString(), "onItemSelected");
//            lb.viewEntries.setAlpha(1);
//            AppPref.setSelectedHostel(HomeActivity.this, parent.getItemAtPosition(position).toString());
//            lb.viewEntries.setEnabled(true);
//        });
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


}