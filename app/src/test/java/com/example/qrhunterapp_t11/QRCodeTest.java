package com.example.qrhunterapp_t11;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class QRCodeTest {
    private QRCode mockQR(String valueString) {
        return new QRCode(valueString);
    }

    @Test
    public void testPoints() {
        int points;

        QRCode qrCode = mockQR("Test this string");
        System.out.println(qrCode.getHash());
        Assertions.assertEquals(40, qrCode.getPoints());

        QRCode qrCode2 = mockQR("");
        System.out.println(qrCode2.getHash());
        Assertions.assertEquals(28, qrCode2.getPoints());

        //test hash from eclass (Should be 115 points)
        points = QRCode.calculatePoints("696ce4dbd7bb57cbfe58b64f530f428b74999cb37e2ee60980490cd9552de3a6");
        Assertions.assertEquals(115, points);

        //test with single and multiple 0s
        points = QRCode.calculatePoints("666004dbd7bb57cbfe58b64f530f428b749999b37e2ee60000490cd9052de333");
        Assertions.assertEquals(8822, points);

        //test largest possible value
        points = QRCode.calculatePoints("0000000000000000000000000000000000000000000000000000000000000000");
        Assertions.assertEquals(Math.pow(10, 8), points);

        //test small value
        points = QRCode.calculatePoints("0");
        Assertions.assertEquals(1, points);

        //test wrapping "hash"
        points = QRCode.calculatePoints("55558ee34121555");
        Assertions.assertEquals(164, points);

    }


}