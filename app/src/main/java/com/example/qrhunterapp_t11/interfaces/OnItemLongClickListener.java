package com.example.qrhunterapp_t11.interfaces;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Interface to handle long clicking on items in RecyclerViews
 *
 * @author Afra
 * @sources Sarah's OnItemClickListener interface
 * @see OnItemClickListener
 */
public interface OnItemLongClickListener {
    void onItemLongClick(@NonNull DocumentSnapshot documentSnapshot, int position);
}
