package com.example.qrhunterapp_t11.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.adapters.QRCodeAdapter;
import com.example.qrhunterapp_t11.interfaces.OnItemClickListener;
import com.example.qrhunterapp_t11.interfaces.OnItemLongClickListener;
import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Handles player profile screen.
 * Outputs the users' QR collection and the users' stats
 *
 * @author Afra, Kristina
 * @reference <a href="https://stackoverflow.com/questions/74092262/calculate-total-from-values-stored-in-firebase-firestore-database-android">How to calculate the sum of a set of documents</a>
 * @reference <a href="https://firebase.google.com/docs/firestore/query-data/listen">How to get a new snapshot everytime the data is updated</a>
 * @reference <a href="https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/">Firestore documentation for RecyclerView</a>
 */
public class ProfileFragment extends Fragment {
    private final CollectionReference usersReference;
    private final CollectionReference qrCodeReference;
    private final FirebaseFirestore db;
    private QRCodeAdapter adapter;
    private RecyclerView qrCodeRecyclerView;
    private FirestoreRecyclerOptions<QRCode> options;
    private final String displayName;
    private final String username;
    private SharedPreferences prefs;

    /**
     * Constructor for profile fragment.
     * Also instantiates a reference to the Users collection for ease of access.
     *
     * @param db Firestore database instance
     */
    public ProfileFragment(@NonNull FirebaseFirestore db, @NonNull String displayName, @NonNull String username) {
        this.db = db;
        this.usersReference = db.collection("Users");
        this.qrCodeReference = db.collection("QRCodes");
        this.displayName = displayName;
        this.username = username;
    }

    /**
     * Inflates the layout for the camera fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return a View containing the inflated layout.
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        Button backButton = view.findViewById(R.id.profile_back_button);

        TextView loginUsernameTextView = view.findViewById(R.id.profile_name);

        loginUsernameTextView.setText(displayName);

        TextView totalScoreText = view.findViewById(R.id.totalScoreText);
        TextView topQRCodeText = view.findViewById(R.id.topQRText);
        TextView lowQRCodeText = view.findViewById(R.id.lowQRText);
        TextView totalQRCodesText = view.findViewById(R.id.totalQRText);

        // Makes a back button visible if not the current user
        if (!CurrentUser(displayName, username, prefs)) {
            backButton.setVisibility(View.VISIBLE);

            // Takes the user back to the leaderboard screen
            backButton.setOnClickListener(view1 -> {
                FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                trans.replace(R.id.main_screen, new SearchFragment(db));
                trans.commit();
            });
        } else {
            backButton.setVisibility(View.INVISIBLE);
        }

        // If the user has at least one QR code, initialize RecyclerView
        hasQRCodesCheck(username, new QueryCallback() {
            public void queryCompleteCheck(boolean hasCodes) {
                if (hasCodes) {

                    // Retrieve all QR Codes the user has and calculate scores
                    queryQRCodes(username, new ProfileUserDataCallback() {
                        public void getUserData(@NonNull ArrayList<String> userData) {

                            qrCodeRecyclerView = view.findViewById(R.id.collectionRecyclerView);

                            adapter = new QRCodeAdapter(options, db);

                            //super.onStart(); man idk
                            adapter.startListening();
                            qrCodeRecyclerView.setAdapter(adapter);
                            qrCodeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            qrCodeRecyclerView.setHasFixedSize(false);

                            // TODO: get the fragment to auto refresh

                            // gets the total score of the user
                            int total = 0;
                            for (String qr : userData) {
                                total += Integer.parseInt(qr);
                            }
                            // Updates the user's total score in the database
                            totalScoreText.setText(MessageFormat.format("Total score: {0}", total));

                            // Gets the largest QR from the user
                            topQRCodeText.setText(MessageFormat.format("Your top QR Code: {0}", userData.get(userData.size() - 1)));
                            usersReference.document(username).update("topQRCode", Integer.parseInt(userData.get(userData.size() - 1)));

                            // Gets the smallest QR from the user
                            lowQRCodeText.setText(MessageFormat.format("Your lowest QR Code: {0}", userData.get(0)));

                            // Gets the number of QR codes the user has
                            totalQRCodesText.setText(MessageFormat.format("Number of QR Codes: {0}", userData.size()));


                            // Handles clicking on an item to view the QR Code
                            adapter.setOnItemClickListener(new OnItemClickListener() {
                                @Override
                                public void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position) {

                                    QRCode qrCode = documentSnapshot.toObject(QRCode.class);
                                    assert qrCode != null;
                                    new QRCodeView(qrCode).show(getActivity().getSupportFragmentManager(), "Show QR");
                                }
                            });

                            // Handles long clicking on an item for deletion
                            adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                                @Override
                                public void onItemLongClick(@NonNull DocumentSnapshot documentSnapshot, int position) {
                                    // only operable if the profile is the current user
                                    if (CurrentUser(displayName, username, prefs)) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                        String QRCodeID = documentSnapshot.getId();

                                        builder
                                                .setTitle("Delete QR Code?")
                                                .setNegativeButton("Cancel", null)
                                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        usersReference.document(username).collection("User QR Codes").document(QRCodeID)
                                                                .get()
                                                                .addOnSuccessListener(userQRSnapshot -> {
                                                                    DocumentReference documentReference = (DocumentReference) userQRSnapshot.get("Reference");
                                                                    assert documentReference != null;
                                                                    documentReference
                                                                            .get()
                                                                            .addOnSuccessListener(QRSnapshot -> {
                                                                                long points = QRSnapshot.getLong("points");
                                                                                points = -points;
                                                                                // Delete code from user's collection
                                                                                usersReference.document(username).collection("User QR Codes").document(QRCodeID).delete();
                                                                                // Subtract point value of that code from user's total points
                                                                                usersReference.document(username).update("totalPoints", FieldValue.increment(points));
                                                                                refreshProfile(); // reload profile fragment to update RecyclerView
                                                                            });
                                                                });
                                                    }
                                                })
                                                .create();
                                        builder.show();
                                    }
                                }

                            });
                        }
                    });
                } else {
                    usersReference.document(username).update("topQRCode", 0);
                }
            }
        });

        return view;
    }

    /**
     * Query database to check if user has any QR codes in their collection or not
     *
     * @param username Current user's username
     * @param hasCodes Callback function
     */
    public void hasQRCodesCheck(@NonNull String username, final @NonNull QueryCallback hasCodes) {

        usersReference.document(username).collection("User QR Codes")
                .get()
                .addOnSuccessListener(userQRCodes ->
                        hasCodes.queryCompleteCheck(!userQRCodes.isEmpty()));
    }

