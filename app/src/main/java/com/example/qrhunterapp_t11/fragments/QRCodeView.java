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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.adapters.CommentAdapter;
import com.example.qrhunterapp_t11.adapters.PhotoAdapter;
import com.example.qrhunterapp_t11.adapters.QRCodeAdapter;
import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.objectclasses.Comment;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * This is the dialog fragment that appears when a user clicks to see more info about a certain QRCode. It shows the user the QR Code's image, name,
 * # of points, photo, and implements comments.
 *
 * @author Afra
 * @author Sarah Thomson
 * @sources <pre>
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
    private int commentListCount;
    private TextView commentNumTextView;
    private ViewPager viewPager;
    private QRCodeAdapter adapter;

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
    public QRCodeView(@NonNull QRCode qrCode, @Nullable QRCodeAdapter adapter) {
        super();
        this.qrCode = qrCode;
        this.qrCodeID = qrCode.getID();
        this.adapter = adapter;
    }

    /**
     * Called when dialog is created
     *
     * @param savedInstanceState - The last saved instance state of the Fragment, or null if this is a freshly created Fragment.
     * @return builder
     */
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        PhotoAdapter photoAdapter;

        prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.qr_view, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        commentNumTextView = view.findViewById(R.id.commentsTV);
        commentEditText = view.findViewById(R.id.editTextComment);

        ListView commentListView = view.findViewById(R.id.commentListView);
        TextView pointsTextView = view.findViewById(R.id.pointsTV);
        TextView scansTextView = view.findViewById(R.id.scansTV);

        LinearLayoutCompat photoBox = view.findViewById(R.id.photoBackground);
        TextView numeratorTextView = view.findViewById(R.id.numeratorTV);
        TextView denominatorTextView = view.findViewById(R.id.denominatorTV);

        ImageView commentImageView = view.findViewById(R.id.imageViewSend);
        ImageView eyesImageView = view.findViewById(R.id.imageEyes);
        ImageView colourImageView = view.findViewById(R.id.imageColour);
        ImageView faceImageView = view.findViewById(R.id.imageFace);
        ImageView noseImageView = view.findViewById(R.id.imageNose);
        ImageView mouthImageView = view.findViewById(R.id.imageMouth);
        ImageView eyebrowsImageView = view.findViewById(R.id.imageEyebrows);

        // Creates the appearance of the QRCode object based on the drawable names stored in its faceList array
        eyesImageView.setImageResource(getResources().getIdentifier(qrCode.getFaceList().get(0), "drawable", getContext().getPackageName()));
        faceImageView.setImageResource(getResources().getIdentifier(qrCode.getFaceList().get(1), "drawable", getContext().getPackageName()));
        colourImageView.setImageResource(getResources().getIdentifier(qrCode.getFaceList().get(2), "drawable", getContext().getPackageName()));
        noseImageView.setImageResource(getResources().getIdentifier(qrCode.getFaceList().get(3), "drawable", getContext().getPackageName()));
        mouthImageView.setImageResource(getResources().getIdentifier(qrCode.getFaceList().get(4), "drawable", getContext().getPackageName()));
        eyebrowsImageView.setImageResource(getResources().getIdentifier(qrCode.getFaceList().get(5), "drawable", getContext().getPackageName()));

        String points = "Points: " + qrCode.getPoints();
        pointsTextView.setText(points);
        String scans = "Total Scans: " + qrCode.getNumberOfScans();
        scansTextView.setText(scans);
        viewPager = view.findViewById(R.id.pager);
        photoAdapter = new PhotoAdapter(getContext(), qrCode.getPhotoList());
        viewPager.setAdapter(photoAdapter);

        // If the QRCode has no associated photo, hide the photo box
        if (photoAdapter.getCount() == 0) {
            photoBox.setVisibility(View.GONE);
        }
        denominatorTextView.setText(String.valueOf(photoAdapter.getCount()));
        numeratorTextView.setText(String.valueOf(viewPager.getCurrentItem() + 1));

        hasCommentsCheck(qrCodeID, new QueryCallback() {
            public void queryCompleteCheck(boolean hasComments) {
                if (hasComments) {
                    getComments(qrCodeID, new QRCodeGetCommentsCallback() {
                        public void setComments(@NonNull ArrayList<Comment> comments) {

                            commentList = comments;

                            commentAdapter = new CommentAdapter(getContext(), commentList);
                            commentListView.setAdapter(commentAdapter);

                            // Count number of comments QR Code has
                            commentListCount = commentList.size();
                            String commentNum = "Comments: " + commentListCount;
                            commentNumTextView.setText(commentNum);
                        }
                    });
                } else {
                    commentList = new ArrayList<>();
                    commentAdapter = new CommentAdapter(getContext(), commentList);
                    commentListView.setAdapter(commentAdapter);
                }
            }
        });

        // When the send image arrow ImageView is clicked, if a comment has been made it will be added
        // to the QRCode object's saved array of comments and appear in the comment box with the associated user
        // User can only comment on QR Codes if they already have that code in their collection
        checkUserHasQRCode(prefs.getString("currentUserUsername", null), qrCodeID, new QueryCallback() {
            @Override
            public void queryCompleteCheck(boolean userHasQRCode) {

                commentImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (userHasQRCode) {
                            String commentString = commentEditText.getText().toString();

                            if (!commentString.isEmpty()) {

                                String currentUserDisplayName = prefs.getString("currentUserDisplayName", null);
                                String currentUser = prefs.getString("currentUserUsername", null);
                                Comment comment = new Comment(commentString, currentUserDisplayName, currentUser);

                                commentAdapter.addToCommentList(comment);
                                commentAdapter.notifyDataSetChanged();
                                commentEditText.getText().clear();

                                qrCodesReference.document(qrCodeID).collection("commentList").add(comment);

                                // Increment comment count
                                commentListCount++;
                                String commentNum = "Comments: " + commentListCount;
                                commentNumTextView.setText(commentNum);
                            }
                        } else {
                            Toast toast = Toast.makeText(getContext(), "You cannot comment on QR Codes you have not scanned.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                numeratorTextView.setText(String.valueOf(viewPager.getCurrentItem() + 1));
            }

            @Override
            public void onPageSelected(int position) {
                // Not needed
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Not needed
            }
        });

        commentListView.setOnTouchListener(new ListView.OnTouchListener() {
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
                        adapter.notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                })
                .create();
    }

    /**
     * Query database to check if QR code has any comments or not
     *
     * @param qrCodeID    QR to check for comments
     * @param hasComments Callback function
     */
    public void hasCommentsCheck(@NonNull String qrCodeID, final @NonNull QueryCallback hasComments) {

        qrCodesReference.document(qrCodeID).collection("commentList")
                .get()
                .addOnSuccessListener(qrCodeCommentList ->
                        hasComments.queryCompleteCheck(!qrCodeCommentList.isEmpty())
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
     * @param username      Username to check
     * @param qrCodeID      QR Code to check
     * @param userHasQRCode Callback for query
     */
    public void checkUserHasQRCode(@NonNull String username, @NonNull String qrCodeID, final @NonNull QueryCallback userHasQRCode) {
        usersReference.document(username).collection("User QR Codes").document(qrCodeID)
                .get()
                .addOnSuccessListener(userQRCode ->
                        userHasQRCode.queryCompleteCheck(userQRCode.exists())
                );
    }

    /**
     * Callback for querying the database to retrieve comments
     *
     * @author Afra
     */
    public interface QRCodeGetCommentsCallback {
        void setComments(@NonNull ArrayList<Comment> comments);
    }
}