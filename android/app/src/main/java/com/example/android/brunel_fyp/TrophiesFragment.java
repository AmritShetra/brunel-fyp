package com.example.android.brunel_fyp;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TrophiesFragment extends Fragment {

    // https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
    // https://www.youtube.com/watch?v=Vyqz_-sJGFk

    private ArrayList<Boolean> statuses = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>(
            Arrays.asList("Trophy 1", "Trophy 2")
    );
    private ArrayList<String> labels = new ArrayList<>(
            Arrays.asList("Uploaded a photo on the chatbot for the first time.", "Accessed the app once a day for an entire month.")
    );

    RecyclerView recyclerView;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentHolder = inflater.inflate(R.layout.fragment_trophies, container, false);

        recyclerView = parentHolder.findViewById(R.id.recyclerView);
        progressBar = parentHolder.findViewById(R.id.progressBar);

        // Set up the RecyclerView
        TrophiesAdapter trophiesAdapter = new TrophiesAdapter(statuses, names, labels);
        recyclerView.setAdapter(trophiesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getTrophiesData();

        return parentHolder;
    }

    private void getTrophiesData() {
        Context context = getContext();
        String username = User.getUsername(context);
        String password = User.getPassword(context);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(username, password);

        String url = Server.trophiesRoute();

        client.get(url, new JsonHttpResponseHandler(){
            @Override
            public void onStart(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                if (response.has("error")) {
                    try {
                        String responseString = response.getString("error");
                        View thisView = getActivity().findViewById(android.R.id.content);
                        Snackbar.make(thisView, responseString, Snackbar.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressBar.setVisibility(View.INVISIBLE);

                // Iterate through the keys, get the values and add them to the ArrayList
                Iterator<String> iter = response.keys();

                while (iter.hasNext()) {
                    String key = iter.next();
                    Boolean value = null;
                    try {
                        value = (Boolean) response.get(key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    statuses.add(value);
                }

                updateRecyclerView();
            }
        });
    }

    private void updateRecyclerView() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}
