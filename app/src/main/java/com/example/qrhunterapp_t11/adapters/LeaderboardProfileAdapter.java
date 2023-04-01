package com.example.qrhunterapp_t11.adapters;

import android.content.res.ColorStateList;
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
    private OnItemClickListener listener;
    private final String viewMode;
    private static final String MOST_POINTS = "Most Points";
    private static final String MOST_SCANS = "Most Scans";
    private static final String TOP_QR_CODE = "Top QR Code";
    private static final String TOP_QR_CODE_REGIONAL = "Top QR Code (Regional)";

    public LeaderboardProfileAdapter(@NonNull FirestoreRecyclerOptions<User> options, @NonNull String viewMode) {
        super(options);
        this.viewMode = viewMode;
    }

    /**
     * @param holder
     * @param position
     * @param model the model object containing the data that should be used to populate the view.
     * @reference Zun - https://stackoverflow.com/q/56749319/14445107 - how to not have recyclerview apply changes to multiple ViewHolders
     */
    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull User model) {
        // Bind the QRCode object to the RecyclerViewHolder
        holder.username.setText(model.getDisplayName());
        holder.ranking.setText(String.valueOf(position + 1));

        // SET APPEARANCE OF VIEWHOLDER BASED ON POSITION
        switch(position + 1) { // set colors of top three rankings //TODO IMPLEMENT REGIONAL QR CODE
            case 1:
                holder.ranking.setText("\uD83C\uDFC6");
                holder.ranking.setTextSize(25);
                holder.ranking.setTextColor(Color.rgb(0,0,0)); // need to set color to black or otherwise emoji will be faded
                holder.username.setTextColor(Color.rgb(255,196,0));

                // SET COLOR FOR THE COUNT OF SELECTED CATEGORY - GOLD
                switch (viewMode) {
                    case MOST_POINTS:
                        holder.totalPoints.setTextColor(Color.rgb(255,196,0));
                        break;
                    case MOST_SCANS:
                        holder.totalScans.setTextColor(Color.rgb(255,196,0));
                        break;
                    case TOP_QR_CODE:
                        holder.topQRCode.setTextColor(Color.rgb(255,196,0));
                        break;
                    case TOP_QR_CODE_REGIONAL:
                        break;
                }

                break;

            case 2:
                holder.ranking.setText("\uD83E\uDD48");
                holder.ranking.setTextSize(25);
                holder.ranking.setTextColor(Color.rgb(0,0,0));
                holder.username.setTextColor(Color.rgb(166,166,166));

                // SET COLOR FOR THE COUNT OF SELECTED CATEGORY - SILVER
                switch (viewMode) {
                    case MOST_POINTS:
                        holder.totalPoints.setTextColor(Color.rgb(166,166,166));
                        break;
                    case MOST_SCANS:
                        holder.totalScans.setTextColor(Color.rgb(166,166,166));
                        break;
                    case TOP_QR_CODE:
                        holder.topQRCode.setTextColor(Color.rgb(166,166,166));
                        break;
                    case TOP_QR_CODE_REGIONAL:
                        break;
                }

                break;

            case 3:
                holder.ranking.setText("\uD83E\uDD49");
                holder.ranking.setTextSize(25);
                holder.ranking.setTextColor(Color.rgb(0,0,0));
                holder.username.setTextColor(Color.rgb(206,112,18));

                // SET COLOR FOR THE COUNT OF SELECTED CATEGORY - BRONZE
                switch (viewMode) {
                    case MOST_POINTS:
                        holder.totalPoints.setTextColor(Color.rgb(206,112,18));
                        break;
                    case MOST_SCANS:
                        holder.totalScans.setTextColor(Color.rgb(206,112,18));
                        break;
                    case TOP_QR_CODE:
                        holder.topQRCode.setTextColor(Color.rgb(206,112,18));
                        break;
                    case TOP_QR_CODE_REGIONAL:
                        break;
                }

                break;

            default: // MUST OVERWRITE DEFAULT CASES, otherwise when you scroll down, the recyclerview will re-apply the above changes to the new
                holder.ranking.setTextSize(17);
                holder.ranking.setTextColor(Color.rgb(128,128,128));
                holder.username.setTextColor(Color.rgb(128,128,128));

                // SET COLOR FOR THE COUNT OF SELECTED CATEGORY - DEFAULT
                switch (viewMode) {
                    case MOST_POINTS:
                        holder.totalPoints.setTextColor(Color.rgb(128,128,128));
                        break;
                    case MOST_SCANS:
                        holder.totalScans.setTextColor(Color.rgb(128,128,128));
                        break;
                    case TOP_QR_CODE:
                        holder.topQRCode.setTextColor(Color.rgb(128,128,128));
                        break;
                    case TOP_QR_CODE_REGIONAL:
                        break;
                }

                break;
        }

        // SET VALUE OF COUNT BASED ON SORTED CATEGORY
        switch (viewMode) {
            case MOST_POINTS:
                String totalPoints = "" + model.getTotalPoints();
                holder.totalPoints.setText(totalPoints);
                break;
            case MOST_SCANS:
                String totalScans = "" + model.getTotalScans();
                holder.totalScans.setText(totalScans);
                break;
            case TOP_QR_CODE:
                String topQRCode = "" + model.getTopQRCode();
                holder.topQRCode.setText(topQRCode);
                break;
            case TOP_QR_CODE_REGIONAL:
                break;
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