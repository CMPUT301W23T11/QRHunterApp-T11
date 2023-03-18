package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * This Class is used to model a QR Code. Calculates its hash value, points, unique name, and unique image on construction.
 */
public class QRCode {
    private String hash;
    private String name;
    private int points;
    private Double longitude;
    private Double latitude;
    private ArrayList<Integer> faceList;
    private ArrayList<String> photoList;

    /**
     * Main QRCode class constructor
     * takes an arbitrary string (supplied by scanning a qr w/ camera) as input and
     * sets all other class attributes using the generated hash of string
     *
     * @param raw - a raw string to be hashed
     */
    public QRCode(@NonNull String raw) {
        // mandatory exception due to message digest library
        try {
            this.hash = setHash(raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        this.points = calculatePoints(hash);
        this.name = uniqueName(hash);
        this.faceList = uniqueImage(hash);
        this.photoList = new ArrayList<>();
        this.longitude = null;
        this.latitude = null;
        // if for some reason null doesn't work out, we can fall back on init to 0
        //this.longitude = Double.valueOf(0);
        //this.latitude = Double.valueOf(0);
    }

    /**
     * special blank constructor for Firebase
     *
     * @reference <a href="https://firebase.google.com/docs/firestore/manage-data/add-data#custom_objects">implemented by referencing firebase documentation</a>
     */
    public QRCode() {
    }

    /**
     * calculatePoints uses the hash value of the QRCode to calculate the points value of the QRCode
     * Static to increase testability
     *
     * @param hash - String hash value of the QR code
     * @return grandTotal - int
     * @reference <a href="https://docs.oracle.com/javase/tutorial/java/data/manipstrings.html">Oracle's documentation on string manipulation</a>
     */
    public static int calculatePoints(@NonNull String hash) {

        // List of possible chars in the hash, other than 0, each corresponds to their base number of points
        Character[] values = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int pointMultiplier = 1;
        int totalPoints = 0;
        int numberOfZero = 0;

        // Iterate through hash string and count every 0 in variable numberOfZero
        for (int k = 0; k < hash.length(); k++) {
            Character hashChar = hash.charAt(k);
            if (hashChar.equals('0')) {
                numberOfZero++;
            }
        }

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

                    // Edge case for the final iteration: calculate based on current pointMultiplier and return pointMultiplier to 1
                    if ((j == (hash.length() - 1))) {
                        if (i == 0) {
                            totalPoints += Math.pow(20, pointMultiplier - 1);
                            // if there are may 0s in a row, subtract the duplicates from numberOfZero to get only the number of single 0s
                            numberOfZero -= pointMultiplier;
                        } else {
                            totalPoints += Math.pow(i, pointMultiplier - 1);
                        }
                        pointMultiplier = 1;
                    }
                }
                // Calculate updated total based on current pointMultiplier and return pointMultiplier to 1
                // if a character in the hash string does not equal the current value char or is not equal to the previous char in the hash
                else {
                    if (pointMultiplier > 1) {
                        if (i == 0) {
                            totalPoints += Math.pow(20, pointMultiplier - 1);
                            numberOfZero -= pointMultiplier;
                        } else {
                            totalPoints += Math.pow(i, pointMultiplier - 1);
                        }
                        pointMultiplier = 1;
                    }
                }
                // move previous char forwards
                previousChar = hashChar;
            }
        }
        // add single 0s to the total
        int grandTotal = (totalPoints + numberOfZero);

