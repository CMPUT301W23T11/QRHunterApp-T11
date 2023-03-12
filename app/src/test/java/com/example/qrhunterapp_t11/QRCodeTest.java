package com.example.qrhunterapp_t11;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;

public class QRCodeTest {
    private QRCode mockQR(String valueString) {
        return new QRCode(valueString);
    }

    @Test
    public void testPoints() {
        int points;

        QRCode qrCode = mockQR("Test this string");
        Assertions.assertEquals(40, qrCode.getPoints());

        QRCode qrCode2 = mockQR("");
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


    /**
     * @reference this website for verifying "ultimate nasty string" test as even Bash's Echo | SHA256 stopped reading with ';' chars inserted
     * https://emn178.github.io/online-tools/sha256.html (verified website output matched all other previous outputs)
     */
    @Test
    public void testName(){
        String name;

        QRCode qrCode = mockQR("Test this string");
        Assertions.assertEquals("Old Boldicanmor", qrCode.getName());

        QRCode qrCode2 = mockQR("");
        Assertions.assertEquals("Young Boldomentas", qrCode2.getName());

        QRCode qrCode3 = mockQR("😭😭😭😭");
        Assertions.assertEquals("Young Laldomenmor", qrCode3.getName());

        //simple test to make sure the hash to binary works correctly
        name = QRCode.uniqueName("080000");
        Assertions.assertEquals("Big Largamengog", name);

        //simple test to make sure the hash to binary works correctly
        name = QRCode.uniqueName("07ffff");
        Assertions.assertEquals("Old Sigsurolfli", name);

        name = QRCode.uniqueName("d1a885c6433d38a8cd1c486196a3b5fe6cb7105ad8f9e30bde61680b43adc4dd");
        Assertions.assertEquals("Young Laldomenmor", name);

    }

    @Test
    public void testImage(){
        ArrayList<Integer> faceList;
        ArrayList<Integer> expectedList = new ArrayList<>();

        QRCode qrCode = mockQR("Test this string");
        expectedList.add(2131165346);
        expectedList.add(2131165344);
        expectedList.add(2131165317);
        expectedList.add(2131165428);
        expectedList.add(2131165387);
        expectedList.add(2131165347);

        Assertions.assertEquals(expectedList, qrCode.getFaceList());

        ArrayList<Integer> expectedList2 = new ArrayList<>();
        expectedList2.add(2131165346);
        expectedList2.add(2131165344);
        expectedList2.add(2131165316);
        expectedList2.add(2131165427);
        expectedList2.add(2131165387);
        expectedList2.add(2131165348);

        QRCode qrCode2 = mockQR("");
        System.out.println(qrCode.getHash());
        Assertions.assertEquals(expectedList2, qrCode2.getFaceList());

        //simple test to make sure the hash to binary works correctly
        ArrayList<Integer> expectedList3 = new ArrayList<>();
        expectedList3.add(2131165345);
        expectedList3.add(2131165343);
        expectedList3.add(2131165316);
        expectedList3.add(2131165427);
        expectedList3.add(2131165387);
        expectedList3.add(2131165347);

        faceList = QRCode.uniqueImage("080000");
        Assertions.assertEquals(expectedList3, faceList);

        //simple test to make sure the hash to binary works correctly
        ArrayList<Integer> expectedList4 = new ArrayList<>();
        expectedList4.add(2131165346);
        expectedList4.add(2131165344);
        expectedList4.add(2131165317);
        expectedList4.add(2131165428);
        expectedList4.add(2131165388);
        expectedList4.add(2131165348);

        faceList = QRCode.uniqueImage("07ffff");
        Assertions.assertEquals(expectedList4, faceList);


        // test emojis w/o \n
        // uniqueName() in QRCode constructor crashes program with this input string,
        // if you run this test with uniqueName disabled, assertion passes
        // problem resolved test, now passes
        QRCode qrCode9 = mockQR("😭😭😭😭");
        Assertions.assertEquals("d1a885c6433d38a8cd1c486196a3b5fe6cb7105ad8f9e30bde61680b43adc4dd", qrCode9.getHash());

    }


}