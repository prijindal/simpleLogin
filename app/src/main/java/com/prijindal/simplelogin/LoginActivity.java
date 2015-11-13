package com.prijindal.simplelogin;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // TODO: If Token present navigate to MainActivity
        // Else: Clear History and make this the home page
    }

    public void loginClick(View view) {
        // TODO: Login User and go to MainActivity
    }

    public void registerClick(View view) {
        // TODO: Go To Register Activity
    }
}
