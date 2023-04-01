package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This Test class is to verify the methods of the QRCode object run as expected.
 */

public class UserTest {

    private User mockUser(@NonNull String displayName, @NonNull String username, int totalPoints, int totalScans, int topQRCode, @NonNull String email) {
        return new User(displayName, username, totalPoints, totalScans, topQRCode, email);
    }

    @Test
    public void testGetSetDisplayName() {

        // initial get
        User user = mockUser("user99", "user99", 10000, 30, 23, "EpicGamer@gmail.com" );
        Assertions.assertEquals("user99", user.getDisplayName());

        // set then get
        user.setDisplayName("EpicGamer");
        Assertions.assertEquals("EpicGamer", user.getDisplayName());

        // case set empty string
        user.setDisplayName("");
        Assertions.assertEquals("", user.getDisplayName());

        // case set emojis
        user.setDisplayName("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", user.getDisplayName());

        // case long string w/ symbols
        user.setDisplayName("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", user.getDisplayName());

    }

    @Test
    public void testGetSetUserName() {

        // initial get
        User user = mockUser("user99", "user99", 10000, 30, 23, "EpicGamer@gmail.com" );
        Assertions.assertEquals("user99", user.getUsername());

        // set then get
        user.setUsername("user9001");
        Assertions.assertEquals("user9001", user.getUsername());

        // case set empty string
        user.setUsername("");
        Assertions.assertEquals("", user.getUsername());

        // case set emojis
        user.setUsername("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", user.getUsername());

        // case long string w/ symbols
        user.setUsername("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", user.getUsername());

    }

    @Test
    public void testGetSetTotalPoints() {

        // initial get
        User user = mockUser("user99", "user99", 10000, 30, 23, "EpicGamer@gmail.com" );
        Assertions.assertEquals(10000, user.getTotalPoints());

        // case points = 0
        user.setTotalPoints(0);
        Assertions.assertEquals(0, user.getTotalPoints());

        // case points = 1000000
        user.setTotalPoints(1000000);
        Assertions.assertEquals(1000000, user.getTotalPoints());

    }

    @Test
    public void testGetSetTotalScans() {

        // initial get
        User user = mockUser("user99", "user99", 10000, 30, 23, "EpicGamer@gmail.com" );
        Assertions.assertEquals(30, user.getTotalScans());

        // case totalScans = 0
        user.setTotalScans(0);
        Assertions.assertEquals(0, user.getTotalScans());

        // case totalScans = 1000000
        user.setTotalScans(1000000);
        Assertions.assertEquals(1000000, user.getTotalScans());

    }

    @Test
    public void testGetSetTopQRCode() {

        // initial get
        User user = mockUser("user99", "user99", 10000, 30, 23, "EpicGamer@gmail.com" );
        Assertions.assertEquals(23, user.getTopQRCode());

        // case TopPoints = 0
        user.setTopQRCode(0);
        Assertions.assertEquals(0, user.getTopQRCode());

        // case TopPoints = 1000000
        user.setTopQRCode(1000000);
        Assertions.assertEquals(1000000, user.getTopQRCode());

    }

    @Test
    public void testGetSetEmail() {

        // initial get
        User user = mockUser("user99", "user99", 10000, 30, 23, "EpicGamer@gmail.com" );
        Assertions.assertEquals("EpicGamer@gmail.com", user.getEmail());

        // case empty
        user.setEmail("");
        Assertions.assertEquals("", user.getEmail());

        // case no email
        user.setEmail("No email");
        Assertions.assertEquals("No email", user.getEmail());

        // case new string email = "nospam@gmail.com"
        user.setEmail("nospam@gmail.com");
        Assertions.assertEquals("nospam@gmail.com", user.getEmail());

        // case set emojis
        user.setEmail("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", user.getEmail());

        // case long string w/ symbols
        user.setEmail("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", user.getEmail());



    }
}
