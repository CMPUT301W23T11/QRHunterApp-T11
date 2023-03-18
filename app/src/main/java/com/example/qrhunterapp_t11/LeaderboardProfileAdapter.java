package com.example.qrhunterapp_t11;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * Adapter class for RecyclerView that holds user profiles in the leaderboard
 *
 * @author Afra
 */
public class LeaderboardProfileAdapter extends FirestoreRecyclerAdapter<User, LeaderboardProfileAdapter.RecyclerViewHolder> {
    private OnItemClickListener listener;
    private final SharedPreferences prefs;

    public LeaderboardProfileAdapter(@NonNull FirestoreRecyclerOptions<User> options, @NonNull SharedPreferences prefs) {
        super(options);
        this.prefs = prefs;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull User model) {
        // Bind the QRCode object to the RecyclerViewHolder
        holder.username.setText(model.getDisplayName());

        String totalPoints = "Points: " + model.getTotalPoints();
        holder.totalPoints.setText(totalPoints);

        holder.ranking.setText(String.valueOf(position + 1));

        if (model.getDisplayName().equals(prefs.getString("currentUser", null))) {
            String rankingText = "Your Ranking: " + (position + 1);
            prefs.edit().putString("currentUserRanking", rankingText).commit();
        }

    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.individual_profile, group, false);

        return new RecyclerViewHolder(view);
    }

    /**
     * Sets the OnClickListener
     *
     * @param listener - OnItemClickListener
     */
    public void setOnItemClickListener(@NonNull OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Holds the layout and Click functionalities for each item in the recyclerView
     */

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView username;
        private final TextView totalPoints;
        private final TextView ranking;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.profile_name_textview);
            totalPoints = itemView.findViewById(R.id.profile_points_search);
            ranking = itemView.findViewById(R.id.ranking_textview);

            // This click listener responds to clicks done on an item in the recyclerview
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }
}