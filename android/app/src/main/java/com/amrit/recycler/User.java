package com.amrit.recycler;

import android.content.Context;
import android.content.SharedPreferences;

class User {

    // SharedPreferences requires a context
    // In Activity, you can pass "getApplicationContext()"
    // In Fragment, you can pass "getActivity()" or "getContext()"

    static void storeToken(Context context, String token) {
        SharedPreferences sharedPref = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("access_token", token);
        editor.apply();
    }

    static String retrieveToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        return sharedPref.getString("access_token", "");
    }

    static void clearToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    static boolean hasToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        return sharedPref.contains("access_token");
    }
}
