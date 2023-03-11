package com.example.qrhunterapp_t11;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Adapter class for the QRCodes that will be displayed on user profiles
 *
 * @author Afra
 * @reference Sarah's comment adapter class
 * @see CommentAdapter
 */
public class QRCodeAdapterClass extends BaseAdapter {

    private ArrayList<QRCode> QRCodeList;
    private Context context;

    public QRCodeAdapterClass(Context context, ArrayList<QRCode> QRCodeList) {
        this.context = context;
        this.QRCodeList = QRCodeList;
    }

    @Override
    public int getCount() {
        return QRCodeList.size();
    }

    @Override
    public QRCode getItem(int i) {
        return QRCodeList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.qrcode_profile_view,
                    parent, false);
        } else {
            view = convertView;
        }

        TextView QRCodeNameTextView = view.findViewById(R.id.qrcode_name);
        TextView QRCodePointsTextView = view.findViewById(R.id.qrcode_points);
        QRCode qrCode = this.getItem(position);
        QRCodeNameTextView.setText(qrCode.getName());
        QRCodePointsTextView.setText(qrCode.getPoints());

        return view;
    }
}