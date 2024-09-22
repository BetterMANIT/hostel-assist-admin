package com.manit.hostel.assist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    @NonNull ActivityHomeBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
    }
}