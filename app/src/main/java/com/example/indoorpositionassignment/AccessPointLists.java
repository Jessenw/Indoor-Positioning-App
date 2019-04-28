package com.example.indoorpositionassignment;

import java.util.ArrayList;
import java.util.Arrays;

public class AccessPointLists {

    /* AccessPointLocation Arrays
            - X, Y coordinates are based on the origin of the floor plan
            - Greater X value -> positive X axis on floor plan
            - Greater Y value -> positive Y axis on floor plan
    */
    private static ArrayList<AccessPointLocation> floorOneAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:80:8b:d3:5e:60", "", 5, 39, 1),
                    new AccessPointLocation("70:70:8b:be:01:af", "", 10, 40, 1),
                    new AccessPointLocation("70:70:8b:d3:5b:6f", "", 24, 41.5, 1),
                    new AccessPointLocation("70:70:8b:ce:29:40", "", 28.5, 40, 1),
                    new AccessPointLocation("00:2c:c8:cc:30:80", "", 45.5, 40, 1),
                    new AccessPointLocation("70:70:8b:be:0c:80", "", 71, 40, 1),
                    new AccessPointLocation("54:a2:74:d2:34:70", "", 53, 33, 1),
                    new AccessPointLocation("e8:65:49:40:0c:10", "", 53, 25.5, 1),
                    new AccessPointLocation("b0:8b:cf:27:7b:cf", "", 82, 12.5, 1),
                    new AccessPointLocation("bc:26:c7:40:c0:00", "", 48.6, 7.5, 1),
                    new AccessPointLocation("b0:8b:cf:35:2f:cf", "", 39, 7.5, 1),
                    new AccessPointLocation("00:a2:ee:d3:80:af", "", -0.5, 14, 1)
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
                    new AccessPointLocation("70:6d:15:36:b6:2f", "School of Mathematics Office", 10.5, 8.5, 0),
                    new AccessPointLocation("bc:26:c7:94:91:40", "Outside CO365", 2, 11.5, 0),
                    new AccessPointLocation("70:6d:15:3b:a2:6f", "Outside CO318", 44, 7, 0),
                    new AccessPointLocation("e8:65:49:10:00:df", "School of Geology Office", 60, 10, 0),
                    new AccessPointLocation("70:6d:15:05:be:40", "Outside CO305", 77, 11, 0),
                    new AccessPointLocation("70:6d:15:16:6c:20", "Outside CO329", 30, 28, 0),
                    new AccessPointLocation("70:6d:15:40:35:cf", "Outside CO353", 12, 28, 0),
                    new AccessPointLocation("70:6d:15:48:15:2f", "Outside CO338", 28.5,40.5, 0)
            )
    );

    private static ArrayList<AccessPointLocation> floorFourAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:6d:15:40:cd:60", "", 11, 47, 0),
                    new AccessPointLocation("70:6d:15:35:32:e0", "", 31, 47.5, 0),
                    new AccessPointLocation("70:6d:15:35:42:00", "", 54, 47, 0),
                    new AccessPointLocation("70:6d:15:40:c9:4e", "", 79, 43, 0)
            )
    );

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
