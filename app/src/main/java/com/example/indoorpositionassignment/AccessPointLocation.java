package com.example.indoorpositionassignment;

public class AccessPointLocation {

    private String BSSID;
    private String desc;
    private int x, y;
    private int floor;
    private double distance;

    public AccessPointLocation(String BSSID, String desc, int x, int y, int floor) {
        this.BSSID = BSSID;
        this.desc = desc;
        this.x = x;
        this.y = y;
        this.floor = floor;
        this.distance = 0.0;
    }

    public void setDistance(double distance) { this.distance = distance; }

    public String getBSSID() {
        return BSSID;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getDistance() { return distance; }
}
