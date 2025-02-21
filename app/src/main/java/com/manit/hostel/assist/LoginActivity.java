package com.manit.hostel.assist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.manit.hostel.assist.data.AppPref;
import com.manit.hostel.assist.databinding.ActivityLoginBinding;
import com.manit.hostel.assist.helper.Utils;

public class LoginActivity extends AppCompatActivity {
    @NonNull
    ActivityLoginBinding lb;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());

        mAuth = FirebaseAuth.getInstance();

        lb.loginButton.setOnClickListener(v -> {
            String email = lb.username.getText().toString();
            String password = lb.password.getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            email = email + "@hostel.assist";
            if (isValidEmail(email)) {
                loginUser(email, password);
            } else {
                lb.username.setError("Please enter a valid username");
            }
        });
    }

    private void loginUser(String email, String password) {
        Utils.disableBtn(lb.loginButton);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(LoginActivity.this, "User login failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                savePref(user);
                openHomeActivity();
            } else {
                Utils.enableBtn(lb.loginButton);
                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePref(FirebaseUser user) {
        AppPref.setAuthToken(this, user.getUid());
        AppPref.setUsername(this, lb.username.getText().toString().trim());
        AppPref.setDeviceId(this, "1234");
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private void openHomeActivity() {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        finish();
    }
}
