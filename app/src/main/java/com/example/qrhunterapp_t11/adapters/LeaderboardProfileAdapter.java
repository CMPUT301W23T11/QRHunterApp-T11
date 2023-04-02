package com.example.qrhunterapp_t11.adapters;

import android.graphics.Color;
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
    private static final String MOST_POINTS = "Most Points";
    private static final String MOST_SCANS = "Most Scans";
    private static final String TOP_QR_CODE = "Top QR Code";
    private static final String TOP_QR_CODE_REGIONAL = "Top QR Code (Regional)";
    private final String viewMode;
    private OnItemClickListener listener;

    public LeaderboardProfileAdapter(@NonNull FirestoreRecyclerOptions<User> options, @NonNull String viewMode) {
        super(options);
        this.viewMode = viewMode;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull User model) {
        // Bind the QRCode object to the RecyclerViewHolder
        holder.username.setText(model.getDisplayName());
        if ((position + 1) >= 4 && (position + 1) <= 9) {
            holder.ranking.setText("0" + (position + 1));
        } else {
            holder.ranking.setText(String.valueOf(position + 1));
        }

        // SET APPEARANCE OF VIEWHOLDER BASED ON POSITION
        switch (position + 1) { // Set colors of top three rankings
            case 1:
                holder.ranking.setText("\uD83C\uDFC6");
                holder.ranking.setTextSize(21);
                holder.ranking.setTextColor(Color.rgb(0, 0, 0)); // need to set color to black or otherwise emoji will be faded
                holder.username.setTextColor(Color.rgb(255, 196, 0));
                holder.typeOfRanking.setTextColor(Color.rgb(255, 196, 0));
                break;

            case 2:
                holder.ranking.setText("\uD83E\uDD48");
                holder.ranking.setTextSize(21);
                holder.ranking.setTextColor(Color.rgb(0, 0, 0));
                holder.username.setTextColor(Color.rgb(166, 166, 166));
                holder.typeOfRanking.setTextColor(Color.rgb(166, 166, 166));
                break;

            case 3:
                holder.ranking.setText("\uD83E\uDD49");
                holder.ranking.setTextSize(21);
                holder.ranking.setTextColor(Color.rgb(0, 0, 0));
                holder.username.setTextColor(Color.rgb(206, 112, 18));
                holder.typeOfRanking.setTextColor(Color.rgb(206, 112, 18));
                break;

            default: // MUST OVERWRITE DEFAULT CASES, otherwise when you scroll down, the recyclerview will re-apply the above changes to the new
                holder.ranking.setTextSize(17);
                holder.ranking.setTextColor(Color.rgb(128, 128, 128));
                holder.username.setTextColor(Color.rgb(128, 128, 128));
                holder.typeOfRanking.setTextColor(Color.rgb(128, 128, 128));
                break;
        }


        // SET VALUE OF COUNT BASED ON SORTED CATEGORY
        switch (viewMode) {
            case MOST_POINTS:
                String totalPoints = "" + model.getTotalPoints();
                holder.typeOfRanking.setText(totalPoints);
                break;
            case MOST_SCANS:
                String totalScans = "" + model.getTotalScans();
                holder.typeOfRanking.setText(totalScans);
                break;
            case TOP_QR_CODE:
            case TOP_QR_CODE_REGIONAL:
                String topQRCode = "" + model.getTopQRCode();
                holder.typeOfRanking.setText(topQRCode);
                break;
        }
    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup group, int i) {
        View view = LayoutInflater.from(group.getContext()).inflate(R.layout.individual_profile_leaderboard, group, false);

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
        private final TextView typeOfRanking;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.profile_name_textview);
            ranking = itemView.findViewById(R.id.ranking_textview);
            typeOfRanking = itemView.findViewById(R.id.leaderboard_filter_type);

            // Click listener for items in the recyclerview
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