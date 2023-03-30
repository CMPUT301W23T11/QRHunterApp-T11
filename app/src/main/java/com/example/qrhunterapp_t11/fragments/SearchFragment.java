package com.example.qrhunterapp_t11.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.adapters.LeaderboardProfileAdapter;
import com.example.qrhunterapp_t11.interfaces.OnItemClickListener;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * Handles search and leaderboard screen.
 * Leaderboard displays ranked list of players, clicking on a player shows their profile
 * Search allows the user to search for other users to view their profiles
 *
 * @author Afra, Kristina
 * @reference <a href="https://stackoverflow.com/a/5241720">For setting spinner</a>
 */
public class SearchFragment extends Fragment {

    private final String tag = "searchFragment";
    private final FirebaseFirestore db;
    private final CollectionReference usersReference;
    private final CollectionReference qrCodeReference;
    private LeaderboardProfileAdapter leaderboardAdapter;
    private RecyclerView leaderboardRecyclerView;
    private FirestoreRecyclerOptions<User> leaderboardOptions;
    private SharedPreferences prefs;
    private AutoCompleteTextView autoCompleteTextView;
    private static final int permissionsRequestLocation = 100;
    private double lat;
    private double lon;

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
        // Set leaderboard options spinner
        String[] leaderboardFilterChoices = new String[]{"Most Points", "Most Scans", "Top QR Code", "Top QR Code (Regional)"};
        Spinner leaderboardFilter = view.findViewById(R.id.leaderboard_filter_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, leaderboardFilterChoices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaderboardFilter.setAdapter(adapter);

        Button deleteSearch = view.findViewById(R.id.close_id);
        autoCompleteTextView = view.findViewById(R.id.search_id);
        final ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);

        // populates the autocomplete list with users display names
        // Todo: edit set displayName to lowercase when adding to database to get case insensitivity
        usersReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable QuerySnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(tag, "Listen failed: ", error);
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
        // https://stackoverflow.com/questions/41670850/prevent-user-to-go-next-line-by-pressing-softkey-enter-in-autocompletetextview
        // - how to handle a ENTER click action
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
                                        Log.d(tag, "Document NOT found");
                                    }
                                } else {
                                    Log.d(tag, "task not successful: ", task.getException());
                                }
                            });
                }
                return false;
            }
        });

        // clears the text from autoCompleteTextView
        deleteSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoCompleteTextView.setText("", false);
            }
        });

        // Set Firestore RecyclerView query and begin monitoring that query
        leaderboardFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // A spinner option will always be selected
            }

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String leaderboardSpinnerChoice = leaderboardFilter.getSelectedItem().toString();
                filterQuery(leaderboardSpinnerChoice, new LeaderboardCallback() {
                    public void queryCallback(boolean queryComplete) {
                        assert (queryComplete);
                        leaderboardRecyclerView = view.findViewById(R.id.leaderboard_recyclerview);

                        prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                        leaderboardAdapter = new LeaderboardProfileAdapter(leaderboardOptions, leaderboardSpinnerChoice, prefs);

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
                        Log.d(tag, "ERROR Location data is not available.");
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
                    Log.d(tag, "Execute if permission granted f.");
                    getCurrentLocation(new LocationCallback() {
                        @Override
                        public void onLocationResult(double latitude, double longitude) {
                            lat = latitude;
                            lon = longitude;
                            Log.d(tag, "Latitude1 " + lat + ", Longitude1 " + lon);
                            findQRCodeNearby(latitude, longitude,50);
                        }
                    });
                } else if (isCoarseLocationGranted) {
                    Log.d(tag, "Execute if permission granted c.");
                    getCurrentLocation(new LocationCallback() {
                        @Override
                        public void onLocationResult(double latitude, double longitude) {
                            lat = latitude;
                            lon = longitude;
                            Log.d(tag, "Latitude2 " + lat + ", Longitude2 " + lon);
                            findQRCodeNearby(latitude, longitude,50);
                        }
                    });
                } else {
                    // Permission is not granted
                    Log.d(tag, "Execute if permission not granted.");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void findQRCodeNearby(double latitude, double longitude, double radius) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference qrCodesRef = db.collection("QRCodes");

        // Define the bounds of the query
        double lowerLat = latitude - (radius / 111.0);
        double lowerLon = longitude - (radius / (111.0 * Math.cos(latitude)));
        double upperLat = latitude + (radius / 111.0);
        double upperLon = longitude + (radius / (111.0 * Math.cos(latitude)));

        // Query the Firestore database for QR codes within the bounds
        qrCodesRef.whereGreaterThanOrEqualTo("latitude", String.valueOf(lowerLat))
                .whereLessThanOrEqualTo("latitude", String.valueOf(upperLat))
                .whereGreaterThanOrEqualTo("longitude", String.valueOf(lowerLon))
                .whereLessThanOrEqualTo("longitude", String.valueOf(upperLon))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String documentId = document.getId();
                                Log.d(tag, "Document ID: " + documentId);
                            }
                        } else {
                            Log.d(tag, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /**
     * Set query for Firestore RecyclerView depending on what spinner option is selected
     *
     * @param filterType    String representing how the leaderboard is filtered
     * @param queryCallback Callback for query
     */
    public void filterQuery(@NonNull String filterType, final @NonNull LeaderboardCallback queryCallback) {
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
                if (hasLocationPermission()) {
                    getCurrentLocation(new LocationCallback() {
                        @Override
                        public void onLocationResult(double latitude, double longitude) {
                            lat = latitude;
                            lon = longitude;
                            Log.d(tag, "Latitude3 " + lat + ", Longitude3 " + lon);
                            findQRCodeNearby(latitude, longitude,50);
                        }
                    });
                } else {
                    Log.d(tag, "ASKING FOR PERMISSION.");
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, permissionsRequestLocation);
                }

                break;
        }
//        Query query = usersReference.orderBy(queryField, Query.Direction.DESCENDING);
//        query
//                .get()
//                .addOnSuccessListener(documentReferenceSnapshots -> {
//                    leaderboardOptions = new FirestoreRecyclerOptions.Builder<User>()
//                            .setQuery(query, User.class)
//                            .build();
//                    queryCallback.queryCallback(true);
//                });
    }

    /**
     * Callback for querying the database to get type of results
     *
     * @author Afra
     */
    public interface LeaderboardCallback {
        void queryCallback(boolean queryComplete);
    }
}