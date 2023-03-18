package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Interface to handle long clicking on items in RecyclerViews
 *
 * @author Afra
 * @reference Sarah's OnItemClickListener interface
 * @see OnItemClickListener
 */
public interface OnItemLongClickListener {
    void onItemLongClick(@NonNull DocumentSnapshot documentSnapshot, int position);
}
