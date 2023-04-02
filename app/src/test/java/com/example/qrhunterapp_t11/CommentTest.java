package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

import com.example.qrhunterapp_t11.objectclasses.Comment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This Test class is to verify the methods of the Comment object run as expected.
 */

public class CommentTest {


    private Comment mockComment(@NonNull String commentString, @NonNull String displayName, @NonNull String username) {
        return new Comment(commentString, displayName, username);
    }

    @Test
    public void testGetSetCommentString() {
        Comment comment = mockComment("Hello", "Epic Gamer", "user99");
        Assertions.assertEquals("Hello", comment.getCommentString());

        // test set empty string
        comment.setCommentString("");
        Assertions.assertEquals("", comment.getCommentString());

        // test set regular string
        comment.setCommentString("Goodbye");
        Assertions.assertEquals("Goodbye", comment.getCommentString());

        // test set emojis
        comment.setCommentString("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", comment.getCommentString());

        // test long string w/ symbols
        comment.setCommentString("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", comment.getCommentString());
    }

    @Test
    public void testGetSetDisplayName() {
        Comment comment = mockComment("Hello", "Epic Gamer", "user99");
        Assertions.assertEquals("Epic Gamer", comment.getDisplayName());

        // test set empty string
        comment.setDisplayName("");
        Assertions.assertEquals("", comment.getDisplayName());

        // test set regular string
        comment.setDisplayName("Goodbye");
        Assertions.assertEquals("Goodbye", comment.getDisplayName());

        // test set emojis
        comment.setDisplayName("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", comment.getDisplayName());

        // test long string w/ symbols
        comment.setDisplayName("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", comment.getDisplayName());

    }

    @Test
    public void testGetSetUserName() {
        Comment comment = mockComment("Hello", "Epic Gamer", "user99");
        Assertions.assertEquals("user99", comment.getUsername());

        // test set empty string
        comment.setUsername("");
        Assertions.assertEquals("", comment.getUsername());

        // test set regular string
        comment.setUsername("Goodbye");
        Assertions.assertEquals("Goodbye", comment.getUsername());

        // test set emojis
        comment.setUsername("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", comment.getUsername());

        // test long string w/ symbols
        comment.setUsername("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", comment.getUsername());

    }
}
