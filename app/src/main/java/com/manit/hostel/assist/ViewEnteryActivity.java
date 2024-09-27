package com.manit.hostel.assist;

import static com.manit.hostel.assist.adapters.EntriesAdapter.ALL_FILTER;
import static com.manit.hostel.assist.adapters.EntriesAdapter.ENTERED_FILTER;
import static com.manit.hostel.assist.adapters.EntriesAdapter.EXIT_ONLY_FILTER;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.VolleyError;
import com.google.android.material.button.MaterialButton;
import com.manit.hostel.assist.adapters.EntriesAdapter;
import com.manit.hostel.assist.data.Entries;
import com.manit.hostel.assist.database.MariaDBConnection;
import com.manit.hostel.assist.databinding.ActivityViewEntriesBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ViewEnteryActivity extends AppCompatActivity {
    ActivityViewEntriesBinding lb;

    private EntriesAdapter entAdapter;
    private int currentFilter = ALL_FILTER;
    private boolean searchEnable = false;

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
                if (!txt.isEmpty() && !txt.isBlank() && entAdapter != null) {
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
                Log.e("list", result);
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        return entriesList;
    }

    private String getMacAddress() {
        return "placeholder";
    }
}