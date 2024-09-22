package com.manit.hostel.assist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.databinding.ActivityMainBinding;

public class LoginActivity extends AppCompatActivity {
    @NonNull ActivityMainBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
    }
}