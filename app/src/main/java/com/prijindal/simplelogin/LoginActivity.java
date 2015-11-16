package com.prijindal.simplelogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.util.HashMap;


public class LoginActivity extends Activity {
    protected String TAG = "LoginActivity";
    protected ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mProgressBar = (ProgressBar) findViewById(R.id.loginProgress);
        mProgressBar.setVisibility(View.INVISIBLE);
        if(User.getInstance().isTokenAvailable()) {
            goToMain();
        }
    }

    public void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
    }

    public void onLoginComplete(Integer response) {
        mProgressBar.setVisibility(View.INVISIBLE);
        if(response == HttpURLConnection.HTTP_OK) {
            goToMain();
        }
        else {
            Toast.makeText(this,"Wrong Credentials! Please Try Again",Toast.LENGTH_LONG).show();
        }
    }

    public void loginClick(View view) {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null :
                getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        mProgressBar.setVisibility(View.VISIBLE);
        EditText username = (EditText) findViewById(R.id.username);
        EditText password = (EditText) findViewById(R.id.password);
        HashMap<String, String> loginCredentials = new HashMap<String, String>();
        loginCredentials.put("username",username.getText().toString());
        loginCredentials.put("password",password.getText().toString());
        new loginTask().execute(loginCredentials);
    }

    public void registerClick(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }


    protected class loginTask extends AsyncTask<HashMap<String, String>, Object, Integer> {
        @Override
        protected Integer doInBackground(HashMap<String, String>... credentialsArray) {
            HashMap<String, String> credentials = credentialsArray[0];
            return User.login(getString(R.string.base_url) + "/users", credentials.get("username"), credentials.get("password"));
        }

        @Override
        protected void onPostExecute(Integer s) {
            onLoginComplete(s);
        }
    }
}
