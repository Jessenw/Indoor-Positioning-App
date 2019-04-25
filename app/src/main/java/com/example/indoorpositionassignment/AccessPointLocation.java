package com.example.indoorpositionassignment;

public class AccessPointLocation {

    private String BSSID;
    private String desc;
    private double x, y; // in meters
    private int floor;
    private double distance;

    double toPixels = 7.4; // 1m = 7.4

    public AccessPointLocation(String BSSID, String desc, double x, double y, int floor) {
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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getCanvasX() { return (int) (this.x * toPixels); }

    public int getCanvasY() { return (int) (this.y * toPixels); }

    public double getDistance() { return distance; }

    public int getCanvasDistance() { return (int) (distance * toPixels); }
}
