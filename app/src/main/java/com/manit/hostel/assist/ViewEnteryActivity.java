package com.manit.hostel.assist;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.manit.hostel.assist.adapters.EntriesAdapter;
import com.manit.hostel.assist.data.Entries;
import com.manit.hostel.assist.databinding.ActivityViewEntriesBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ViewEnteryActivity extends AppCompatActivity {
    ActivityViewEntriesBinding lb;
    int ALL_FILTER = 1;
    int EXIT_ONLY_FILTER = 2;
    int ENTERED_FILTER = 3;
    private EntriesAdapter entAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityViewEntriesBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        lb.hostelName.setText(getHostelName(getMacAddress())); // Set the hostel name
        lb.date.setText(getDateFormated());  // Set the date

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
        ArrayList<Entries> entriesList = fetchEntries(getMacAddress(),new Date());
        lb.status.setText("Exit : "+entriesList.size()+"       Entered : 50");
        if (entAdapter == null) {
            entAdapter = new EntriesAdapter(entriesList);
            lb.studentsListRecyclerview.setAdapter(entAdapter);
        } else {
            entAdapter.updateEntries(entriesList);
        }

    }

    private ArrayList<Entries> fetchEntries(String macAddress, Date date) {
        ArrayList<Entries> entriesList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            entriesList.add(new Entries("Salmon Khan"));
        }
        return entriesList;
    }

    private String getMacAddress() {
        return "placeholder";
    }
}