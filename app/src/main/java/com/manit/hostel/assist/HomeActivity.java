package com.manit.hostel.assist;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.databinding.ActivityHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {
    @NonNull ActivityHomeBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        addClickLogic();
        lb.viewEntries.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ViewEnteryActivity.class)));
        lb.date.setText(getDateFormated());
    }

    private void addClickLogic() {
        lb.backbtn.setOnClickListener(v -> finish());
    }

    private String getDateFormated() {
        //get todays date in dd-mm-yyyy format
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }
}