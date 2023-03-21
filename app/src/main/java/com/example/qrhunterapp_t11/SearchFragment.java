package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handles search and leaderboard screen.
 * Leaderboard displays ranked list of players, clicking on a player shows their profile
 * Search allows the user to search for other users to view their profiles
 *
 * @author Afra, Kristina
 */
public class SearchFragment extends Fragment {

    private final FirebaseFirestore db;
    private final CollectionReference usersReference;
    private final CollectionReference QRCodeReference;
    private LeaderboardProfileAdapter leaderboardAdapter;
    private RecyclerView leaderboardRecyclerView;
    private FirestoreRecyclerOptions<User> leaderboardOptions;
    private SharedPreferences prefs;

    public SearchFragment(@NonNull FirebaseFirestore db) {
        this.db = db;
        this.usersReference = db.collection("Users");
        this.QRCodeReference = db.collection("QRCodes");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Set Firestore RecyclerView query and begin monitoring that query
        topScoresQuery(new LeaderboardCallback() {
            public void completedQueryCheck(boolean queryComplete) {

                if (queryComplete) {
                    leaderboardRecyclerView = view.findViewById(R.id.leaderboard_recyclerview);

                    prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    leaderboardAdapter = new LeaderboardProfileAdapter(leaderboardOptions, prefs);

                    //super.onStart(); man idk
                    leaderboardAdapter.startListening();
                    leaderboardRecyclerView.setAdapter(leaderboardAdapter);

                    TextView yourRanking = view.findViewById(R.id.your_ranking_textview);
                    yourRanking.setText(prefs.getString("currentUserRanking", null));

                    leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                    // Handles clicking on a user to view their profile
                    leaderboardAdapter.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position) {

                            User user = documentSnapshot.toObject(User.class);
                            FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                            trans.replace(R.id.main_screen, new ProfileFragment(db, user.getDisplayName(), user.getUsername()));
                            trans.commit();
                        }
                    });
                }
            }
        });

        return view;
    }

    /**
     * Set query for Firestore RecyclerView
     * Query gets a sorted list of users based on total points
     *
     * @param completedQueryCheck Callback for query
     */
    public void topScoresQuery(final @NonNull LeaderboardCallback completedQueryCheck) {

        Query query = usersReference.orderBy("totalPoints", Query.Direction.DESCENDING);
        query
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .build();
                    completedQueryCheck.completedQueryCheck(true);
                });
    }

    /**
     * Set query for Firestore RecyclerView
     * Query gets a sorted list of users based on total scans
     *
     * @param completedQueryCheck Callback for query
     */
    public void topScansQuery(final @NonNull LeaderboardCallback completedQueryCheck) {

        Query query = usersReference.orderBy("totalPoints", Query.Direction.DESCENDING);
        query
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .build();
                    completedQueryCheck.completedQueryCheck(true);
                });
    }

    /**
     * Set query for Firestore RecyclerView
     * Query gets a sorted list of users based on their top scoring QR code
     *
     * @param completedQueryCheck Callback for query
     */
    public void topScoringCodesQuery(final @NonNull LeaderboardCallback completedQueryCheck) {

        Query query = usersReference.orderBy("totalPoints", Query.Direction.DESCENDING);
        query
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .build();
                    completedQueryCheck.completedQueryCheck(true);
                });
    }

    /**
     * Set query for Firestore RecyclerView
     * Query gets a sorted list of users based on their top scoring QR code in a region
     *
     * @param completedQueryCheck Callback for query
     */
    public void topScoringCodesInRegionQuery(final @NonNull LeaderboardCallback completedQueryCheck) {

        Query query = usersReference.orderBy("totalPoints", Query.Direction.DESCENDING);
        query
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .build();
                    completedQueryCheck.completedQueryCheck(true);
                });
    }

    /**
     * Callback for querying the database to get ordered users
     *
     * @author Afra
     */
    public interface LeaderboardCallback {
        void completedQueryCheck(boolean queryComplete);
    }
}