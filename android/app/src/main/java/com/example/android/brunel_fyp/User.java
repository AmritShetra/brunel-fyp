package com.example.android.brunel_fyp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;

class User {

    // Check if SharedPreferences has details saved
    static boolean loggedInCheck(Context context){
        SharedPreferences user = context.getSharedPreferences("User", 0);
        return user.contains("username");
    }

    // Add details to SharedPreferences
    static void setDetails(Context context, String username, String password) {
        SharedPreferences user = context.getSharedPreferences("User", 0);
        SharedPreferences.Editor editor = user.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    // Returns the saved username
    static String getUsername(FragmentActivity activity) {
        SharedPreferences user = activity.getSharedPreferences("User", 0);
        return user.getString("username","");
    }

    // Returns the saved password
    static String getPassword(FragmentActivity activity) {
        SharedPreferences user = activity.getSharedPreferences("User", 0);
        return user.getString("password", "");
    }

    // Logout and remove details from SharedPreferences
    static void clear(FragmentActivity activity) {
        SharedPreferences user = activity.getSharedPreferences("User", 0);
        SharedPreferences.Editor editor = user.edit();
        editor.clear();
        editor.apply();
    }
}
