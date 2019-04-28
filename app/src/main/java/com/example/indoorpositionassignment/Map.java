package com.example.indoorpositionassignment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

public class Map extends Fragment {

    final int FLOOR_ONE_WIDTH = 711;
    final int FLOOR_ONE_HEIGHT = 468;

    final int FLOOR_TWO_WIDTH = 710;
    final int FLOOR_TWO_HEIGHT = 437;

    final int FLOOR_THREE_WIDTH = 709;
    final int FLOOR_THREE_HEIGHT = 468;

    final int FLOOR_FOUR_WIDTH = 710;
    final int FLOOR_FOUR_HEIGHT = 209;

    int currentFloor = 2;

    private OnFragmentInteractionListener mListener;

    ArrayList<AccessPointLocation> floorOneAccessPoints = AccessPointLists.getFloorOneAccessPoints();
    ArrayList<AccessPointLocation> floorTwoAccessPoints = AccessPointLists.getFloorTwoAccessPoints();
    ArrayList<AccessPointLocation> floorThreeAccessPoints = AccessPointLists.getFloorThreeAccessPoints();
    ArrayList<AccessPointLocation> floorFourAccessPoints = AccessPointLists.getFloorFourAccessPoints();

    public Map() {}

    public static Map newInstance() {
        Map fragment = new Map();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final MyView view = new MyView(getActivity());

        // Update access point list every 100ms
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Make sure that this fragment is attached to the activity
                if (isAdded()) { view.invalidate(); }
                handler.postDelayed(this, 100);
            }
        }, 100);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private ArrayList<AccessPointLocation> getClosestAccessPoints() {
        ArrayList<AccessPointLocation> strongestAccessPoints = new ArrayList<>();

        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) { throw new Error("WifiManager null"); }
        wifiManager.startScan();
        List<ScanResult> results = wifiManager.getScanResults();

        // Filter data from scan results
        ArrayList<AccessPoint> accessPointList = new ArrayList<>();
        if (results != null) {
            final int size = results.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    ScanResult result = results.get(i);
                    if (result.SSID.contains("")) {
                        double distance = calculateDistance(result.level, result.frequency);
                        AccessPoint accessPoint = new AccessPoint(result.SSID, result.BSSID, result.level, distance, result.frequency);
                        accessPointList.add(accessPoint);
                    }
                }

                // Order results list based on closest distance
                Collections.sort(accessPointList, new Comparator<AccessPoint>() {
                    @Override
                    public int compare(AccessPoint o1, AccessPoint o2) {
                        double distance1 = o1.getDistance();
                        double distance2 = o2.getDistance();
                        return Double.compare(distance1, distance2);
                    }
                });
            }
        }

        int threshold = 60; // furthermost distance an access point can be from position to be considered strongest
        if (accessPointList.size() >= 2) {
            for (AccessPoint accessPoint: accessPointList) {
                AccessPointLocation accessPointLocation = getAccessPointLocationByBSSID(accessPoint.getBSSID());
                if (accessPointLocation != null) {
                    accessPointLocation.setDistance(calculateDistance(accessPoint.getLevel(), accessPoint.getFrequency()) * 0.97);
                    if (accessPoint.getDistance() < threshold) {
                        strongestAccessPoints.add(accessPointLocation);
                    }
                }
            }
        }

        return strongestAccessPoints;
    }

    private AccessPointLocation getAccessPointLocationByBSSID(String BSSID) {
        ArrayList<AccessPointLocation> current = floorOneAccessPoints;
        if (currentFloor == 1) current = floorOneAccessPoints;
        else if (currentFloor == 2) current = floorTwoAccessPoints;
        else if (currentFloor == 3) current = floorThreeAccessPoints;
        else if (currentFloor == 4) current = floorFourAccessPoints;

        for (AccessPointLocation accessPointLocation: current) {
            String accessPointLocationBSSID = accessPointLocation.getBSSID();
            if (accessPointLocationBSSID.equals(BSSID)) { return accessPointLocation; }
        }

        return null;
    }

    private double calculateDistance(double level, double freq) {
        // https://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
        // http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
        return Math.pow(10.0, (27.55 - (20 * Math.log10(freq)) + Math.abs(level)) / 20.0);
    }

    private double[] getLocation() {
        ArrayList<AccessPointLocation> closestPoints = getClosestAccessPoints();

        double[][] positions = new double[closestPoints.size()][2];
        double[] distances = new double[closestPoints.size()];

        // Convert coordinates into meters
        for (int i = 0; i < closestPoints.size(); i++) {
            AccessPointLocation accessPointLocation = closestPoints.get(i);
            // Coordinates have to be converted into meters
            positions[i][0] = accessPointLocation.getY();
            positions[i][1] = accessPointLocation.getX();
            distances[i] = accessPointLocation.getDistance();
        }

//        try {
//            return LocationSolver.getLocation(positions, distances);
//        } catch (IllegalArgumentException e) {}

        try {
            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();

            double[] calculatedPosition = optimum.getPoint().toArray();
            System.out.println(calculatedPosition[0] + ", " + calculatedPosition[1]);
            return calculatedPosition;
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
        }
        return null;
    }

    /**
     * This View class handles drawing:
     *      - Floor plan
     *      - Access Points
     *      - Strongest Access Points
     *      - Approximate distance to device
     *      - Distance bounding box
     */
    public class MyView extends View {
        Paint paint;
        Context context;
        Canvas myCanvas;

        // Floor button bounding boxes
        Rect[] floorButtonRect = new Rect[]{
                new Rect(0, 40, 100, 80),
                new Rect(110 , 40, 210, 80),
                new Rect(220 , 40, 320, 80),
                new Rect(330, 40, 430, 80)
        };

        public MyView(Context context) {
            super(context);
            paint = new Paint();
            this.context = context;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            int touchX = (int) (event.getX() / 2);
            int touchY = (int) (event.getY() / 2);
            if (floorButtonRect[0].contains(touchY, touchX)) {
                currentFloor = 1;
            } else if (floorButtonRect[1].contains(touchY, touchX)) {
                currentFloor = 2;
            } else if (floorButtonRect[2].contains(touchY, touchX)) {
                currentFloor = 3;
            } else if (floorButtonRect[3].contains(touchY, touchX)) {
                currentFloor = 4;
            }
            return true;
        }

        @Override
         protected void onDraw(Canvas canvas) {
             super.onDraw(canvas);

             int floorPlanWidth = FLOOR_TWO_WIDTH;
             int floorPlanHeight = FLOOR_TWO_HEIGHT;

             // Setup canvas for horizontal orientation
             canvas.translate(getWidth(), 0);
             canvas.rotate(90.0f);
             canvas.scale(2, 2);

            // Draw floor-plan
             Bitmap floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_1);
             if (currentFloor == 1) {
                 floorPlanWidth = FLOOR_ONE_WIDTH;
                 floorPlanHeight = FLOOR_ONE_HEIGHT;
                 floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_1);
             } else if (currentFloor == 2) {
                 floorPlanWidth = FLOOR_TWO_WIDTH;
                 floorPlanHeight = FLOOR_TWO_HEIGHT;
                 floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_2);
             } else if (currentFloor == 3) {
                 floorPlanWidth = FLOOR_THREE_WIDTH;
                 floorPlanHeight = FLOOR_THREE_HEIGHT;
                 floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_3);
             } else if (currentFloor == 4) {
                 floorPlanWidth = FLOOR_FOUR_WIDTH;
                 floorPlanHeight = FLOOR_FOUR_HEIGHT;
                 floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_4);
             }
             canvas.drawBitmap(floorPlan, null, new Rect(0, 0, floorPlanWidth, floorPlanHeight), null);

             // Move origin to origin of floor plan
             canvas.translate(14, 408);

             // Draw access points on current floor
            ArrayList<AccessPointLocation> current = floorOneAccessPoints;
            if (currentFloor == 1) {
                current = floorOneAccessPoints;
            } else if (currentFloor == 2) {
                current = floorTwoAccessPoints;
            } else if (currentFloor == 3) {
                current = floorThreeAccessPoints;
            } else if (currentFloor == 4) {
                current = floorFourAccessPoints;
            }
             for (AccessPointLocation accessPointLocation: current) {
                 drawAccessPoint(accessPointLocation.getCanvasX(), accessPointLocation.getCanvasY() * -1, accessPointLocation.getBSSID(), canvas);
             }

             // Draw distance radius of closest access points
             ArrayList<AccessPointLocation> strongestAccessPoints = getClosestAccessPoints();
             if (strongestAccessPoints != null) {
                 int i = 0;
                 for (AccessPointLocation accessPointLocation : strongestAccessPoints) {
                     switch (i) {
                         case 0:
                             paint.setColor(Color.RED);
                             break;
                         case 1:
                             paint.setColor(Color.GREEN);
                             break;
                         case 2:
                             paint.setColor(Color.CYAN);
                             break;
                     }
                     drawClosestAccessPoint(accessPointLocation, accessPointLocation.getBSSID(), canvas);
                     paint.setTextSize(15);
                     paint.setColor(Color.BLACK);
                     canvas.drawText(accessPointLocation.getBSSID(), 0 + (i * 130), 100, paint);
                     i++;
                 }
             }

             if (getLocation() != null) {
                 double[] loc = getLocation();
                 drawLocation(loc, canvas);
                 paint.setTextSize(15);
                 paint.setColor(Color.BLACK);
                 canvas.drawText("Location X : " + loc[0] + " Location Y: " + loc[1], 0, 120, paint);
             }

             // Draw floor buttons
             for (int i = 0; i < floorButtonRect.length; i++) {
                 paint.setColor(Color.BLUE);
                 if (currentFloor - 1 == i) { paint.setColor(Color.GREEN); }
                 canvas.drawRect(floorButtonRect[i], paint);
             }

             paint.setColor(Color.GREEN);
             canvas.drawCircle(363, -50, 8, paint);

             myCanvas = canvas;
         }

        protected void drawAccessPoint(int x, int y, String BSSID, Canvas canvas) {
            int radius = 8;
            paint.setColor(Color.BLUE);
            canvas.drawCircle(x, y, radius, paint);
        }

        protected void drawClosestAccessPoint(AccessPointLocation accessPointLocation, String BSSID, Canvas canvas) {
            int cx = accessPointLocation.getCanvasX();
            int cy = accessPointLocation.getCanvasY();

            // Draw distance radius
            float areaRadius = (float) accessPointLocation.getCanvasDistance();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            canvas.drawCircle(cx, cy * -1, areaRadius, paint);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(1);

            // Change access point location colour to red
            float locationRadius = 8.0f;
            paint.setColor(Color.RED);
            paint.setAlpha(255);
            canvas.drawCircle(cx, cy * -1, locationRadius, paint);
        }

         protected void drawLocation(double[] coordinates, Canvas canvas) {
            if (coordinates != null) {
                float locationRadius = 8.0f;
                paint.setColor(Color.GREEN);
                float cx = ((float) coordinates[0]) * 7.4f;
                float cy = ((float) coordinates[1]) * 7.4f;
                canvas.drawCircle(cy, cx * -1, locationRadius, paint);
            }
         }
    }
}
