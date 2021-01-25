package com.example.android.brunel_fyp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    // TODO: Eventually remove this, it's basically just for testing the API

    TextView text;
    Button button;
    ProgressBar progressBar;
    AsyncHttpClient client = new AsyncHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
        button = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);

        // Displaying the stored username once logged in
        SharedPreferences user = getSharedPreferences("User", 0);
        String username = user.getString("username","");
        button.setText(username);

        button.setOnClickListener(view -> getMessage());
    }

    private void getMessage() {
        client.get(Server.route("/"), null, new JsonHttpResponseHandler(){
            @Override
            public void onStart(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String message = response.getString("text");
                    text.setText(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