    /**
     * Query database to retrieve referenced QR Codes in the user's collection, then
     * use those DocumentReferences to retrieve data from the referenced QR codes
     *
     * @param username    User's username
     * @param getUserData Callback for query
     * @reference Firestore documentation
     */
    public void queryQRCodes(@NonNull String username, final @NonNull ProfileUserDataCallback getUserData) {

        ArrayList<DocumentReference> userQRCodesRef = new ArrayList<>();
        ArrayList<String> userPoints = new ArrayList<>();

        // Retrieve DocumentReferences in the user's QR code collection and store them in an array
        usersReference.document(username).collection("User QR Codes")
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    for (QueryDocumentSnapshot snapshot : documentReferenceSnapshots) {

                        DocumentReference documentReference = (DocumentReference) snapshot.get("Reference");
                        userQRCodesRef.add(documentReference);
                    }

                    // Retrieve QR Code data from the QRCodes collection using DocumentReferences
                    Query query = qrCodeReference.whereIn(FieldPath.documentId(), userQRCodesRef);
                    query
                            .get()
                            .addOnSuccessListener(referencedQRDocumentSnapshots -> {
                                for (QueryDocumentSnapshot snapshot : referencedQRDocumentSnapshots) {
                                    String QRCodePoints = snapshot.get("points").toString();
                                    options = new FirestoreRecyclerOptions.Builder<QRCode>()
                                            .setQuery(query, QRCode.class)
                                            .build();
                                    userPoints.add(QRCodePoints);

                                    Collections.sort(userPoints);
                                    getUserData.getUserData(userPoints);
                                }
                            });
                });
    }


    /**
     * Helper function to refresh profile screen once user has deleted a QR code; otherwise the user would have to toggle screens for the deleted QR Code to be removed.
     * There may have been a more elegant way to do this by re-querying the database, but this seems to work fine.
     */
    private void refreshProfile() {
        FragmentTransaction trans = getParentFragmentManager().beginTransaction();
        trans.replace(R.id.main_screen, new ProfileFragment(db, prefs.getString("currentUserDisplayName", null), prefs.getString("currentUserUsername", null)));
        trans.commit();
    }

    /**
     * Helper class to check if the current profile is the current user
     *
     * @param displayName The current profiles display name
     * @param username    The current profiles username
     * @param prefs       The Shared Preferences of the app
     * @return A boolean of if the profile is the current user
     */
    public boolean CurrentUser(@NonNull String displayName, @NonNull String username, @NonNull SharedPreferences prefs) {
        return displayName.equals(prefs.getString("currentUserDisplayName", null)) && (username.equals(prefs.getString("currentUserUsername", null)));
    }

    /**
     * Callback for querying the database.
     * userData is an array containing the user's points
     *
     * @author Afra
     */
    public interface ProfileUserDataCallback {
        void getUserData(@NonNull ArrayList<String> userData);
    }
}