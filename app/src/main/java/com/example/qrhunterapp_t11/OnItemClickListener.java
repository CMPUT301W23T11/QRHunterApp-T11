package com.example.qrhunterapp_t11;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnItemClickListener {
    void onItemClick(DocumentSnapshot documentSnapshot, int position);
}
