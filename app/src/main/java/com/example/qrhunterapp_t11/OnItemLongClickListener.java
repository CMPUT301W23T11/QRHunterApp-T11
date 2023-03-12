package com.example.qrhunterapp_t11;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnItemLongClickListener {
    void onItemLongClick(DocumentSnapshot documentSnapshot, int position);
}
