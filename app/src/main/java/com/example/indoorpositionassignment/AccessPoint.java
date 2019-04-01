package com.example.indoorpositionassignment;

import java.util.Comparator;

public class AccessPoint implements Comparator<AccessPoint> {

    private String SSID;
    private String BSSID;       // MAC Address
    private int    level;       // signal strength
    private double distance;    // approximate distance to AP

    public AccessPoint(String SSID, String BSSID, int level, double distance) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.level = level;
        this.distance = distance;
    }

    public String getSSID() {
        return this.SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public int getLevel() {
        return level;
    }

    public double getDistance() {
        return distance;
    }

    public int compare(AccessPoint o1, AccessPoint o2) {
        double distance1 = o1.getDistance();
        double distance2 = o2.getDistance();

        if (distance1 > distance2) return -1;
        else if (distance1 < distance2) return 1;
        else return 0;
    }

    @Override
    public String toString() {
        return getSSID() + " : " + getBSSID() + " : " + String.format("%.2f", getDistance());
    }
}
