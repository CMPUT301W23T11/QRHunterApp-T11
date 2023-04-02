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
import com.example.qrhunterapp_t11.objectclasses.Preference;
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
 * Displays the user's QR collection and stats
 *
 * @author Afra, Kristina
 * @sources <pre>
 * <ul>
 * <li><a href="https://firebase.google.com/docs/firestore/query-data/listen">How to get a new snapshot everytime the data is updated</a></li>
 * <li><a href="https://stackoverflow.com/questions/74092262/calculate-total-from-values-stored-in-firebase-firestore-database-android">How to calculate the sum of a set of documents</a></li>
 * <li><a href="https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/">Firestore documentation for RecyclerView</a></li>
 * </ul>
 * </pre>
 */
public class ProfileFragment extends Fragment {
    private final CollectionReference usersReference;
    private final CollectionReference qrCodeReference;
    private final FirebaseFirestore db;
    private QRCodeAdapter adapter;
    private RecyclerView qrCodeRecyclerView;
    private FirestoreRecyclerOptions<QRCode> options;
    private final String username;
    private final String displayName;
    private FirebaseQueryAssistant firebaseQueryAssistant;
    //private SharedPreferences prefs;

    /**
     * Constructor for profile fragment.
     *
     * @param db Firestore database instance
     */
    public ProfileFragment(@NonNull FirebaseFirestore db, @NonNull String username, @NonNull String displayName) {
        this.db = db;
        this.firebaseQueryAssistant = new FirebaseQueryAssistant(db);
        this.usersReference = db.collection("Users");
        this.qrCodeReference = db.collection("QRCodes");
        this.username = username;
        this.displayName = displayName;
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

        //prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        TextView displayNameTextView = view.findViewById(R.id.profile_name);
        displayNameTextView.setText(displayName);

        Button backButton = view.findViewById(R.id.profile_back_button);

        // Makes a back button visible if not the current user
        if (!currentUserCheck(username)) {
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
        firebaseQueryAssistant.hasQRCodesCheck(username, new QueryCallback() {
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

                            updateScores(view, userData);

                            // Handles clicking on an item to view the QR Code
                            adapter.setOnItemClickListener(new OnItemClickListener() {
                                @Override
                                public void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position) {

                                    QRCode qrCode = documentSnapshot.toObject(QRCode.class);
                                    assert qrCode != null;
                                    QRCodeView viewQR = new QRCodeView(qrCode, adapter);
                                    viewQR.show(getActivity().getSupportFragmentManager(), "Show QR");
                                }
                            });

                            // Handles long clicking on an item for deletion
                            adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                                @Override
                                public void onItemLongClick(@NonNull DocumentSnapshot documentSnapshot, int position) {
                                    // Only operable if the profile is the current user
                                    if (currentUserCheck(username)) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                        String qrCodeID = documentSnapshot.getId();

                                        builder
                                                .setTitle("Delete QR Code?")
                                                .setNegativeButton("Cancel", null)
                                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        firebaseQueryAssistant.deleteQR(username, qrCodeID, new QueryCallback() {
                                                            @Override
                                                            public void queryCompleteCheck(boolean deleted) {
                                                                assert deleted;
                                                                FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                                                                trans.replace(R.id.main_screen, new ProfileFragment(db, username, displayName));
                                                                trans.commit();
                                                            }
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
     * Query database to retrieve referenced QR Codes in the user's collection, then
     * use those DocumentReferences to retrieve data from the referenced QR codes
     *
     * @param username    User's username
     * @param getUserData Callback for query
     * @sources Firestore documentation
     */
    public void queryQRCodes(@NonNull String username, final @NonNull ProfileFragment.ProfileUserDataCallback getUserData) {


        ArrayList<DocumentReference> userQRCodesRef = new ArrayList<>();
        ArrayList<String> userPoints = new ArrayList<>();

        // Retrieve DocumentReferences in the user's QR code collection and store them in an array
        usersReference.document(username).collection("User QR Codes")
                .get()
                .addOnSuccessListener(documentReferences -> {
                    for (QueryDocumentSnapshot qrReference : documentReferences) {

                        DocumentReference documentReference = (DocumentReference) qrReference.get("Reference");
                        userQRCodesRef.add(documentReference);
                    }

                    // Retrieve QR Code data from the QRCodes collection using DocumentReferences
                    Query query = qrCodeReference.whereIn(FieldPath.documentId(), userQRCodesRef);
                    query
                            .get()
                            .addOnSuccessListener(referencedQRDocuments -> {
                                for (QueryDocumentSnapshot referencedQR : referencedQRDocuments) {
                                    String qrCodePoints = referencedQR.get("points").toString();
                                    options = new FirestoreRecyclerOptions.Builder<QRCode>()
                                            .setQuery(query, QRCode.class)
                                            .build();
                                    userPoints.add(qrCodePoints);
                                    Collections.sort(userPoints);
                                    getUserData.getUserData(userPoints);
                                }
                            });
                });
    }

    /**
     * Updates the current user's scores
     *
     * @param userData Array containing user's scores
     */
    public void updateScores(@NonNull View view, @NonNull ArrayList<String> userData) {

        TextView totalScoreText = view.findViewById(R.id.totalScoreText);
        TextView topQRCodeText = view.findViewById(R.id.topQRText);
        TextView lowQRCodeText = view.findViewById(R.id.lowQRText);
        TextView totalQRCodesText = view.findViewById(R.id.totalQRText);

        // Gets the total score of the user
        int total = 0;
        for (String qr : userData) {
            total += Integer.parseInt(qr);
        }
        // Updates the user's total score in the database
        totalScoreText.setText(MessageFormat.format("Total score: {0}", total));

        // Gets the largest QR from the user
        topQRCodeText.setText(MessageFormat.format("Top QR Code: {0}", userData.get(userData.size() - 1)));
        usersReference.document(username).update("topQRCode", Integer.parseInt(userData.get(userData.size() - 1)));

        // Gets the smallest QR from the user
        lowQRCodeText.setText(MessageFormat.format("Lowest QR Code: {0}", userData.get(0)));

        // Gets the number of QR codes the user has
        totalQRCodesText.setText(MessageFormat.format("Number of QR Codes: {0}", userData.size()));
    }

    /**
     * Helper class to check if the current profile is the current user
     *
     * @param username The current profile's username
     * @return A boolean representing if the profile is the current user
     */
    public boolean currentUserCheck(@NonNull String username) {
        return (username.equals(Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null)));
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