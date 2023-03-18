package com.example.qrhunterapp_t11;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.min;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Handles player profile screen.
 * Outputs the users' QR collection and the users' stats
 *
 * @author Afra, Kristina, Sarah
 * @reference <a href="https://stackoverflow.com/questions/74092262/calculate-total-from-values-stored-in-firebase-firestore-database-android">How to calculate the sum of a set of documents</a>
 * @reference <a href="https://firebase.google.com/docs/firestore/query-data/listen">How to get a new snapshot everytime the data is updated</a>
 * @reference <a href="https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/">Firestore documentation for RecyclerView</a>
 */
public class ProfileFragment extends Fragment {
    private static final String tag = "ProfileFragment";
    private static final String listenFailed = "listenFailed";
    private final CollectionReference usersReference;
    private final CollectionReference QRCodeReference;
    private QRAdapterClass adapter;
    private RecyclerView QRCodeRecyclerView;
    private FirestoreRecyclerOptions<QRCode> options;
    private boolean userHasNoCodes;
    private final String displayName;
    private final String username;

    /**
     * Constructor for profile fragment.
     * Also instantiates a reference to the Users collection for ease of access.
     *
     * @param db Firestore database instance
     */
    public ProfileFragment(@NonNull FirebaseFirestore db, @NonNull String displayName, @NonNull String username) {
        this.usersReference = db.collection("Users");
        this.QRCodeReference = db.collection("QRCodes");
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

        TextView loginUsernameTextView = view.findViewById(R.id.profile_name);

        loginUsernameTextView.setText(displayName);

        TextView totalScoreText = view.findViewById(R.id.totalScoreText);
        TextView topQRCodeText = view.findViewById(R.id.topQRText);
        TextView lowQRCodeText = view.findViewById(R.id.lowQRText);
        TextView totalQRCodesText = view.findViewById(R.id.totalQRText);

        // If the user has at least one QR code, initialize RecyclerView
        noQRCodesCheck(username, new ProfileNoCodesCallback() {
            public void noCodes(boolean noCodes) {
                userHasNoCodes = noCodes;

                if (!userHasNoCodes) {

                    // Retrieve all QR Codes the user has and calculate scores
                    queryQRCodes(username, new ProfileQRCodeCallback() {
                        public void getReferencedQRCodes(@NonNull Map<String, String> referencedQRCodes) {

                            CollectionReference QRColl = usersReference.document(username).collection("User QR Codes");

                            QRCodeRecyclerView = view.findViewById(R.id.collectionRecyclerView);

                            adapter = new QRAdapterClass(options);

                            //super.onStart(); man idk
                            adapter.startListening();
                            QRCodeRecyclerView.setAdapter(adapter);
                            QRCodeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            QRCodeRecyclerView.setHasFixedSize(false);

                            // TODO: Reimplement scores using referencedQRCodes and set Total Points field in user's document
                            //  referencedQRCodes is just a map with points, see line 272

                            // gets the total score of the user
                            int total = 0;
                            for (String qr: referencedQRCodes.values()) {
                                total += Integer.parseInt(qr);
                            }
                            totalScoreText.setText(MessageFormat.format("Total score: {0}", (int) total));

                            // gets the largest QR from the user
                            Map.Entry<String, String> maxQR = null;
                            for (Map.Entry<String, String> val : referencedQRCodes.entrySet()) {
                                if (maxQR == null || val.getValue().compareTo(maxQR.getValue()) > 0){
                                    maxQR = val;
                                }
                            }
                            assert maxQR != null;
                            topQRCodeText.setText(MessageFormat.format("Your top QR Code: {0}", maxQR.getValue()));

                            // gets the smallest QR from the user
                            Map.Entry<String, String> minQR = null;
                            for (Map.Entry<String, String> val : referencedQRCodes.entrySet()) {
                                if (minQR == null || minQR.getValue().compareTo(val.getValue()) > 0){
                                    minQR = val;
                                }
                            }
                            assert minQR != null;
                            lowQRCodeText.setText(MessageFormat.format("Your lowest QR Code: {0}", minQR.getValue()));

                            // Gets the size of the amount of QR codes the user has
                            int size = referencedQRCodes.size();
                            totalQRCodesText.setText(MessageFormat.format("Total number of QR codes: {0}", size));

                            // Handles clicking on an item to view the QR Code
                            adapter.setOnItemClickListener(new OnItemClickListener() {
                                @Override
                                public void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position) {

                                    QRCode qrCode = documentSnapshot.toObject(QRCode.class);
                                    new ViewQR(qrCode).show(getActivity().getSupportFragmentManager(), "Show QR");
                                    //adapter.notifyDataSetChanged();
                                }
                            });

                            // Handles long clicking on an item for deletion
                            adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                                @Override
                                public void onItemLongClick(@NonNull DocumentSnapshot documentSnapshot, int position) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                    String documentId = documentSnapshot.getId();

                                    builder
                                            .setTitle("Delete QR Code?")
                                            .setNegativeButton("Cancel", null)
                                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    usersReference.document(username).collection("User QR Codes").document(documentId).delete();
                                                }
                                            })
                                            .create();

                                    builder.show();
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
     * Query database to check if user has any QR codes in their collection or not
     *
     * @param username Current user's username
     * @param noCodes  Callback function
     */
    public void noQRCodesCheck(@NonNull String username, final @NonNull ProfileNoCodesCallback noCodes) {

        usersReference.document(username).collection("User QR Codes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            noCodes.noCodes(task.getResult().size() == 0);
                        }
                    }
                });
    }

    /**
     * Query database to retrieve referenced QR Codes in the user's collection, then
     * use those DocumentReferences to retrieve data from the referenced QR codes
     *
     * @param username             User's username
     * @param getReferencedQRCodes Callback for query
     * @reference Firestore documentation
     */
    public void queryQRCodes(@NonNull String username, final @NonNull ProfileQRCodeCallback getReferencedQRCodes) {

        ArrayList<DocumentReference> userQRCodesRef = new ArrayList<>();
        Map<String, String> referencedQRCodes = new HashMap<>();

        // Retrieve DocumentReferences in the user's QR code collection and store them in an array
        usersReference.document(username).collection("User QR Codes")
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    for (QueryDocumentSnapshot snapshot : documentReferenceSnapshots) {

                        DocumentReference documentReference = snapshot.getDocumentReference(snapshot.getId());
                        userQRCodesRef.add(documentReference);
                    }

                    // Retrieve QR Code data from the QRCodes collection using DocumentReferences
                    Query query = QRCodeReference.whereIn(FieldPath.documentId(), userQRCodesRef);
                    query
                            .get()
                            .addOnSuccessListener(referencedQRDocumentSnapshots -> {
                                for (QueryDocumentSnapshot snapshot : referencedQRDocumentSnapshots) {
                                    long QRCodePointsLong = snapshot.getLong("points");
                                    String QRCodePoints = String.valueOf(QRCodePointsLong);
                                    referencedQRCodes.put("points", QRCodePoints);
                                    options = new FirestoreRecyclerOptions.Builder<QRCode>()
                                            .setQuery(query, QRCode.class)
                                            .build();
                                    getReferencedQRCodes.getReferencedQRCodes(referencedQRCodes);
                                }
                            });
                });

    }

    /**
     * Callback for querying the database to see if user has codes
     *
     * @author Afra
     */
    public interface ProfileNoCodesCallback {
        void noCodes(boolean noCodes);
    }

    /**
     * Callback for querying the database to get QR object.
     * referencedQRCodes is a map containing data for each
     * QR code in the user's collection
     *
     * @author Afra
     */
    public interface ProfileQRCodeCallback {
        void getReferencedQRCodes(@NonNull Map<String, String> referencedQRCodes);
    }
}