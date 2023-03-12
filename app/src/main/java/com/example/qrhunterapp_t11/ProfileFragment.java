package com.example.qrhunterapp_t11;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Objects;


/**
 * Handles player profile screen.
 * Outputs the users' QR collection and the users' stats
 *
 * @author Afra, Kristina, Sarah
 * @reference Url: <https://stackoverflow.com/questions/74092262/calculate-total-from-values-stored-in-firebase-firestore-database-android> How to calculate the sum of a set of documents</a>
 * @reference Url: <https://firebase.google.com/docs/firestore/query-data/listen> How to get a new snapshot everytime the data is updated</a>
 * @reference <a href="https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/">Firestore documentation for RecyclerView</a>
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private final CollectionReference usersReference;
    private RecyclerView QRCodeRecyclerView;
    QRAdapterClass adapter;
    FirestoreRecyclerOptions<QRCode> options;

    /**
     * Constructor for registration fragment.
     * Also instantiates a reference to the Users and QRCodes collections for ease of access.
     *
     * @param db Firestore database instance
     */
    public ProfileFragment(FirebaseFirestore db) {
        this.usersReference = db.collection("Users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView loginUsernameTextView = view.findViewById(R.id.profile_name);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        String username = prefs.getString("currentUser", null);
        loginUsernameTextView.setText(username);

        TextView totalScoreText = view.findViewById(R.id.totalScoreText);
        TextView topQRCodeText = view.findViewById(R.id.topQRText);
        TextView lowQRCodeText = view.findViewById(R.id.lowQRText);
        TextView totalQRCodesText = view.findViewById(R.id.totalQRText);

        CollectionReference QRColl = usersReference.document(username).collection("QR Codes");

        QRCodeRecyclerView = view.findViewById(R.id.collectionRecyclerView);

        Query query = usersReference.document(username).collection("QR Codes");
        options = new FirestoreRecyclerOptions.Builder<QRCode>()
                .setQuery(query, QRCode.class)
                .build();

        adapter = new QRAdapterClass(options);

        //super.onStart(); man idk
        adapter.startListening();
        QRCodeRecyclerView.setAdapter(adapter);
        QRCodeRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // Gets the sum of points from all the QR Code documents
        QRColl.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen FAILED", error);
            }
            double total = 0;

            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                int points = document.getLong("points").intValue();
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
        // Orders the QR collection from smallest to largest, then returns the first QR Code
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

        // Handles clicking on an item to view the QR Code
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                QRCode qrCode = documentSnapshot.toObject(QRCode.class);
                DocumentReference QrReference = documentSnapshot.getReference();
                String documentId = documentSnapshot.getId();

                System.out.println("click position " + position);
                new ViewQR(qrCode, QrReference).show(getActivity().getSupportFragmentManager(), "Show QR");

            }
        });

        // Handles long clicking on an item for deletion
        adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot, int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                String documentId = documentSnapshot.getId();

                builder
                        .setTitle("Delete QR Code?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                usersReference.document(username).collection("QR Codes").document(documentId).delete();
                            }
                        })
                        .create();

                builder.show();
            }
        });

        return view;
    }
}