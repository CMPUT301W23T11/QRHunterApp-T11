package com.example.qrhunterapp_t11;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

/**
 * This is the dialog fragment that appears when a user clicks to see more info about a certain QRCode. It handles...
 *
 * @author Sarah Thomson
 */
public class ViewQR extends DialogFragment {
    private QRCode qrCode;
    private Integer pos;
    private ListView commentListView;
    private ArrayList<Comment> commentList;
    private CommentAdapter commentAdapter;
    private ImageView commentIV;
    private EditText commentET;
    public ViewQR(){
        super();
    }

    public ViewQR(QRCode qrCode, DocumentReference qrReference){
        super();
        this.qrCode = qrCode;
    }

    public ViewQR(QRCode qrCode ,Integer pos){
        super();
        this.qrCode = qrCode;
        this.pos=pos;
    }
    interface ViewQRDialogListener {
        void ViewCode(QRCode qrCode);
    }
    private ViewQR.ViewQRDialogListener listener;
    private ImageView eyesImageView, faceImageView, colourImageView, noseImageView, mouthImageView, eyebrowsImageView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ViewQR.ViewQRDialogListener){
            listener = (ViewQR.ViewQRDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement DialogListener");
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.qr_view, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        commentET = view.findViewById(R.id.editTextComment);
        commentIV = view.findViewById(R.id.imageViewSend);
        commentList = qrCode.getCommentList();
        commentListView = view.findViewById(R.id.commentListView);
        commentAdapter = new CommentAdapter(getContext(), commentList);
        commentListView.setAdapter(commentAdapter);

        eyesImageView = view.findViewById(R.id.imageEyes);
        colourImageView = view.findViewById(R.id.imageColour);
        faceImageView = view.findViewById(R.id.imageFace);
        noseImageView = view.findViewById(R.id.imageNose);
        mouthImageView = view.findViewById(R.id.imageMouth);
        eyebrowsImageView = view.findViewById(R.id.imageEyebrows);
        colourImageView.setImageResource((qrCode.getFaceList()).get(2));
        eyesImageView.setImageResource((qrCode.getFaceList()).get(0));
        faceImageView.setImageResource((qrCode.getFaceList()).get(1));
        noseImageView.setImageResource((qrCode.getFaceList()).get(3));
        mouthImageView.setImageResource((qrCode.getFaceList()).get(4));
        eyebrowsImageView.setImageResource((qrCode.getFaceList()).get(5));

        commentIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentString = commentET.getText().toString();

                if (!commentString.isEmpty()) {
                    Comment c = new Comment(commentString, "Test Profile");
                    commentList.add(c);
                    qrCode.setCommentList(commentList);
                    commentAdapter.notifyDataSetChanged();
                    commentET.getText().clear();
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
                        // Disallow ScrollView to intercept touch events
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                // Handle ListView touch events
                v.onTouchEvent(event);
                return true;
            }
        });

        return builder
                .setView(view)
                .setTitle(qrCode.getName())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
    }
}
