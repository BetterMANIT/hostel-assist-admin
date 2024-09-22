package com.manit.hostel.assist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    @NonNull ActivityLoginBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
    }
}