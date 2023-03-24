package com.example.qrhunterapp_t11.objectclasses;

import androidx.annotation.NonNull;

/**
 * This class represents a Comment that will belong to a QRCode object. Contains the comment contents and the commenter's info
 *
 * @author Sarah Thomson
 */

public class Comment {

    private String commentString;
    private String displayName;
    private String username;

    /**
     * Constructor for Comment
     *
     * @param commentString - String representing the comment
     * @param displayName   - String representing the user's display name
     * @param username      - String representing the username
     */
    public Comment(@NonNull String commentString, @NonNull String displayName, @NonNull String username) {
        this.commentString = commentString;
        this.displayName = displayName;
        this.username = username;
    }

    /**
     * Empty constructor for the db
     */
    public Comment() {
    }

    /**
     * Getter for commentString
     *
     * @return commentString
     */
    @NonNull
    public String getCommentString() {
        return commentString;
    }

    /**
     * Setter for commentString
     *
     * @param commentString commentString to set
     */
    public void setCommentString(@NonNull String commentString) {
        this.commentString = commentString;
    }

    /**
     * Getter for userDisplayName
     *
     * @return userDisplayName
     */
    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter for displayName
     *
     * @param displayName displayName to set
     */
    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for username
     *
     * @return username
     */
    @NonNull
    public String getUsername() {
        return username;
    }

    /**
     * Setter for username
     *
     * @param username Username to set
     */
    public void setUsername(@NonNull String username) {
        this.username = username;
    }

}
