package com.manit.hostel.assist;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.manit.hostel.assist.database.MariaDBConnection;
import com.manit.hostel.assist.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding lb;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private MariaDBConnection dbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(36000).build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        fetchRemoteConfig();
        /*  requestPermissions();
        Log.d(MainActivity.class.getSimpleName(), "Wifi Name list : " + new WifiScanner(this).getWifiList(this).toString());
        if (isLocationEnabled()) {
            // Start Wi-Fi scanning
            showWifiCheckDialog();
        } else {
            promptEnableLocation();
        }
        setupOneSignal();*/
    }

    private void fetchRemoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                String BASE_URL = mFirebaseRemoteConfig.getString("BASE_URL");
                Log.d(MainActivity.class.getSimpleName(), "BASE_URL: " + BASE_URL);
                dbConnection = new MariaDBConnection(this);
                lb.getRoot().post(() -> {
                    if (!isInternetAvailable(this)) {
                        showNoInternetDialog(this);
                    } else {
                        lb.getRoot().postDelayed(() -> {
                            Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(in);
                            finish();
                        }, 2000);
                    }
                });
            } else {
                showNoInternetDialog(this);
            }
        });
    }

    public boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void showNoInternetDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cant reach the server").setMessage("Please check your internet connection or try again later").setCancelable(false).setNegativeButton("Exit", (dialog, which) -> {
            finish();
        }).setPositiveButton("try again", (dialog, which) -> {
            dialog.dismiss();
            if (!isInternetAvailable(this)) {
                showNoInternetDialog(this);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}