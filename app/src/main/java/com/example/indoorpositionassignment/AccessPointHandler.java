package com.example.indoorpositionassignment;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AccessPointHandler {

    /* AccessPointLocation Arrays
            - X, Y coordinates are based on the origin of the floor plan
            - Greater X value -> positive X axis on floor plan
            - Greater Y value -> positive Y axis on floor plan
    */
    private static ArrayList<AccessPointLocation> floorOneAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:80:8b:d3:5e:60", "", 3.5, 37, 1),
                    new AccessPointLocation("70:70:8b:be:01:af", "", 8.5, 38, 1),
                    new AccessPointLocation("70:70:8b:d3:5b:6f", "", 22.5, 39.5, 1),
                    new AccessPointLocation("70:70:8b:ce:29:40", "", 27, 38, 1),
                    new AccessPointLocation("00:2c:c8:cc:30:80", "", 44, 38, 1),
                    new AccessPointLocation("70:70:8b:be:0c:80", "", 69.5, 38, 1),
                    new AccessPointLocation("54:a2:74:d2:34:70", "", 51.5, 31, 1),
                    new AccessPointLocation("e8:65:49:40:0c:10", "", 51.5, 23.5, 1),
                    new AccessPointLocation("b0:8b:cf:27:7b:cf", "", 80.5, 10.5, 1),
                    new AccessPointLocation("bc:26:c7:40:c0:00", "", 47, 5.5, 1),
                    new AccessPointLocation("b0:8b:cf:35:2f:cf", "", 37.5, 5.5, 1),
                    new AccessPointLocation("00:a2:ee:d3:80:af", "", -2, 12, 1)
            )
    );

    private static ArrayList<AccessPointLocation> floorTwoAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:b3:17:d5:34:40", "CO228", 44.6, 10.81, 2),
                    new AccessPointLocation("70:6d:15:28:83:4f", "CO236", 31.1, 25, 2),
                    new AccessPointLocation("70:6d:15:40:a3:8f", "CO219", 42.56, 29.73, 2),
                    new AccessPointLocation("70:6d:15:40:cd:2f", "CO246", 13.51, 43.91, 2),
                    new AccessPointLocation("70:b3:17:d5:37:e0", "Outside CO228", 49.32, 6.48, 2),
                    new AccessPointLocation("70:6d:15:40:56:0f", "Outside CO232", 33.78, 6.08, 2),
                    new AccessPointLocation("70:6d:15:48:23:20", "Outside CO262", 18.91, 6.48, 2),
                    new AccessPointLocation("70:6d:15:40:ca:a0", "Outside CO243", 21.62, 36.75, 2),
                    new AccessPointLocation("70:6d:15:40:b5:c0", "Outside CO217", 60.13, 30.40, 2),
                    new AccessPointLocation("70:6d:15:36:91:8f", "Outside CO258", 6.75, 8.10, 2),
                    new AccessPointLocation("00:d7:8f:f3:95:8f", "Outside CO220", 54.05, 11.48, 2)
            )
    );

    private static ArrayList<AccessPointLocation> floorThreeAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:6d:15:36:b6:2f", "School of Mathematics Office", 9.5, 8, 3),
                    new AccessPointLocation("bc:26:c7:94:91:40", "Outside CO365", 1.5, 10.5, 3),
                    new AccessPointLocation("70:6d:15:3b:a2:6f", "Outside CO318", 43.5, 6, 3),
                    new AccessPointLocation("e8:65:49:10:00:df", "School of Geology Office", 58.5, 10, 3),
                    new AccessPointLocation("70:6d:15:05:be:40", "Outside CO305", 76.5, 10, 3),
                    new AccessPointLocation("70:6d:15:16:6c:20", "Outside CO329", 29.5, 27, 3),
                    new AccessPointLocation("70:6d:15:40:35:cf", "Outside CO353", 11.5, 27, 3),
                    new AccessPointLocation("70:6d:15:48:15:2f", "Outside CO338", 28,39.5, 3)
            )
    );

    private static ArrayList<AccessPointLocation> floorFourAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:6d:15:40:cd:60", "", 11, 47, 4),
                    new AccessPointLocation("70:6d:15:35:32:e0", "", 31, 47.5, 4),
                    new AccessPointLocation("70:6d:15:35:42:00", "", 54, 47, 4),
                    new AccessPointLocation("70:6d:15:40:c9:4e", "", 79, 43, 4)
            )
    );

    Context context = null;

    AccessPointHandler (Context context) {
        this.context = context;
    }

    /**
     * Returns a list of AP's ordered by nearest distance
     */
    public ArrayList<Router> getAccessPoints() {
        WifiManager wifiManager = MainActivity.getWifiManager();
        wifiManager.startScan(); // perform a fresh scan before getting results
        List<ScanResult> results = wifiManager.getScanResults();

        // filter data from scan results
        ArrayList<Router> routers = new ArrayList<>();
        if (results != null) {
            int size = results.size();
            if (size > 0) {
                for (ScanResult result : results) {
                    double distance = solveDistance(result.level, result.frequency);
                    Router router = new Router(result.SSID, result.BSSID, result.level, distance, result.frequency);
                    routers.add(router);
                }

                // order results list based on closest distance
                Collections.sort(routers, new Comparator<Router>() {
                    @Override
                    public int compare(Router r1, Router r2) {
                        double distance1 = r1.getDistance();
                        double distance2 = r2.getDistance();

                        if (distance1 > distance2) return 1;
                        else if (distance1 < distance2) return -1;
                        else return 0;
                    }
                });
            }

            return routers;
        }

        return null;
    }

    /**
     * Solves the distance between a device and access point based on the frequency and strength
     * of the signal
     * @param level
     * @param freq
     * @return
     */
    public double solveDistance(double level, double freq) {
        return Math.pow(10.0, (27.55 - (20 * Math.log10(freq)) + Math.abs(level)) / 20.0);
    }

    public static double[] getLocation(double positions[][], double distances[]) {
        // If there are less than 2 points, return the only position
        if (positions.length <= 0 && distances.length <= 0) {
            //throw new IllegalArgumentException("Need at least one position");
            return null;
        }
        else if (positions.length < 2 && distances.length < 2) {
            double[] location = new double[2];
            location[0] = positions[0][0];
            location[1] = positions[0][1];
            return location;
        }

        for (int i = 0; i < distances.length; i++) {
            distances[i] = Math.max(distances[i], 1E-7);
        }

        // Get the average vertex of all positions
        double[] initialPoint = new double[positions[0].length];
        for (int i = 0; i < positions.length; i++) {
            double[] vertex = positions[i];
            for (int j = 0; j < vertex.length; j++) {
                initialPoint[j] += vertex[j];
            }
        }
        for (int j = 0; j < initialPoint.length; j++) {
            initialPoint[j] /= positions.length;
        }

        // Factor weight into calculation
        double[] target = new double[positions.length];
        double[] weights = new double[target.length];
        for (int i = 0; i < target.length; i++) {
            target[i] = 0.0;
            weights[i] = (1 / (distances[i] * distances[i]));
        }

        return initialPoint;
    }

    /**
     * Look-up a stores access point by BSSID. If an AP exists, return it. Otherwise return null
     * @param BSSID
     * @param currentFloor
     * @return
     */
    public AccessPointLocation getAccessPointLocationByBSSID(String BSSID, int currentFloor) {
        ArrayList<AccessPointLocation> current = floorOneAccessPoints;
        if (currentFloor == 1)
            current = floorOneAccessPoints;
        else if (currentFloor == 2)
            current = floorTwoAccessPoints;
        else if (currentFloor == 3)
            current = floorThreeAccessPoints;
        else if (currentFloor == 4)
            current = floorFourAccessPoints;

        for (AccessPointLocation accessPointLocation: current) {
            String accessPointLocationBSSID = accessPointLocation.getBSSID();
            if (accessPointLocationBSSID.equals(BSSID))
                return accessPointLocation;
        }

        return null;
    }

    /**
     * Returns the top 3 AP's based on distance
     * @param context
     * @param currentFloor
     * @return
     */
    public ArrayList<AccessPointLocation> getClosestAccessPoints(Context context, int currentFloor) {
        ArrayList<AccessPointLocation> strongestAccessPoints = new ArrayList<>();

        ArrayList<Router> routers = getAccessPoints();

        int threshold = 60; // furthermost distance an access point can be from position to be considered strongest
        int count = 0;
        if (routers.size() >= 2) {
            for (Router router: routers) {
                AccessPointLocation accessPointLocation = getAccessPointLocationByBSSID(router.getBSSID(), currentFloor);
                if (accessPointLocation != null) {
                    accessPointLocation.setDistance(solveDistance(router.getLevel(), router.getFrequency()) * 0.97);
                    accessPointLocation.setSignalStrength(router.getLevel());
                    if (router.getDistance() < threshold && count < 3)
                        strongestAccessPoints.add(accessPointLocation);
                        count++;
                    if (count >= 3)
                        return strongestAccessPoints;
                }
            }
        }

        return strongestAccessPoints;
    }

    /**
     * Returns the estimated location
     * @param context
     * @param currentFloor
     * @return
     */
    public double[] getLocation(Context context, int currentFloor) {
        ArrayList<AccessPointLocation> closestPoints = getClosestAccessPoints(context, currentFloor);

        double[][] positions = new double[closestPoints.size()][2];
        double[] distances = new double[closestPoints.size()];

        // convert coordinates into meters
        for (int i = 0; i < closestPoints.size(); i++) {
            AccessPointLocation accessPointLocation = closestPoints.get(i);
            positions[i][0] = accessPointLocation.getY();
            positions[i][1] = accessPointLocation.getX();
            distances[i] = accessPointLocation.getDistance();
        }

        // get location
        try {
            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();
            double[] calculatedPosition = optimum.getPoint().toArray();

            return calculatedPosition;
        } catch (IllegalArgumentException e) {} // prevent crashing when location found

        return null;
    }

    public static ArrayList<AccessPointLocation> getFloorOneAccessPoints() {
        return floorOneAccessPoints;
    }

    public static ArrayList<AccessPointLocation> getFloorTwoAccessPoints() {
        return floorTwoAccessPoints;
    }

    public static ArrayList<AccessPointLocation> getFloorThreeAccessPoints() {
        return floorThreeAccessPoints;
    }

    public static ArrayList<AccessPointLocation> getFloorFourAccessPoints() {
        return floorFourAccessPoints;
    }
}
