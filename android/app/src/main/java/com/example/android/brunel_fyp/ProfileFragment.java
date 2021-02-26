package com.example.android.brunel_fyp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ProfileFragment extends Fragment {
    Context context;

    TextView usernameText, firstNameText, lastNameText, emailText, passwordText;
    Switch passwordSwitch;
    ProgressBar progressBar;
    Boolean canEdit = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentHolder = inflater.inflate(R.layout.fragment_profile, container, false);

        context = getContext();

        ImageView logout = parentHolder.findViewById(R.id.logOut);
        ImageView edit = parentHolder.findViewById(R.id.editProfile);

        usernameText = parentHolder.findViewById(R.id.username);
        passwordText = parentHolder.findViewById(R.id.password);
        emailText = parentHolder.findViewById(R.id.email);
        firstNameText = parentHolder.findViewById(R.id.firstName);
        lastNameText = parentHolder.findViewById(R.id.lastName);

        passwordText.setTransformationMethod(new PasswordTransformationMethod());

        passwordSwitch = parentHolder.findViewById(R.id.passwordSwitch);
        progressBar = parentHolder.findViewById(R.id.progressBar);

        loadProfileData();

        logout.setOnClickListener(view -> {
            User.clearToken(context);

            Intent intent = new Intent(getContext(), TitleActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        edit.setOnClickListener(view -> {
            // Only true once we have loaded profile data
            if (canEdit) {
                Intent intent = new Intent(getContext(), ProfileEditActivity.class);
                intent.putExtra("username", usernameText.getText().toString());
                intent.putExtra("password", passwordText.getText().toString());
                intent.putExtra("email", emailText.getText().toString());
                intent.putExtra("first_name", firstNameText.getText().toString());
                intent.putExtra("last_name", lastNameText.getText().toString());
                startActivity(intent);
            }
        });

        // Switch between the clear-text password and asterisks version of it
        passwordSwitch.setOnClickListener(view -> {
            if (passwordText.getText().toString().equals(""))
                return;

            if (passwordSwitch.isChecked())
                passwordText.setTransformationMethod(null);
            else
                passwordText.setTransformationMethod(new PasswordTransformationMethod());
        });

        return parentHolder;
    }

    private void loadProfileData() {
        AsyncHttpClient client = new AsyncHttpClient();
        String token = User.retrieveToken(context);
        client.addHeader("Authorization", "Bearer " + token);

        String url = Server.profileRoute();
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onStart(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                View thisView = getActivity().findViewById(android.R.id.content);
                Snackbar.make(thisView, R.string.try_again_api, Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    usernameText.setText(response.getString("username"));
                    passwordText.setText(response.getString("password"));
                    emailText.setText(response.getString("email"));
                    firstNameText.setText(response.getString("first_name"));
                    lastNameText.setText(response.getString("last_name"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                canEdit = true;
                passwordSwitch.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

}
