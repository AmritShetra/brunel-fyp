package com.example.android.brunel_fyp;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
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

public class SignUpActivity extends AppCompatActivity {

    ImageView userWarning, passwordWarning, emailWarning, firstNameWarning, lastNameWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText userField = findViewById(R.id.username);
        EditText passwordField = findViewById(R.id.password);
        EditText emailField = findViewById(R.id.email);
        EditText firstNameField = findViewById(R.id.firstName);
        EditText lastNameField = findViewById(R.id.lastName);

        userWarning = findViewById(R.id.userWarning);
        passwordWarning = findViewById(R.id.passwordWarning);
        emailWarning = findViewById(R.id.emailWarning);
        firstNameWarning = findViewById(R.id.firstNameWarning);
        lastNameWarning = findViewById(R.id.lastNameWarning);

        Button signUpButton = findViewById(R.id.signUp);
        signUpButton.setOnClickListener(view -> {
            HashMap<String, String> details = new HashMap<>();
            details.put("username", userField.getText().toString());
            details.put("password", passwordField.getText().toString());
            details.put("email", emailField.getText().toString());
            details.put("first_name", firstNameField.getText().toString());
            details.put("last_name", lastNameField.getText().toString());

            if (validateInputs(details)) {
                try {
                    signUp(details);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public boolean validateInputs(HashMap<String, String> details) {
        // Make sure the warning icons are invisible before checking the new inputs
        userWarning.setVisibility(View.INVISIBLE);
        passwordWarning.setVisibility(View.INVISIBLE);
        emailWarning.setVisibility(View.INVISIBLE);
        firstNameWarning.setVisibility(View.INVISIBLE);
        lastNameWarning.setVisibility(View.INVISIBLE);

        boolean valid = true;
        String text = "";

        String username = details.get("username");
        if (!username.matches("[A-Za-z0-9_]+")) {
            text += "Username must be alphanumeric characters only. \n";
            userWarning.setVisibility(View.VISIBLE);
            valid = false;
        }

        String password = details.get("password");
        if (password.length() < 6) {
            text += "Password must be greater than 6 characters. \n";
            passwordWarning.setVisibility(View.VISIBLE);
            valid = false;
        }

        String email = details.get("email");
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            text += "Email is not valid. \n";
            emailWarning.setVisibility(View.VISIBLE);
            valid = false;
        }

        String firstName = details.get("first_name");
        if (firstName.isEmpty()) {
            text += "First name is empty. \n";
            firstNameWarning.setVisibility(View.VISIBLE);
            valid = false;
        }

        String lastName = details.get("last_name");
        if (lastName.isEmpty()) {
            text += "Last name is empty.";
            lastNameWarning.setVisibility(View.VISIBLE);
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

    private void signUp(HashMap<String, String> details) throws UnsupportedEncodingException {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        JSONObject json = new JSONObject(details);
        StringEntity entity = new StringEntity(json.toString());

        AsyncHttpClient client = new AsyncHttpClient();
        String url = Server.registerRoute();
        client.post(getApplicationContext(), url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                try {
                    if (response.has("username")) {
                        userWarning.setVisibility(View.VISIBLE);
                    }
                    if (response.has("email")) {
                        emailWarning.setVisibility(View.VISIBLE);
                    }
                    View thisView = findViewById(android.R.id.content);
                    Snackbar.make(thisView, R.string.sign_up_error, Snackbar.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
