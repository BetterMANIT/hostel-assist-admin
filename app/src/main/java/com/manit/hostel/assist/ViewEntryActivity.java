package com.manit.hostel.assist;

import static com.manit.hostel.assist.adapters.EntriesAdapter.ALL_FILTER;
import static com.manit.hostel.assist.adapters.EntriesAdapter.ENTERED_FILTER;
import static com.manit.hostel.assist.adapters.EntriesAdapter.EXIT_ONLY_FILTER;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ViewEntryActivity extends AppCompatActivity {
    public static final int UPDATE_MILLISEC = 10000;
    ActivityViewEntriesBinding lb;

    private EntriesAdapter entAdapter;
    private int currentFilter = ALL_FILTER;
    private boolean searchEnable = false;
    private TextView mStudentsBackInHostel, mStudentsOutOfHostel;
    private String dateSelected;
    private MariaDBConnection mMariaDBConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityViewEntriesBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        mStudentsOutOfHostel = findViewById(R.id.students_out_of_hostel_textview);
        mStudentsBackInHostel = findViewById(R.id.students_back_in_hostel);

        this.mMariaDBConnection = new MariaDBConnection(this);
        Glide.get(this).clearMemory(); // Clears memory cache (Must run on UI thread)

        new Thread(() -> {
            Glide.get(ViewEntryActivity.this).clearDiskCache(); // Clears disk cache (Must run on background thread)
        }).start();

        if (getIntent().hasExtra(INTENT_KEY_HOSTEL_NAME))
            lb.hostelName.setText("Hostel : " + getIntent().getStringExtra(INTENT_KEY_HOSTEL_NAME)); // Set the hostel name
        if (getIntent().hasExtra(INTENT_PURPOSE))
            lb.purpose.setText(getIntent().getStringExtra(INTENT_PURPOSE));
        if (getIntent().hasExtra(INTENT_KEY_DATE))
            dateSelected = (getIntent().getStringExtra(INTENT_KEY_DATE));
        lb.studentsListRecyclerview.post(() -> lb.studentsListRecyclerview.setLayoutManager(new LinearLayoutManager(this)));
        addClickLogicToFilters();
        addClickLogic();
        addExtendedFloatingActionButton();
        fetchAllEntriesFromDBAndUpdateRecyclerView();
        Log.d(ViewEntryActivity.class.getSimpleName(), "table name : " + getTableName());

    }

    public static String INTENT_KEY_DATE = "date", INTENT_KEY_HOSTEL_NAME = "hostel_name", INTENT_TABLE_NAME = "table_name", INTENT_PURPOSE = "purpose";

    @Nullable
    private String getPurpose() {
        if (getIntent().hasExtra(INTENT_PURPOSE)) {
            Log.d(ViewEntryActivity.class.getSimpleName(), "purpose : " + getIntent().getStringExtra(INTENT_PURPOSE));

            return getIntent().getStringExtra(INTENT_PURPOSE);
        }
        Log.d(ViewEntryActivity.class.getSimpleName(), "Purpose is null");
        return null;
    }

    @NonNull
    private String getTableName() {
        return getIntent().getStringExtra(INTENT_TABLE_NAME);
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
                    try {
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
                                    mCategorySelectionAutoCompleteTextView.setAdapter(new ArrayAdapter<>(ViewEntryActivity.this, android.R.layout.simple_dropdown_item_1line, mPurposeList // Convert Collection to String[]
                                    ));
                                    mCategorySelectionAutoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
                                        table_name.set(mTableNameList.get(position));
                                        mCreateEntryButtonByScholarNo.setAlpha(1f);
                                    });
                                    mCategorySelectionAutoCompleteTextView.showDropDown();
                                    isCategoryDetailsLoaded[0] = true;

                                } catch (Exception e) {
                                    onErrorResponse(e.getMessage());
                                }
                            }

                            @Override
                            public void onErrorResponse(String error) {
                                Toast.makeText(ViewEntryActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();

                            }
                        }, student_info.getString("hostel_name"));
                        mScholarNumberTextView.setText(getString(R.string.scholar_no) + " : " + scholarNumber);
                        mPhoneNumberTextView.setText(getString(R.string.phone_no) + " : " + String.valueOf(student_info.getInt("phone_no")));
                        mStudentNameTextView.setText(getString(R.string.name) + " : " + student_info.getString("name"));
                        mRoomNoTextView.setText(getString(R.string.room_no) + " : " + student_info.getString("room_no"));
                        mHostelNameTextView.setText(getString(R.string.hostel_name) + " : " + student_info.getString("hostel_name"));
                        mPurposeTextView.setText(getString(R.string.purpose) + " : " + student_info.getString("purpose"));
                        if (!student_info.isNull("entry_exit_table_name")) {
                            Log.d(ViewEntryActivity.class.getSimpleName(), "entry exit table is not null");
                            mCreateEntryButtonByScholarNo.setVisibility(View.GONE);
                            mEntryAlreadyExists.setVisibility(View.VISIBLE);
                            mCloseEntryButtonByScholarNo.setVisibility(View.VISIBLE);
                            mEntryOpenTimeTextView.setVisibility(View.VISIBLE);
                            mEntryOpenTimeTextView.setText(student_info.getString("entry_exit_table_name"));
                            mPurposeTextView.setVisibility(View.VISIBLE);
                            dialog.findViewById(R.id.spinner_purpose_selection_text_input_layout).setVisibility(View.GONE);
                        } else {
                            Log.d(ViewEntryActivity.class.getSimpleName(), "entry exit table is null");
                            mCreateEntryButtonByScholarNo.setVisibility(View.VISIBLE);
                            mCloseEntryButtonByScholarNo.setVisibility(View.GONE);
                            mEntryAlreadyExists.setVisibility(View.GONE);
                            mPurposeTextView.setVisibility(View.GONE);
                            dialog.findViewById(R.id.spinner_purpose_selection_text_input_layout).setVisibility(View.VISIBLE);

                        }
                        Glide.with(ViewEntryActivity.this).load(student_info.getString("photo_url")).placeholder(R.drawable.demo_pic1).into(mStudentImageview);
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                        onErrorResponse(e.getMessage());
                    }


                    Log.d(ViewEntryActivity.class.getSimpleName(), "result : " + result);
                    Toast.makeText(ViewEntryActivity.this, result, Toast.LENGTH_SHORT).show();
                    mEnterScholarNoLinearLayout.setVisibility(View.GONE);


                    mShowDetailsByScholarNoLinearLayout.setVisibility(View.VISIBLE);
                    mCreateEntryButtonByScholarNo.setOnClickListener(v -> {
                        if (!isCategoryDetailsLoaded[0]) {
                            Toast.makeText(ViewEntryActivity.this, "Please wait for the purpose details to load", Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            final JSONObject mJSONObject = new JSONObject(result);
                            final JSONObject student_info = mJSONObject.getJSONObject("data");
                            final String name = student_info.getString("name");
                            final long scholar_no = Long.parseLong(scholarNumber);
                            final String phone_no = student_info.getString("phone_no");
                            final String room_no = student_info.getString("room_no");
                            final String photo_url = student_info.getString("photo_url");
                            final String section = student_info.getString("section");
                            final String hostel_name = student_info.getString("hostel_name");

                            mMariaDBConnection.add_entry_student(scholar_no, name, room_no, photo_url, phone_no, section, hostel_name, mCategorySelectionAutoCompleteTextView.getText().toString(), table_name.get(), AppPref.getUsername(ViewEntryActivity.this), new MariaDBConnection.Callback() {
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

                        } catch (Exception e) {
                            Toast.makeText(ViewEntryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        mCreateEntryButtonByScholarNo.setOnClickListener(v1 -> Toast.makeText(ViewEntryActivity.this, "In progress", Toast.LENGTH_LONG).show());
                        dialog.dismiss();
                    });
                    mCloseEntryButtonByScholarNo.setOnClickListener(v -> {
                        mMariaDBConnection.close_entry_student(scholarNumber, new MariaDBConnection.Callback() {
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
        lb.filterChipGroup.setSelectionRequired(true);
        lb.filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                blinkAnim(lb.studentsListRecyclerview);
                if (checkedId == R.id.all_chip) {
                    if (entAdapter != null) {
                        currentFilter = ALL_FILTER;
                        entAdapter.filterEntries(ALL_FILTER);
                    }
                } else if (checkedId == R.id.exit_only_chip) {
                    if (entAdapter != null) {
                        currentFilter = EXIT_ONLY_FILTER;
                        entAdapter.filterEntries(EXIT_ONLY_FILTER);
                    }
                } else if (checkedId == R.id.entered_chip) {
                    if (entAdapter != null) {
                        currentFilter = ENTERED_FILTER;
                        entAdapter.filterEntries(ENTERED_FILTER);
                    }
                }
            }
        });
    }

    private void checkForUpdate() {
        blinkAnim(lb.status);
        lb.status.setText("syncing...");
        mMariaDBConnection.check_if_new_update_in_table(new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                try {
                    final JSONObject responseObject = new JSONObject(result);
                    // Extract the 'status' value
                    String status = responseObject.getString("status");

                    AppPref.setLastTimeTableFetchedUNIXTimestamp(ViewEntryActivity.this, getTableName(), responseObject.getLong("last_update"));

                    // Extract the 'hasUpdates' value
                    boolean hasUpdates = responseObject.getBoolean("hasUpdates");

                    // Check if the status is 'success' and hasUpdates is true
                    if ("success".equals(status) && hasUpdates) {
                        fetchAllEntriesFromDBAndUpdateRecyclerView();
                        System.out.println("Status is success, and there are updates.");
                    } else {
                        mPostDelayedHandler.removeCallbacks(updateListRunnable);
                        mPostDelayedHandler.postDelayed(updateListRunnable, UPDATE_MILLISEC);

                        System.out.println("No updates available or status is not suc");
                    }
                    runOnUiThread(() -> {
                        blinkAnim(lb.status);
                        lb.status.setText("Entries");
                    });
                    //Log.d(ViewEntryActivity.class.getSimpleName(), "Table updated : " +);
                } catch (Exception e) {
                }
            }

            @Override
            public void onErrorResponse(String error) {

            }
        }, getTableName(), getPurpose());

    }

    private void blinkAnim(View view) {
        view.animate().alpha(0).setDuration(200).start();
        view.animate().alpha(1).setDuration(200).start();
    }

    private final Handler mPostDelayedHandler = new Handler();
    private final Runnable updateListRunnable = () -> checkForUpdate();

    private void fetchAllEntriesFromDBAndUpdateRecyclerView() {
        mMariaDBConnection.fetchEntryExitList(new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                Log.d(ViewEntryActivity.class.getSimpleName(), result);
                Log.e("list", result);

                final ArrayList<Entries> entriesList = new ArrayList<>();
                final JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();

                // Extract Statistics
                final JsonObject statistics = jsonObject.getAsJsonObject("statistics");
                int totalEntries = statistics.get("total_entries").getAsInt();
                int studentsBack = statistics.get("came_back_students").getAsInt();
                int studentsOut = statistics.get("out_students").getAsInt();

                // Extract Entries Data
                final JsonArray dataArray = jsonObject.getAsJsonArray("data");
                for (int i = dataArray.size() - 1; i >= 0; i--) {
                    JsonObject entryObject = dataArray.get(i).getAsJsonObject();

                    String entryNo = entryObject.get("id").getAsString();
                    String name = entryObject.get("name").getAsString();
                    String roomNo = entryObject.get("room_no").getAsString();
                    String scholarNo = entryObject.get("scholar_no").getAsString();
                    String exitTime = entryObject.get("open_time").getAsString();
                    String entryTime = entryObject.has("close_time") && !entryObject.get("close_time").isJsonNull()
                            ? entryObject.get("close_time").getAsString()
                            : "Not Returned";
                    String photoURL = mMariaDBConnection.getPhotoUrl(scholarNo);
                    String purpose = entryObject.get("purpose").getAsString();

                    entriesList.add(new Entries(entryNo, name, roomNo, scholarNo,purpose, exitTime, entryTime, photoURL));
                }

                // Update RecyclerView
                mPostDelayedHandler.removeCallbacks(updateListRunnable);
                mPostDelayedHandler.postDelayed(updateListRunnable, 10000);
                updateRecyclerViewList(entriesList);
                enableXcelButton(entriesList);

                // Display Statistics
                if (mStudentsBackInHostel != null)
                    mStudentsBackInHostel.setText(getString(R.string.students_back_in_hostel) + " : " + studentsBack);
                if (mStudentsOutOfHostel != null)
                    mStudentsOutOfHostel.setText(getString(R.string.students_out_of_hostel) + " : " + studentsOut);
            }

            @Override
            public void onErrorResponse(String error) {
                Toast.makeText(ViewEntryActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                Log.d(ViewEntryActivity.class.getSimpleName(), error);
            }
        }, getTableName(), getPurpose(), dateSelected);
    }

    private void enableXcelButton(ArrayList<Entries> entriesList) {
        lb.export.setOnClickListener(v -> {
            Uri fileUri = saveListAsExcel(ViewEntryActivity.this, entriesList);
            if (fileUri != null) {
                openExcelFile(ViewEntryActivity.this, fileUri);
            }
        });
    }

    private void openExcelFile(Context context, Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No app found to open Excel file!", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri saveListAsExcel(Context context, ArrayList<Entries> entriesList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Student Entries");

        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 9000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 5000);
        sheet.setColumnWidth(4, 6000);
        sheet.setColumnWidth(5, 9000);
        sheet.setColumnWidth(6, 9000);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Room No", "Scholar No", "Purpose", "Open Time", "Close Time"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }


        // Fill Data
        for (int i = 0; i < entriesList.size(); i++) {
            Entries entry = entriesList.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(entry.getEntryNo());
            row.createCell(1).setCellValue(entry.getName());
            row.createCell(2).setCellValue(entry.getRoomNo());
            row.createCell(3).setCellValue(entry.getScholarNo());
            row.createCell(4).setCellValue(entry.getPurpose());
            row.createCell(5).setCellValue(entry.getExitTime());
            row.createCell(6).setCellValue(entry.getEntryTime());
        }

        // Save File in Downloads Folder
        try {
            OutputStream outputStream;
            Uri fileUri = null;
            String fileName = dateSelected+"_entries_"+getTableName()+".xlsx";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                // Save inside "Downloads/YourAppFolder/"
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/HostelAssist");

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                outputStream = context.getContentResolver().openOutputStream(uri);
                fileUri = uri;
            } else {
                // Save inside "Downloads/YourAppFolder/" for older versions
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "HostelAssist");
                if (!directory.exists()) {
                    directory.mkdirs(); // Create folder if it doesn't exist
                }

                File file = new File(directory, fileName);
                outputStream = new FileOutputStream(file);
                fileUri = Uri.fromFile(file);
            }
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();

            Toast.makeText(context, "Excel saved in Downloads folder!", Toast.LENGTH_SHORT).show();
            return fileUri;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save Excel!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    void updateRecyclerViewList(ArrayList<Entries> entriesList) {
        if (entAdapter == null) {
            entAdapter = new EntriesAdapter(entriesList);
            lb.studentsListRecyclerview.setAdapter(entAdapter);
        } else {
            entAdapter.updateEntries(entriesList);
        }
        entAdapter.filterEntries(currentFilter);
    }



}