package com.example.qrhunterapp_t11.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.objectclasses.QRCode;

import java.util.ArrayList;
import java.util.List;

public class QRCodeAdapterMap extends BaseAdapter {
    private final Context context;
    private final ArrayList<List<?>> qrCodeList;

    public QRCodeAdapterMap(@NonNull Context context, @NonNull ArrayList<List<?>> qrCodeList) {
        this.context = context;
        this.qrCodeList = qrCodeList;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View nearbyCodeView = inflater.inflate(R.layout.fragment_map_nearby_code, parent, false);

        List<?> qrCodeDistance = qrCodeList.get(i);
        QRCode qrCode = (QRCode) qrCodeDistance.get(0);
        double distance = (double) qrCodeDistance.get(1);

        TextView distanceTextView = nearbyCodeView.findViewById(R.id.map_nearby_code_distance);
        TextView qrCodeNameTextView = nearbyCodeView.findViewById(R.id.map_nearby_code_name);
        TextView pointsTextView = nearbyCodeView.findViewById(R.id.map_nearby_code_points);
        TextView numberOfScansTextView = nearbyCodeView.findViewById(R.id.map_nearby_code_scans);

        String distanceString = distance + " km";
        String pointsString = qrCode.getPoints() + " points";
        String scansString = qrCode.getNumberOfScans() + " scans";

        distanceTextView.setText(distanceString);
        qrCodeNameTextView.setText(qrCode.getName());
        pointsTextView.setText(pointsString);
        numberOfScansTextView.setText(scansString);

        return nearbyCodeView;
    }

    /**
     * Gets the size of the qrCodeList
     *
     * @return qrCodeList.size() - integer
     */
    @Override
    public int getCount() {
        return this.qrCodeList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
}
