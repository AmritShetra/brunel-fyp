package com.example.android.brunel_fyp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Profile extends Fragment {

    TextView text;
    Button button;
    ProgressBar progressBar;
    AsyncHttpClient client = new AsyncHttpClient();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View parentHolder = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inflate the layout for this fragment
        text = parentHolder.findViewById(R.id.text);
        button = parentHolder.findViewById(R.id.button);
        progressBar = parentHolder.findViewById(R.id.progressBar);

        // Displaying the stored username once logged in
        SharedPreferences user = getActivity().getSharedPreferences("User", 0);
        String username = user.getString("username","");
        button.setText(username);

        button.setOnClickListener(view -> getMessage());

        return parentHolder;
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
