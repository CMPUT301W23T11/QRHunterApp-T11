package com.example.qrhunterapp_t11.interfaces;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Interface to handle clicking on items in RecyclerViews
 *
 * @author Sarah
 */
public interface OnItemClickListener {
    void onItemClick(@NonNull DocumentSnapshot documentSnapshot, int position);
}