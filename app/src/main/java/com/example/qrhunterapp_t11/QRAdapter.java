package com.example.qrhunterapp_t11;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import io.reactivex.rxjava3.annotations.NonNull;

public class QRAdapter extends FirestoreRecyclerAdapter<QRCode, QRAdapter.RecyclerViewHolder> {

    private  OnItemClickListener listener;

    public QRAdapter(@NonNull FirestoreRecyclerOptions<QRCode> options){
        super(options);
    }

    @Override
    protected void onBindViewHolder(@androidx.annotation.NonNull RecyclerViewHolder holder, int position, @androidx.annotation.NonNull QRCode model) {
        // Bind the QRCode object to the RecyclerViewHolder
        holder.QRCodeName.setText(model.getName());
        holder.QRCodePoints.setText("Points: " + model.getPoints());
        holder.QRCodeNumComments.setText("Comments: " + model.getCommentList().size());
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup group, int i) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.qrcode_profile_view, group, false);

        return new RecyclerViewHolder(view);
    }
    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        public TextView QRCodeName;
        public TextView QRCodePoints;
        public TextView QRCodeNumComments;

        public RecyclerViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            QRCodeName = itemView.findViewById(R.id.qrcode_name);
            QRCodePoints = itemView.findViewById(R.id.qrcode_points);
            QRCodeNumComments = itemView.findViewById(R.id.qrcode_numcomments);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


}