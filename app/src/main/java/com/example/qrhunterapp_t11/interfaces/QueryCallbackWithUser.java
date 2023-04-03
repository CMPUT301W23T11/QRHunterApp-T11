package com.example.qrhunterapp_t11.interfaces;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;

/**
 * Callback for querying the database
 *
 * @author Sarah
 */
public interface QueryCallbackWithUser {
    void queryCompleteCheckUser(boolean queryComplete, User user, QRCode qrCode);
}
