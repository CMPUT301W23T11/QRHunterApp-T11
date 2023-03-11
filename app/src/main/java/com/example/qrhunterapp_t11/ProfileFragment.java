package com.example.qrhunterapp_t11;

import static java.lang.String.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.text.MessageFormat;


/**
 * Handles player profile screen.
 * Outputs the users' QR collection and the users' stats
 *
 * @author Afra, Kristina
 * @reference Url: <https://stackoverflow.com/questions/74092262/calculate-total-from-values-stored-in-firebase-firestore-database-android> How to calculate the sum of a set of documents</a>
 * @reference Url: <https://firebase.google.com/docs/firestore/query-data/listen> How to get a new snapshot everytime the data is updated</a>
 */
public class ProfileFragment extends Fragment {
    private final FirebaseFirestore db;
    private final CollectionReference usersReference;
    private final CollectionReference QRCodesReference;

    /**
     * Constructor for registration fragment.
     * Also instantiates a reference to the Users and QRCodes collections for ease of access.
     *
     * @param db Firestore database instance
     */
    public ProfileFragment(FirebaseFirestore db) {
        this.db = db;
        this.usersReference = db.collection("Users");
        this.QRCodesReference = db.collection("QRCodes");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        String TAG = "Profile";
        TextView loginUsernameTextView = view.findViewById(R.id.profile_name);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        String username = prefs.getString("currentUser", null);
        loginUsernameTextView.setText(username);

        TextView totalScoreText = view.findViewById(R.id.totalScoreText);
        TextView topQRCodeText = view.findViewById(R.id.topQRText);
        TextView lowQRCodeText = view.findViewById(R.id.lowQRText);
        TextView totalQRCodesText = view.findViewById(R.id.totalQRText);

        CollectionReference QRColl = usersReference.document(username).collection("QR Codes");


        //Gets the sum of points from all the QR Code documents
        QRColl.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen FAILED", error);
            }
            double total = 0;

            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                double points = document.getDouble("points");
                total += points;
            }
            totalScoreText.setText(MessageFormat.format("Total score: {0}", (int) total));
            Log.d(TAG, "Total Score: " + total);
        });

        Query topQR = QRColl.orderBy("points", Query.Direction.DESCENDING).limit(1);
         // Orders the QR collection from biggest to smallest, then returns the first QR Code
        topQR.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen FAILED", error);
            }
            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                topQRCodeText.setText(MessageFormat.format("Your top QR Code: {0}", document.get("points")));
                Log.d(TAG, "top QR code: " + document.get("points"));

            }
        });


        Query lowQR = QRColl.orderBy("points", Query.Direction.ASCENDING).limit(1);
        //Orders the QR collection from smallest to largest, then returns the first QR Code
        lowQR.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen FAILED", error);
            }
            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                lowQRCodeText.setText(MessageFormat.format("Your lowest QR Code: {0}", document.get("points")));
                Log.d(TAG, "lowest QR code: " + document.get("points"));

            }
        });

        // Gets the size of the amount of QR codes there are
        QRColl.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen FAILED", error);
            }
            Log.d(TAG, "num of QR: " + value.size());
            totalQRCodesText.setText(MessageFormat.format("Total number of QR codes: {0}", value.size()));
            });


        return view;
    }
}