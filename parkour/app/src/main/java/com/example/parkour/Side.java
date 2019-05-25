package com.example.parkour;

public class Side {


    public Side(Double lat1, Double lng1, Double lat2, Double lng2) {
        this.lat1 = lat1;
        this.lng1 = lng1;
        this.lat2 = lat2;
        this.lng2 = lng2;
    }

    public Double getLat1() {
        return lat1;
    }

    public void setLat1(Double lat1) {
        this.lat1 = lat1;
    }

    public Double getLng1() {
        return lng1;
    }

    public void setLng1(Double lng1) {
        this.lng1 = lng1;
    }

    public Double getLat2() {
        return lat2;
    }

    public void setLat2(Double lat2) {
        this.lat2 = lat2;
    }

    public Double getLng2() {
        return lng2;
    }

    public void setLng2(Double lng2) {
        this.lng2 = lng2;
    }

    private Double lat1;
    private Double lng1;
    private Double lat2;
    private Double lng2;


}
