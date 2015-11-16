package com.prijindal.simplelogin;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends ListActivity {
    // TODO: Refactor Code to reuse some components
    Context self = this;
    String TAG = "Main Activity";
    JSONArray mUsersJson;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        mUsersJson = null;
        if(isNetworkAvailable()) {
            // TODO: Show some Kind of info showing that person is logging in
            if (User.getInstance().isLoggedIn(this)) {
                mProgressBar.setVisibility(View.VISIBLE);
                new GetUsersTask().execute();
            } else {
                goToLogin();
            }
        }
        else {
            Toast.makeText(this, "You are not connected to the internet", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        if(User.getInstance().isTokenAvailable(this)) {
            super.onResume();
        }
        else {
            super.onDestroy();
            goToLogin();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    protected void goToLogin() {
        Intent loginActivity = new Intent(this, LoginActivity.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(loginActivity);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void updateList() {
        mProgressBar.setVisibility(View.INVISIBLE);
        if(mUsersJson == null) {
            AlertDialog.Builder errorAlert = new AlertDialog.Builder(this);
            errorAlert.setTitle("ERROR");
            errorAlert.setMessage("There was an error in the request");
            errorAlert.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = errorAlert.create();
            dialog.show();

            TextView textView = (TextView) getListView().getEmptyView();
            textView.setText("No data to display");
        }
        else {
            try {
                Log.d(TAG, mUsersJson.toString(2));
                ArrayList<HashMap<String, String>> users = new ArrayList<>();
                for(int i = 0;i< mUsersJson.length();++i) {
                    JSONObject user = mUsersJson.getJSONObject(i);
                    String username = user.getString("username");
                    String email = user.getString("email");
                    HashMap<String, String> userHash = new HashMap<>();
                    userHash.put("username", username);
                    userHash.put("email", email);
                    users.add(userHash);
                }
                String[] keys = {"username", "email"};
                int[] ids = {android.R.id.text1, android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this, users, android.R.layout.simple_list_item_2, keys, ids);
                setListAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected class GetUsersTask extends AsyncTask<Object, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Object... objects) {
            JSONArray jsonUsers = null;
            try {
                URL usersUrl = new URL(getString(R.string.base_url) + "/users?token=" + User.getInstance().getToken(self));
                HttpURLConnection usersConnection = (HttpURLConnection) usersUrl.openConnection();
                usersConnection.connect();

                int responseCode = usersConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream usersStream = usersConnection.getInputStream();
                    Reader reader = new InputStreamReader(usersStream);
                    char[] charArray = new char[usersConnection.getContentLength()];
                    reader.read(charArray);

                    String usersResponse = new String(charArray);
                    jsonUsers = new JSONArray(usersResponse);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Exception Caught: ", e);
            } catch (IOException e) {
                Log.e(TAG, "Exception Caught: ", e);
            } catch (JSONException e) {
                Log.e(TAG, "Exception Caught: ", e);
            }
            return jsonUsers;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            mUsersJson = jsonArray;
            updateList();
        }
    }
}
