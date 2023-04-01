package com.example.qrhunterapp_t11.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.List;
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
    private AutoCompleteTextView autoCompleteTextView;
    private TextView yourRank;
    private static final int permissionsRequestLocation = 100;
    private double lat;
    private double lon;

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
            }

            // makes the delete button invisible if there is no input
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                deleteSearch.setVisibility(charSequence.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Finds the user after clicking enter
        autoCompleteTextView.setOnEditorActionListener((textView, i, keyEvent) -> {
            if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)) {
                autoCompleteTextView.dismissDropDown();
                String searchText = autoCompleteTextView.getText().toString().toLowerCase();

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

                                } else {  // if the user is not found
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
        Spinner leaderboardRadiusSpinner = view.findViewById(R.id.leaderboard_radius_spinner);

        String[] leaderboardFilterChoices = new String[]{"Most Points", "Most Scans", "Top QR Code", "Top QR Code (Regional)"};
        Spinner leaderboardFilterSpinner = view.findViewById(R.id.leaderboard_filter_spinner);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, leaderboardFilterChoices);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                switch(leaderboardFilterChoice) {
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

                        // If user selects regional QR filter, initialize radius spinner
                        if (leaderboardFilterChoice.equals("Top QR Code (Regional)")) {
                            // Set leaderboard radius spinner
                            String[] leaderboardRadiusChoices = new String[]{"5 km", "10 km", "25 km", "Custom radius"};
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

                                    // If custom radius is selected, prompt for choice
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
                                    } else {
                                        leaderboardRadiusChoices[3] = "Custom radius";
                                    }
                                }
                            });
                        } else {
                            leaderboardRadiusSpinner.setVisibility(View.GONE);

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

    public interface LocationCallback {
        void onLocationResult(double latitude, double longitude);
    }

    private void getCurrentLocation(LocationCallback callback) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        callback.onLocationResult(latitude, longitude);
                    } else {
                        // Location data is not available
                        Log.d(TAG, "ERROR Location data is not available.");
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, permissionsRequestLocation);
                    }
                }
            });
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case permissionsRequestLocation:
                boolean isFineLocationGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean isCoarseLocationGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (isFineLocationGranted) {
                    Log.d(TAG, "Execute if permission granted f.");
                    getCurrentLocation(new LocationCallback() {
                        @Override
                        public void onLocationResult(double latitude, double longitude) {
                            lat = latitude;
                            lon = longitude;
                            Log.d(TAG, "Latitude1 " + lat + ", Longitude1 " + lon);
                            findQRCodeNearby(lat, lon, 50);
                        }
                    });
                } else if (isCoarseLocationGranted) {
                    Log.d(TAG, "Execute if permission granted c.");
                    getCurrentLocation(new LocationCallback() {
                        @Override
                        public void onLocationResult(double latitude, double longitude) {
                            lat = latitude;
                            lon = longitude;
                            Log.d(TAG, "Latitude2 " + lat + ", Longitude2 " + lon);
                            findQRCodeNearby(lat, lon, 50);
                        }
                    });
                } else {
                    // Permission is not granted
                    Log.d(TAG, "Execute if permission not granted.");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
                queryField = "topQRCode";
                break;
            case "Top QR Code (Regional)":
                queryField = "topQRCode";
                if (hasLocationPermission()) {
                    getCurrentLocation(new LocationCallback() {
                        @Override
                        public void onLocationResult(double latitude, double longitude) {
                            lat = latitude;
                            lon = longitude;
                            Log.d(TAG, "Latitude3 " + lat + ", Longitude3 " + lon);
                            findQRCodeNearby(lat, lon, 50);
                        }
                    });
                } else {
                    Log.d(TAG, "ASKING FOR PERMISSION.");
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, permissionsRequestLocation);
                }

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
}