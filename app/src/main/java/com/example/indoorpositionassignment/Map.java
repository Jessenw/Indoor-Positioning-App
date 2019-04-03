package com.example.indoorpositionassignment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Map extends Fragment {

    // Floor plan width and height
    final int FLOOR_TWO_WIDTH = 710;
    final int FLOOR_TWO_HEIGHT = 437;

    private OnFragmentInteractionListener mListener;

    /* AccessPointLocation Arrays
            - X, Y coordinates are based on the origin of the floor plan
            - Greater X value -> positive X axis on floor plan
            - Greater Y value -> positive Y axis on floor plan
     */
    ArrayList<AccessPointLocation> floorOneAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:b3:17:d5:34:40", "CO228", 330, 80, 2),
                    new AccessPointLocation("70:6d:15:28:83:4f", "CO236", 230, 185, 2),
                    new AccessPointLocation("70:6d:15:40:a3:8f", "CO219", 315, 220, 2),
                    new AccessPointLocation("70:6d:15:40:cd:2f", "CO246", 100, 325, 2),         // "70:6d:15:40:cd:20"
                    new AccessPointLocation("70:b3:17:d5:37:e0", "Outside CO228", 365, 48, 2),
                    new AccessPointLocation("70:6d:15:40:56:0f", "Outside CO232", 250, 45, 2),
                    new AccessPointLocation("70:6d:15:48:23:20", "Outside CO262", 140, 48, 2),
                    new AccessPointLocation("70:6d:15:40:ca:a0", "Outside CO243", 160, 272, 2),
                    new AccessPointLocation("70:6d:15:40:b5:c0", "Outside CO217", 445, 225, 2),
                    new AccessPointLocation("70:6d:15:36:91:8f", "Outside CO258", 50, 60, 2),   // 70:6d:15:36:91:80
                    new AccessPointLocation("00:d7:8f:f3:95:8f", "Outside CO220", 400, 85, 2)   // 00:d7:8f:f3:95:80
            )
    );

    private ArrayList<AccessPointLocation> getClosestAccessPoints() {
        ArrayList<AccessPointLocation> strongestAccessPoints = new ArrayList<>();

        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        List<ScanResult> results = wifiManager.getScanResults();

        // Filter data from scan results
        ArrayList<AccessPoint> accessPointList = new ArrayList<>();
        if (results != null) {
            final int size = results.size();
            if (size == 0) { /* "No results" handling goes here */ } else {
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

                        if (distance1 > distance2) return 1;
                        else if (distance1 < distance2) return -1;
                        else return 0;
                    }
                });
            }
        }

        // Get top 3 results
        int count = 0;
        if (accessPointList.size() >= 3) {
            for (AccessPoint accessPoint: accessPointList) {
                if (count < 3) {
                    AccessPointLocation accessPointLocation = getAccessPointLocationByBSSID(accessPoint.getBSSID());
                    if (accessPointLocation != null) {
                        accessPointLocation.setDistance(calculateDistance(accessPoint.getLevel(), accessPoint.getFrequency()));
                        strongestAccessPoints.add(accessPointLocation);
                        count++;
                    }
                } else { return strongestAccessPoints; }
            }
        }

        return null;
    }

    private AccessPointLocation getAccessPointLocationByBSSID(String BSSID) {
        for (AccessPointLocation accessPointLocation: floorOneAccessPoints) {
            String accessPointLocationBSSID = accessPointLocation.getBSSID();
            if (accessPointLocationBSSID.equals(BSSID)) { return accessPointLocation; }
        }

        return null;
    }

    private double calculateDistance(double level, double freq) {
        // https://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
        // http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
        double exp = (27.55 - (20 * Math.log10(freq)) + Math.abs(level)) / 20.0;
        return Math.pow(10.0, exp);
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

         public MyView(Context context) {
             super(context);
             paint = new Paint();
         }

         @Override
         protected void onDraw(Canvas canvas) {
             super.onDraw(canvas);

             // TODO: Add logic to set floor plan height, width and offsets based on known level
             int floorPlanWidth = FLOOR_TWO_WIDTH;
             int floorPlanHeight = FLOOR_TWO_HEIGHT;

             // Setup canvas for horizontal orientation
             canvas.translate(getWidth(), 0);
             canvas.rotate(90.0f);
             canvas.scale(2, 2);

             // Draw floor-plan
             Bitmap floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_2);
             canvas.drawBitmap(floorPlan, null, new Rect(0, 0, floorPlanWidth, floorPlanHeight), null);

             // Move origin to origin of floor plan
             canvas.translate(14, 408);

             // Draw canvas origin
             paint.setColor(Color.RED);
             canvas.drawCircle(0, 0, 5, paint);

             // Draw access points on current floor
             for (AccessPointLocation accessPointLocation: floorOneAccessPoints) {
                 drawAccessPoint(accessPointLocation.getX(), accessPointLocation.getY() * -1, accessPointLocation.getBSSID(), canvas);
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
                     drawClosestAccessPoint(accessPointLocation, canvas);
                     i++;
                 }

                 // Draw bounding box of access points radius
                 for (AccessPointLocation accessPointLocation : strongestAccessPoints) {
                     drawBoundingBox(accessPointLocation, canvas);
                 }
             }
         }

        protected void drawAccessPoint(int x, int y, String BSSID, Canvas canvas) {
            int cx = x;
            int cy = y;
            int radius = 8;
            paint.setColor(Color.BLUE);
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setColor(Color.RED);
            canvas.drawText(BSSID, cx, cy + 20, paint);
        }

        protected void drawClosestAccessPoint(AccessPointLocation accessPointLocation, Canvas canvas) {
            int cx = accessPointLocation.getX();
            int cy = accessPointLocation.getY();

            // Draw distance radius
            float areaRadius = (float) accessPointLocation.getDistance();
            paint.setAlpha(60);
            canvas.drawCircle(cx, cy * -1, areaRadius, paint);

            // Change access point location colour to red
            float locationRadius = 8.0f;
            paint.setColor(Color.RED);
            paint.setAlpha(255);
            canvas.drawCircle(cx, cy * -1, locationRadius, paint);
        }

         protected void drawBoundingBox(AccessPointLocation accessPointLocation, Canvas canvas) {
             Rect rect = new Rect();
             rect.left = accessPointLocation.getX() - (int) accessPointLocation.getDistance();
             rect.right = accessPointLocation.getX() + (int) accessPointLocation.getDistance();
             rect.bottom = accessPointLocation.getY() * -1 + (int) accessPointLocation.getDistance();
             rect.top = accessPointLocation.getY() * -1 - (int) accessPointLocation.getDistance();

             paint.setStyle(Paint.Style.STROKE);
             paint.setStrokeWidth(2.0f);
             paint.setColor(Color.rgb(255, 165, 0));
             canvas.drawRect(rect, paint);
             paint.setStyle(Paint.Style.FILL);
         }
    }

    // ----- FRAGMENT SETUP METHODS -----

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final MyView view = new MyView(getActivity());

        // Update access point list every 100ms
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Make sure that this fragment is attached to the activity
                if (isAdded() == true) {
                    view.invalidate();
                }
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
}
