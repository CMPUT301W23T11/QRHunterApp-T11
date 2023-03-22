package com.example.qrhunterapp_t11;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Handles search and leaderboard screen.
 * Leaderboard displays ranked list of players, clicking on a player shows their profile
 * Search allows the user to search for other users to view their profiles
 *
 * @author Afra, Kristina
 */
public class SearchFragment extends Fragment {

    private String tag = "searchFragment";
    private final FirebaseFirestore db;
    private final CollectionReference usersReference;
    private final CollectionReference QRCodeReference;
    private LeaderboardProfileAdapter leaderboardAdapter;
    private RecyclerView leaderboardRecyclerView;
    private FirestoreRecyclerOptions<User> leaderboardOptions;
    private SharedPreferences prefs;
    private AutoCompleteTextView autoCompleteTextView;

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
        autoCompleteTextView = view.findViewById(R.id.search_id);

        ArrayList<String> displayNameList = new ArrayList<String>();
        usersReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots){
                            if (doc.exists()) {
                                User user = doc.toObject(User.class);
                                displayNameList.add(Objects.requireNonNull(user).getDisplayName());}
                        }
                    }
                });
        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, displayNameList);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(adapter);
        // gets the searchView to be clickable on the whole bar
        /*searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setIconified(false);
            }
        });

        //finds the user with the specific inputted displayName
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String pattern = query.toLowerCase().trim();
               Query getUser = usersReference.whereEqualTo("displayName", pattern);
                getUser.get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){

                                // checks if a user is found
                                if (task.getResult().size() > 0) {
                                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);

                                    // opens the users profile
                                    User user = doc.toObject(User.class);
                                    FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                                    trans.replace(R.id.main_screen, new ProfileFragment(db, user.getDisplayName(), user.getUsername()));
                                    trans.commit();

                                    } else { // if the user is not found
                                        Toast.makeText(getContext(), "User not found!", Toast.LENGTH_SHORT).show();
                                        Log.d(tag, "Document NOT found");
                                    }
                                }
                            else {
                                Log.d(tag, "task not successful: ", task.getException());
                            }
                        });

                // fixes bug where onQueryTextSubmit is fired twice
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });*/

        // Set Firestore RecyclerView query and begin monitoring that query
        leaderboardProfileQuery(new LeaderboardCallback() {
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
    public void leaderboardProfileQuery(final @NonNull LeaderboardCallback completedQueryCheck) {

        Query leaderboardQuery = usersReference.orderBy("totalPoints", Query.Direction.DESCENDING);
        leaderboardQuery
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
                            .setQuery(leaderboardQuery, User.class)
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