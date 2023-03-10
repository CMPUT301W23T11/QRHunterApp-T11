package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

/**
 * This Class represents a Comment that will belong to a QRCode object. Contains the comment contents and the profile who posted it.
 *
 * @author Sarah Thomson
 */

public class Comment {
    private String comment;
    private String profile;

    /**
     * Constructor for Comment
     *
     * @param comment - String representing the comment
     * @param profile - String representing the user's profile
     */
    public Comment(@NonNull String comment, @NonNull String profile) {
        this.comment = comment;
        this.profile = profile;
    }

    /**
     * Empty constructor for the db
     */
    public Comment() {
    }

    /**
     * Getter for the comment String
     *
     * @return comment - String
     */
    @NonNull
    public String getComment() {
        return comment;
    }

    /**
     * Getter for the profile String
     *
     * @return profile - String
     */
    @NonNull
    public String getProfile() {
        return profile;
    }

}
