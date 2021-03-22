package com.amrit.recycler;


import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

public class TrophiesFragment extends Fragment {

    private ArrayList<Boolean> statuses = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>(
            Arrays.asList(
                    "Hello, World!",
                    "1 Month"
            )
    );
    private ArrayList<String> labels = new ArrayList<>(
            Arrays.asList(
                    "Uploaded a photo on the chatbot for the first time.",
                    "Accessed the app once a day for an entire month."
            )
    );

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

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
        AsyncHttpClient client = new AsyncHttpClient();
        String token = User.retrieveToken(getContext());
        client.addHeader("Authorization", "Bearer " + token);

        String url = Server.trophiesRoute();
        client.get(url, new JsonHttpResponseHandler(){
            @Override
            public void onStart(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                View thisView = getActivity().findViewById(android.R.id.content);
                Snackbar.make(thisView, R.string.try_again_api, Snackbar.LENGTH_LONG).show();
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
