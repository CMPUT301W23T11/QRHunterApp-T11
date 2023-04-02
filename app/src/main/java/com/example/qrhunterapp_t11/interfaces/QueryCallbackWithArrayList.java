package com.example.qrhunterapp_t11.interfaces;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Callback for querying the database
 *
 * @author Afra
 */
public interface QueryCallbackWithArrayList {
    void setArrayList(@NonNull ArrayList<?> arrayList);
}
