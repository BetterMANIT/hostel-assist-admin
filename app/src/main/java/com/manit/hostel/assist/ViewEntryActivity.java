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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.manit.hostel.assist.adapters.EntriesAdapter;
import com.manit.hostel.assist.data.Entries;
import com.manit.hostel.assist.database.MariaDBConnection;
import com.manit.hostel.assist.databinding.ActivityViewEntriesBinding;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewEntryActivity extends AppCompatActivity {
    ActivityViewEntriesBinding lb;

    private EntriesAdapter entAdapter;
    private int currentFilter = ALL_FILTER;
    private boolean searchEnable = false;
    private TextView mStudentsBackInHostel, mStudentsOutOfHostel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityViewEntriesBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        mStudentsOutOfHostel = findViewById(R.id.students_out_of_hostel_textview);
        mStudentsBackInHostel = findViewById(R.id.students_back_in_hostel);


        if(getIntent().hasExtra(INTENT_KEY_HOSTEL_NAME))
            lb.hostelName.setText(getIntent().getStringExtra(INTENT_KEY_HOSTEL_NAME)); // Set the hostel name
        if(getIntent().hasExtra(INTENT_KEY_DATE))
         lb.date.setText(getIntent().getStringExtra(INTENT_KEY_DATE));  // Set the date
        lb.studentsListRecyclerview.post(() -> lb.studentsListRecyclerview.setLayoutManager(new LinearLayoutManager(this)));
        addClickLogicToFilters();
        addClickLogic();
        addFloatingActionButton();
        fetchAllEntriesFromDBAndUpdateRecyclerView();
        Log.d(ViewEntryActivity.class.getSimpleName(), "table name : " + getTableName());

    }


    public static String INTENT_KEY_DATE = "date";
    public static String INTENT_KEY_HOSTEL_NAME = "hostel_name";

    @NonNull
    private String getTableName(){
        String DATE_MMYYYY = new SimpleDateFormat("MMyyyy", Locale.getDefault()).format(new Date());
        final String originalDateString = getIntent().getStringExtra(INTENT_KEY_DATE);
        final SimpleDateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        final SimpleDateFormat targetFormat = new SimpleDateFormat("MMyyyy", Locale.getDefault());

        try {
            assert originalDateString != null;
            Date date = originalFormat.parse(originalDateString);
            assert date != null;
            DATE_MMYYYY = targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();  // Handle the parsing error if the date format is incorrect
        }

        return DATE_MMYYYY + getIntent().getStringExtra(INTENT_KEY_HOSTEL_NAME);
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
        final TextView mEntryOpenTimeTextView = dialog.findViewById(R.id.entry_open_time);
        final Button mCreateEntryButtonByScholarNo = dialog.findViewById(R.id.create_entry_button);
        final Button mCloseEntryButtonByScholarNo = dialog.findViewById(R.id.close_entry_button);

        final ImageView mStudentImageview = dialog.findViewById(R.id.student_imageview);



        // Set a click listener on the button
        buttonProceed.setOnClickListener(view -> {
            final String scholarNumber = editTextScholarNumber.getText().toString();
            new MariaDBConnection(ViewEntryActivity.this).get_student_info(new MariaDBConnection.Callback() {
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
                            Log.d(ViewEntryActivity.class.getSimpleName(),"entry exit table is not null");
                            mCreateEntryButtonByScholarNo.setVisibility(View.GONE);
                            mEntryAlreadyExists.setVisibility(View.VISIBLE);
                            mCloseEntryButtonByScholarNo.setVisibility(View.VISIBLE);
                            mEntryOpenTimeTextView.setVisibility(View.VISIBLE);
                            mEntryOpenTimeTextView.setText(student_info.getString("entry_exit_table_name"));
                        }else {
                            Log.d(ViewEntryActivity.class.getSimpleName(),"entry exit table is null");
                            mCreateEntryButtonByScholarNo.setVisibility(View.VISIBLE);
                            mCloseEntryButtonByScholarNo.setVisibility(View.GONE);
                            mEntryAlreadyExists.setVisibility(View.GONE);
                        }
                        Glide.with(ViewEntryActivity.this)
                                .load(student_info.getString("photo_url"))
                                .placeholder(R.drawable.demo_pic1)
                                .error(R.drawable.baseline_error_24)
                                .into(mStudentImageview);
                    }catch (Exception e){
                        Toast.makeText(ViewEntryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        onErrorResponse(e.getMessage());
                    }


                    Log.d(ViewEntryActivity.class.getSimpleName(), "result : " + result);
                    Toast.makeText(ViewEntryActivity.this, result, Toast.LENGTH_SHORT).show();
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

                            new MariaDBConnection(ViewEntryActivity.this).add_entry_student(scholar_no, name, room_no, photo_url, phone_no, section, hostel_name, new MariaDBConnection.Callback() {
                                @Override
                                public void onResponse(String result) {
                                    Toast.makeText(ViewEntryActivity.this, result, Toast.LENGTH_LONG).show();
                                }
                                @Override
                                public void onErrorResponse(String error) {
                                    Toast.makeText(ViewEntryActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            });

                        }catch (Exception e){
                            Toast.makeText(ViewEntryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        mCreateEntryButtonByScholarNo.setOnClickListener(v1 -> Toast.makeText(ViewEntryActivity.this, "In progress", Toast.LENGTH_LONG).show());
                        dialog.dismiss();
                   });
                    mCloseEntryButtonByScholarNo.setOnClickListener(v ->{ new MariaDBConnection(ViewEntryActivity.this).close_entry_student(scholarNumber, new MariaDBConnection.Callback() {
                        @Override
                        public void onResponse(String result) {
                            Toast.makeText(ViewEntryActivity.this, result, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onErrorResponse(String error) {
                            Toast.makeText(ViewEntryActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
                        dialog.dismiss();
                    });
                }

                @Override
                public void onErrorResponse(String error) {
                    Toast.makeText(ViewEntryActivity.this, error, Toast.LENGTH_SHORT).show();

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



//    @Override
//    protected void onResume() {
//        super.onResume();
//        updateRecyclerViewList(fetchEntries(new Date()));
////        ArrayList<Entries> entriesList = fetchEntries(new Date());
////        lb.status.setText("Exit : " + entriesList.size() + "       Entered : 50");
////        if (entAdapter == null) {
////            entAdapter = new EntriesAdapter(entriesList);
////            lb.studentsListRecyclerview.setAdapter(entAdapter);
////        } else {
////            entAdapter.updateEntries(entriesList);
////        }
////        entAdapter.filterEntries(currentFilter);
//
//    }


    private void fetchAllEntriesFromDBAndUpdateRecyclerView() {
//        ArrayList<Entries> entriesList = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            Entries entries = new Entries("Salmon Khan");
//            entries.setEntryNo("24092024C"+i);
//            entriesList.add(entries);
//        }


        MariaDBConnection dbConnection = new MariaDBConnection(this);
        dbConnection.fetchEntryExitList(new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                Log.d(ViewEntryActivity.class.getSimpleName(), result.toString());
                Log.e("list", result);

                final ArrayList<Entries> entriesList = new ArrayList<>();
                final JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                final JsonArray dataArray = jsonObject.getAsJsonArray("data");

                for (int i = 0; i < dataArray.size(); i++) {
                    JsonObject entryObject = dataArray.get(i).getAsJsonObject();

                    String entryNo = entryObject.get("id").getAsString();
                    String name = entryObject.get("name").getAsString();
                    String roomNo = entryObject.get("room_no").getAsString();
                    String scholarNo = entryObject.get("scholar_no").getAsString();
                    String exitTime = entryObject.get("open_time").getAsString();
                    String entryTime = null;
                    if(!entryObject.get("close_time").isJsonNull())
                        entryTime = entryObject.get("close_time").getAsString();
                    String photoURL = entryObject.get("photo_url").getAsString();

                    entriesList.add(new Entries(entryNo, name, roomNo, scholarNo, exitTime, entryTime, photoURL));
                }
                updateRecyclerViewList(entriesList);

            }

            @Override
            public void onErrorResponse(String error) {
                Toast.makeText(ViewEntryActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                Log.d(ViewEntryActivity.class.getSimpleName(), error.toString());

            }
        }, getTableName());

    }

    void updateRecyclerViewList(ArrayList<Entries> entriesList){
        if (entAdapter == null) {
            entAdapter = new EntriesAdapter(entriesList);
            lb.studentsListRecyclerview.setAdapter(entAdapter);
        } else {
            entAdapter.updateEntries(entriesList);
        }
        entAdapter.filterEntries(currentFilter);
        int countOfStudentsInHostel = getCountOfStudentsInBacKHostel(entriesList);
        if(mStudentsBackInHostel !=null)
            mStudentsBackInHostel.setText(getString(R.string.students_back_in_hostel) + " : " + countOfStudentsInHostel);
        if(mStudentsOutOfHostel!=null)
            mStudentsOutOfHostel.setText(getString(R.string.students_out_of_hostel) + " : " + (entriesList.size() - countOfStudentsInHostel));

    }

    private int getCountOfStudentsInBacKHostel(ArrayList<Entries> entriesList){
        if(entAdapter==null)
            return -1;

        final AtomicInteger studentsInHostel = new AtomicInteger();
        entriesList.forEach(entries -> {
            if(entries.isBackInHostel())
                studentsInHostel.getAndIncrement();
        });

        return studentsInHostel.get();
    }


}