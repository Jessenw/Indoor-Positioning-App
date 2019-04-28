package com.example.indoorpositionassignment;

public class LocationSolver {

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
}
