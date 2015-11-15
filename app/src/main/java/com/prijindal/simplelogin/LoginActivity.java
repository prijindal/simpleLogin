package com.prijindal.simplelogin;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
        // TODO: Go To Register Activity
    }


    protected class loginTask extends AsyncTask<HashMap<String, String>, Object, Integer> {
        @Override
        protected Integer doInBackground(HashMap<String, String>... credentialsArray) {
            Integer responseCode = -1;
            try {
                URL loginUrl = new URL(getString(R.string.base_url) + "/users");
                HttpURLConnection loginConnection = (HttpURLConnection) loginUrl.openConnection();

                loginConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                loginConnection.setRequestMethod("POST");
                loginConnection.setRequestProperty("Content-Type", "application/json");
                loginConnection.setUseCaches(false);
                loginConnection.setDoInput(true);
                loginConnection.setDoOutput(true);
                loginConnection.connect();

                HashMap<String, String> credentials = credentialsArray[0];

                JSONObject requestJson = new JSONObject();
                requestJson.put("username", credentials.get("username"));
                requestJson.put("password", credentials.get("password"));

                DataOutputStream printout = new DataOutputStream(loginConnection.getOutputStream());
                printout.writeBytes(requestJson.toString());
                printout.flush();
                printout.close();

                responseCode = loginConnection.getResponseCode();
                Log.v(TAG, "Response Code: " + responseCode);
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream usersStream = loginConnection.getInputStream();
                    Reader reader = new InputStreamReader(usersStream);
                    char[] charArray = new char[loginConnection.getContentLength()];
                    reader.read(charArray);

                    String usersResponse = new String(charArray);
                    Log.v(TAG, usersResponse);
                    JSONObject jsonObject = new JSONObject(usersResponse);
                    String tok = jsonObject.getString("token");
                    User.getInstance().setToken(tok);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Exception Caught: ", e);
            } catch (IOException e) {
                Log.e(TAG, "Exception Caught: ", e);
            } catch (JSONException e) {
                Log.e(TAG, "Exception Caught: ", e);
            }
            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer s) {
            onLoginComplete(s);
        }
    }
}
