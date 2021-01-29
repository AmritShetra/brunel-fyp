package com.example.android.brunel_fyp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ProfileFragment extends Fragment {

    ImageView logout, edit;
    TextView usernameText, firstNameText, lastNameText, emailText, passwordText;
    Switch passwordSwitch;
    ProgressBar progressBar;
    AsyncHttpClient client = new AsyncHttpClient();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentHolder = inflater.inflate(R.layout.fragment_profile, container, false);

        logout = parentHolder.findViewById(R.id.logOut);
        edit = parentHolder.findViewById(R.id.editProfile);

        usernameText = parentHolder.findViewById(R.id.username);
        firstNameText = parentHolder.findViewById(R.id.firstName);
        lastNameText = parentHolder.findViewById(R.id.lastName);
        emailText = parentHolder.findViewById(R.id.email);
        passwordText = parentHolder.findViewById(R.id.password);

        passwordSwitch = parentHolder.findViewById(R.id.passwordSwitch);

        progressBar = parentHolder.findViewById(R.id.progressBar);

        // Getting the username and password that was used to log in with
        String username = User.getUsername(getActivity());
        String password = User.getPassword(getActivity());

        getProfileData(username, password);

        // Clear the stored details and go back to the Title screen
        logout.setOnClickListener(view -> {
            User.clear(getActivity());

            Intent intent = new Intent(getContext(), TitleActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        // TODO: Edit Profile screen
        edit.setOnClickListener(view -> System.out.println("--- Edit ---"));

        // Switch between the clear-text password and asterisks version of it
        passwordSwitch.setOnClickListener(view -> {
            if (password == null) {
                return;
            }

            if (passwordSwitch.isChecked()) {
                passwordText.setText(password);
            }
            else {
                passwordText.setText(hidePassword(password));
            }
        });

        return parentHolder;
    }

    private void getProfileData(String username, String password) {
        String url = Server.profileRoute();

        client.setBasicAuth(username, password);
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onStart(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                View content = getActivity().findViewById(android.R.id.content);
                Snackbar.make(content, responseString, Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    usernameText.setText(username);
                    firstNameText.setText(response.getString("first_name"));
                    lastNameText.setText(response.getString("last_name"));
                    emailText.setText(response.getString("email"));
                    passwordText.setText(hidePassword(password));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // Convert the password to asterisks
    private String hidePassword(String password) {
        StringBuilder asterisks = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            asterisks.append("*");
        }
        return asterisks.toString();
    }

}
