package com.amrit.recycler;

import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Load profile data passed from previous Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("sign_up")) {
            Snackbar.make(findViewById(android.R.id.content), "Sign Up successful", Snackbar.LENGTH_LONG).show();
        }

        EditText userField = findViewById(R.id.username);
        EditText passwordField = findViewById(R.id.password);

        Button loginButton = findViewById(R.id.logIn);
        loginButton.setOnClickListener(view -> {
            try {
                String username = userField.getText().toString();
                String password = passwordField.getText().toString();
                login(username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void login(String username, String password) throws UnsupportedEncodingException, JSONException {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        if (username.isEmpty() || password.isEmpty()) {
            String text = "Field(s) blank, please try again.";
            Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show();
            return;
        }

        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);
        StringEntity entity = new StringEntity(json.toString());

        AsyncHttpClient client = new AsyncHttpClient();
        String url = Server.loginRoute();
        client.post(getApplicationContext(), url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onStart(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                try {
                    String responseString = response.getString("message");
                    Snackbar.make(findViewById(android.R.id.content), responseString, Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // Save token to device's shared preferences
                try {
                    User.storeToken(getApplicationContext(), response.getString("access_token"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Take the user to the main screen (which contains the fragments)
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
