package com.manit.hostel.assist;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    @NonNull ActivityLoginBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lb = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        lb.loginButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            finish();
        });
    }
}