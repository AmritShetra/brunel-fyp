package com.example.android.brunel_fyp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class TrophiesAdapter extends RecyclerView.Adapter<TrophiesAdapter.ViewHolder> {

    private ArrayList<Boolean> trophyStatus;
    private ArrayList<String> trophyNames;
    private ArrayList<String> trophyLabels;

    public TrophiesAdapter(ArrayList<Boolean> trophyStatus, ArrayList<String> trophyNames, ArrayList<String> trophyLabels) {
        this.trophyStatus = trophyStatus;
        this.trophyNames = trophyNames;
        this.trophyLabels = trophyLabels;
    }

    // Inflates the row layout from XML when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.trophy, parent, false
        );
        return new ViewHolder(view);
    }

    // Binds data to the elements in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (trophyStatus.get(position)) {
            holder.image.setImageResource(R.drawable.trophy);
        }
        else {
            holder.image.setImageResource(R.drawable.locked_trophy);
        }

        holder.trophyName.setText(trophyNames.get(position));
        holder.trophyLabel.setText(trophyLabels.get(position));
    }

    // Return number of rows
    @Override
    public int getItemCount() {
        return trophyNames.size();
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView trophyName;
        TextView trophyLabel;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            trophyName = itemView.findViewById(R.id.trophyName);
            trophyLabel = itemView.findViewById(R.id.trophyLabel);
            parentLayout = itemView.findViewById(R.id.parentLayout);
        }
    }
}
