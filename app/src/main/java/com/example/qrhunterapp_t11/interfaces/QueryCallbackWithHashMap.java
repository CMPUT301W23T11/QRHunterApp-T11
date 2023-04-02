package com.example.qrhunterapp_t11.interfaces;

import androidx.annotation.NonNull;

import java.util.HashMap;

/**
 * Callback for querying the database
 *
 * @author Afra
 */
public interface QueryCallbackWithHashMap {
    void setHashMap(@NonNull HashMap<?, ?> hashMap);
}

