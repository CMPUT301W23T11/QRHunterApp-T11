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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * This is the dialog fragment that appears when a user clicks to see more info about a certain QRCode. It shows the user the QR Code's image, name,
 * # of points, photo, and implements comments.
 *
 * @author Sarah Thomson
 * @reference https://stackoverflow.com/questions/6210895/listview-inside-scrollview-is-not-scrolling-on-android/17503823#17503823 by Moisés Olmedo, License: CC BY-SA 3.0
 * for scrollable comment box
 */
public class ViewQR extends DialogFragment {
    private QRCode qrCode;
    private ListView commentListView;
    private ArrayList<Comment> commentList;
    private CommentAdapter commentAdapter;
    private ImageView commentIV;
    private EditText commentET;
    private SharedPreferences prefs;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference QRCodesReference = db.collection("QRCodes");
    private CollectionReference usersReference = db.collection("Users");
    private ViewQR.ViewQRDialogListener listener;
    private ImageView eyesImageView, faceImageView, colourImageView, noseImageView, mouthImageView, eyebrowsImageView, photoImageView;
    private TextView pointsTV;

    public ViewQR() {
        super();
    }

    public ViewQR(QRCode qrCode, DocumentReference qrReference) {
        super();
        this.qrCode = qrCode;
    }

    /**
     * Listener for the ViewQR dialog
     */
    interface ViewQRDialogListener {
        void ViewCode(QRCode qrCode);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ViewQR.ViewQRDialogListener) {
            listener = (ViewQR.ViewQRDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement DialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.qr_view, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        commentET = view.findViewById(R.id.editTextComment);
        commentIV = view.findViewById(R.id.imageViewSend);
        commentList = qrCode.getCommentList();
        commentListView = view.findViewById(R.id.commentListView);
        commentAdapter = new CommentAdapter(getContext(), commentList);
        commentListView.setAdapter(commentAdapter);
        pointsTV = view.findViewById(R.id.pointsTV);
        pointsTV.setText("Points: " + String.valueOf(qrCode.getPoints()));

        eyesImageView = view.findViewById(R.id.imageEyes);
        colourImageView = view.findViewById(R.id.imageColour);
        faceImageView = view.findViewById(R.id.imageFace);
        noseImageView = view.findViewById(R.id.imageNose);
        mouthImageView = view.findViewById(R.id.imageMouth);
        eyebrowsImageView = view.findViewById(R.id.imageEyebrows);
        photoImageView = view.findViewById(R.id.imagePhoto);
        colourImageView.setImageResource((qrCode.getFaceList()).get(2));
        eyesImageView.setImageResource((qrCode.getFaceList()).get(0));
        faceImageView.setImageResource((qrCode.getFaceList()).get(1));
        noseImageView.setImageResource((qrCode.getFaceList()).get(3));
        mouthImageView.setImageResource((qrCode.getFaceList()).get(4));
        eyebrowsImageView.setImageResource((qrCode.getFaceList()).get(5));

        if (!qrCode.getPhotoList().isEmpty()) {
            Picasso.with(getContext()).load(qrCode.getPhotoList().get(0)).into(photoImageView);
        }
        commentIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentString = commentET.getText().toString();

                if (!commentString.isEmpty()) {
                    String currentUser = prefs.getString("currentUser", null);
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
                .setNegativeButton("Back", null)
                .create();
    }
}