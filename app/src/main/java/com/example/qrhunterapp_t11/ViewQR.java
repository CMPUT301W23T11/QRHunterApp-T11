package com.example.qrhunterapp_t11;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the dialog fragment that appears when a user clicks to see more info about a certain QRCode. It shows the user the QR Code's image, name,
 * # of points, photo, and implements comments.
 *
 * @author Sarah Thomson
 * @reference <a href="https://stackoverflow.com/questions/6210895/listview-inside-scrollview-is-not-scrolling-on-android/17503823#17503823">by Moisés Olmedo for scrollable comment box</a>
 * @reference <a href="https://www.youtube.com/watch?v=LMdxZ8UC00k">by Technical Skillz for the Comment box/comment layout in the qr_view layout, and the comment_box drawable</a>
 * @reference <a href="https://icon-icons.com/icon/send-button/72565">by Icons.com for the send button</a>
 * @reference <a href="https://cloud.google.com/firestore/docs/manage-data/add-data">for adding comment to db using arrayUnion</a>
 * @references <a href="https://www.svgbackgrounds.com/license/">for liquid_cheese_background</a>
 */
public class ViewQR extends DialogFragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference QRCodesReference = db.collection("QRCodes");
    private final CollectionReference usersReference = db.collection("Users");
    private QRCode qrCode;
    private ArrayList<Comment> commentList;
    private CommentAdapter commentAdapter;
    private EditText commentET;
    private SharedPreferences prefs;
    private String QRCodeHash;
    private String QRCodeId;
    private TextView commentNumTV;

    /**
     * Empty constructor
     */
    public ViewQR() {
        super();
    }

    /**
     * Constructor used when an item in the recyclerview is clicked in the profile
     *
     * @param qrCode - qrCode object that was clicked
     */
    public ViewQR(@NonNull QRCode qrCode) {
        super();
        this.qrCode = qrCode;
        this.QRCodeHash = qrCode.getHash();
        this.QRCodeId = qrCode.getId();
    }

    /**
     * Called when dialog is first attached to context
     *
     * @param context - Context should be instance of ViewQR.ViewQRDialogListener
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ViewQR.ViewQRDialogListener) {
            ViewQRDialogListener listener = (ViewQRDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement DialogListener");
        }
    }

    /**
     * Called when dialog is created
     *
     * @param savedInstanceState - The last saved instance state of the Fragment, or null if this is a freshly created Fragment.
     * @return - builder
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.qr_view, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        commentNumTV = view.findViewById(R.id.commentsNumTv);
        commentET = view.findViewById(R.id.editTextComment);
        ImageView commentIV = view.findViewById(R.id.imageViewSend);
        ListView commentListView = view.findViewById(R.id.commentListView);
        TextView pointsTV = view.findViewById(R.id.pointsTV);
        TextView scansTV = view.findViewById(R.id.scansTV);
        ImageView eyesImageView = view.findViewById(R.id.imageEyes);
        ImageView colourImageView = view.findViewById(R.id.imageColour);
        ImageView faceImageView = view.findViewById(R.id.imageFace);
        ImageView noseImageView = view.findViewById(R.id.imageNose);
        ImageView mouthImageView = view.findViewById(R.id.imageMouth);
        ImageView eyebrowsImageView = view.findViewById(R.id.imageEyebrows);
        ImageView photoImageView = view.findViewById(R.id.imagePhoto);

        String points = "Points: " + qrCode.getPoints();
        pointsTV.setText(points);
        String scans = "Total Scans: " + qrCode.getNumberOfScans();
        scansTV.setText(scans);

        // Creates the appearance of the qrCode based on the drawable ids stored in its faceList array
        colourImageView.setImageResource((qrCode.getFaceList()).get(2));
        eyesImageView.setImageResource((qrCode.getFaceList()).get(0));
        faceImageView.setImageResource((qrCode.getFaceList()).get(1));
        noseImageView.setImageResource((qrCode.getFaceList()).get(3));
        mouthImageView.setImageResource((qrCode.getFaceList()).get(4));
        eyebrowsImageView.setImageResource((qrCode.getFaceList()).get(5));

        // If the QRCode has an associated photo, use Picasso to load it into the photoImageView (Rotated for some reason)
        if ((!qrCode.getPhotoList().isEmpty()) && (qrCode.getPhotoList().get(0) != null)) {
            Picasso.with(getContext()).load(qrCode.getPhotoList().get(0)).into(photoImageView);
        }

        noCommentsCheck(QRCodeId, new QRCodeNoCommentsCallback() {
            public void noComments(boolean noComments) {
                if (!noComments) {
                    getComments(QRCodeId, new QRCodeCommentsCallback() {
                        public void comments(@NonNull ArrayList<ArrayList<Map<String, Object>>> comments) {

                            commentList = new ArrayList<>();
                            for (int i = 0; i < comments.size(); i++) {
                                Map<String, Object> commentMap;
                                commentMap = comments.get(i).get(0);
                                String username = (String) commentMap.get("username");
                                String displayName = (String) commentMap.get("displayName");
                                String commentString = (String) commentMap.get("comment");

                                assert commentString != null;
                                assert displayName != null;
                                assert username != null;
                                Comment comment = new Comment(commentString, displayName, username);
                                commentList.add(comment);
                            }
                            commentAdapter = new CommentAdapter(getContext(), commentList);
                            commentListView.setAdapter(commentAdapter);
                            CollectionReference query = db.collection("QRCodes").document(qrCode.getId()).collection("commentList");
                            AggregateQuery countQuery = query.count();
                            countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        // Count fetched successfully
                                        AggregateQuerySnapshot snapshot = task.getResult();
                                        String comments = String.valueOf(snapshot.getCount());
                                        commentNumTV.setText(comments);
                                        Log.d("Comments", "Count: " + snapshot.getCount());
                                    } else {
                                        Log.d("Comments", "Count failed: ", task.getException());
                                    }
                                }
                            });

                        }
                    });
                } else {
                    commentList = new ArrayList<>();
                    commentAdapter = new CommentAdapter(getContext(), commentList);
                    commentListView.setAdapter(commentAdapter);
                    commentNumTV.setText("0");

                }
            }
        });

        // When the send image arrow ImageView is clicked, if a comment has been made it will be added
        // to the QRCode object's saved array of comments and appear in the comment box with the associated user
        // User can only comment on QR Codes if they already have that code in their collection
        commentIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentString = commentET.getText().toString();

                if (!commentString.isEmpty()) {

                    String currentUserDisplayName = prefs.getString("currentUserDisplayName", null);
                    String currentUser = prefs.getString("currentUser", null);
                    Comment c = new Comment(commentString, currentUserDisplayName, currentUser);
                    Integer commentNum = 0;

                    commentAdapter.setCommentList(c);
                    commentAdapter.notifyDataSetChanged();
                    commentET.getText().clear();

                    Map<String, String> comment = new HashMap<>();
                    comment.put("username", currentUser);
                    comment.put("displayName", currentUserDisplayName);
                    comment.put("comment", commentString);

                    Map<String, CollectionReference> QRCodeRef = new HashMap<>();
                    CollectionReference QRCodeCollectionRef = QRCodesReference.document(QRCodeId).collection("commentList");
                    QRCodeRef.put(QRCodeId, QRCodeCollectionRef);

                    QRCodesReference.document(QRCodeId).collection("commentList").add(comment);
                    commentNum = Integer.parseInt(commentNumTV.getText().toString());
                    commentNum++;
                    commentNumTV.setText(commentNum.toString());

                }
            }
        });

        //This OnTouchListener code block came from Moisés Olmedo to make a list view be scrollable within a scrollview.
        //Link: https://stackoverflow.com/questions/6210895/listview-inside-scrollview-is-not-scrolling-on-android/17503823#17503823
        //License: CC BY-SA 3.0
        commentListView.setOnTouchListener(new ListView.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                v.onTouchEvent(event);
                return true;
            }
        });

        return builder
                .setView(view)
                .setTitle(qrCode.getName())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
    }

    /**
     * Query database to check if QR code has any comments or not
     *
     * @param QRCodeId   QR to check for comments
     * @param noComments Callback function
     */
    public void noCommentsCheck(@NonNull String QRCodeId, final @NonNull QRCodeNoCommentsCallback noComments) {

        QRCodesReference.document(QRCodeId).collection("commentList")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            noComments.noComments(task.getResult().size() == 0);
                        }
                    }
                });
    }

    public void getComments(@NonNull String QRCodeId, final @NonNull QRCodeCommentsCallback comments) {

        ArrayList<ArrayList<Map<String, Object>>> commentsTemp = new ArrayList<>();

        // Retrieve DocumentReferences in the user's QR code collection and store them in an array
        QRCodesReference.document(QRCodeId).collection("commentList")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot document : snapshot) {
                        ArrayList<Map<String, Object>> comment = new ArrayList<>();
                        comment.add(document.getData());
                        commentsTemp.add(comment);
                    }
                    comments.comments(commentsTemp);
                });
    }

    /**
     * Listener for the ViewQR dialog
     */
    interface ViewQRDialogListener {
        void viewCode(QRCode qrCode);
    }

    /**
     * Callback for querying the database to see if QR code has comments
     *
     * @author Afra
     */
    public interface QRCodeNoCommentsCallback {
        void noComments(boolean noComments);
    }

    /**
     * Callback for querying the database to retrieve comments
     *
     * @author Afra
     */
    public interface QRCodeCommentsCallback {
        void comments(@NonNull ArrayList<ArrayList<Map<String, Object>>> comments);
    }
}