        //Max points will be 1E9
        if (grandTotal > Math.pow(10, 8)) {
            grandTotal = (int) Math.pow(10, 8);
        }
        return grandTotal;
    }

    /**
     * uniqueImage uses the 6 bits of a shortened hash function to determine which drawables will be used to make the unique image
     * Static to increase testability
     *
     * @param hash - String hash value of the QR code
     * @return faceList - ArrayList of drawables to form the image
     * @reference educative.io <a href="https://www.educative.io/answers/how-to-convert-an-integer-to-binary-in-java">for converting integer to binary</a>
     * @reference techiedelight.com <a href="https://www.techiedelight.com/convert-hex-string-to-integer-java/">for converting string to hexadecimal integer</a>
     */
    @NonNull
    public static ArrayList<Integer> uniqueImage(@NonNull String hash) {

        //Lists of possible drawables for each part of the face
        int[] eyesNumbers = {
                R.drawable.eyes1,
                R.drawable.eyes2
        };
        int[] eyebrowsNumbers = {
                R.drawable.eyebrows1,
                R.drawable.eyebrows2
        };
        int[] colourNumbers = {
                R.drawable.colour1,
                R.drawable.colour2
        };
        int[] noseNumbers = {
                R.drawable.nose1,
                R.drawable.nose2
        };
        int[] mouthNumbers = {
                R.drawable.mouth1,
                R.drawable.mouth2
        };
        int[] faceNumbers = {
                R.drawable.face1,
                R.drawable.face2
        };

        ArrayList<Integer> faceList = new ArrayList<>();
        // Shorten hash and convert it to binary, remove the first 1 from the binary string
        int hashSmall = Integer.parseInt(hash.substring(0, 5), 16);
        String hashBinary = Integer.toBinaryString(hashSmall).substring(1);

        // Choose what element in each face list to add to the complete faceList array
        faceList.add(eyesNumbers[hashBinary.charAt(0) - '0']);
        faceList.add(eyebrowsNumbers[hashBinary.charAt(1) - '0']);
        faceList.add(colourNumbers[hashBinary.charAt(2) - '0']);
        faceList.add(noseNumbers[hashBinary.charAt(3) - '0']);
        faceList.add(mouthNumbers[hashBinary.charAt(4) - '0']);
        faceList.add(faceNumbers[hashBinary.charAt(5) - '0']);
        return faceList;
    }

    /**
     * Uses the 6 bits of a shortened hash function to determine which drawables will be used to make the unique image
     * Static to increase testability
     *
     * @param hash - String hash value of the QR code
     * @return newName - String of the QRCode's name
     */
    @NonNull
    public static String uniqueName(@NonNull String hash) {

        String[] nameParts = {
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

        String newName = "";
        // Shorten hash and convert it to binary, remove the first 1 from the binary string
        int hashSmall = Integer.parseInt(hash.substring(0, 6), 16);
        String hashBinary = Integer.toBinaryString(hashSmall).substring(1);

        // iterate through binary in groups of 2, selecting name parts based on the permutation of the 2 bits. j holds the place of the name group within nameParts
        for (int i = 0, j = 0; i < 12; i += 2, j += 4) {

            if ((hashBinary.charAt(i) == '0') && (hashBinary.charAt(i + 1) == '0')) {
                newName = newName + nameParts[j];
            } else if ((hashBinary.charAt(i) == '0') && (hashBinary.charAt(i + 1) == '1')) {
                newName = newName + nameParts[j + 1];
            } else if ((hashBinary.charAt(i) == '1') && (hashBinary.charAt(i + 1) == '0')) {
                newName = newName + nameParts[j + 2];
            } else if ((hashBinary.charAt(i) == '1') && (hashBinary.charAt(i + 1) == '1')) {
                newName = newName + nameParts[j + 3];
            }
        }
        return newName;
    }

    /**
     * Getter for QRCode's points attribute
     *
     * @return points - integer amount of points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Getter for QRCode's name attribute
     *
     * @return name - string representing a human readable name for the QRCode
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Getter for QRCode's photoList attribute
     *
     * @return photoList - an ArrayList containing links to all photos associated with QRCode
     */
    @NonNull
    public ArrayList<String> getPhotoList() {
        return photoList;
    }

    /**
     * Setter for QRCode's photoList attribute
     *
     * @param photoList - an ArrayList containing links of photos to be added to QRCode
     */
    public void setPhotoList(@NonNull ArrayList<String> photoList) {
        this.photoList = photoList;
    }

    /**
     * Getter for QRCode Object's Longitude attribute, which represents the
     * Longitude where the code was scanned. initialized at 0
     * @return longitude - a string ArrayList with 2 elements representing [Longitude , Latitude ]
     */
    @NonNull
    public Double getLongitude() {
        return this.longitude;
    }

    /**
     * Setter for QRCode Object's longitude attribute, which represents the
     * Longitude where the code was scanned.
     * @param longitude - longitude of the Location where QR was scanned

     */
    public void setLongitude(@NonNull Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Setter for QRCode Object's latitude attribute, which represents the
     * Latitude where the code was scanned.
     * @param latitude - latitude of the location where QR was scanned
     */
    public void setLatitude(@NonNull Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Getter for QRCode Object's latitude attribute, which represents the
     * Latitude where the code was scanned. initialized at 0
     * @return latitude - a string ArrayList with 2 elements representing [Longitude , Latitude ]
     */
    @NonNull
    public Double getLatitude() {
        return this.latitude;
    }

    /**
     * Getter for QRCode Object's faceList attribute
     *
     * @return faceList - An ArrayList containing information to create face visual display
     */
    @NonNull
    public ArrayList<Integer> getFaceList() {
        return faceList;
    }


    /**
     * Setter for QRCode Object's hash attribute
     * takes a string and runs it though Java's built in SHA256 hash algorithm
     *
     * @param input - string provided by a QR code camera scan
     * @return output - a string of the hex representation of the int generated by SHA-256
     * @throws NoSuchAlgorithmException but is impossible, as SHA-256 is the hardcoded choice
     * @reference found Oracle's documentation on hash algorithms and MessageDigest's convoluted
     * <a href="https://docs.oracle.com/javase/9/docs/api/java/security/MessageDigest.html"></a>
     * googling found more information on MessageDigest's in the following tutorial:
     * <a href="https://www.tutorialspoint.com/java_cryptography/java_cryptography_message_digest.htm"></a>
     * *no author or date listed
     * discovered toHexString() drops leading zero, implemented formatting solution found here:
     * <a href="https://stackoverflow.com/questions/8689526/integer-to-two-digits-hex-in-java"></a>
     * by user GabrielOshiro, dated Oct 10th, 2013
     */
    @NonNull
    public String setHash(@NonNull String input) throws NoSuchAlgorithmException {
        // select SHA-256 hash algorithm and convert input string into a byte array "digest"
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input.getBytes());
        byte[] digest = md.digest();

        // iterate through array and covert each ith byte to a string of byte's hex rep
        StringBuffer hexString = new StringBuffer();    //mutable string
        for (int i = 0; i < digest.length; i++) {
            hexString.append(String.format("%02x", (0xFF & digest[i])));
        }
        // convert to immutable string and return output
        return hexString.toString();
    }

    /**
     * Getter for QRCode Object's Hash value
     *
     * @return hash - a string representation of the Hash
     */
    @NonNull
    public String getHash() {
        return hash;
    }

}