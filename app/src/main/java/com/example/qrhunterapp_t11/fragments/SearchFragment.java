package com.example.qrhunterapp_t11.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.adapters.LeaderboardProfileAdapter;
import com.example.qrhunterapp_t11.interfaces.OnItemClickListener;
import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Handles search and leaderboard screen.
 * Leaderboard displays ranked list of players, clicking on a player shows their profile
 * Search allows the user to search for other users to view their profiles
 *
 * @author Afra, Kristina
 * @sources <pre>
 * <ul>
 * <li><a href="https://stackoverflow.com/a/5241720">For setting spinner</a></li>
 * <li><a href="https://stackoverflow.com/questions/41670850/prevent-user-to-go-next-line-by-pressing-softkey-enter-in-autocompletetextview">How to handle a ENTER click action</a></li>
 * <li><a href="https://stackoverflow.com/a/4145983">For setting EditText filter</a></li>
 * </ul>
 * </pre>
 */
public class SearchFragment extends Fragment {

    private static final String TAG = "searchFragment";
    private final FirebaseFirestore db;
    private final CollectionReference usersReference;
    private final CollectionReference qrCodeReference;
    private LeaderboardProfileAdapter leaderboardAdapter;
    private RecyclerView leaderboardRecyclerView;
    private FirestoreRecyclerOptions<User> leaderboardOptions;
    private SharedPreferences prefs;
    private AutoCompleteTextView autoCompleteTextView;

    public SearchFragment(@NonNull FirebaseFirestore db) {
        this.db = db;
        this.usersReference = db.collection("Users");
        this.qrCodeReference = db.collection("QRCodes");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Set leaderboard filter spinner
        String[] leaderboardFilterChoices = new String[]{"Most Points", "Most Scans", "Top QR Code", "Top QR Code (Regional)"};
        Spinner leaderboardFilterSpinner = view.findViewById(R.id.leaderboard_filter_spinner);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, leaderboardFilterChoices);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaderboardFilterSpinner.setAdapter(filterAdapter);
        leaderboardFilterSpinner.setPrompt("Filter Leaderboard");

        Button deleteSearch = view.findViewById(R.id.close_id);
        autoCompleteTextView = view.findViewById(R.id.search_id);
        final ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);

        // Populates the autocomplete list with users display names
        // Todo: edit set displayName to lowercase when adding to database to get case insensitivity
        usersReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed: ", error);
                    return;
                }
                if (value != null) {
                    autoCompleteAdapter.clear();
                    for (DocumentSnapshot doc : value) {
                        if (doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                autoCompleteAdapter.add(user.getDisplayName());
                            }
                            //}
                        }
                    }
                }
            }
        });


        // sets up the autocomplete with the provided array Adapter
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(autoCompleteAdapter);

        // Finds the user after clicking enter
        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)) {
                    autoCompleteTextView.dismissDropDown();
                    String searchText = autoCompleteTextView.getText().toString();

                    Query getUser = usersReference.whereEqualTo("displayName", searchText);
                    getUser.get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    // checks if a user is found
                                    if (task.getResult().size() > 0) {
                                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);

                                        // opens the users profile
                                        User user = doc.toObject(User.class);
                                        assert user != null;

                                        // Checks to make sure user cant search their own name
                                        if (!user.getDisplayName().equals(prefs.getString("currentUserDisplayName", null))) {
                                            autoCompleteTextView.setText("");
                                            FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                                            trans.replace(R.id.main_screen, new ProfileFragment(db, user.getDisplayName(), user.getUsername()));
                                            trans.commit();
                                        }

                                    } else { // if the user is not found
                                        Toast.makeText(getContext(), "User not found!", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Document NOT found");
                                    }
                                } else {
                                    Log.d(TAG, "task not successful: ", task.getException());
                                }
                            });
                }
                return false;
            }
        });

        // Clears the text from autoCompleteTextView
        deleteSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoCompleteTextView.setText("", false);
            }
        });

        // Set Firestore RecyclerView query and begin monitoring that query
        leaderboardFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // A spinner option will always be selected
            }

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String leaderboardFilterChoice = leaderboardFilterSpinner.getSelectedItem().toString();
                filterQuery(leaderboardFilterChoice, new QueryCallback() {
                    public void queryCompleteCheck(boolean queryComplete) {
                        assert (queryComplete);
                        leaderboardRecyclerView = view.findViewById(R.id.leaderboard_recyclerview);

                        prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                        leaderboardAdapter = new LeaderboardProfileAdapter(leaderboardOptions, leaderboardFilterChoice, prefs);

                        //super.onStart(); man idk
                        leaderboardAdapter.startListening();
                        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

                        TextView yourRanking = view.findViewById(R.id.your_ranking_textview);
                        yourRanking.setText(prefs.getString("currentUserRanking", null));

                        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                        // If user selects regional QR filter,
                        if (leaderboardFilterChoice.equals("Top QR Code (Regional)")) {
                            // Set leaderboard radius spinner
                            String[] leaderboardRadiusChoices = new String[]{"5 km", "10 km", "25 km", "Custom radius"};
                            Spinner leaderboardRadiusSpinner = view.findViewById(R.id.leaderboard_radius_spinner);
                            ArrayAdapter<String> radiusAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, leaderboardRadiusChoices);
                            radiusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            leaderboardRadiusSpinner.setAdapter(radiusAdapter);
                            leaderboardRadiusSpinner.setVisibility(View.VISIBLE);
                            leaderboardRadiusSpinner.setPrompt("Set a radius");

                            leaderboardRadiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                    // A spinner option will always be selected
                                }

                                @Override
                                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                    String leaderboardRadiusChoice = leaderboardRadiusSpinner.getSelectedItem().toString();

                                    if (leaderboardRadiusChoice.equals(leaderboardRadiusChoices[3])) {
                                        // Set input EditText
                                        final EditText customRadius = new EditText(getContext());
                                        InputFilter[] filterArray = new InputFilter[1];
                                        filterArray[0] = new InputFilter.LengthFilter(3);
                                        customRadius.setFilters(filterArray);
                                        customRadius.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder
                                                .setTitle("Custom radius")
                                                .setView(customRadius)
                                                .setMessage("Enter a custom radius in km")
                                                .setNegativeButton("Cancel", null)
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        String customRadiusAmount = customRadius.getText().toString();
                                                        leaderboardRadiusChoices[3] = "Custom radius (" + customRadiusAmount + " km)";
                                                        radiusAdapter.notifyDataSetChanged();
                                                    }
                                                })
                                                .create();
                                        builder.show();
                                    }
                                }
                            });
                        }

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
                });
            }
        });

        return view;
    }

    /**
     * Set query for Firestore RecyclerView depending on what spinner option is selected
     *
     * @param filterType         String representing how the leaderboard is filtered
     * @param queryCompleteCheck Callback for query
     */
    public void filterQuery(@NonNull String filterType, final @NonNull QueryCallback queryCompleteCheck) {
        String queryField = "";
        switch (filterType) {
            case "Most Points":
                queryField = "totalPoints";
                break;
            case "Most Scans":
                queryField = "totalScans";
                break;
            case "Top QR Code":
                queryField = "topQRCode";
                break;
            case "Top QR Code (Regional)":
                queryField = "topQRCode";
                break;

        }
        Query query = usersReference.orderBy(queryField, Query.Direction.DESCENDING);
        query
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .build();
                    queryCompleteCheck.queryCompleteCheck(true);
                });
    }
}