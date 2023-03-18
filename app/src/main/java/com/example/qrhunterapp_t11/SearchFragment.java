package com.example.qrhunterapp_t11;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;


public class SearchFragment extends Fragment {

    private final CollectionReference usersReference;
    private final CollectionReference QRCodeReference;
    private LeaderboardProfileAdapter adapter;
    private RecyclerView leaderboardRecyclerView;
    private FirestoreRecyclerOptions<LeaderboardProfile> options;

    public SearchFragment(@NonNull FirebaseFirestore db) {
        this.usersReference = db.collection("Users");
        this.QRCodeReference = db.collection("QRCodes");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        leaderboardRecyclerView = view.findViewById(R.id.leaderboard_recyclerview);

        adapter = new LeaderboardProfileAdapter(options);

        //super.onStart(); man idk
        adapter.startListening();
        leaderboardRecyclerView.setAdapter(adapter);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // TODO: Implement this
        // Handles clicking on an item to view the user's profile
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position) {

//                QRCode qrCode = documentSnapshot.toObject(QRCode.class);
//                new ViewQR(qrCode).show(getActivity().getSupportFragmentManager(), "Show QR");
            }
        });


        return view;
    }

    public void leaderboardProfileQuery() {

        Query query = usersReference.orderBy("Points");
        query
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    for (QueryDocumentSnapshot snapshot : documentReferenceSnapshots) {
                        DocumentReference documentReference = snapshot.getDocumentReference(snapshot.getId());
                        LeaderboardProfile profile = new LeaderboardProfile(snapshot.get("Display Name").toString(), snapshot.get("Points").toString(), documentReference);

                    }
                    options = new FirestoreRecyclerOptions.Builder<LeaderboardProfile>()
                            .setQuery(query, LeaderboardProfile.class)
                            .build();
                });
    }
}