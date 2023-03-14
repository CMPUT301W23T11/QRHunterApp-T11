package com.example.qrhunterapp_t11;

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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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

        commentET = view.findViewById(R.id.editTextComment);
        ImageView commentIV = view.findViewById(R.id.imageViewSend);
        ListView commentListView = view.findViewById(R.id.commentListView);
        TextView pointsTV = view.findViewById(R.id.pointsTV);
        ImageView eyesImageView = view.findViewById(R.id.imageEyes);
        ImageView colourImageView = view.findViewById(R.id.imageColour);
        ImageView faceImageView = view.findViewById(R.id.imageFace);
        ImageView noseImageView = view.findViewById(R.id.imageNose);
        ImageView mouthImageView = view.findViewById(R.id.imageMouth);
        ImageView eyebrowsImageView = view.findViewById(R.id.imageEyebrows);
        ImageView photoImageView = view.findViewById(R.id.imagePhoto);

        commentList = qrCode.getCommentList();
        commentAdapter = new CommentAdapter(getContext(), commentList);
        commentListView.setAdapter(commentAdapter);
        String points = "Points: " + qrCode.getPoints();
        pointsTV.setText(points);

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

        // When the send image arrow ImageView is clicked, if a comment has been made it will be added to the QRCode object's saved array of comments and appear in the comment box with the associated user
        commentIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentString = commentET.getText().toString();

                if (!commentString.isEmpty()) {
                    String currentUser = prefs.getString("currentUserDisplayName", null);
                    Comment c = new Comment(commentString, currentUser);
                    commentList.add(c);
                    qrCode.setCommentList(commentList);
                    commentAdapter.notifyDataSetChanged();
                    commentET.getText().clear();
                    usersReference.document(currentUser).collection("QR Codes").document(qrCode.getHash()).update("commentList", FieldValue.arrayUnion(c));
                    QRCodesReference.document(qrCode.getHash()).update("commentList", FieldValue.arrayUnion(c));
                }
            }
        });

        //This OnTouchListener code block came from Moisés Olmedo to make a list view be scrollable within a scrollview.
        //Link: https://stackoverflow.com/questions/6210895/listview-inside-scrollview-is-not-scrolling-on-android/17503823#17503823
        //License: CC BY-SA 3.0
        commentListView.setOnTouchListener(new ListView.OnTouchListener() {
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
     * Listener for the ViewQR dialog
     */
    interface ViewQRDialogListener {
        void viewCode(QRCode qrCode);
    }
}