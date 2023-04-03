package com.example.qrhunterapp_t11.fragments;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.qrhunterapp_t11.fragments.MapFragment.AUTOCOMPLETE_REQUEST_CODE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.qrhunterapp_t11.interfaces.QueryCallbackWithHashMap;
import com.example.qrhunterapp_t11.objectclasses.Preference;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
 * <li><a href="https://www.geeksforgeeks.org/traverse-through-a-hashmap-in-java/">Iterating through HashMap</a></li>
 * </ul>
 * </pre>
 */
public class SearchFragment extends Fragment {

    private static final String TAG = "searchFragment";
    private final FirebaseFirestore db;
    private final CollectionReference usersReference;
    private final CollectionReference qrCodesReference;
    private LeaderboardProfileAdapter leaderboardAdapter;
    private RecyclerView leaderboardRecyclerView;
    private FirestoreRecyclerOptions<User> leaderboardOptions;
    private TextView leaderboardTextView;
    private AutoCompleteTextView autoCompleteTextView;
    private TextView yourRank;
    private List<Place.Field> fields;
    private QRCode usersTopCodeRegional;
    private Spinner leaderboardFilterSpinner;

    public SearchFragment(@NonNull FirebaseFirestore db) {
        this.db = db;
        this.usersReference = db.collection("Users");
        this.qrCodesReference = db.collection("QRCodes");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        Button deleteSearch = view.findViewById(R.id.close_id);
        leaderboardTextView = view.findViewById(R.id.leaderboard_textview);
        autoCompleteTextView = view.findViewById(R.id.search_id);
        final ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);

