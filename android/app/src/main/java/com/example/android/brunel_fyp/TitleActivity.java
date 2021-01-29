package com.example.android.brunel_fyp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class TitleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        // If the user is logged in, skip this activity completely
        SharedPreferences user = getSharedPreferences("User", 0);
        if (user.contains("username")) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        Button logIn = findViewById(R.id.logIn);
        Button signUp = findViewById(R.id.signUp);

        logIn.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), LoginActivity.class);
            startActivity(intent);
        });
        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), SignUpActivity.class);
            startActivity(intent);
        });
    }
}
