package com.manit.hostel.assist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.manit.hostel.assist.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    @NonNull ActivityMainBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
    }
}