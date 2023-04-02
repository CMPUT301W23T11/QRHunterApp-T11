package com.example.qrhunterapp_t11.objectclasses;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


public class Preference {
    private static SharedPreferences mPref;
    public static final String PREFS_CURRENT_USER = "currentUserUsername";
    public static final String PREFS_CURRENT_USER_EMAIL = "currentUserEmail";
    public static final String PREFS_CURRENT_USER_DISPLAY_NAME = "currentUserDisplayName";
    public static final String DATABASE_DISPLAY_NAME_FIELD = "displayName";

    private Preference(){}

    public static void init(Context context)
    {
        if(mPref == null)
            mPref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static void setPrefsString(String key, String value) {
            SharedPreferences.Editor prefsEditor = mPref.edit();
            prefsEditor.putString(key, value);
            prefsEditor.commit();
    }

    public static void setPrefsBool(String key, Boolean value) {
        SharedPreferences.Editor prefsEditor = mPref.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }

    public static String getPrefsString(String key, String value) {
        return mPref.getString(key, value);
    }
    public static boolean getPrefsBool(String key, boolean value) {
        return mPref.getBoolean(key, value);
    }

}
