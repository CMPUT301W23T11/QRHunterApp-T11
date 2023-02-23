package com.example.qrhunterapp_t11;

import android.location.Location;
import java.util.ArrayList;

/**
 * QRCode class (INCOMPLETE)
 * To do: implement geolocation, implement unique name
 */
public class QRCode {
    private String hash;

    private int points;

    private Location geolocation;

    private ArrayList<Integer> faceList;

    private String name;

    private int eyesNumbers [] = {
            R.drawable.eyes1,
            R.drawable.eyes2
    };
    private int eyebrowsNumbers [] = {
            R.drawable.eyebrows1,
            R.drawable.eyebrows2
    };
    private int colourNumbers [] = {
            R.drawable.colour1,
            R.drawable.colour2
    };
    private int noseNumbers [] = {
            R.drawable.nose1,
            R.drawable.nose2
    };
    private int mouthNumbers [] = {
            R.drawable.mouth1,
            R.drawable.mouth2
    };
    private int faceNumbers [] = {
            R.drawable.face1,
            R.drawable.face2
    };

    public QRCode(String hash) {
        this.hash = hash;
        this.points = calculatePoints(hash);
        this.name = "Test Name";
        this.faceList = uniqueImage(hash);
    }

    public int getPoints() {
        return points;
    }
    public ArrayList<Integer> getFaceList() {
        return faceList;
    }

//https://docs.oracle.com/javase/tutorial/java/data/manipstrings.html

    /**
     * calculatePoints uses the hash value of the QRCode to calculate the points value of the QRCode
     * @param hash
     * hash is a String identifying the QRCode
     * Will probably need to have geolocation as a parameter also
     * @return
     * Returns the totalPoints int
     */
    private int calculatePoints(String hash){

        Character[] values = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int pointMultiplier = 1;
        int totalPoints = 0;

        for(int i = 0; i<values.length; i++){
            Character previousChar = 'X';

            for(int j = 0; j<hash.length(); j++){
                Character hashChar = hash.charAt(j);

                if ((hashChar.equals(values[i])) && (hashChar.equals(previousChar))){

                    pointMultiplier++;

                    if((j == (hash.length() -1))){
                        if (i == 0) {
                            totalPoints += Math.pow(20, pointMultiplier - 1);
                        } else {
                            totalPoints += Math.pow(i, pointMultiplier - 1);
                        }
                        pointMultiplier = 1;
                    }
                }else{
                    if(pointMultiplier > 1){
                        if(i == 0) {
                            totalPoints += Math.pow(20, pointMultiplier - 1);
                        }else{
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
     * @param hash
     * hash is a String identifying the QRCode
     * @return
     * Returns an ArrayList of drawables to form the image
     */
    private ArrayList<Integer> uniqueImage(String hash){

        int hashSmall = Integer.parseInt(hash.substring(0,5), 16);

        ArrayList<Integer> faceList = new ArrayList<>();

        String hashBinary = Integer.toBinaryString(hashSmall);
        faceList.add(eyesNumbers[hashBinary.charAt(0)-'0']);
        faceList.add(eyebrowsNumbers[hashBinary.charAt(1)-'0']);
        faceList.add(colourNumbers[hashBinary.charAt(2)-'0']);
        faceList.add(noseNumbers[hashBinary.charAt(3)-'0']);
        faceList.add(mouthNumbers[hashBinary.charAt(4)-'0']);
        faceList.add(faceNumbers[hashBinary.charAt(5)-'0']);
        return faceList;
    }
}