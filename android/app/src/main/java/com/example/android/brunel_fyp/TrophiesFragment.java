package com.example.android.brunel_fyp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class TrophiesFragment extends Fragment {

    // https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
    // https://www.youtube.com/watch?v=Vyqz_-sJGFk

    private ArrayList<Boolean> statuses = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> labels = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentHolder = inflater.inflate(R.layout.fragment_trophies, container, false);

        // Data to populate the RecyclerView with
        statuses.add(Boolean.TRUE);
        names.add("Trophy One");
        labels.add("You signed up for the app! Well done.");

        statuses.add(Boolean.FALSE);
        names.add("Trophy Two");
        labels.add("Unlock this by using the app for an entire week.");

        // Set up the RecyclerView
        RecyclerView recyclerView = parentHolder.findViewById(R.id.recyclerView);
        TrophiesAdapter trophiesAdapter = new TrophiesAdapter(statuses, names, labels);
        recyclerView.setAdapter(trophiesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return parentHolder;
    }
}
