package com.example.qrhunterapp_t11.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.interfaces.OnItemClickListener;
import com.example.qrhunterapp_t11.interfaces.OnItemLongClickListener;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * Adapter class for RecyclerView that holds user's collection of QR Codes
 *
 * @author Afra, Sarah
 * @sources <pre>
 * <ul>
 * <li><a href="https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/">Firestore documentation</a></li>
 * <li><a href="https://www.youtube.com/watch?v=3WR4QAiVuCw">by Coding in Flow for adding OnClick functionality to the recyclerView</a></li>
 * <li><a href="https://www.youtube.com/watch?v=JLW7z_AaUHA">by Akshay Jhajhra for more help with the OnClick</a></li>
 * <li><a href="https://www.youtube.com/watch?v=k7GR3h5OsXk">by Technical Skillz for FirebaseRecyclerOptions help</a></li>
 * </ul>
 * </pre>
 */
public class QRCodeAdapter extends FirestoreRecyclerAdapter<QRCode, QRCodeAdapter.RecyclerViewHolder> {
    private OnItemClickListener listener;
    private OnItemLongClickListener listenerLong;
    private final FirebaseFirestore db;

    public QRCodeAdapter(@NonNull FirestoreRecyclerOptions<QRCode> options, @NonNull FirebaseFirestore db) {
        super(options);
        this.db = db;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull QRCode model) {
        // Bind the QRCode object to the RecyclerViewHolder
        holder.qrCodeName.setText(model.getName());

        String points = "Points: " + model.getPoints();
        holder.qrCodePoints.setText(points);

        numCommentsCheck(model.getID(), new QRCodeNumCommentsCallback() {
            @Override
            public void setNumComments(int numComments) {
                String comments = "Comments: " + numComments;
                holder.qrCodeNumComments.setText(comments);
            }
        });
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
     * @see OnItemClickListener
     */
    public void setOnItemLongClickListener(@NonNull OnItemLongClickListener listener) {
        this.listenerLong = listener;
    }

    /**
     * Query database to get number of comments on QR Code
     *
     * @param qrCodeID       QR to check for comments
     * @param setNumComments Callback function
     */
    public void numCommentsCheck(@NonNull String qrCodeID, final @NonNull QRCodeNumCommentsCallback setNumComments) {

        db.collection("QRCodes").document(qrCodeID).collection("commentList")
                .get()
                .addOnSuccessListener(qrCodeCommentList ->
                        setNumComments.setNumComments(qrCodeCommentList.size())
                );
    }

    /**
     * Callback for querying the database to get number of comments on QR Code
     *
     * @author Afra
     */
    public interface QRCodeNumCommentsCallback {
        void setNumComments(int numComments);
    }

    /**
     * Holds the layout and Click functionalities for each item in the recyclerView
     */
    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView qrCodeName;
        private final TextView qrCodePoints;
        private final TextView qrCodeNumComments;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            qrCodeName = itemView.findViewById(R.id.qrcode_name);
            qrCodePoints = itemView.findViewById(R.id.qrcode_points);
            qrCodeNumComments = itemView.findViewById(R.id.qrcode_numcomments);

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
