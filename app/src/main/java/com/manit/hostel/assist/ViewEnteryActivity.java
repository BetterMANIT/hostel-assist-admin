package com.manit.hostel.assist;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.databinding.ActivityViewEntriesBinding;

public class ViewEnteryActivity extends AppCompatActivity {
    ActivityViewEntriesBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityViewEntriesBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());

    }
}