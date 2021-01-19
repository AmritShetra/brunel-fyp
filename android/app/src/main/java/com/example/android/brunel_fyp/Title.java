package com.example.android.brunel_fyp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class Title extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        Button logIn = findViewById(R.id.logIn);
        Button signUp = findViewById(R.id.signUp);

        logIn.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), Login.class);
            startActivity(intent);
            finish();
        });
        signUp.setOnClickListener(view -> System.out.println("signUp"));
    }
}
