package com.prijindal.simplelogin;
/**
 * Created by Priyanshu on 11/14/15.
 */
public class User {
    private String TAG = "UserSingleton";
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

    public boolean isTokenAvailable() {
        return getToken()!=null;
    }

    public boolean isLoggedIn() {
        return isTokenAvailable();
    }

    public void setToken(String newToken) {
        // TODO: Save to app data
        token = newToken;
    }

    public String getToken() {

        // TODO: Fetch from data
        return token;
    }
}
