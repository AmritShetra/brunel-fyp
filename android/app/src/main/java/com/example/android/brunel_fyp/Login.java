package com.example.android.brunel_fyp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Login extends AppCompatActivity {

    EditText userField, passwordField;
    Button loginButton;
    ProgressBar progressBar;
    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.logIn);
        progressBar = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(view -> {
            try {
                login(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void login(View view) throws UnsupportedEncodingException, JSONException {
        String username = userField.getText().toString();
        String password = passwordField.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            String text = "Field(s) blank, please try again.";
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
            return;
        }

        String url = Server.loginRoute();
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);
        StringEntity entity = new StringEntity(json.toString());

        client.post(view.getContext(), url, entity, "application/json", new TextHttpResponseHandler() {
            @Override
            public void onStart(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Snackbar.make(view, responseString, Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                // Saving user details to the device
                SharedPreferences user = getSharedPreferences("User", 0);
                SharedPreferences.Editor editor = user.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.apply();

                // Take the user to the main screen (which contains the fragments)
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

}
