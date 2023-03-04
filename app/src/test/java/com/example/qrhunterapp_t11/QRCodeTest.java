package com.example.qrhunterapp_t11;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QRCodeTest {
    private QRCode mockQR() {
        return new QRCode("696ce4dbd7bb57cbfe58b64f530f428b74999cb37e2ee60980490cd9552de3a6");
    }

    private QRCode mockQR2() {
        return new QRCode("555000500222ffff");
    }

    @Test
    public void testPoints() {
        QRCode qrCode = mockQR();
        Assertions.assertEquals(111, qrCode.getPoints());
        QRCode qrCode2 = mockQR2();
        Assertions.assertEquals(3824, qrCode2.getPoints());
    }
}