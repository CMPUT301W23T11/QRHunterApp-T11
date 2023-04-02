package com.example.qrhunterapp_t11.interfaces;

import com.example.qrhunterapp_t11.objectclasses.QRCode;

public interface QueryCallbackWithQRCode {
    void queryCompleteCheckObject(boolean queryComplete, QRCode qr);
}
