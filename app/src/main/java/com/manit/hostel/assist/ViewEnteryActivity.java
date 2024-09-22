package com.manit.hostel.assist;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.databinding.ActivityMainBinding;

public class ViewEnteryActivity extends AppCompatActivity {
    ActivityMainBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        lb.getRoot().postDelayed(()->{
            Intent in = new Intent();
            in.setClass(getApplicationContext(),LoginActivity.class);
            startActivity(in);
        },2000);
    }
}