package com.manit.hostel.assist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.manit.hostel.assist.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        lb.getRoot().postDelayed(()->{
            Intent in = new Intent(getApplicationContext(), CategoryCreationActivity.class);
            startActivity(in);
        },2000);
    }
}