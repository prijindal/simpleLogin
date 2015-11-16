package com.prijindal.simplelogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

/**
 * Created by Priyanshu on 11/14/15.
 */
public class User {
    private static String PREFS_NAME = "User";
    private static String TAG = "UserSingleton";
    private static User ourInstance = null;

    private int id;
    private String token;

    public static User getInstance() {
        if(ourInstance == null) {
            ourInstance = new User();
        }
        return ourInstance;
    }

    private User() {
        token = null;
        id = -1;
    }

    public boolean isTokenAvailable(Context activity) {
        return getToken(activity)!=null;
    }

    public boolean isLoggedIn(Context activity) {
        return isTokenAvailable(activity);
    }

    public void setToken(Context activity, String newToken) {
        SharedPreferences settings = activity.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token", newToken);
        editor.apply();
        token = newToken;
    }

    public String getToken(Context activity) {
        SharedPreferences settings = activity.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("token", token);
    }

    public static Integer register(Context Activity, String username, String email, String password) {
        Integer responseCode = -1;
        try {
            URL loginUrl = new URL(Activity.getString(R.string.base_url) + "/users");
            HttpURLConnection loginConnection = (HttpURLConnection) loginUrl.openConnection();

            loginConnection.setRequestProperty("X-HTTP-Method-Override", "POST");
            loginConnection.setRequestMethod("POST");
            loginConnection.setRequestProperty("Content-Type", "application/json");
            loginConnection.setUseCaches(false);
            loginConnection.setDoInput(true);
            loginConnection.setDoOutput(true);
            loginConnection.connect();

            JSONObject requestJson = new JSONObject();
            requestJson.put("username", username);
            requestJson.put("email", email);
            requestJson.put("password", password);

            DataOutputStream printout = new DataOutputStream(loginConnection.getOutputStream());
            printout.writeBytes(requestJson.toString());
            printout.flush();
            printout.close();

            responseCode = loginConnection.getResponseCode();
            Log.v(TAG, "Response Code: " + responseCode);
            if(responseCode == HttpURLConnection.HTTP_OK) {
                responseCode = User.login(Activity, username, password);
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

    // Accepts username and password
    // Returns response Code for the request
    public static Integer login(Context Activity, String username, String password) {
        Integer responseCode = -1;
        try {
            URL loginUrl = new URL(Activity.getString(R.string.base_url) + "/users");
            HttpURLConnection loginConnection = (HttpURLConnection) loginUrl.openConnection();

            loginConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            loginConnection.setRequestMethod("POST");
            loginConnection.setRequestProperty("Content-Type", "application/json");
            loginConnection.setUseCaches(false);
            loginConnection.setDoInput(true);
            loginConnection.setDoOutput(true);
            loginConnection.connect();

            JSONObject requestJson = new JSONObject();
            requestJson.put("username", username);
            requestJson.put("password", password);

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
                User.getInstance().setToken(Activity, tok);
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
}