        // Populates the autocomplete list with users display names
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
                        }
                    }
                }
            }
        });

        // Sets up the autocomplete with the provided array Adapter
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(autoCompleteAdapter);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed
            }

            // Makes the delete button invisible if there is no input
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                deleteSearch.setVisibility(charSequence.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed
            }
        });

        // Finds the user after clicking enter
        autoCompleteTextView.setOnEditorActionListener((textView, i, keyEvent) -> {
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
                                    if (!user.getDisplayName().equals(Preference.getPrefsString(Preference.PREFS_CURRENT_USER_DISPLAY_NAME, null))) {
                                        autoCompleteTextView.setText("");
                                        FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                                        trans.replace(R.id.main_screen, new ProfileFragment(db, user.getDisplayName(), user.getUsername()));
                                        trans.commit();
                                    }
                                    else {
                                        Toast.makeText(getContext(), "You cant search yourself!", Toast.LENGTH_SHORT).show();
                                    }

                                } else {  // If the user is not found
                                    Toast.makeText(getContext(), "User not found!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Document NOT found");
                                }
                            } else {
                                Log.d(TAG, "task not successful: ", task.getException());
                            }
                        });
            }
            return false;
        });

        // Clears the text from autoCompleteTextView
        deleteSearch.setOnClickListener(view1 -> autoCompleteTextView.setText("", false));

        // Setup spinners
        String[] leaderboardFilterChoices = new String[]{"Most Points", "Most Scans", "Top QR Code", "Top QR Code (Regional)"};
        leaderboardFilterSpinner = view.findViewById(R.id.leaderboard_filter_spinner);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this.getContext(), R.layout.custom_spinner_item, leaderboardFilterChoices);
        filterAdapter.setDropDownViewResource(R.layout.custom_dropdown_item);
        leaderboardFilterSpinner.setAdapter(filterAdapter);
        leaderboardFilterSpinner.setPrompt("Filter Leaderboard");

        leaderboardFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // A spinner option will always be selected
            }

            // Set Firestore RecyclerView query depending on selected filter and begin monitoring that query
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String leaderboardFilterChoice = leaderboardFilterSpinner.getSelectedItem().toString();
                yourRank = view.findViewById(R.id.your_ranking_textview);
                TextView filterHeader = view.findViewById(R.id.filter_header);
                switch (leaderboardFilterChoice) {
                    case "Most Points":
                        filterHeader.setText("Points");
                        break;
                    case "Most Scans":
                        filterHeader.setText("Scans");
                        break;
                    case "Top QR Code":
                    case "Top QR Code (Regional)":
                        filterHeader.setText("Top Code");
                        break;
                }

                // If user selects regional QR filter, prompt them to search for a region
                if (leaderboardFilterChoice.equals("Top QR Code (Regional)")) {

                    Places.initialize(getActivity().getApplicationContext(), getResources().getString(R.string.google_map_api_key));
                    fields = Arrays.asList(Place.Field.NAME, Place.Field.TYPES);

                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                            .setHint("Search for a region")
                            .setTypeFilter(TypeFilter.REGIONS)
                            .build(getActivity().getApplicationContext());
                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                } else {
                    filterQuery(leaderboardFilterChoice, new QueryCallback() {
                        public void queryCompleteCheck(boolean queryComplete) {
                            assert (queryComplete);
                            leaderboardRecyclerView = view.findViewById(R.id.leaderboard_recyclerview);

                            leaderboardAdapter = new LeaderboardProfileAdapter(leaderboardOptions, leaderboardFilterChoice);

                            //super.onStart(); man idk
                            leaderboardAdapter.startListening();
                            leaderboardRecyclerView.setAdapter(leaderboardAdapter);

                            leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                            // Handles clicking on a user to view their profile
                            leaderboardAdapter.setOnItemClickListener(new OnItemClickListener() {
                                @Override
                                public void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position) {

                                    User user = documentSnapshot.toObject(User.class);
                                    FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                                    trans.replace(R.id.main_screen, new ProfileFragment(db, user.getUsername(), user.getDisplayName()));
                                    trans.commit();
                                }
                            });
                        }
                    });
                }
            }
        });

        return view;
    }

    /**
     * Handles the result of the region search
     *
     * @param requestCode Code used for the request
     * @param resultCode  Result of the request
     * @param data        Data returned from the request
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;

                Place place = Autocomplete.getPlaceFromIntent(data);
                String placeName = place.getName();
                String placeType = place.getTypes().get(0).toString();

                Log.i("TAG", "Place: " + placeName + ", " + place.getLatLng() + ", " + placeType);

                if (placeName != null) {
                    filterQueryRegional(placeName, placeType, new QueryCallbackWithHashMap() {
                        @Override
                        public void setHashMap(@NonNull HashMap<String, String> hashMap) {

                            if (!hashMap.isEmpty()) {
                                ArrayList<String> users = new ArrayList<>();
                                ArrayList<String> qrsPoints = new ArrayList<>();

                                for (Map.Entry<String, String> mapElement : hashMap.entrySet()) {
                                    String user = mapElement.getKey();
                                    users.add(user);
                                    String qrPoints = mapElement.getValue();
                                    qrsPoints.add(qrPoints);
                                }

                                List<List<String>> userChunks = new ArrayList<>();
                                for (int i = 0; i < users.size(); i += 10) {
                                    int end = Math.min(i + 10, users.size());
                                    List<String> sublist = users.subList(i, end);
                                    userChunks.add(sublist);
                                }
                                System.out.println(userChunks);


                                Query query = usersReference.whereIn(FieldPath.documentId(), userChunks);
                                leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
                                        .setQuery(query, User.class)
                                        .build();

                                // Set user's rank if they are on the leaderboard
                                String yourRankString;
                                if (users.contains(Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null))) {
                                    yourRankString = "Your Rank: " + (users.indexOf(Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null)));
                                } else {
                                    yourRankString = "Your Rank: N/A";
                                }
                                yourRank.setText(yourRankString);

                                String leaderboardText = "Leaderboard (" + placeName + ")";
                                leaderboardTextView.setText(leaderboardText);

                                leaderboardRecyclerView = getView().findViewById(R.id.leaderboard_recyclerview);

                                leaderboardAdapter = new LeaderboardProfileAdapter(leaderboardOptions, "Top QR Code (Regional)");

                                //super.onStart(); man idk
                                leaderboardAdapter.startListening();
                                leaderboardRecyclerView.setAdapter(leaderboardAdapter);

                                leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                                // Handles clicking on a user to view their profile
                                leaderboardAdapter.setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position) {

                                        User user = documentSnapshot.toObject(User.class);
                                        FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                                        trans.replace(R.id.main_screen, new ProfileFragment(db, user.getUsername(), user.getDisplayName()));
                                        trans.commit();
                                    }
                                });
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder
                                        .setMessage("No QR Codes found in " + placeName + ".")
                                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                leaderboardFilterSpinner.setSelection(0, true);
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                                                        .setHint("Search for a region")
                                                        .setTypeFilter(TypeFilter.REGIONS)
                                                        .build(getActivity().getApplicationContext());
                                                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                                            }
                                        })
                                        .create();
                                builder.show();
                            }
                        }
                    });
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Do something
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
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
        }

        Query query = usersReference.orderBy(queryField, Query.Direction.DESCENDING);
        query
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    // Set user's rank
                    for (int i = 0; i < documentReferenceSnapshots.size(); i++) {
                        DocumentSnapshot user = documentReferenceSnapshots.getDocuments().get(i);
                        if (user.get("username").equals(Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null))) {
                            String yourRankString = "Your Rank: " + (i + 1);
                            yourRank.setText(yourRankString);
                            break;
                        }
                    }
                    leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .build();
                    queryCompleteCheck.queryCompleteCheck(true);
                });
    }

    /**
     * Set query for regional leaderboard setting
     *
     * @param placeName  Name of selected region in maps autocomplete search
     * @param placeType  Type of selected region in maps autocomplete search
     * @param setHashMap Callback for query
     */
    public void filterQueryRegional(@NonNull String placeName, @NonNull String placeType, final @NonNull QueryCallbackWithHashMap setHashMap) {

        String qrCodeField = null;
        switch (placeType) {

            // Country
            case "COUNTRY":
                qrCodeField = "country";
                break;

            // 1st order civil entity below country level, e.g. province/state
            case "ADMINISTRATIVE_AREA_LEVEL_1":
                qrCodeField = "adminArea";
                break;

            // 2nd order civil entity below country level, e.g. county
            case "ADMINISTRATIVE_AREA_LEVEL_2":
                qrCodeField = "subAdminArea";
                break;

            // City or town
            case "LOCALITY":
                qrCodeField = "locality";
                break;

            // 1st order civil entity below locality, e.g. borough/neighborhood
            case "SUBLOCALITY_LEVEL_1":
                qrCodeField = "subLocality";
                break;

            // Postal or zip code prefix
            case "POSTAL_CODE_PREFIX":
                qrCodeField = "postalCodePrefix";
                break;

            // Postal or zip code
            case "POSTAL_CODE":
                qrCodeField = "postalCode";
                break;
        }

        // Contains users and their top scoring QR Code within the selected region
        HashMap<String, String> usersPoints = new HashMap<>();

        assert qrCodeField != null;
        // Get all QR Codes within selected region
        qrCodesReference
                .whereEqualTo(qrCodeField, placeName)
                .orderBy("points", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(qrCodesAtPlace -> {
                    // For each QR Code, record which users have it in their collection
                    if (!qrCodesAtPlace.isEmpty()) {
                        for (QueryDocumentSnapshot qrCode : qrCodesAtPlace) {
                            qrCodesReference.document(qrCode.getId()).collection("In Collection")
                                    .get()
                                    .addOnSuccessListener(usersWithQR -> {
                                        if (!usersWithQR.isEmpty()) {
                                            for (QueryDocumentSnapshot userWithQR : usersWithQR) {

                                                usersPoints.put(userWithQR.get("username").toString(), qrCode.get("points").toString());
                                                setHashMap.setHashMap(usersPoints);
                                            }
                                        }
                                    });
                        }
                    } else {
                        setHashMap.setHashMap(usersPoints);
                    }
                });
    }
}