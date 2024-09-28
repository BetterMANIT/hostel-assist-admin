package com.manit.hostel.assist;

import static com.manit.hostel.assist.adapters.EntriesAdapter.ALL_FILTER;
import static com.manit.hostel.assist.adapters.EntriesAdapter.ENTERED_FILTER;
import static com.manit.hostel.assist.adapters.EntriesAdapter.EXIT_ONLY_FILTER;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.VolleyError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.manit.hostel.assist.adapters.EntriesAdapter;
import com.manit.hostel.assist.data.Entries;
import com.manit.hostel.assist.database.MariaDBConnection;
import com.manit.hostel.assist.databinding.ActivityViewEntriesBinding;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ViewEnteryActivity extends AppCompatActivity {
    ActivityViewEntriesBinding lb;

    private EntriesAdapter entAdapter;
    private int currentFilter = ALL_FILTER;
    private boolean searchEnable = false;

    public static String INTENT_KEY_TABLE_NAME = "table_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityViewEntriesBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        lb.hostelName.setText(getHostelName(getMacAddress())); // Set the hostel name
        lb.date.setText(getDateFormated());  // Set the date
        lb.studentsListRecyclerview.post(() -> lb.studentsListRecyclerview.setLayoutManager(new LinearLayoutManager(this)));
        addClickLogicToFilters();
        addClickLogic();
        addFloatingActionButton();
        fetchEntries("j",new Date());
        Log.d(ViewEnteryActivity.class.getSimpleName(), "table name : " + getIntent().getStringExtra(INTENT_KEY_TABLE_NAME));


    }

    private void addFloatingActionButton() {
        final FloatingActionButton mFloatingActionButton = findViewById(R.id.floatingActionButton);
        mFloatingActionButton.setOnClickListener(v -> showAddEntriesDialog());
    }

    private void showAddEntriesDialog() {
        // Create a Dialog

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_entries);
        dialog.setCancelable(true);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Set to match parent width
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Set height as desired
        dialog.getWindow().setAttributes(layoutParams);


        // Initialize the input field and button
        final TextInputEditText editTextScholarNumber = dialog.findViewById(R.id.editTextScholarNumber);
        Button buttonProceed = dialog.findViewById(R.id.buttonProceed);

        final LinearLayout mEnterScholarNoLinearLayout = dialog.findViewById(R.id.enter_scholar_no_linear_layout);
        final LinearLayout mShowDetailsByScholarNoLinearLayout = dialog.findViewById(R.id.fetched_student_info_linear_layout);
        mEnterScholarNoLinearLayout.setVisibility(View.VISIBLE);
        mShowDetailsByScholarNoLinearLayout.setVisibility(View.GONE);
        final Button mGoBackButtonShowDetailsByScholarNo = dialog.findViewById(R.id.go_back_button);
        mGoBackButtonShowDetailsByScholarNo.setOnClickListener(v -> {
            mEnterScholarNoLinearLayout.setVisibility(View.VISIBLE);
            mShowDetailsByScholarNoLinearLayout.setVisibility(View.GONE);
        });

        final TextView mStudentNameTextView = dialog.findViewById(R.id.name);
        final TextView mScholarNumberTextView = dialog.findViewById(R.id.scholar_no);
        final TextView mPhoneNumberTextView = dialog.findViewById(R.id.phone_no);
        final TextView mRoomNoTextView = dialog.findViewById(R.id.room_no);
        final TextView mHostelNameTextView = dialog.findViewById(R.id.hostel_name);
        final TextView mEntryAlreadyExists = dialog.findViewById(R.id.entry_already_exists);
        final Button mCreateEntryButtonByScholarNo = dialog.findViewById(R.id.create_entry_button);
        final Button mCloseEntryButtonByScholarNo = dialog.findViewById(R.id.close_entry_button);


        // Set a click listener on the button
        buttonProceed.setOnClickListener(view -> {
            final String scholarNumber = editTextScholarNumber.getText().toString();
            new MariaDBConnection(ViewEnteryActivity.this).get_student_info(new MariaDBConnection.Callback() {
                @Override
                public void onResponse(String result) {
                    try{
                        final JSONObject mJSONObject = new JSONObject(result);
                        final JSONObject student_info = mJSONObject.getJSONObject("data");
                        mScholarNumberTextView.setText(getString(R.string.scholar_no) + " : " + scholarNumber);
                        mPhoneNumberTextView.setText(getString(R.string.phone_no) + " : " + String.valueOf(student_info.getInt("phone_no")));
                        mStudentNameTextView.setText(getString(R.string.name) + " : " + student_info.getString("name"));
                        mRoomNoTextView.setText(getString(R.string.room_no) + " : " + student_info.getInt("room_no"));
                        mHostelNameTextView.setText(getString(R.string.hostel_name) + " : " + student_info.getString("hostel_name"));
                        if(!student_info.isNull("entry_exit_table_name")){
                            Log.d(ViewEnteryActivity.class.getSimpleName(),"entry exit table is not null");
                            mCreateEntryButtonByScholarNo.setVisibility(View.GONE);
                            mEntryAlreadyExists.setVisibility(View.VISIBLE);
                            mCloseEntryButtonByScholarNo.setVisibility(View.VISIBLE);
                        }else {
                            Log.d(ViewEnteryActivity.class.getSimpleName(),"entry exit table is null");
//                            mCreateEntryButtonByScholarNo.setVisibility(View.VISIBLE);
//                            mCloseEntryButtonByScholarNo.setVisibility(View.GONE);
//                            mEntryAlreadyExists.setVisibility(View.GONE);

                        }
//                        else {
//
//                        }
                    }catch (Exception e){
                        Toast.makeText(ViewEnteryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        onErrorResponse(e.getMessage());
                    }
                    Log.d(ViewEnteryActivity.class.getSimpleName(), "result : " + result);
                    Toast.makeText(ViewEnteryActivity.this, result, Toast.LENGTH_SHORT).show();
                    mEnterScholarNoLinearLayout.setVisibility(View.GONE);


                    mShowDetailsByScholarNoLinearLayout.setVisibility(View.VISIBLE);
                    mCreateEntryButtonByScholarNo.setOnClickListener(v -> {
                        try{
                            final JSONObject mJSONObject = new JSONObject(result);
                            final JSONObject student_info = mJSONObject.getJSONObject("data");
                            final String name = student_info.getString("name");
                            final long scholar_no = Long.parseLong(scholarNumber);
                            final int phone_no = student_info.getInt("phone_no");
                            final int room_no = student_info.getInt("room_no");
                            final String photo_url = student_info.getString("photo_url");
                            final String section = student_info.getString("section");
                            final String hostel_name = student_info.getString("hostel_name");

                            new MariaDBConnection(ViewEnteryActivity.this).add_entry_student(scholar_no, name, room_no, photo_url, phone_no, section, hostel_name, new MariaDBConnection.Callback() {
                                @Override
                                public void onResponse(String result) {
                                    Toast.makeText(ViewEnteryActivity.this, result, Toast.LENGTH_LONG).show();
                                }
                                @Override
                                public void onErrorResponse(String error) {
                                    Toast.makeText(ViewEnteryActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            });

                        }catch (Exception e){
                            Toast.makeText(ViewEnteryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        mCreateEntryButtonByScholarNo.setOnClickListener(v1 -> Toast.makeText(ViewEnteryActivity.this, "In progress", Toast.LENGTH_LONG).show());
                        dialog.dismiss();
                   });
                    mCloseEntryButtonByScholarNo.setOnClickListener(v ->{ new MariaDBConnection(ViewEnteryActivity.this).close_entry_student(scholarNumber, new MariaDBConnection.Callback() {
                        @Override
                        public void onResponse(String result) {
                            Toast.makeText(ViewEnteryActivity.this, result, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onErrorResponse(String error) {
                            Toast.makeText(ViewEnteryActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
                        dialog.dismiss();
                    });
                }

                @Override
                public void onErrorResponse(String error) {
                    Toast.makeText(ViewEnteryActivity.this, error, Toast.LENGTH_SHORT).show();

                    Log.e("Error", error);

                }
            }, scholarNumber);
            // Proceed with the scholar number
            // You can call a method here or pass the scholar number to another activity or fragment
            // Example: Log.d("Scholar Number", scholarNumber);
//            dialog.dismiss(); // Close the dialog
        });

        // Show the dialog
        dialog.show();
    }

    private void addClickLogic() {
        lb.backbtn.setOnClickListener(v -> finish());
        lb.searchBar.setVisibility(View.GONE);
        lb.searchBtn.setOnClickListener(v -> {
            if (searchEnable) {
                lb.searchBar.setVisibility(View.GONE);
                searchEnable = false;
                lb.searchBtn.setImageTintList(getResources().getColorStateList(R.color.white, null));
                lb.searchBtn.setImageDrawable(getDrawable(R.drawable.search));
            } else {
                searchEnable = true;
                lb.searchBar.setVisibility(View.VISIBLE);
                lb.searchBtn.setImageTintList(getResources().getColorStateList(R.color.color1, null));
                lb.searchBtn.setImageDrawable(getDrawable(R.drawable.close_24dp_5f6368_fill0_wght400_grad0_opsz24));
            }
        });

        lb.searchtxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String txt = lb.searchtxt.getText().toString();
                if (!txt.isEmpty() && !txt.isEmpty() && entAdapter != null) {
                    entAdapter.filterSearch(txt);
                }
            }
        });
    }

    private void addClickLogicToFilters() {
        lb.allFilter.setOnClickListener(v -> {
            if (entAdapter != null) {
                currentFilter = ALL_FILTER;
                entAdapter.filterEntries(ALL_FILTER);
                setFilterActive(lb.allFilter);
            }
        });
        lb.exitOnlyFilter.setOnClickListener(v -> {
            if (entAdapter != null) {
                currentFilter = EXIT_ONLY_FILTER;
                entAdapter.filterEntries(EXIT_ONLY_FILTER);
            }
        });
        lb.entered.setOnClickListener(v -> {
            if (entAdapter != null) {
                currentFilter = ENTERED_FILTER;
                entAdapter.filterEntries(ENTERED_FILTER);
            }
        });
    }

    private void setFilterActive(MaterialButton view) {
        lb.allFilter.setBackgroundTintList(getResources().getColorStateList(R.color.color2_light, null));
        lb.exitOnlyFilter.setBackgroundTintList(getResources().getColorStateList(R.color.color2_light, null));
        lb.entered.setBackgroundTintList(getResources().getColorStateList(R.color.color2_light, null));
        lb.allFilter.setTextColor(getResources().getColor(R.color.color1, null));
        lb.exitOnlyFilter.setTextColor(getResources().getColor(R.color.color1, null));
        lb.entered.setTextColor(getResources().getColor(R.color.color1, null));
        view.setBackgroundTintList(getResources().getColorStateList(R.color.color1, null));
        view.setTextColor(getResources().getColor(R.color.white, null));

    }

    private String getDateFormated() {
        //get todays date in dd-mm-yyyy format
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }

    private String getHostelName(String macAddress) {
        return "Hostel 10 C";
    }


    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<Entries> entriesList = fetchEntries(getMacAddress(), new Date());
        lb.status.setText("Exit : " + entriesList.size() + "       Entered : 50");
        if (entAdapter == null) {
            entAdapter = new EntriesAdapter(entriesList);
            lb.studentsListRecyclerview.setAdapter(entAdapter);
        } else {
            entAdapter.updateEntries(entriesList);
        }
        entAdapter.filterEntries(currentFilter);

    }

    private ArrayList<Entries> fetchEntries(String hostelName, Date date) {
        ArrayList<Entries> entriesList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Entries entries = new Entries("Salmon Khan");
            entries.setEntryNo("24092024C"+i);
            entriesList.add(entries);
        }


        MariaDBConnection dbConnection = new MariaDBConnection(this);
        dbConnection.fetchEntryExitList(new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                Log.d(ViewEnteryActivity.class.getSimpleName(), result.toString());
                Log.e("list", result);
            }

            @Override
            public void onErrorResponse(String error) {
                Log.d(ViewEnteryActivity.class.getSimpleName(), error.toString());

            }
        }, getIntent().getStringExtra(INTENT_KEY_TABLE_NAME) + "239HD");

        return entriesList;
    }

    private String getMacAddress() {
        return "placeholder";
    }
}