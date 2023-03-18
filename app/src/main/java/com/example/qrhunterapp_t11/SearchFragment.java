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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;


public class SearchFragment extends Fragment {

    private final CollectionReference usersReference;
    private final CollectionReference QRCodeReference;
    private LeaderboardProfileAdapter leaderboardAdapter;
    private RecyclerView leaderboardRecyclerView;
    private FirestoreRecyclerOptions<User> leaderboardOptions;

    public SearchFragment(@NonNull FirebaseFirestore db) {
        this.usersReference = db.collection("Users");
        this.QRCodeReference = db.collection("QRCodes");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        leaderboardProfileQuery(new LeaderboardCallback() {
            public void completedQueryCheck(boolean queryComplete) {

                if (queryComplete) {
                    leaderboardRecyclerView = view.findViewById(R.id.leaderboard_recyclerview);

                    leaderboardAdapter = new LeaderboardProfileAdapter(leaderboardOptions);

                    //super.onStart(); man idk
                    leaderboardAdapter.startListening();
                    leaderboardRecyclerView.setAdapter(leaderboardAdapter);
                    leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                    // TODO: Implement this
                    // Handles clicking on an item to view the user's profile
//                    adapter.setOnItemClickListener(new OnItemClickListener() {
//                        @Override
//                        public void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position) {
//
//                            QRCode qrCode = documentSnapshot.toObject(QRCode.class);
//                            new ViewQR(qrCode).show(getActivity().getSupportFragmentManager(), "Show QR");
//                        }
//                    });
                }
            }
        });

        return view;
    }

    public void leaderboardProfileQuery(final @NonNull LeaderboardCallback completedQueryCheck) {

        Query leaderboardQuery = usersReference.orderBy("totalPoints");
        leaderboardQuery
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    for (QueryDocumentSnapshot snapshot : documentReferenceSnapshots) {
                        //DocumentReference documentReference = snapshot.getDocumentReference(snapshot.getId());
                        //profile = new User(snapshot.get("Display Name").toString(), 8);
                        //newref.document(profile.getUsername()).set(profile);
                    }
                    leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
                            .setQuery(leaderboardQuery, User.class)
                            .build();
                    completedQueryCheck.completedQueryCheck(true);
                });
    }

    /**
     * Callback for querying the database to get QR object.
     * referencedQRCodes is a map containing data for each
     * QR code in the user's collection
     *
     * @author Afra
     */
    public interface LeaderboardCallback {
        void completedQueryCheck(boolean queryComplete);
    }
}