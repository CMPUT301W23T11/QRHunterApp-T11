package com.example.qrhunterapp_t11.objectclasses;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton class for SharedPreferences
 *
 * @author Sarah
 *  @sources <a href="https://stackoverflow.com/questions/19612993/writing-singleton-class-to-manage-android-sharedpreferences"> Answer by Magesh Pandian</a></li>
 */
public class Preference {
    public static final String PREFS_CURRENT_USER = "currentUserUsername";
    public static final String PREFS_CURRENT_USER_EMAIL = "currentUserEmail";
    public static final String PREFS_CURRENT_USER_DISPLAY_NAME = "currentUserDisplayName";
    public static final String DATABASE_DISPLAY_NAME_FIELD = "displayName";
    private static SharedPreferences mPref;

    private Preference() {
    }

    /**
     * initializes the instance of the Preference (only done in Main Activity)
     *
     * @param context
     */
    public static void init(Context context) {
        if (mPref == null)
            mPref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    /**
     * Sets a preference with a String value
     *
     * @param key
     * @param value
     */
    public static void setPrefsString(String key, String value) {
        SharedPreferences.Editor prefsEditor = mPref.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

    /**
     * Sets a preference with a boolean value
     *
     * @param key
     * @param value
     */
    public static void setPrefsBool(String key, Boolean value) {
        SharedPreferences.Editor prefsEditor = mPref.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }

    /**
     * Gets a preference with a String value
     *
     * @param key
     * @param value
     * @return String
     */
    public static String getPrefsString(String key, String value) {
        return mPref.getString(key, value);
    }

    /**
     * Gets a preference with a boolean value
     *
     * @param key
     * @param value
     * @return boolean
     */
    public static boolean getPrefsBool(String key, boolean value) {
        return mPref.getBoolean(key, value);
    }

    /**
     * Clears preferences
     */
    public static void clearPrefs() {
        mPref.edit().clear().commit();
    }

}
