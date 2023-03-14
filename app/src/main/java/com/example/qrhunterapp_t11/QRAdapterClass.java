package com.example.qrhunterapp_t11;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * Adapter class for RecyclerView that holds user's collection of QR Codes
 *
 * @author Afra, Sarah
 * @reference <a href="https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/">Firestore documentation</a>
 * @reference <a href="https://www.youtube.com/watch?v=3WR4QAiVuCw">by Coding in Flow for adding OnClick functionality to the recyclerView</a>
 * @reference <a href="https://www.youtube.com/watch?v=JLW7z_AaUHA">by Akshay Jhajhra for more help with the OnClick</a>
 * @reference <a href="https://www.youtube.com/watch?v=k7GR3h5OsXk">by Technical Skillz for FirebaseRecyclerOptions help</a>
 */
public class QRAdapterClass extends FirestoreRecyclerAdapter<QRCode, QRAdapterClass.RecyclerViewHolder> {
    private OnItemClickListener listener;
    private OnItemLongClickListener listenerLong;

    public QRAdapterClass(@NonNull FirestoreRecyclerOptions<QRCode> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull QRCode model) {
        // Bind the QRCode object to the RecyclerViewHolder
        holder.QRCodeName.setText(model.getName());

        String points = "Points: " + model.getPoints();
        holder.QRCodePoints.setText(points);

        String comments = "Comments: " + model.getCommentListSize();
        holder.QRCodeNumComments.setText(comments);
    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.qrcode_profile_view, group, false);

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
     * Constructor for OnItemLongClickListener
     *
     * @param listener OnItemLongClickListener object
     * @see OnItemLongClickListener
     */
    public void setOnItemLongClickListener(@NonNull OnItemLongClickListener listener) {
        this.listenerLong = listener;
    }

    /**
     * Holds the layout and Click functionalities for each item in the recyclerView
     */

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView QRCodeName;
        private final TextView QRCodePoints;
        private final TextView QRCodeNumComments;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            QRCodeName = itemView.findViewById(R.id.qrcode_name);
            QRCodePoints = itemView.findViewById(R.id.qrcode_points);
            QRCodeNumComments = itemView.findViewById(R.id.qrcode_numcomments);

            // This click listener responds to clicks done on an item in the recyclerview

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            // Long click listener for deletion
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listenerLong.onItemLongClick(getSnapshots().getSnapshot(position), position);
                    }
                    return true;
                }
            });
        }
    }

}
