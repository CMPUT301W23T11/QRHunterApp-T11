package com.example.qrhunterapp_t11;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This Class is used to model a QR Code. Calculates its hash value, points, unique name, and unique image on construction.
 */
public class QRCode{
    private String hash;
    private String name;
    private int points;
    private Location geolocation;
    private ArrayList<Integer> faceList;
    private ArrayList<Comment> commentList;
    private ArrayList<String> photoList;

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

    public QRCode(String raw) {
        // mandatory exception due to message digest library
        try {
            this.hash = setHash(raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        this.points = calculatePoints(hash);
        this.name = uniqueName();
        this.faceList = uniqueImage();
        this.commentList = new ArrayList<Comment>();
        this.photoList = new ArrayList<String>();
        this.geolocation = null;
    }

    // special blank constructor for Firebase
    public QRCode(){
    }

    public int getPoints() {
        return points;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(ArrayList<String> photoList) {
        this.photoList = photoList;
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
    public void setHashDebug(String hash){
        this.hash = hash;
    }

    /* I don't think we're going to need these
    public int[] getEyesNumbers() {return eyesNumbers;}

    public int[] getEyebrowsNumbers() {return eyebrowsNumbers;}

    public int[] getColourNumbers() {return colourNumbers;}

    public int[] getNoseNumbers() {return noseNumbers;}

    public int[] getMouthNumbers() {return mouthNumbers;}

    public int[] getFaceNumbers() {return faceNumbers;}

    public String[] getNameParts() {return nameParts;}

    */

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
    public String setHash(String input) throws NoSuchAlgorithmException {
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

    public String getHash() { return hash;}

    /**
     * calculatePoints uses the hash value of the QRCode to calculate the points value of the QRCode
     *
     * @reference Oracle's documentation on string manipulation https://docs.oracle.com/javase/tutorial/java/data/manipstrings.html
     *
     * @return Returns the totalPoints int
     */
    public static int calculatePoints(String hash) {

        // L of possible chars in the hash, other than 0, each corresponds to their base number of points
        Character[] values = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int pointMultiplier = 1;
        int totalPoints = 0;
        int numberOfZero = 0;

        // Iterate through hash string and count every 0
        for (int k = 0; k < hash.length(); k++) {
            Character hashChar = hash.charAt(k);
            if (hashChar.equals('0')){
                numberOfZero ++;
            }
        }

        System.out.println(numberOfZero);

        // Iterate through each character in values
        for (int i = 0; i < values.length; i++) {
            // Set previous char to a default unused char to handle edge cases
            Character previousChar = 'X';

            // Iterate through hash string
            for (int j = 0; j < hash.length(); j++) {
                Character hashChar = hash.charAt(j);

                // If a character in the hash string equals the current value char as well as is equal to the previous char in the hash, increase the point multiplier.
                if ((hashChar.equals(values[i])) && (hashChar.equals(previousChar))) {
                    pointMultiplier++;
                    System.out.println("digit " + values[i] + "points multiplier: " + pointMultiplier);

                    // Edge case for the final iteration: calculate based on current pointMultiplier and return pointMultiplier to 1
                    if ((j == (hash.length() - 1))) {
                        if (i == 0) {
                            totalPoints += Math.pow(20, pointMultiplier - 1);
                            numberOfZero -= pointMultiplier;
                        } else {
                            totalPoints += Math.pow(i, pointMultiplier - 1);
                            System.out.println(totalPoints);
                        }
                        pointMultiplier = 1;
                    }
                } else {
                    if (pointMultiplier > 1) {
                        if (i == 0) {
                            totalPoints += Math.pow(20, pointMultiplier - 1);
                            numberOfZero -= pointMultiplier;
                        } else {
                            totalPoints += Math.pow(i, pointMultiplier - 1);
                            System.out.println(totalPoints);
                        }
                        pointMultiplier = 1;
                    }
                }
                previousChar = hashChar;
            }
        }
        System.out.println(totalPoints + " at end");
        System.out.println(numberOfZero + " at end");
        System.out.println(numberOfZero + totalPoints + " at end");
        return (totalPoints + numberOfZero);
    }


    /**
     * uniqueImage uses the 6 bits of a shortened hash function to determine which drawables will be used to make the unique image
     *
     * @reference educative.io https://www.educative.io/answers/how-to-convert-an-integer-to-binary-in-java for converting integer to binary, License: Creative Commons-Attribution-ShareAlike 4.0 (CC-BY-SA 4.0)
     * @reference techiedelight.com https://www.techiedelight.com/convert-hex-string-to-integer-java/ for converting string to hexadecimal integer
     *
     * @return Returns an ArrayList of drawables to form the image
     */

    private ArrayList<Integer> uniqueImage() {

        int hashSmall = Integer.parseInt(this.hash.substring(0, 5), 16);

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
     *
     * @return Returns an ArrayList of drawables to form the image
     */

    private String uniqueName(){

        String newName = "";
        Integer hashSmall = Integer.parseInt(this.hash.substring(1,6), 16);
        String hashBinary = Integer.toBinaryString(hashSmall);

        for (int i = 0, j = 0; i <= 17; i = i + 3, j = j + 4){

            Integer num = Integer.parseInt(String.valueOf(hashBinary.charAt(i))) + Integer.parseInt(String.valueOf(hashBinary.charAt(i+1))) + Integer.parseInt(String.valueOf(hashBinary.charAt(i+2)));
            //System.out.println("hash " + hashBinary);
           // System.out.println("num: " + num);
            if (num == 0){
                newName = newName + nameParts[j];
            }
            else if (num == 1){
                newName = newName + nameParts[j+1];
            }
            else if (num == 2){
                newName = newName + nameParts[j+2];
            }
            else if (num == 3) {
                newName = newName + nameParts[j+3];
            }
           // System.out.println("new part " + newName + " from i = " + i);
        }
        return newName;
    }

}