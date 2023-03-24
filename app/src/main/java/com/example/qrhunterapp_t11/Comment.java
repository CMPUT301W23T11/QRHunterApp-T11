package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

/**
 * This Class represents a Comment that will belong to a QRCode object. Contains the comment contents and the profile who posted it.
 *
 * @author Sarah Thomson
 */

public class Comment {

    private String comment;
    private String displayName;

    private String username;

    /**
     * Constructor for Comment
     *
     * @param comment     - String representing the comment
     * @param displayName - String representing the user's display name
     * @param username    - String representing the username
     */
    public Comment(@NonNull String comment, @NonNull String displayName, @NonNull String username) {
        this.comment = comment;
        this.displayName = displayName;
        this.username = username;
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
     * Getter for the userDisplayName String
     *
     * @return userDisplayName - String
     */
    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for the username String
     *
     * @return username - String
     */
    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public void setComment(@NonNull String comment) {
        this.comment = comment;
    }

}
