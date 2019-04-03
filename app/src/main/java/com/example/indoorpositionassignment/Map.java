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

    private OnFragmentInteractionListener mListener;

    ArrayList<AccessPointLocation> floorOneAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:b3:17:d5:34:40", "CO228", 18, 70, 2),
                    new AccessPointLocation("70:6d:15:28:83:4f", "CO236", 38, 47, 2),
                    new AccessPointLocation("70:6d:15:40:a3:8f", "CO219", 40, 75, 2),
                    new AccessPointLocation("70:6d:15:40:cd:2f", "CO246", 68, 21, 2), // "70:6d:15:40:cd:20"
                    new AccessPointLocation("70:b3:17:d5:37:e0", "Outside CO228", 10, 76, 2),
                    new AccessPointLocation("70:6d:15:40:56:0f", "Outside CO232", 10, 53, 2),
                    new AccessPointLocation("70:6d:15:48:23:20", "Outside CO262", 10, 30, 2),
                    new AccessPointLocation("70:6d:15:40:ca:a0", "Outside CO243", 57, 34, 2),
                    new AccessPointLocation("70:6d:15:40:b5:c0", "Outside CO217", 47, 93, 2),
                    new AccessPointLocation("70:6d:15:36:91:8f", "Outside CO258", 12, 11, 2), // 70:6d:15:36:91:80
                    new AccessPointLocation("00:d7:8f:f3:95:8f", "Outside CO220", 18, 82, 2)  // 00:d7:8f:f3:95:80
            )
    );

    ArrayList<AccessPointLocation> floorTwoAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:b3:17:d5:34:40", "CO228", 18, 70, 2)
            )
    );

    ArrayList<AccessPointLocation> floorThreeAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:b3:17:d5:34:40", "CO228", 18, 70, 2)
            )
    );

    public Map() {
        // Required empty public constructor
    }

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
        // return inflater.inflate(R.layout.fragment_map, container, false);
        final MyView view = new MyView(getActivity());
        // Update access point list every 100ms
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Make sure that this fragment is attached to the activity
                if (isAdded() == true) { view.invalidate(); }
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

    public class MyView extends View {

         Paint paint;

         public MyView(Context context) {
             super(context);
             paint = new Paint();
         }

         @Override
         protected void onDraw(Canvas canvas) {
             super.onDraw(canvas);

             // Draw floor-plan
             Bitmap floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_2);

             double scale = 2.1; // How much to scale floor plan by
             int right = (int) (437 * scale);
             int bottom = (int) (710 * scale);
             // Rotate floor-plan
             Matrix matrix = new Matrix();
             matrix.postRotate(90.0f);
             Bitmap rotatedFloorPlan = Bitmap.createBitmap(floorPlan, 0, 0, floorPlan.getWidth(), floorPlan.getHeight(), matrix, true);
             // Add floor-plan to canvas
             canvas.drawBitmap(rotatedFloorPlan, null, new Rect(0, 0, right, bottom), null);

             int offsetX = 60;
             int offsetY = 30;

             // Draw access points on current floor
             for (AccessPointLocation accessPointLocation: floorOneAccessPoints)
                 drawAccessPoint(accessPointLocation.getX() * 10, accessPointLocation.getY() * 10, offsetX, offsetY, canvas);

             // Draw distance radius of closest access points
             ArrayList<AccessPointLocation> strongestAccessPoints = getClosestAccessPoints();

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
                 drawClosestAccessPoint(accessPointLocation, offsetX, offsetY, canvas);
                 i++;
             }
         }

         protected void drawAccessPoint(int x, int y, int offsetX, int offsetY, Canvas canvas) {
             int cx = x + offsetX;
             int cy = y + offsetY;
             int radius = 10;
             paint.setColor(Color.BLUE);
             canvas.drawCircle(cx, cy, radius, paint);
         }

         protected void drawClosestAccessPoint(AccessPointLocation accessPointLocation, int offsetX,
                                               int offsetY, Canvas canvas) {
             int cx = accessPointLocation.getX() * 10 + offsetX;
             int cy = accessPointLocation.getY() * 10 + offsetY;

             // Draw distance radius
             float areaRadius = (float) accessPointLocation.getDistance() * 5;
             paint.setAlpha(60);
             canvas.drawCircle(cx, cy, areaRadius, paint);

             // Change access point location color to red
             float locationRadius = 10.0f;
             paint.setColor(Color.RED);
             paint.setAlpha(255);
             canvas.drawCircle(cx, cy, locationRadius, paint);
         }

         protected ArrayList<AccessPointLocation> getClosestAccessPoints() {
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
             // We only want to consider relevant Access Points in the network
             int count = 0;
             if (accessPointList.size() >= 3) {
                 for (AccessPoint accessPoint: accessPointList) {
                     if (count < 3) {
                         AccessPointLocation accessPointLocation = getAccessPointByBSSID(accessPoint.getBSSID());
                         if (accessPointLocation != null) {
                             accessPointLocation.setDistance(calculateDistance(accessPoint.getLevel(), accessPoint.getFrequency()));
                             strongestAccessPoints.add(accessPointLocation);
                             count++;
                         } else {
                             // System.out.println("No Access Point with BSSID: [" + accessPoint.getBSSID() + "] found");
                         }
                     } else {
                         System.out.println(strongestAccessPoints.toString());
                         return strongestAccessPoints;
                     }
                 }
             }

             return null;
         }

         protected AccessPointLocation getAccessPointByBSSID(String BSSID) {
             for (AccessPointLocation accessPointLocation: floorOneAccessPoints) {
                 String accessPointLocationBSSID = accessPointLocation.getBSSID();
                 if (accessPointLocationBSSID.equals(BSSID)) {
                     return accessPointLocation;
                 }
             }

             return null;
         }

        public double calculateDistance(double level, double freq) {
            // https://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
            // http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
            double exp = (27.55 - (20 * Math.log10(freq)) + Math.abs(level)) / 20.0;
            return Math.pow(10.0, exp);
        }
    }
}
