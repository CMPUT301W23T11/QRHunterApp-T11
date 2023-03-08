package com.example.qrhunterapp_t11;

import android.location.Location;

import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * QRCode class (INCOMPLETE)
 * To do: implement geolocation
 */
public class QRCode {
    private String hash;
    private String name;
    private int points;

    private Location geolocation;
    private ArrayList<Integer> faceList;

    private ArrayList<Comment> commentList;

    private int eyesNumbers[] = {
            R.drawable.eyes1,
            R.drawable.eyes2
    };
    private int eyebrowsNumbers[] = {
            R.drawable.eyebrows1,
            R.drawable.eyebrows2
    };
    private int colourNumbers[] = {
            R.drawable.colour1,
            R.drawable.colour2
    };
    private int noseNumbers[] = {
            R.drawable.nose1,
            R.drawable.nose2
    };
    private int mouthNumbers[] = {
            R.drawable.mouth1,
            R.drawable.mouth2
    };
    private int faceNumbers[] = {
            R.drawable.face1,
            R.drawable.face2
    };
    private String nameParts [] = {
            "Big ",
            "Little ",
            "Young ",
            "Old ",

            "La",
            "Bo",
            "We",
            "Si",

            "rg",
            "p",
            "ld",
            "gs",

            "a",
            "i",
            "o",
            "u",

            "men",
            "can",
            "yog",
            "rol",

            "gog",
            "mor",
            "tas",
            "fli"
    };

