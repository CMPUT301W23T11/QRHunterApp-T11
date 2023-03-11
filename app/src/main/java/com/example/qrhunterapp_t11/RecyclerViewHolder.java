package com.example.qrhunterapp_t11;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    public TextView QRCodeName;
    public TextView QRCodePoints;
    public TextView QRCodeNumComments;

    public RecyclerViewHolder(@NonNull View itemView) {
        super(itemView);

        QRCodeName = itemView.findViewById(R.id.qrcode_name);
        QRCodePoints = itemView.findViewById(R.id.qrcode_points);
        QRCodeNumComments = itemView.findViewById(R.id.qrcode_numcomments);
    }

}
