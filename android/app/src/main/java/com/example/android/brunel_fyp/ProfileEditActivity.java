package com.example.android.brunel_fyp;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ProfileEditActivity extends AppCompatActivity {

    EditText usernameField, passwordField, firstNameField, lastNameField, emailField;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        firstNameField = findViewById(R.id.firstName);
        lastNameField = findViewById(R.id.lastName);
        emailField = findViewById(R.id.email);

        progressBar = findViewById(R.id.progressBar);

        // Load profile data that was retrieved from the API in the previous screen
        setUpProfile();

        ImageView cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(view -> finish());

        ImageView saveChanges = findViewById(R.id.saveChanges);
        saveChanges.setOnClickListener(view -> {
            if (validateInputs()) {
                try {
                    updateProfile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Show the password if it's checked. Otherwise, hide it
        Switch passwordSwitch = findViewById(R.id.passwordSwitch);
        passwordSwitch.setOnClickListener(view -> {
            // https://stackoverflow.com/questions/3685790/how-to-switch-between-hide-and-view-password
            if (passwordSwitch.isChecked())
                passwordField.setTransformationMethod(null);
            else
                passwordField.setTransformationMethod(new PasswordTransformationMethod());
        });
    }

    private void setUpProfile() {
        usernameField.setText(User.getUsername(getApplicationContext()));
        passwordField.setText(User.getPassword(getApplicationContext()));

        // Data was passed when making a new Intent, as we left the Profile screen
        Bundle extras = getIntent().getExtras();
        firstNameField.setText(extras.getString("first_name"));
        lastNameField.setText(extras.getString("last_name"));
        emailField.setText(extras.getString("email"));
    }

    private boolean validateInputs() {
        boolean valid = true;
        String text = "";

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String firstName = firstNameField.getText().toString();
        String lastName = lastNameField.getText().toString();
        String email = emailField.getText().toString();

        if (!username.matches("[A-za-z0-9]+")) {
            text += "Username must be alphanumeric characters only. \n";
            valid = false;
        }

        if (password.length() < 6) {
            text += "Password must be greater than 6 characters. \n";
            valid = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            text += "Email is not valid. \n";
            valid = false;
        }

        if (firstName.isEmpty()) {
            text += "First name is empty. \n";
            valid = false;
        }

        if (lastName.isEmpty()) {
            text += "Last name is empty.";
            valid = false;
        }

        if (valid) {
            return true;
        }
        else {
            Snackbar snackbar = Snackbar.make(
                    findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG
            );

            // Snackbar doesn't show all of the error text if it's > 2 lines, so we make it
            View snackbarView = snackbar.getView();
            TextView snackbarText = snackbarView.findViewById(R.id.snackbar_text);
            snackbarText.setMaxLines(5);
            snackbar.show();
            return false;
        }
    }

    private void updateProfile() throws JSONException, UnsupportedEncodingException {
        String url = Server.profileEditRoute();
        JSONObject json = new JSONObject();

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String firstName = firstNameField.getText().toString();
        String lastName = lastNameField.getText().toString();
        String email = emailField.getText().toString();

        json.put("username", username);
        json.put("password", password);
        json.put("email", email);
        json.put("first_name", firstName);
        json.put("last_name", lastName);
        StringEntity entity = new StringEntity(json.toString());

        String storedUsername = User.getUsername(getApplicationContext());
        String storedPassword = User.getPassword(getApplicationContext());
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(storedUsername, storedPassword);
        client.put(getApplicationContext(), url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                if (response.has("error")) {
                    try {
                        String responseString = response.getString("error");
                        View thisView = findViewById(android.R.id.content);
                        Snackbar.make(thisView, responseString, Snackbar.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // Update the User object
                User.setDetails(
                        getApplicationContext(),
                        usernameField.getText().toString(),
                        passwordField.getText().toString()
                );

                // Go back to MainActivity (as Profile is the default fragment) and reload profile data
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
