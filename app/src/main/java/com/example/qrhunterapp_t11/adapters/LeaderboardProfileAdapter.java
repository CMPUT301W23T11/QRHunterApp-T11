package com.example.qrhunterapp_t11.adapters;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.interfaces.OnItemClickListener;
import com.example.qrhunterapp_t11.objectclasses.User;
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
    private final String viewMode;
    private static final String MOST_POINTS = "Most Points";
    private static final String MOST_SCANS = "Most Scans";
    private static final String TOP_QR_CODE = "Top QR Code";
    private static final String TOP_QR_CODE_REGIONAL = "Top QR Code (Regional)";

    public LeaderboardProfileAdapter(@NonNull FirestoreRecyclerOptions<User> options, @NonNull String viewMode, @NonNull SharedPreferences prefs) {
        super(options);
        this.viewMode = viewMode;
        this.prefs = prefs;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull User model) {
        // Bind the QRCode object to the RecyclerViewHolder
        holder.username.setText(model.getDisplayName());
        holder.ranking.setText(String.valueOf(position + 1));

        switch (viewMode) {
            case MOST_POINTS:
                String totalPoints = "Points: " + model.getTotalPoints();
                holder.totalPoints.setText(totalPoints);
                break;
            case MOST_SCANS:
                String totalScans = "Scans: " + model.getTotalScans();
                holder.totalScans.setText(totalScans);
                break;
            case TOP_QR_CODE:
                String topQRCode = "Top QR Code: " + model.getTopQRCode();
                holder.topQRCode.setText(topQRCode);
                break;
            case TOP_QR_CODE_REGIONAL:
                break;
        }

        if (model.getDisplayName().equals(prefs.getString("currentUserDisplayName", null))) {
            String rankingText = "Your Ranking: " + (position + 1);
            prefs.edit().putString("currentUserRanking", rankingText).commit();
        }
    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup group, int i) {
        View view = null;
        switch (viewMode) {
            case MOST_POINTS:
                view = LayoutInflater.from(group.getContext()).inflate(R.layout.individual_profile_top_points, group, false);
                break;
            case MOST_SCANS:
                view = LayoutInflater.from(group.getContext()).inflate(R.layout.individual_profile_top_scans, group, false);
                break;
            case TOP_QR_CODE:
                view = LayoutInflater.from(group.getContext()).inflate(R.layout.individual_profile_top_code, group, false);
                break;
            case TOP_QR_CODE_REGIONAL:
                view = LayoutInflater.from(group.getContext()).inflate(R.layout.individual_profile_top_code, group, false);
                break;
        }

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
        private final TextView ranking;
        private TextView totalPoints;
        private TextView totalScans;
        private TextView topQRCode;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.profile_name_textview);
            ranking = itemView.findViewById(R.id.ranking_textview);

            switch (viewMode) {
                case MOST_POINTS:
                    totalPoints = itemView.findViewById(R.id.profile_points_search);
                    break;
                case MOST_SCANS:
                    totalScans = itemView.findViewById(R.id.profile_scans_search);
                    break;
                case TOP_QR_CODE:
                    topQRCode = itemView.findViewById(R.id.profile_top_qr_code_search);
                    break;
                case TOP_QR_CODE_REGIONAL:
                    break;
            }

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