package com.example.qrhunterapp_t11;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(8840, points);
    }

    /**
     * Used this website for "ultimate nasty string" test as even Bash's Echo | SHA256 stopped reading with ';' chars inserted
     * https://emn178.github.io/online-tools/sha256.html (verified website output matched all other previous outputs)
     */
    @Test
    public void testSetHash(){

        // test string from eclass
        QRCode qrCode = mockQR("BFG5DGW54\n");
        Assertions.assertEquals("696ce4dbd7bb57cbfe58b64f530f428b74999cb37e2ee60980490cd9552de3a6", qrCode.getHash());

        // test regular string w/o new line
        QRCode qrCode4 = mockQR("test");
        Assertions.assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", qrCode4.getHash());

        // test empty string
        QRCode qrCode2 = mockQR("");
        Assertions.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", qrCode2.getHash());

        // test emojis w/ \n
        QRCode qrCode3 = mockQR("😭😭😭😭😭😭😭😭\n");
        Assertions.assertEquals("17acdb4cceb0c4999372cbf0ae6b6298e3d710e8c6e38cec2a6036cf55f5394f", qrCode3.getHash());

        // test emojis w/o \n
        QRCode qrCode5 = mockQR("😭😭😭😭😭😭😭😭");
        Assertions.assertEquals("05a62d8d5ae9aa2850e993e64cbb128838b0c8dc34c136fbb271a62e72e2ecc0", qrCode5.getHash());

        // test random symbols
        QRCode qrCode6 = mockQR("aergkjankASKJSDN#^&*^$*@$;'[[]{}{}\\\\||🎉");
        Assertions.assertEquals("eeb057e85460be1983e5e9d03fe63b6e04b794ad8377c43dc2f029c31d9106f5", qrCode6.getHash());

        // test LONG string
        QRCode qrCode7 = mockQR("aegaergkjaehrlgkaherlighaleiruhgliaerhgoiaeropgjoiaerjogijaeiorargaergaklerjgioaejriogjioaerogiaergaergaerggaloerjgoaierjgaerjgoiaerjgoiaerg");
        Assertions.assertEquals("df85d9f4a371f20694e0fce3c7735eb71dca7827d3a300d4c5fe5510fc85f0da", qrCode7.getHash());

        // ultimate nasty string
        QRCode qrCode8 = mockQR("u*SAt<BX<%P{!aUaIjyD?LkD`.DuY.>buZn\\'A6%W[nd<QDC];L#g)\"fURP:ddwOqZHTy`:del?a:,HnO}]7kd`elp_\"$ru/lg,P[$VU{XD{Ev^5L#C^K_yd//^#!$nu!9b%Fxz#4O>3r8D/p8x`VwpJ}]J@w{QR1s`2ZUCufs^3Jy&6_]V\\\\(d{T7tu.WReBE!iDhYQ;!uO+T\"nP9x5)<FyeE`K9{>!!:dFix[4#o1Zsj\\RdP@CjR>v]#RTV]^>}Rl6aQa<}s,PeT|X\"7E]Lv\\Av0|,QeH#]AO`(UD5g1y|ugghXivv1\\4a2#uM>Adv2gq$16O75ZyNQ)FJF@*o?).JULn\\jq1#%oqK#J_:!<sMa<^'y[[>|jG(\"@+MV+%NeDPQ5Y!pUQ'3HvXHj$SZk7VUgnMzz/Ve!>?v0x+h\\p(NkCN:zj@^S0dXv5dSfx..uB\\\\*[\"[lK`\"o_]$y]K4;s3gNo}yu/P^FjhDY.Cg0@?!2qxK,^4KzYT7J,0MU8<Vb8*I`DJEk7.V`fZs[mvYcSYwX3_#KA]UN>nL]TE2Vk]P]}[]](:@z[dxK8OQn#jbW]dx3Y8by{*#wX1xl5CDpCR|8Bh_:H{WT+)ArV@FDo,o<$zL\\Lm?`oxZa<)(>hp:s>1ulvPvb7G(3mpY5pBGy)mzQ+D*Y}'`gd)d_2eL'>mgM,F]UapFJb]D&Rn[FIKfHR[Uf]NI+tGZ2u46@aZ/ejmhS;prV\\Z,q1_\\F+9]&mr5v`>2f%#;k,2F[k/H\\`c0PNafc+De9r#t2YiV)K6R1ZzTNZev^wC`[_ec2{Z+tS0uKg{?s|\"/j3m3QEtj'e+F{G>PyuBuJH*I*FcX#!'i[?wq\\\\c_v;Z+T]woa&xte[nM}n0i%xdt(UG{UpZ(H7i9Sf/.0A[2IR*%B{C8xasWaVrS*$vtAFl93qWeH.9zFn.>plehw)59>K_@^%\\&MOsq`[O|oWg%GT2_[VW`*cC+_[f?pI]{w4!pjH7oZjx<\n");
        Assertions.assertEquals("8fa598cc9ccec1b254e124620a86d9e79768c6f8c6fc59b06980fc25920b09fe", qrCode8.getHash());

        // test emojis w/o \n
        // uniqueName() in QRCode constructor crashes program with this input string,
        // if you run this test with uniqueName disabled, assertion passes
        //QRCode qrCode9 = mockQR("😭😭😭😭");
        //Assertions.assertEquals("d1a885c6433d38a8cd1c486196a3b5fe6cb7105ad8f9e30bde61680b43adc4dd", qrCode9.getHash());
    }
}