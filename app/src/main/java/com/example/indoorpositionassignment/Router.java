package com.example.indoorpositionassignment;

/**
 * Used to store information about an Access Point collected by WiFiManager
 */
public class Router {

    private String SSID;
    private String BSSID;       // MAC Address
    private double distance;    // approximate distance to AP
    private int level;          // signal strength
    private int frequency;      // signal frequency in MHz

    public Router(String SSID, String BSSID, int level, double distance, int frequency) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.level = level;
        this.distance = distance;
        this.frequency = frequency;
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

    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return getSSID() + " : " + getBSSID() + " : " + String.format("%.2f", getDistance());
    }
}
