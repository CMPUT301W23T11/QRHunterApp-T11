package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Interface to handle long clicking on the RecyclerView
 *
 * @author Sarah
 */
public interface OnItemClickListener {
    void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position);
}