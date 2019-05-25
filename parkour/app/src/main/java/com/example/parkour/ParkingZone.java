package com.example.parkour;

import android.util.Pair;

import java.util.ArrayList;

public class ParkingZone {

    private ArrayList<Side> sides;
    private int timeLimit;
    private String validPermit;
    private int fineAmount;

    public ParkingZone() {
        this.sides = new ArrayList<Side>();
        this.timeLimit = 16;
        this.validPermit = "C";
        this.fineAmount = 0;
    }

    public ParkingZone(ArrayList<Side> sides, int timeLimit, String validZone, int fineAmount) {
        this.sides = sides;
        this.timeLimit = timeLimit;
        this.validPermit = validZone;
        this.fineAmount = fineAmount;
    }

    public ArrayList<Side> getSides() {
        return sides;
    }

    public void setSides(ArrayList<Side> sides) {
        this.sides = sides;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getValidPermit() {
        return validPermit;
    }

    public void setValidPermit(String validZone) {
        this.validPermit = validZone;
    }

    public int getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(int fineAmount) {
        this.fineAmount = fineAmount;
    }

    public boolean withinZone(Double lng, Double lat) {
        return true;
    }

    public void addSide(Double lat1, Double lng1, Double lat2, Double lng2) {
        sides.add(new Side(lat1, lng1, lat2, lng2));
    }


}
