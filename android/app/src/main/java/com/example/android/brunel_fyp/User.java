package com.example.android.brunel_fyp;

import android.content.Context;
import android.content.SharedPreferences;

class User {

    // https://stackoverflow.com/questions/29047777/android-using-shared-preferences-in-separate-class
    // SharedPreferences requires a context
    // In Activity, pass "this" or "getApplicationContext()" as a parameter
    // In Fragment, pass "getActivity" as a parameter

    static boolean loggedInCheck(Context context){
        SharedPreferences user = context.getSharedPreferences("User", 0);
        return user.contains("username");
    }

    static void setDetails(Context context, String username, String password) {
        SharedPreferences user = context.getSharedPreferences("User", 0);
        SharedPreferences.Editor editor = user.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    static String getUsername(Context context) {
        SharedPreferences user = context.getSharedPreferences("User", 0);
        return user.getString("username","");
    }

    static String getPassword(Context context) {
        SharedPreferences user = context.getSharedPreferences("User", 0);
        return user.getString("password", "");
    }

    // Logout and remove details from SharedPreferences
    static void clear(Context context) {
        SharedPreferences user = context.getSharedPreferences("User", 0);
        SharedPreferences.Editor editor = user.edit();
        editor.clear();
        editor.apply();
    }
}
