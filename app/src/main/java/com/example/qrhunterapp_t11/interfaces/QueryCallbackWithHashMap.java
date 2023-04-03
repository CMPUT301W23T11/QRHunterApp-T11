package com.example.qrhunterapp_t11.interfaces;

import androidx.annotation.NonNull;

import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;

import java.util.HashMap;

/**
 * Callback for querying the database
 *
 * @author Afra
 */
public interface QueryCallbackWithHashMap {
    void setHashMap(@NonNull HashMap<User, QRCode> hashMap);
}

