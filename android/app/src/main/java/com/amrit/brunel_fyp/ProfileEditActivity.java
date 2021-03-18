package com.amrit.brunel_fyp;

import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ProfileEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        EditText usernameField = findViewById(R.id.username);
        EditText firstNameField = findViewById(R.id.firstName);
        EditText lastNameField = findViewById(R.id.lastName);
        EditText emailField = findViewById(R.id.email);

        // Load profile data passed from previous Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usernameField.setText(extras.getString("username"));
            emailField.setText(extras.getString("email"));
            firstNameField.setText(extras.getString("first_name"));
            lastNameField.setText(extras.getString("last_name"));
        }

        ImageView cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(view -> finish());

        ImageView saveChanges = findViewById(R.id.saveChanges);
        saveChanges.setOnClickListener(view -> {
            HashMap<String, String> details = new HashMap<>();
            details.put("username", usernameField.getText().toString());
            details.put("email", emailField.getText().toString());
            details.put("first_name", firstNameField.getText().toString());
            details.put("last_name", lastNameField.getText().toString());

            if (validateInputs(details)) {
                try {
                    updateProfile(details);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean validateInputs(HashMap<String, String> details) {
        boolean valid = true;
        String text = "";

        String username = details.get("username");
        if (!username.matches("[A-Za-z0-9_]+")) {
            text += "Username must be alphanumeric characters only. \n";
            valid = false;
        }

        String email = details.get("email");
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            text += "Email is not valid. \n";
            valid = false;
        }

        String firstName = details.get("first_name");
        if (firstName.isEmpty()) {
            text += "First name is empty. \n";
            valid = false;
        }

        String lastName = details.get("last_name");
        if (lastName.isEmpty()) {
            text += "Last name is empty.";
            valid = false;
        }

        if (valid) {
            return true;
        }
        else {
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG);
            // Snackbar doesn't show all of the error text if it's > 2 lines, so we make it
            View snackbarView = snackbar.getView();
            TextView snackbarText = snackbarView.findViewById(R.id.snackbar_text);
            snackbarText.setMaxLines(5);
            snackbar.show();

            return false;
        }
    }

    private void updateProfile(HashMap<String, String> details) throws UnsupportedEncodingException {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        JSONObject json = new JSONObject(details);
        StringEntity entity = new StringEntity(json.toString());

        AsyncHttpClient client = new AsyncHttpClient();
        String token = User.retrieveToken(getApplicationContext());
        client.addHeader("Authorization", "Bearer " + token);

        String url = Server.profileEditRoute();
        client.put(getApplicationContext(), url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                try {
                    String responseString = response.getString("message");
                    View thisView = findViewById(android.R.id.content);
                    Snackbar.make(thisView, responseString, Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // Go back to MainActivity (as Profile is the default fragment) and reload profile data
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
