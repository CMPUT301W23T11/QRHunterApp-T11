package com.example.qrhunterapp_t11.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.adapters.CommentAdapter;
import com.example.qrhunterapp_t11.objectclasses.Comment;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the dialog fragment that appears when a user clicks to see more info about a certain QRCode. It shows the user the QR Code's image, name,
 * # of points, photo, and implements comments.
 *
 * @author Afra Rahmanfard
 * @author Sarah Thomson
 * @sources: <pre>
 * <ul>
 * <li><a href="https://stackoverflow.com/a/17503823">For scrollable comment box</a></li>
 * <li><a href="https://www.youtube.com/watch?v=LMdxZ8UC00k">by Technical Skillz for the Comment box/comment layout in the qr_view layout, and the comment_box drawable</a></li>
 * <li><a href="https://icon-icons.com/icon/send-button/72565">by Icons.com for the send button</a></li>
 * <li><a href="https://cloud.google.com/firestore/docs/manage-data/add-data">for adding comment to db using arrayUnion</a></li>
 * <li><a href="https://www.svgbackgrounds.com/license/">for liquid_cheese_background</a></li>
 * </ul>
 * </pre>
 */
public class QRCodeView extends DialogFragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference qrCodesReference = db.collection("QRCodes");
    private final CollectionReference usersReference = db.collection("Users");
    private QRCode qrCode;
    private ArrayList<Comment> commentList;
    private CommentAdapter commentAdapter;
    private EditText commentEditText;
    private SharedPreferences prefs;
    private String qrCodeID;
    private TextView commentNumTextView;

    /**
     * Empty constructor
     */
    public QRCodeView() {
        super();
    }

    /**
     * Constructor used when an item in the recyclerview is clicked in the profile
     *
     * @param qrCode - QRCode object that was clicked
     */
    public QRCodeView(@NonNull QRCode qrCode) {
        super();
        this.qrCode = qrCode;
        this.qrCodeID = qrCode.getID();
    }

    /**
     * Called when dialog is created
     *
     * @param savedInstanceState - The last saved instance state of the Fragment, or null if this is a freshly created Fragment.
     * @return - builder
     */
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.qr_view, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        commentNumTextView = view.findViewById(R.id.commentsNumTv);
        commentEditText = view.findViewById(R.id.editTextComment);

        ListView commentListView = view.findViewById(R.id.commentListView);
        TextView pointsTextView = view.findViewById(R.id.pointsTV);
        TextView scansTextView = view.findViewById(R.id.scansTV);

        ImageView commentImageView = view.findViewById(R.id.imageViewSend);
        ImageView eyesImageView = view.findViewById(R.id.imageEyes);
        ImageView colourImageView = view.findViewById(R.id.imageColour);
        ImageView faceImageView = view.findViewById(R.id.imageFace);
        ImageView noseImageView = view.findViewById(R.id.imageNose);
        ImageView mouthImageView = view.findViewById(R.id.imageMouth);
        ImageView eyebrowsImageView = view.findViewById(R.id.imageEyebrows);
        ImageView photoImageView = view.findViewById(R.id.imagePhoto);

        String points = "Points: " + qrCode.getPoints();
        pointsTextView.setText(points);
        String scans = "Total Scans: " + qrCode.getNumberOfScans();
        scansTextView.setText(scans);

        // Creates the appearance of the QRCode object based on the drawable ids stored in its faceList array
        colourImageView.setImageResource((qrCode.getFaceList()).get(2));
        eyesImageView.setImageResource((qrCode.getFaceList()).get(0));
        faceImageView.setImageResource((qrCode.getFaceList()).get(1));
        noseImageView.setImageResource((qrCode.getFaceList()).get(3));
        mouthImageView.setImageResource((qrCode.getFaceList()).get(4));
        eyebrowsImageView.setImageResource((qrCode.getFaceList()).get(5));

        // If the QRCode object has an associated photo, use Picasso to load it into the photoImageView (Rotated for some reason)
        if ((!qrCode.getPhotoList().isEmpty()) && (qrCode.getPhotoList().get(0) != null)) {
            Picasso.with(getContext()).load(qrCode.getPhotoList().get(0)).into(photoImageView);
        }

        noCommentsCheck(qrCodeID, new QRCodeHasCommentsCallback() {
            public void setHasComments(boolean hasComments) {
                if (hasComments) {
                    getComments(qrCodeID, new QRCodeGetCommentsCallback() {
                        public void setComments(@NonNull ArrayList<Comment> comments) {

                            commentList = comments;

                            commentAdapter = new CommentAdapter(getContext(), commentList);
                            commentListView.setAdapter(commentAdapter);

                            // Count number of comments QR Code has
                            qrCodesReference.document(qrCode.getID()).collection("commentList")
                                    .count()
                                    .get(AggregateSource.SERVER)
                                    .addOnSuccessListener(snapshot -> {
                                        String commentListCount = String.valueOf(snapshot.getCount());
                                        commentNumTextView.setText(commentListCount);

                                    });
                        }
                    });
                } else {
                    commentList = new ArrayList<>();
                    commentAdapter = new CommentAdapter(getContext(), commentList);
                    commentListView.setAdapter(commentAdapter);
                    commentNumTextView.setText("0");

                }
            }
        });

        // When the send image arrow ImageView is clicked, if a comment has been made it will be added
        // to the QRCode object's saved array of comments and appear in the comment box with the associated user
        // User can only comment on QR Codes if they already have that code in their collection
        checkUserHasQRCode(prefs.getString("currentUserUsername", null), qrCodeID, new UserHasQRCodeCallback() {
            @Override
            public void setHasCode(boolean hasCode) {
                if (hasCode) {
                    commentImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String commentString = commentEditText.getText().toString();

                            if (!commentString.isEmpty()) {

                                String currentUserDisplayName = prefs.getString("currentUserDisplayName", null);
                                String currentUser = prefs.getString("currentUserUsername", null);
                                Comment c = new Comment(commentString, currentUserDisplayName, currentUser);
                                int commentNum;

                                commentAdapter.addToCommentList(c);
                                commentAdapter.notifyDataSetChanged();
                                commentEditText.getText().clear();

                                HashMap<String, String> comment = new HashMap<>();
                                comment.put("username", currentUser);
                                comment.put("displayName", currentUserDisplayName);
                                comment.put("commentString", commentString);

                                qrCodesReference.document(qrCodeID).collection("commentList").add(comment);
                                commentNum = Integer.parseInt(commentNumTextView.getText().toString());
                                commentNum++;
                                commentNumTextView.setText(String.valueOf(commentNum));

                            }
                        }
                    });
                }
            }
        });

        commentListView.setOnTouchListener(new ListView.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
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
                        dialogInterface.dismiss();
                    }
                })
                .create();
    }

    /**
     * Query database to check if QR code has any comments or not
     *
     * @param qrCodeID       QR to check for comments
     * @param setHasComments Callback function
     */
    public void noCommentsCheck(@NonNull String qrCodeID, final @NonNull QRCodeHasCommentsCallback setHasComments) {

        qrCodesReference.document(qrCodeID).collection("commentList")
                .get()
                .addOnSuccessListener(qrCodeCommentList ->
                        setHasComments.setHasComments(!qrCodeCommentList.isEmpty())
                );
    }

    /**
     * Gets all comments on a QR Code.
     * Comments are stored in the callback array
     *
     * @param qrCodeID    ID of QR Code to get comments of
     * @param setComments Callback for query
     */
    public void getComments(@NonNull String qrCodeID, final @NonNull QRCodeGetCommentsCallback setComments) {

        ArrayList<Comment> comments = new ArrayList<>();

        // Retrieve all comment items for the given QR Code and add them to the array
        qrCodesReference.document(qrCodeID).collection("commentList")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot document : snapshot) {
                        Comment comment;
                        comment = document.toObject(Comment.class);
                        comments.add(comment);
                    }
                    setComments.setComments(comments);
                });
    }

    /**
     * Check if the given user has the given QR Code
     *
     * @param username   Username to check
     * @param qrCodeID   QR Code to check
     * @param setHasCode Callback for query
     */
    public void checkUserHasQRCode(@NonNull String username, @NonNull String qrCodeID, final @NonNull UserHasQRCodeCallback setHasCode) {
        usersReference.document(username).collection("User QR Codes").document(qrCodeID)
                .get()
                .addOnSuccessListener(userQRCode ->
                        setHasCode.setHasCode(userQRCode.exists())
                );
    }

    /**
     * Listener for the QRCodeView dialog
     */
    interface QRCodeViewDialogListener {
        void viewCode(QRCode qrCode);
    }

    /**
     * Callback for querying the database to see if QR code has comments
     *
     * @author Afra
     */
    public interface QRCodeHasCommentsCallback {
        void setHasComments(boolean hasComments);
    }

    /**
     * Callback for querying the database to retrieve comments
     *
     * @author Afra
     */
    public interface QRCodeGetCommentsCallback {
        void setComments(@NonNull ArrayList<Comment> comments);
    }

    /**
     * Callback for querying the database to see if user has given QR Code
     *
     * @author Afra
     */
    public interface UserHasQRCodeCallback {
        void setHasCode(boolean hasCode);
    }
}