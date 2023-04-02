package com.example.qrhunterapp_t11.fragments;

import static android.app.Activity.RESULT_OK;
import static com.example.qrhunterapp_t11.fragments.MapFragment.AUTOCOMPLETE_REQUEST_CODE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
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
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


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
    private final CollectionReference qrCodesReference;
    private LeaderboardProfileAdapter leaderboardAdapter;
    private RecyclerView leaderboardRecyclerView;
    private FirestoreRecyclerOptions<User> leaderboardOptions;
    private SharedPreferences prefs;
    private TextView leaderboardTextView;
    private AutoCompleteTextView autoCompleteTextView;
    private TextView yourRank;
    private QRCode usersTopCodeRegional;

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
        prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
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
                                    if (!user.getDisplayName().equals(prefs.getString("currentUserDisplayName", null))) {
                                        autoCompleteTextView.setText("");
                                        FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                                        trans.replace(R.id.main_screen, new ProfileFragment(db, user.getDisplayName(), user.getUsername()));
                                        trans.commit();
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
        Spinner leaderboardFilterSpinner = view.findViewById(R.id.leaderboard_filter_spinner);
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

                filterQuery(leaderboardFilterChoice, new QueryCallback() {
                    public void queryCompleteCheck(boolean queryComplete) {
                        assert (queryComplete);
                        leaderboardRecyclerView = view.findViewById(R.id.leaderboard_recyclerview);

                        leaderboardAdapter = new LeaderboardProfileAdapter(leaderboardOptions, leaderboardFilterChoice);

                        //super.onStart(); man idk
                        leaderboardAdapter.startListening();
                        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

                        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                        // If user selects regional QR filter, prompt them to search for a region
                        if (leaderboardFilterChoice.equals("Top QR Code (Regional)")) {

                            Places.initialize(getActivity().getApplicationContext(), getResources().getString(R.string.google_map_api_key));
                            List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES);

                            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                                    .setHint("Search for a region")
                                    .setTypeFilter(TypeFilter.REGIONS)
                                    .build(getActivity().getApplicationContext());
                            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                        }

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
                String leaderboardText = "Leaderboard (" + placeName + ")";
                leaderboardTextView.setText(leaderboardText);

                String placeType = place.getTypes().get(0).toString();
                Log.i("TAG", "Place: " + placeName + ", " + place.getLatLng() + ", " + placeType);

                if (placeName != null) {
                    filterQueryRegional(placeName, placeType, new QueryCallback() {
                        @Override
                        public void queryCompleteCheck(boolean queryComplete) {
                            System.out.println(queryComplete);
                        }
                    });
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void findQRCodeNearby(double latitude, double longitude, double radius) {
        // Define the bounds of the query
        double lowerLat = latitude - (radius / 111.0);
        double lowerLon = longitude - (radius / (111.0 * Math.cos(latitude)));
        double upperLat = latitude + (radius / 111.0);
        double upperLon = longitude + (radius / (111.0 * Math.cos(latitude)));

        // Query the Firestore database for QR codes within the bounds of latitude
        Query latQuery = qrCodesReference.whereGreaterThanOrEqualTo("latitude", lowerLat)
                .whereLessThanOrEqualTo("latitude", upperLat);

        // Query the Firestore database for QR codes within the bounds of longitude
        Query lonQuery = qrCodesReference.whereGreaterThanOrEqualTo("longitude", lowerLon)
                .whereLessThanOrEqualTo("longitude", upperLon);

        // Combine the results of the two queries
        Task<List<QuerySnapshot>> combinedResults = Tasks.whenAllSuccess(latQuery.get(), lonQuery.get());

        // Process the combined results
        combinedResults.addOnSuccessListener(querySnapshotsList -> {

            Set<DocumentSnapshot> documents = new HashSet<>();

            for (QuerySnapshot snapshot : querySnapshotsList) {
                documents.addAll(snapshot.getDocuments());
            }

            for (DocumentSnapshot document : documents) {
                double documentLat = document.getDouble("latitude");
                double documentLon = document.getDouble("longitude");

                // Check if the QR code is within the radius
                if (Math.pow(documentLat - latitude, 2) + Math.pow(documentLon - longitude, 2) <= Math.pow(radius / 111.0, 2)) {
                    String documentId = document.getId();
                    Log.d(TAG, "Document ID: " + documentId);

                }
            }
        });
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
            case "Top QR Code (Regional)":
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
                        if (user.get("username").equals(prefs.getString("currentUserUsername", null))) {
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
     * @param placeName          Name of selected region in maps autocomplete search
     * @param placeType          Type of selected region in maps autocomplete search
     * @param queryCompleteCheck Callback for query
     */
    public void filterQueryRegional(@NonNull String placeName, @NonNull String placeType, final @NonNull QueryCallback queryCompleteCheck) {

        Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());

        // First, retrieve all users
        usersReference
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    for (QueryDocumentSnapshot userDocument : documentReferenceSnapshots) {
                        String username = userDocument.get("username").toString();

                        // The user's current top scoring code
                        usersTopCodeRegional = null;

                        // For each user, iterate through their collection of QR Codes
                        usersReference.document(username).collection("User QR Codes")
                                .get()
                                .addOnSuccessListener(userQRCodes -> {

                                    // Retrieve the location of each of the user's QR Codes
                                    for (QueryDocumentSnapshot reference : userQRCodes) {
                                        DocumentReference qrCodeReference = (DocumentReference) reference.get("Reference");

                                        qrCodeReference
                                                .get()
                                                .addOnSuccessListener(qrCode -> {
                                                    if ((qrCode.getDouble("latitude") != null) && (qrCode.getDouble("longitude") != null)) {
                                                        Double qrCodeLat = qrCode.getDouble("latitude");
                                                        Double qrCodeLong = qrCode.getDouble("longitude");

                                                        List<Address> addresses;

                                                        try {
                                                            // Get more data about the QR Code's location based on latitude and longitude
                                                            addresses = geocoder.getFromLocation(qrCodeLat, qrCodeLong, 1);
                                                        } catch (IOException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                        if (!addresses.isEmpty()) {
                                                            System.out.println(placeName + ", " + placeType);
                                                            String qrLocationName = null;

                                                            // Depending on the type of region selected, see if QR Code is in that region
                                                            switch (placeType) {
                                                                // Country
                                                                case "COUNTRY":
                                                                    qrLocationName = addresses.get(0).getCountryName();
                                                                    break;

                                                                // 1st order civil entity below country level, e.g. province/state
                                                                case "ADMINISTRATIVE_AREA_LEVEL_1":
                                                                    qrLocationName = addresses.get(0).getAdminArea();
                                                                    break;

                                                                // 2nd order civil entity below country level, e.g. county
                                                                case "ADMINISTRATIVE_AREA_LEVEL_2":
                                                                    qrLocationName = addresses.get(0).getSubAdminArea();
                                                                    break;

                                                                // City or town
                                                                case "LOCALITY":
                                                                    qrLocationName = addresses.get(0).getLocality();
                                                                    break;

                                                                // 1st order civil entity below locality, e.g. borough/neighborhood
                                                                case "SUBLOCALITY_LEVEL_1":
                                                                    qrLocationName = addresses.get(0).getSubLocality();
                                                                    break;

                                                                // Postal or zip code prefix
                                                                case "POSTAL_CODE_PREFIX":
                                                                    qrLocationName = addresses.get(0).getPostalCode();
                                                                    // Convert code to prefix (For most countries this is just the first three digits)
                                                                    qrLocationName = qrLocationName.substring(0, 3);
                                                                    break;

                                                                // Postal or zip code
                                                                case "POSTAL_CODE":
                                                                    qrLocationName = addresses.get(0).getPostalCode();
                                                                    break;
                                                            }

                                                            // If QR Code is in the chosen region, compare its points value and keep it if it is higher
                                                            if (qrLocationName != null && usersTopCodeRegional != null && qrLocationName.equals(placeName)) {
                                                                QRCode currentQRCode = qrCode.toObject(QRCode.class);
                                                                if (currentQRCode.getPoints() >= usersTopCodeRegional.getPoints()) {
                                                                    usersTopCodeRegional = currentQRCode;
                                                                    System.out.println(username + ", " + qrLocationName + ", " + qrCode.getId() + ", " + qrCode.get("points") + ", " + usersTopCodeRegional.getID());
                                                                }
                                                            } else if (qrLocationName != null && usersTopCodeRegional == null && qrLocationName.equals(placeName)) {
                                                                usersTopCodeRegional = qrCode.toObject(QRCode.class);
                                                                System.out.println(username + ", " + qrLocationName + ", " + qrCode.getId() + ", " + qrCode.get("points") + ", " + usersTopCodeRegional.getID());
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                    if (usersTopCodeRegional != null) {
                                        // This will contain users and their top scoring codes
                                        HashMap<User, QRCode> usersQRReferences = new HashMap<>();
                                        usersQRReferences.put(userDocument.toObject(User.class), usersTopCodeRegional);
                                    }
                                });
//                        Query query = db.collection("Regional Top QR").orderBy("queryField", Query.Direction.DESCENDING);
//                        leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
//                                .setQuery(query, User.class)
//                                .build();
                        queryCompleteCheck.queryCompleteCheck(true);
                    }
                });
    }
}