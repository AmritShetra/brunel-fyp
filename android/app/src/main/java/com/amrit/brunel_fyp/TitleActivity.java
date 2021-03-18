package com.amrit.brunel_fyp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class TitleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        // If the user is logged in, skip this activity completely
        if (User.hasToken(this)) {
            Intent intent = new Intent(this, MainActivity.class);
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
