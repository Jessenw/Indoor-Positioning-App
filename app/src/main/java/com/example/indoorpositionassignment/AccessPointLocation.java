package com.example.indoorpositionassignment;

public class AccessPointLocation {

    private String BSSID;
    private String desc;
    private int x, y;
    private int floor;

    public AccessPointLocation(String BSSID, String desc, int x, int y, int floor) {
        this.BSSID = BSSID;
        this.desc = desc;
        this.x = x;
        this.y = y;
        this.floor = floor;
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getDesc() {
        return desc;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getFloor() {
        return floor;
    }
}
