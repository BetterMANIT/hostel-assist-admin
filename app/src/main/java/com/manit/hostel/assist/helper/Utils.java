package com.manit.hostel.assist.helper;

import com.google.android.material.button.MaterialButton;

public class Utils {
    public static void disableBtn(MaterialButton loginButton) {
        loginButton.setAlpha(0.7f);
        loginButton.setEnabled(false);
    }
    public static void enableBtn(MaterialButton loginButton) {
        loginButton.setAlpha(1f);
        loginButton.setEnabled(true);
    }
}
