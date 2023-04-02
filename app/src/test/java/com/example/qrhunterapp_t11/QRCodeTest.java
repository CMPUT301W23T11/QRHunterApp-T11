package com.example.qrhunterapp_t11;

import com.example.qrhunterapp_t11.objectclasses.QRCode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * This Test class is to verify the methods of the QRCode object run as expected.
 */

public class QRCodeTest {
    private QRCode mockQRCode(String valueString) {
        return new QRCode(valueString);
    }

    /**
     * This test verifies the points are calculated correctly
     */
    @Test
    public void testPoints() {
        int points;

        QRCode qrCode = mockQRCode("Test this string");
        Assertions.assertEquals(40, qrCode.getPoints());

        QRCode qrCode2 = mockQRCode("");
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
     * <a href="https://emn178.github.io/online-tools/sha256.html">verified website output matched all other previous outputs</a>
     */
    @Test
    public void testGetSetId() {

        // test string from eclass
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        Assertions.assertEquals("696ce4dbd7bb57cbfe58b64f530f428b74999cb37e2ee60980490cd9552de3a6", qrCode.getID());
        qrCode.setID(37.4219983, -122.084);
        Assertions.assertEquals("696ce4dbd7bb57cbfe58b64f530f428b74999cb37e2ee60980490cd9552de3a637.4219983-122.084", qrCode.getID());

        // test empty string
        QRCode qrCode1 = mockQRCode("");
        Assertions.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", qrCode1.getID());
        qrCode1.setID(37.4219983, -122.084);
        Assertions.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b85537.4219983-122.084", qrCode1.getID());


        // test emojis w/o \n
        QRCode qrCode2 = mockQRCode("😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("05a62d8d5ae9aa2850e993e64cbb128838b0c8dc34c136fbb271a62e72e2ecc0", qrCode2.getID());
        qrCode2.setID(37.4219983, -122.084);
        Assertions.assertEquals("05a62d8d5ae9aa2850e993e64cbb128838b0c8dc34c136fbb271a62e72e2ecc037.4219983-122.084", qrCode2.getID());


        // ultimate nasty string
        QRCode qrCode3 = mockQRCode("u*SAt<BX<%P{!aUaIjyD?LkD`.DuY.>buZn\\'A6%W[nd<QDC];L#g)\"fURP:ddwOqZHTy`:del?a:,HnO}]7kd`elp_\"$ru/lg,P[$VU{XD{Ev^5L#C^K_yd//^#!$nu!9b%Fxz#4O>3r8D/p8x`VwpJ}]J@w{QR1s`2ZUCufs^3Jy&6_]V\\\\(d{T7tu.WReBE!iDhYQ;!uO+T\"nP9x5)<FyeE`K9{>!!:dFix[4#o1Zsj\\RdP@CjR>v]#RTV]^>}Rl6aQa<}s,PeT|X\"7E]Lv\\Av0|,QeH#]AO`(UD5g1y|ugghXivv1\\4a2#uM>Adv2gq$16O75ZyNQ)FJF@*o?).JULn\\jq1#%oqK#J_:!<sMa<^'y[[>|jG(\"@+MV+%NeDPQ5Y!pUQ'3HvXHj$SZk7VUgnMzz/Ve!>?v0x+h\\p(NkCN:zj@^S0dXv5dSfx..uB\\\\*[\"[lK`\"o_]$y]K4;s3gNo}yu/P^FjhDY.Cg0@?!2qxK,^4KzYT7J,0MU8<Vb8*I`DJEk7.V`fZs[mvYcSYwX3_#KA]UN>nL]TE2Vk]P]}[]](:@z[dxK8OQn#jbW]dx3Y8by{*#wX1xl5CDpCR|8Bh_:H{WT+)ArV@FDo,o<$zL\\Lm?`oxZa<)(>hp:s>1ulvPvb7G(3mpY5pBGy)mzQ+D*Y}'`gd)d_2eL'>mgM,F]UapFJb]D&Rn[FIKfHR[Uf]NI+tGZ2u46@aZ/ejmhS;prV\\Z,q1_\\F+9]&mr5v`>2f%#;k,2F[k/H\\`c0PNafc+De9r#t2YiV)K6R1ZzTNZev^wC`[_ec2{Z+tS0uKg{?s|\"/j3m3QEtj'e+F{G>PyuBuJH*I*FcX#!'i[?wq\\\\c_v;Z+T]woa&xte[nM}n0i%xdt(UG{UpZ(H7i9Sf/.0A[2IR*%B{C8xasWaVrS*$vtAFl93qWeH.9zFn.>plehw)59>K_@^%\\&MOsq`[O|oWg%GT2_[VW`*cC+_[f?pI]{w4!pjH7oZjx<\n");
        Assertions.assertEquals("8fa598cc9ccec1b254e124620a86d9e79768c6f8c6fc59b06980fc25920b09fe", qrCode3.getID());
        qrCode3.setID(37.4219983, -122.084);
        Assertions.assertEquals("8fa598cc9ccec1b254e124620a86d9e79768c6f8c6fc59b06980fc25920b09fe37.4219983-122.084", qrCode3.getID());
    }


    /**
     * This test verifies the name is created correctly
     */
    @Test
    public void testName() {
        String name;

        QRCode qrCode = mockQRCode("Test this string");
        Assertions.assertEquals("Old Sirgoyogmor", qrCode.getName());

        QRCode qrCode2 = mockQRCode("");
        Assertions.assertEquals("Old Lapucantas", qrCode2.getName());

        QRCode qrCode3 = mockQRCode("😭😭😭😭");
        Assertions.assertEquals("Young Wergucanmor", qrCode3.getName());

        //simple test to make sure the hash to binary works correctly
        name = QRCode.uniqueName("080000");
        Assertions.assertEquals("Big Largamengog", name);

        //simple test to make sure the hash to binary works correctly
        name = QRCode.uniqueName("07ffff");
        Assertions.assertEquals("Old Sigsurolfli", name);

        name = QRCode.uniqueName("d1a885c6433d38a8cd1c486196a3b5fe6cb7105ad8f9e30bde61680b43adc4dd");
        Assertions.assertEquals("Young Wergucanmor", name);

    }

    @Test
    public void testImage() {
        ArrayList<String> faceList;
        ArrayList<String> expectedList = new ArrayList<>();

        QRCode qrCode = mockQRCode("Test this string");
        expectedList.add("eyes2");
        expectedList.add("eyebrows2");
        expectedList.add("colour2");
        expectedList.add("nose2");
        expectedList.add("mouth1");
        expectedList.add("face1");

        Assertions.assertEquals(expectedList, qrCode.getFaceList());

        ArrayList<String> expectedList2 = new ArrayList<>();
        expectedList2.add("eyes2");
        expectedList2.add("eyebrows2");
        expectedList2.add("colour1");
        expectedList2.add("nose1");
        expectedList2.add("mouth1");
        expectedList2.add("face2");

        QRCode qrCode2 = mockQRCode("");
        Assertions.assertEquals(expectedList2, qrCode2.getFaceList());

        //simple test to make sure the hash to binary works correctly
        ArrayList<String> expectedList3 = new ArrayList<>();
        expectedList3.add("eyes1");
        expectedList3.add("eyebrows1");
        expectedList3.add("colour1");
        expectedList3.add("nose1");
        expectedList3.add("mouth1");
        expectedList3.add("face1");

        faceList = QRCode.uniqueImage("080000");
        Assertions.assertEquals(expectedList3, faceList);

        //simple test to make sure the hash to binary works correctly
        ArrayList<String> expectedList4 = new ArrayList<>();
        expectedList4.add("eyes2");
        expectedList4.add("eyebrows2");
        expectedList4.add("colour2");
        expectedList4.add("nose2");
        expectedList4.add("mouth2");
        expectedList4.add("face2");

        faceList = QRCode.uniqueImage("07ffff");
        Assertions.assertEquals(expectedList4, faceList);
    }

    @Test
    public void testGetSetPhotos() {
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        ArrayList<String> photoList = new ArrayList<>();

        // test empty
        qrCode.setPhotoList(photoList);
        Assertions.assertEquals(photoList, qrCode.getPhotoList());

        // test 1 element
        photoList.add("https://firebasestorage.googleapis.com/v0/b/project-f807a.appspot.com/o/uploads%2F1680299497710%20_504x416.jpg?alt=media&token=51fda9bf-c614-4630-917c-bc698c043759");
        qrCode.setPhotoList(photoList);
        Assertions.assertEquals(photoList, qrCode.getPhotoList());

        // test 2 elements
        photoList.add("https://firebasestorage.googleapis.com/v0/b/project-f807a.appspot.com/o/uploads%2F1680299645852%20_504x416.jpg?alt=media&token=e082fbee-f222-44d9-bb37-93bde3ff936a");
        qrCode.setPhotoList(photoList);
        Assertions.assertEquals(photoList, qrCode.getPhotoList());

        // clear list
        photoList.clear();
        qrCode.setPhotoList(photoList);
        Assertions.assertEquals(photoList, qrCode.getPhotoList());
    }

    @Test
    public void testGetSetLatitude() {
        QRCode qrCode = mockQRCode("BFG5DGW54\n");

        // test null
        Assertions.assertEquals(null, qrCode.getLatitude());

        // test value
        qrCode.setLatitude(37.4219983);
        Assertions.assertEquals(37.4219983, qrCode.getLatitude());
    }

    @Test
    public void testGetSetLongitude() {
        QRCode qrCode = mockQRCode("BFG5DGW54\n");

        // test null
        Assertions.assertEquals(null, qrCode.getLongitude());

        // test value
        qrCode.setLongitude(-122.084);
        Assertions.assertEquals(-122.084, qrCode.getLongitude());
    }

    /**
     * @reference this website for verifying "ultimate nasty string" test as even Bash's Echo | SHA256 stopped reading with ';' chars inserted
     * <a href="https://emn178.github.io/online-tools/sha256.html">verified website output matched all other previous outputs</a>
     */
    @Test
    public void testSetHash() {

        // test string from eclass
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        Assertions.assertEquals("696ce4dbd7bb57cbfe58b64f530f428b74999cb37e2ee60980490cd9552de3a6", qrCode.getHash());

        // test regular string w/o new line
        QRCode qrCode4 = mockQRCode("test");
        Assertions.assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", qrCode4.getHash());

        // test empty string
        QRCode qrCode2 = mockQRCode("");
        Assertions.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", qrCode2.getHash());

        // test emojis w/ \n
        QRCode qrCode3 = mockQRCode("😭😭😭😭😭😭😭😭\n");
        Assertions.assertEquals("17acdb4cceb0c4999372cbf0ae6b6298e3d710e8c6e38cec2a6036cf55f5394f", qrCode3.getHash());

        // test emojis w/o \n
        QRCode qrCode5 = mockQRCode("😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("05a62d8d5ae9aa2850e993e64cbb128838b0c8dc34c136fbb271a62e72e2ecc0", qrCode5.getHash());

        // test random symbols
        QRCode qrCode6 = mockQRCode("aergkjankASKJSDN#^&*^$*@$;'[[]{}{}\\\\||🎉");
        Assertions.assertEquals("eeb057e85460be1983e5e9d03fe63b6e04b794ad8377c43dc2f029c31d9106f5", qrCode6.getHash());

        // test LONG string
        QRCode qrCode7 = mockQRCode("aegaergkjaehrlgkaherlighaleiruhgliaerhgoiaeropgjoiaerjogijaeiorargaergaklerjgioaejriogjioaerogiaergaergaerggaloerjgoaierjgaerjgoiaerjgoiaerg");
        Assertions.assertEquals("df85d9f4a371f20694e0fce3c7735eb71dca7827d3a300d4c5fe5510fc85f0da", qrCode7.getHash());

        // ultimate nasty string
        QRCode qrCode8 = mockQRCode("u*SAt<BX<%P{!aUaIjyD?LkD`.DuY.>buZn\\'A6%W[nd<QDC];L#g)\"fURP:ddwOqZHTy`:del?a:,HnO}]7kd`elp_\"$ru/lg,P[$VU{XD{Ev^5L#C^K_yd//^#!$nu!9b%Fxz#4O>3r8D/p8x`VwpJ}]J@w{QR1s`2ZUCufs^3Jy&6_]V\\\\(d{T7tu.WReBE!iDhYQ;!uO+T\"nP9x5)<FyeE`K9{>!!:dFix[4#o1Zsj\\RdP@CjR>v]#RTV]^>}Rl6aQa<}s,PeT|X\"7E]Lv\\Av0|,QeH#]AO`(UD5g1y|ugghXivv1\\4a2#uM>Adv2gq$16O75ZyNQ)FJF@*o?).JULn\\jq1#%oqK#J_:!<sMa<^'y[[>|jG(\"@+MV+%NeDPQ5Y!pUQ'3HvXHj$SZk7VUgnMzz/Ve!>?v0x+h\\p(NkCN:zj@^S0dXv5dSfx..uB\\\\*[\"[lK`\"o_]$y]K4;s3gNo}yu/P^FjhDY.Cg0@?!2qxK,^4KzYT7J,0MU8<Vb8*I`DJEk7.V`fZs[mvYcSYwX3_#KA]UN>nL]TE2Vk]P]}[]](:@z[dxK8OQn#jbW]dx3Y8by{*#wX1xl5CDpCR|8Bh_:H{WT+)ArV@FDo,o<$zL\\Lm?`oxZa<)(>hp:s>1ulvPvb7G(3mpY5pBGy)mzQ+D*Y}'`gd)d_2eL'>mgM,F]UapFJb]D&Rn[FIKfHR[Uf]NI+tGZ2u46@aZ/ejmhS;prV\\Z,q1_\\F+9]&mr5v`>2f%#;k,2F[k/H\\`c0PNafc+De9r#t2YiV)K6R1ZzTNZev^wC`[_ec2{Z+tS0uKg{?s|\"/j3m3QEtj'e+F{G>PyuBuJH*I*FcX#!'i[?wq\\\\c_v;Z+T]woa&xte[nM}n0i%xdt(UG{UpZ(H7i9Sf/.0A[2IR*%B{C8xasWaVrS*$vtAFl93qWeH.9zFn.>plehw)59>K_@^%\\&MOsq`[O|oWg%GT2_[VW`*cC+_[f?pI]{w4!pjH7oZjx<\n");
        Assertions.assertEquals("8fa598cc9ccec1b254e124620a86d9e79768c6f8c6fc59b06980fc25920b09fe", qrCode8.getHash());

        // test emojis w/o \n
        // uniqueName() in QRCode constructor crashes program with this input string,
        // if you run this test with uniqueName disabled, assertion passes
        // problem resolved test, now passes
        QRCode qrCode9 = mockQRCode("😭😭😭😭");
        Assertions.assertEquals("d1a885c6433d38a8cd1c486196a3b5fe6cb7105ad8f9e30bde61680b43adc4dd", qrCode9.getHash());
    }

    @Test
    public void testGetNumScans() {
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        Assertions.assertEquals(1, qrCode.getNumberOfScans());
    }

    @Test
    public void testGetSetCountry() {
        // test initial state
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        Assertions.assertEquals(null, qrCode.getCountry());

        // test empty string
        qrCode.setCountry("");
        Assertions.assertEquals("", qrCode.getCountry());

        // case realistic
        qrCode.setCountry("Zimbabwe");
        Assertions.assertEquals("Zimbabwe", qrCode.getCountry());

        // case set emojis
        qrCode.setCountry("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", qrCode.getCountry());

        // case long string w/ symbols
        qrCode.setCountry("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", qrCode.getCountry());


    }

    @Test
    public void testGetSetAdminArea() {
        // test initial state
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        Assertions.assertEquals(null, qrCode.getAdminArea());

        // test empty string
        qrCode.setAdminArea("");
        Assertions.assertEquals("", qrCode.getAdminArea());

        // case realistic
        qrCode.setAdminArea("Zimbabwe");
        Assertions.assertEquals("Zimbabwe", qrCode.getAdminArea());

        // case set emojis
        qrCode.setAdminArea("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", qrCode.getAdminArea());

        // case long string w/ symbols
        qrCode.setAdminArea("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", qrCode.getAdminArea());

    }



    @Test
    public void testGetSetSubAdminArea() {
        // test initial state
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        Assertions.assertEquals(null, qrCode.getSubAdminArea());

        // test empty string
        qrCode.setSubAdminArea("");
        Assertions.assertEquals("", qrCode.getSubAdminArea());

        // case realistic
        qrCode.setSubAdminArea("Zimbabwe");
        Assertions.assertEquals("Zimbabwe", qrCode.getSubAdminArea());

        // case set emojis
        qrCode.setSubAdminArea("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", qrCode.getSubAdminArea());

        // case long string w/ symbols
        qrCode.setSubAdminArea("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", qrCode.getSubAdminArea());

    }


    @Test
    public void testGetSetLocality() {
        // test initial state
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        Assertions.assertEquals(null, qrCode.getLocality());

        // test empty string
        qrCode.setLocality("");
        Assertions.assertEquals("", qrCode.getLocality());

        // case realistic
        qrCode.setLocality("Zimbabwe");
        Assertions.assertEquals("Zimbabwe", qrCode.getLocality());

        // case set emojis
        qrCode.setLocality("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", qrCode.getLocality());

        // case long string w/ symbols
        qrCode.setLocality("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", qrCode.getLocality());

    }


    @Test
    public void testGetSetSubLocality() {
        // test initial state
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        Assertions.assertEquals(null, qrCode.getSubLocality());

        // test empty string
        qrCode.setSubLocality("");
        Assertions.assertEquals("", qrCode.getSubLocality());

        // case realistic
        qrCode.setSubLocality("Zimbabwe");
        Assertions.assertEquals("Zimbabwe", qrCode.getSubLocality());

        // case set emojis
        qrCode.setSubLocality("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", qrCode.getSubLocality());

        // case long string w/ symbols
        qrCode.setSubLocality("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", qrCode.getSubLocality());

    }



    @Test
    public void testGetSetPostalCode() {
        // test initial state
        QRCode qrCode = mockQRCode("BFG5DGW54\n");
        Assertions.assertEquals(null, qrCode.getPostalCode());

        // test empty string
        qrCode.setPostalCode("");
        Assertions.assertEquals("", qrCode.getPostalCode());

        // case realistic
        qrCode.setPostalCode("T6W 0L7");
        Assertions.assertEquals("T6W 0L7", qrCode.getPostalCode());

        // case set emojis
        qrCode.setPostalCode("😭😭😭😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", qrCode.getPostalCode());

        // case long string w/ symbols
        qrCode.setPostalCode("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
        Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", qrCode.getPostalCode());

      }

     @Test
    public void testSetPostalCodePrefix() {
         // test initial state
         QRCode qrCode = mockQRCode("BFG5DGW54\n");
         Assertions.assertEquals(null, qrCode.getPostalCodePrefix());

         // test empty string
         qrCode.setPostalCodePrefix("");
         Assertions.assertEquals("", qrCode.getPostalCodePrefix());

         // case realistic
         qrCode.setPostalCodePrefix("T6W");
         Assertions.assertEquals("T6W", qrCode.getPostalCodePrefix());

         // case set emojis
         qrCode.setPostalCodePrefix("😭😭😭😭😭😭😭😭😭😭😭");
         Assertions.assertEquals("😭😭😭😭😭😭😭😭😭😭😭", qrCode.getPostalCodePrefix());

         // case long string w/ symbols
         qrCode.setPostalCodePrefix("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0");
         Assertions.assertEquals("aerguiahlerighluiaehrguihaleirughliuaehrguihILUAIUHLIUSHLDUIHALIUHFLUIEHRFLIUWEHLFhiuhraewgfhaelrighnlaierhngliuaehgluaeirg./.'')((@*()(#*&*(-0", qrCode.getPostalCodePrefix());

     }

}
