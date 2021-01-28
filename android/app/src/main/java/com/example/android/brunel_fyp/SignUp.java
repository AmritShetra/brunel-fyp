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
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class SignUp extends AppCompatActivity {

    EditText userField, passwordField, emailField, firstNameField, lastNameField;
    ImageView userWarning, passwordWarning, emailWarning, firstNameWarning, lastNameWarning;
    Button signUpButton;
    ProgressBar progressBar;
    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        emailField = findViewById(R.id.email);
        firstNameField = findViewById(R.id.firstName);
        lastNameField = findViewById(R.id.lastName);
        signUpButton = findViewById(R.id.signUp);
        progressBar = findViewById(R.id.progressBar);

        userWarning = findViewById(R.id.userWarning);
        passwordWarning = findViewById(R.id.passwordWarning);
        emailWarning = findViewById(R.id.emailWarning);
        firstNameWarning = findViewById(R.id.firstNameWarning);
        lastNameWarning = findViewById(R.id.lastNameWarning);

        signUpButton.setOnClickListener(view -> {
            try {
                signUp(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void signUp(View view) throws JSONException, UnsupportedEncodingException {

        String username = userField.getText().toString();
        String password = passwordField.getText().toString();
        String email = emailField.getText().toString();
        String firstName = firstNameField.getText().toString();
        String lastName = lastNameField.getText().toString();

        // If the inputs are invalid, don't try an API call
        if (!validateInputs(view)) {
            return;
        }

        String url = Server.registerRoute();
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);
        json.put("email", email);
        json.put("first_name", firstName);
        json.put("last_name", lastName);
        StringEntity entity = new StringEntity(json.toString());

        client.post(view.getContext(), url, entity, "application/json", new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (responseString.contains("Username")) {
                    userWarning.setVisibility(View.VISIBLE);
                }

                if (responseString.contains("Email")) {
                    emailWarning.setVisibility(View.VISIBLE);
                }

                Snackbar.make(view, responseString, Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Intent intent = new Intent(view.getContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private boolean validateInputs(View view) {
        String username = userField.getText().toString();
        String password = passwordField.getText().toString();
        String email = emailField.getText().toString();
        String firstName = firstNameField.getText().toString();
        String lastName = lastNameField.getText().toString();

        // Make sure the warning icons are invisible before checking the new inputs
        userWarning.setVisibility(View.INVISIBLE);
        passwordWarning.setVisibility(View.INVISIBLE);
        emailWarning.setVisibility(View.INVISIBLE);
        firstNameWarning.setVisibility(View.INVISIBLE);
        lastNameWarning.setVisibility(View.INVISIBLE);

        boolean valid = true;
        String text = "";

        if (!username.matches("[A-za-z0-9]+")) {
            text += "Username must be alphanumeric characters only. \n";
            userWarning.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (password.length() < 6) {
            text += "Password must be greater than 6 characters. \n";
            passwordWarning.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            text += "Email is not valid. \n";
            emailWarning.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (firstName.isEmpty()) {
            text += "First name is empty. \n";
            firstNameWarning.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (lastName.isEmpty()) {
            text += "Last name is empty.";
            lastNameWarning.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (valid) {
            return true;
        }
        else {
            Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);

            // Snackbar doesn't show all of the error text if it's > 2 lines, so we make it
            View snackbarView = snackbar.getView();
            TextView snackbarText = snackbarView.findViewById(R.id.snackbar_text);
            snackbarText.setMaxLines(5);
            snackbar.show();
            return false;
        }
    }
}