    public QRCode(String hash) {
        // mandatory exception due to message digest library
        try {
            this.hash = strToHash(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        this.points = calculatePoints(hash);
        System.out.println(points);
        this.name = uniqueName(hash);
        this.faceList = uniqueImage(hash);
        this.commentList = new ArrayList<Comment>();
    }

    public int getPoints() {
        return points;
    }
    public String getName() {
        return name;
    }

    public Location getLocation() { return geolocation;}
    public void setLocation(Location location) { this.geolocation = location;}
    public ArrayList<Integer> getFaceList() {
        return faceList;
    }

    public ArrayList<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(ArrayList<Comment> commentList) {
        this.commentList = commentList;
    }

    /**
     * takes a string and runs it though Java's built in SHA256 hash algorithm
     * references:
     * found Oracle's documentation on hash algorithms and MessageDigest's convoluted
     * https://docs.oracle.com/javase/9/docs/api/java/security/MessageDigest.html
     * googling found more information on MessageDigest's in the following tutorial:
     * https://www.tutorialspoint.com/java_cryptography/java_cryptography_message_digest.htm
     * *no author or date listed
     * discovered toHexString() drops leading zero, implemented formatting solution found here:
     * https://stackoverflow.com/questions/8689526/integer-to-two-digits-hex-in-java
     * by user GabrielOshiro, dated Oct 10th, 2013
     *
     * @param input - string provided by a QR code camera scan
     * @return output - a string of the hex representation of the int generated by SHA-256
     */
    public static String strToHash(String input) throws NoSuchAlgorithmException {
        // select SHA-256 hash algorithm and convert input string into a byte array "digest"
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input.getBytes());
        byte[] digest = md.digest();

        // iterate through array and covert each ith byte to a string of byte's hex rep
        StringBuffer hexString = new StringBuffer();    //mutable string
        for (int i = 0; i < digest.length; i++) {
            hexString.append(String.format("%02x", (0xFF & digest[i])));
        }
        // print to console log, can be removed later
        System.out.println(hexString);

        // convert to immutable string and return output
        String output = hexString.toString();
        return output;
    }

    /**
     * calculatePoints uses the hash value of the QRCode to calculate the points value of the QRCode
     * References: Oracle's documentation on string manipulation https://docs.oracle.com/javase/tutorial/java/data/manipstrings.html
     * @param hash hash is a String identifying the QRCode
     *             Will probably need to have geolocation as a parameter also
     * @return Returns the totalPoints int
     */
    public static int calculatePoints(String hash) {

        //list of possible chars in the hash, other than 0, each corresponds to their base number of points
        Character[] values = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int pointMultiplier = 1;
        int totalPoints = 0;

        //iterate through each character in values
        for (int i = 0; i < values.length; i++) {
            //set previous char to a default unused char to handle edge cases
            Character previousChar = 'X';

            //iterate through hash string
            for (int j = 0; j < hash.length(); j++) {
                Character hashChar = hash.charAt(j);

                //if a character in the hash string equals the current value char as well as is equal to the previous char in the hash, increase the point multiplier.
                if ((hashChar.equals(values[i])) && (hashChar.equals(previousChar))) {

                    pointMultiplier++;

                    if ((j == (hash.length() - 1))) {
                        if (i == 0) {
                            totalPoints += Math.pow(20, pointMultiplier - 1);
                        } else {
                            totalPoints += Math.pow(i, pointMultiplier - 1);
                        }
                        pointMultiplier = 1;
                    }
                } else {
                    if (pointMultiplier > 1) {
                        if (i == 0) {
                            totalPoints += Math.pow(20, pointMultiplier - 1);
                        } else {
                            totalPoints += Math.pow(i, pointMultiplier - 1);
                        }
                        pointMultiplier = 1;

                    }
                }
                previousChar = hashChar;
            }
        }
        return totalPoints;
    }

    /**
     * uniqueImage uses the 6 bits of a shortened hash function to determine which drawables will be used to make the unique image
     * References:
     * educative.io https://www.educative.io/answers/how-to-convert-an-integer-to-binary-in-java for converting integer to binary
     * License: Creative Commons-Attribution-ShareAlike 4.0 (CC-BY-SA 4.0)
     * techiedelight.com https://www.techiedelight.com/convert-hex-string-to-integer-java/ for converting string to hexadecimal integer
     * @param hash hash is a String identifying the QRCode
     * @return Returns an ArrayList of drawables to form the image
     */
    private ArrayList<Integer> uniqueImage(String hash) {

        int hashSmall = Integer.parseInt(hash.substring(0, 5), 16);

        ArrayList<Integer> faceList = new ArrayList<>();

        String hashBinary = Integer.toBinaryString(hashSmall);
        faceList.add(eyesNumbers[hashBinary.charAt(0) - '0']);
        faceList.add(eyebrowsNumbers[hashBinary.charAt(1) - '0']);
        faceList.add(colourNumbers[hashBinary.charAt(2) - '0']);
        faceList.add(noseNumbers[hashBinary.charAt(3) - '0']);
        faceList.add(mouthNumbers[hashBinary.charAt(4) - '0']);
        faceList.add(faceNumbers[hashBinary.charAt(5) - '0']);
        return faceList;
    }

    /**
     * uniqueImage uses the 6 bits of a shortened hash function to determine which drawables will be used to make the unique image
     * @param hash
     * hash is a String identifying the QRCode
     * @return
     * Returns an ArrayList of drawables to form the image
     */
    private String uniqueName(String hash){

        String newName = "";
        Integer hashSmall = Integer.parseInt(hash.substring(1,6), 16);
        String hashBinary = Integer.toBinaryString(hashSmall);

        for (int i = 0, j = 0; i <= 17; i = i + 3, j = j + 4){

            Integer num = Integer.parseInt(String.valueOf(hashBinary.charAt(i))) + Integer.parseInt(String.valueOf(hashBinary.charAt(i+1))) + Integer.parseInt(String.valueOf(hashBinary.charAt(i+2)));
            System.out.println("hash " + hashBinary);
            System.out.println("num: " + num);
            if (num == 0){
                newName = newName + nameParts[j];
            }
            else if (num == 1){
                newName = newName + nameParts[j];
            }
            else if (num == 2){
                newName = newName + nameParts[j];
            }
            else if (num == 3) {
                newName = newName + nameParts[j];
            }
            System.out.println("new part " + newName + " from i = " + i);
        }
        return newName;
    }
}