package com.manit.hostel.assist;

import static com.manit.hostel.assist.adapters.EntriesAdapter.ALL_FILTER;
import static com.manit.hostel.assist.adapters.EntriesAdapter.ENTERED_FILTER;
import static com.manit.hostel.assist.adapters.EntriesAdapter.EXIT_ONLY_FILTER;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.manit.hostel.assist.adapters.EntriesAdapter;
import com.manit.hostel.assist.data.AppPref;
import com.manit.hostel.assist.data.Entries;
import com.manit.hostel.assist.database.MariaDBConnection;
import com.manit.hostel.assist.databinding.ActivityViewEntriesBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ViewEntryActivity extends AppCompatActivity {
    ActivityViewEntriesBinding lb;

    private EntriesAdapter entAdapter;
    private int currentFilter = ALL_FILTER;
    private boolean searchEnable = false;
    private TextView mStudentsBackInHostel, mStudentsOutOfHostel;

    private MariaDBConnection mMariaDBConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityViewEntriesBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        mStudentsOutOfHostel = findViewById(R.id.students_out_of_hostel_textview);
        mStudentsBackInHostel = findViewById(R.id.students_back_in_hostel);

        this.mMariaDBConnection = new MariaDBConnection(this);


        if(getIntent().hasExtra(INTENT_KEY_HOSTEL_NAME))
            lb.hostelName.setText(getString(R.string.hostel_name) + " : " + getIntent().getStringExtra(INTENT_KEY_HOSTEL_NAME)); // Set the hostel name
        if(getIntent().hasExtra(INTENT_KEY_DATE))
         lb.date.setText(getIntent().getStringExtra(INTENT_KEY_DATE));  // Set the date
        lb.studentsListRecyclerview.post(() -> lb.studentsListRecyclerview.setLayoutManager(new LinearLayoutManager(this)));
        addClickLogicToFilters();
        addClickLogic();
        addExtendedFloatingActionButton();
        fetchAllEntriesFromDBAndUpdateRecyclerView();
        Log.d(ViewEntryActivity.class.getSimpleName(), "table name : " + getTableName());

    }


    public static String INTENT_KEY_DATE = "date",
            INTENT_KEY_HOSTEL_NAME = "hostel_name",
            INTENT_TABLE_NAME = "table_name",
            INTENT_PURPOSE = "purpose";

    @Nullable
    private String getPurpose(){
         if (getIntent().hasExtra(INTENT_PURPOSE)) {
             Log.d(ViewEntryActivity.class.getSimpleName(), "purpose : " + getIntent().getStringExtra(INTENT_PURPOSE));

             return getIntent().getStringExtra(INTENT_PURPOSE);
         }
         Log.d(ViewEntryActivity.class.getSimpleName(), "Purpose is null");
         return null;
    }
    @NonNull
    private String getTableName(){
        if(true)
           return  getIntent().getStringExtra(INTENT_TABLE_NAME);
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


    private void addExtendedFloatingActionButton() {
        final ExtendedFloatingActionButton mExtendedFloatingActionButton = findViewById(R.id.extended_fab);
        mExtendedFloatingActionButton.setOnClickListener(v -> showAddEntriesDialog());
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
        final TextView mPurposeTextView = dialog.findViewById(R.id.purpose);
        final TextView mEntryAlreadyExists = dialog.findViewById(R.id.entry_already_exists);
        final TextView mEntryOpenTimeTextView = dialog.findViewById(R.id.entry_open_time);
        final Button mCreateEntryButtonByScholarNo = dialog.findViewById(R.id.create_entry_button);
        final Button mCloseEntryButtonByScholarNo = dialog.findViewById(R.id.close_entry_button);
        final AutoCompleteTextView mCategorySelectionAutoCompleteTextView = dialog.findViewById(R.id.spinner_to_category_selection);
        mCategorySelectionAutoCompleteTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCreateEntryButtonByScholarNo.setAlpha(1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final ImageView mStudentImageview = dialog.findViewById(R.id.student_imageview);


        final boolean[] isCategoryDetailsLoaded = {false};

        AtomicReference<String> table_name = new AtomicReference<>("");

        // Set a click listener on the button
        buttonProceed.setOnClickListener(view -> {
            final String scholarNumber = editTextScholarNumber.getText().toString();
            mMariaDBConnection.get_student_info(new MariaDBConnection.Callback() {
                @Override
                public void onResponse(String result) {
                    try{
                        final JSONObject mJSONObject = new JSONObject(result);
                        final JSONObject student_info = mJSONObject.getJSONObject("data");
                        Log.d(ViewEntryActivity.class.getSimpleName(), "student_info : " + student_info.toString());
                        mMariaDBConnection.get_category_details_by_hostel_name(new MariaDBConnection.Callback() {
                            @Override
                            public void onResponse(String result) {
                                try {

                                    final JSONObject mJSONObject = new JSONObject(result);
                                    final JSONArray category_name_with_table_name = mJSONObject.getJSONArray("data");

                                    final List<String> mTableNameList = new ArrayList<>();
                                    final List<String> mPurposeList = new ArrayList<>();

                                    for (int i = 0; i < category_name_with_table_name.length(); i++) {
                                        final JSONObject specific_table_info = category_name_with_table_name.getJSONObject(i);
                                        String table_name = specific_table_info.getString("table_name");
                                        String purpose = specific_table_info.getString("purpose");
                                        mTableNameList.add(table_name);
                                        mPurposeList.add(purpose);
                                        Log.d(ViewEntryActivity.class.getSimpleName(), "Table name : " + table_name + "\nPurpose");
                                    }
                                    mCategorySelectionAutoCompleteTextView.setAdapter(new ArrayAdapter<>(
                                            ViewEntryActivity.this,
                                            android.R.layout.simple_dropdown_item_1line,
                                            mPurposeList // Convert Collection to String[]
                                    ));
                                    mCategorySelectionAutoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
                                        table_name.set(mTableNameList.get(position));
                                        mCreateEntryButtonByScholarNo.setAlpha(1f);});
                                    mCategorySelectionAutoCompleteTextView.showDropDown();
                                    isCategoryDetailsLoaded[0] = true;

                                }catch (Exception e){
                                    onErrorResponse(e.getMessage());
                                }
                            }

                            @Override
                            public void onErrorResponse(String error) {
                                Toast.makeText(ViewEntryActivity.this, "Error : " + error,Toast.LENGTH_LONG).show();

                            }
                        }, student_info.getString("hostel_name"));
                        mScholarNumberTextView.setText(getString(R.string.scholar_no) + " : " + scholarNumber);
                        mPhoneNumberTextView.setText(getString(R.string.phone_no) + " : " + String.valueOf(student_info.getInt("phone_no")));
                        mStudentNameTextView.setText(getString(R.string.name) + " : " + student_info.getString("name"));
                        mRoomNoTextView.setText(getString(R.string.room_no) + " : " + student_info.getInt("room_no"));
                        mHostelNameTextView.setText(getString(R.string.hostel_name) + " : " + student_info.getString("hostel_name"));
                        mPurposeTextView.setText(getString(R.string.purpose) + " : " + student_info.getString("purpose"));
                        if(!student_info.isNull("entry_exit_table_name")){
                            Log.d(ViewEntryActivity.class.getSimpleName(),"entry exit table is not null");
                            mCreateEntryButtonByScholarNo.setVisibility(View.GONE);
                            mEntryAlreadyExists.setVisibility(View.VISIBLE);
                            mCloseEntryButtonByScholarNo.setVisibility(View.VISIBLE);
                            mEntryOpenTimeTextView.setVisibility(View.VISIBLE);
                            mEntryOpenTimeTextView.setText(student_info.getString("entry_exit_table_name"));
                            mPurposeTextView.setVisibility(View.VISIBLE);
                            dialog.findViewById(R.id.spinner_purpose_selection_text_input_layout).setVisibility(View.GONE);
                        }else {
                            Log.d(ViewEntryActivity.class.getSimpleName(),"entry exit table is null");
                            mCreateEntryButtonByScholarNo.setVisibility(View.VISIBLE);
                            mCloseEntryButtonByScholarNo.setVisibility(View.GONE);
                            mEntryAlreadyExists.setVisibility(View.GONE);
                            mPurposeTextView.setVisibility(View.GONE);
                            dialog.findViewById(R.id.spinner_purpose_selection_text_input_layout).setVisibility(View.VISIBLE);

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
                        if(!isCategoryDetailsLoaded[0]){
                            Toast.makeText(ViewEntryActivity.this, "Please wait for the purpose details to load", Toast.LENGTH_LONG).show();
                            return;
                        }
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

                           mMariaDBConnection.add_entry_student(scholar_no, name, room_no, photo_url, phone_no, section, hostel_name, mCategorySelectionAutoCompleteTextView.getText().toString(), table_name.get(),AppPref.getUsername(ViewEntryActivity.this),new MariaDBConnection.Callback() {
                                @Override
                                public void onResponse(String result) {
                                    fetchAllEntriesFromDBAndUpdateRecyclerView();
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
                    mCloseEntryButtonByScholarNo.setOnClickListener(v ->{ mMariaDBConnection.close_entry_student(scholarNumber, new MariaDBConnection.Callback() {
                        @Override
                        public void onResponse(String result) {
                            fetchAllEntriesFromDBAndUpdateRecyclerView();
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
//        dialog.setOnShowListener(dialog1 -> {
//        });


        // Show the dialog
        dialog.show();
        editTextScholarNumber.requestFocus();
        openKeyboard(editTextScholarNumber);

    }

    private void openKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
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





    private void check_for_updates_in_db_and_update_recyclerview_accordingly(){
        Toast.makeText(this, "check_for_updates_in_db_and_update_recyclerview_accordingly", Toast.LENGTH_LONG).show();
        mMariaDBConnection.check_if_new_update_in_table(new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                try {
                    final JSONObject responseObject = new JSONObject(result);
                    // Extract the 'status' value
                    String status = responseObject.getString("status");

                    AppPref.setLastTimeTableFetchedUNIXTimestamp(ViewEntryActivity.this, getTableName(), responseObject.getLong("last_update") );

                    // Extract the 'hasUpdates' value
                    boolean hasUpdates = responseObject.getBoolean("hasUpdates");

                    // Check if the status is 'success' and hasUpdates is true
                    if ("success".equals(status) && hasUpdates) {
                        fetchAllEntriesFromDBAndUpdateRecyclerView();
                        System.out.println("Status is success, and there are updates.");
                    } else {
                        mPostDelayedHandler.removeCallbacks(mCheckForUpdatesRunnableInDBAndUpdateAccordinglyRunnable);
                        mPostDelayedHandler.postDelayed(mCheckForUpdatesRunnableInDBAndUpdateAccordinglyRunnable, 10000);

                        System.out.println("No updates available or status is not suc");
                    }

//                    Log.d(ViewEntryActivity.class.getSimpleName(), "Table updated : " +);
                }catch (Exception e){}
            }

            @Override
            public void onErrorResponse(String error) {

            }
        }, getTableName(), getPurpose());

    }

   private final Handler mPostDelayedHandler = new Handler();
    private final Runnable mCheckForUpdatesRunnableInDBAndUpdateAccordinglyRunnable = () -> check_for_updates_in_db_and_update_recyclerview_accordingly();


    private void fetchAllEntriesFromDBAndUpdateRecyclerView() {

        mMariaDBConnection.fetchEntryExitList(new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                Log.d(ViewEntryActivity.class.getSimpleName(), result.toString());
                Log.e("list", result);

                final ArrayList<Entries> entriesList = new ArrayList<>();
                final JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                final JsonArray dataArray = jsonObject.getAsJsonArray("data");

                for (int i = dataArray.size()-1; i > 0 ; i--) {
                    JsonObject entryObject = dataArray.get(i).getAsJsonObject();

                    String entryNo = entryObject.get("id").getAsString();
                    String name = entryObject.get("name").getAsString();
                    String roomNo = entryObject.get("room_no").getAsString();
                    String scholarNo = entryObject.get("scholar_no").getAsString();
                    String exitTime = entryObject.get("open_time").getAsString();
                    String entryTime = "";
                    if(!entryObject.get("close_time").isJsonNull())
                        entryTime = entryObject.get("close_time").getAsString();
                    String photoURL = entryObject.get("photo_url").getAsString();

                    entriesList.add(new Entries(entryNo, name, roomNo, scholarNo, exitTime, entryTime, photoURL));
                }

                mPostDelayedHandler.removeCallbacks(mCheckForUpdatesRunnableInDBAndUpdateAccordinglyRunnable);
                mPostDelayedHandler.postDelayed(mCheckForUpdatesRunnableInDBAndUpdateAccordinglyRunnable, 10000);
                updateRecyclerViewList(entriesList);

            }

            @Override
            public void onErrorResponse(String error) {
                Toast.makeText(ViewEntryActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                Log.d(ViewEntryActivity.class.getSimpleName(), error.toString());

            }
        }, getTableName(), getPurpose());

    }

    void updateRecyclerViewList(ArrayList<Entries> entriesList){
        if (entAdapter == null) {
            entAdapter = new EntriesAdapter(entriesList);
            lb.studentsListRecyclerview.setAdapter(entAdapter);
        } else {
            entAdapter.updateEntries(entriesList);
        }
        entAdapter.filterEntries(currentFilter);
        Integer[] countOfStudentsInHostel = getCountOfStudentsInBacKHostel(entriesList);
        if(mStudentsBackInHostel !=null)
            mStudentsBackInHostel.setText(getString(R.string.students_back_in_hostel) + " : " + countOfStudentsInHostel[0]);
        if(mStudentsOutOfHostel!=null)
            mStudentsOutOfHostel.setText(getString(R.string.students_out_of_hostel) + " : " + countOfStudentsInHostel[1]);

    }

    private Integer[] getCountOfStudentsInBacKHostel(ArrayList<Entries> entriesList){
        if(entAdapter==null)
            return new Integer[]{-1,-1};

        Set<String> scholarNoSetBackInHostel = new HashSet<>();
        Set<String> scholarNoSetOutsideOfHostel = new HashSet<>();
        entriesList.forEach(entries -> {
            if(entries.isBackInHostel() && !scholarNoSetOutsideOfHostel.contains(entries.getEntryNo())) {
                scholarNoSetBackInHostel.add(entries.getScholarNo());
            }else if(!scholarNoSetBackInHostel.contains(entries.getEntryNo())){
                scholarNoSetOutsideOfHostel.add(entries.getScholarNo());
            }

        });

        return new Integer[]{scholarNoSetBackInHostel.size(), scholarNoSetOutsideOfHostel.size()};
    }


}