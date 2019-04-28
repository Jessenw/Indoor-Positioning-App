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


/**
 * Handles tasks found in the "Map" tab
 * The "Map" tab displays AP's, the nearest AP's distance and an approximate distance
 */
public class MapFragment extends Fragment {

    ArrayList<AccessPointLocation> floorOneAccessPoints = AccessPointHandler.getFloorOneAccessPoints();
    ArrayList<AccessPointLocation> floorTwoAccessPoints = AccessPointHandler.getFloorTwoAccessPoints();
    ArrayList<AccessPointLocation> floorThreeAccessPoints = AccessPointHandler.getFloorThreeAccessPoints();
    ArrayList<AccessPointLocation> floorFourAccessPoints = AccessPointHandler.getFloorFourAccessPoints();

    int currentFloor = 2;

    AccessPointHandler accessPointHandler;

    private OnFragmentInteractionListener mListener;

    /**
     * Constructor
     */
    public MapFragment() {}

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
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

        accessPointHandler = new AccessPointHandler(getContext());

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

            if (floorButtonRect[0].contains(touchY, touchX))
                currentFloor = 1;
            else if (floorButtonRect[1].contains(touchY, touchX))
                currentFloor = 2;
            else if (floorButtonRect[2].contains(touchY, touchX))
                currentFloor = 3;
            else if (floorButtonRect[3].contains(touchY, touchX))
                currentFloor = 4;

            return true;
        }

        @Override
         protected void onDraw(Canvas canvas) {
             super.onDraw(canvas);

             // setup canvas for horizontal orientation
             canvas.translate(getWidth(), 0);
             canvas.rotate(90.0f);
             canvas.scale(2, 2);

             // set current floor size and origin and AP list
             ArrayList<AccessPointLocation> current = floorOneAccessPoints;
             int[] floorOrigin = new int[2];
             int[] floorPlanSize = new int[2];
             Bitmap floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_1);
             if (currentFloor == 1) {
                 current = floorOneAccessPoints;
                 floorOrigin = new int[] {26, 395};
                 floorPlanSize = new int[] {711, 468};
                 floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_1);
             } else if (currentFloor == 2) {
                 current = floorTwoAccessPoints;
                 floorOrigin = new int[] {14, 408};
                 floorPlanSize = new int[] {710, 437};
                 floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_2);
             } else if (currentFloor == 3) {
                 current = floorThreeAccessPoints;
                 floorOrigin = new int[] {20, 400};
                 floorPlanSize = new int[] {709, 468};
                 floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_3);
             } else if (currentFloor == 4) {
                 current = floorFourAccessPoints;
                 floorOrigin = new int[] {15, 140};
                 floorPlanSize = new int[] {710, 209};
                 floorPlan = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cotton_level_4);
             }
             canvas.drawBitmap(floorPlan, null, new Rect(0, 0, floorPlanSize[0], floorPlanSize[1]), null);

             // translate canvas origin to origin of floor plan
             canvas.translate(floorOrigin[0], floorOrigin[1]);
             canvas.drawCircle(0,0,8, paint);

             // draw access points on current floor
             for (AccessPointLocation accessPointLocation: current)
                 drawAccessPoint(accessPointLocation.getCanvasX(), accessPointLocation.getCanvasY() * -1,  canvas);

             // draw distance radius of closest access points
             ArrayList<AccessPointLocation> strongestAccessPoints = accessPointHandler.getClosestAccessPoints(getContext(), currentFloor);
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
                     canvas.drawText(accessPointLocation.getBSSID() +
                             " : " + Math.abs(accessPointLocation.getSignalStrength()) + " dBm", 0 + (i * 200), 100, paint);
                     i++;
                 }
             }

             // draw location data text
             double[] loc = accessPointHandler.getLocation(getContext(), currentFloor);
             if (loc != null) {
                 drawLocation(loc, canvas);
                 paint.setTextSize(15);
                 paint.setColor(Color.BLACK);
                 canvas.drawText("Location X : " + loc[0] + " Location Y: " + loc[1], 0, 120, paint);
             }

             // draw floor select buttons
             for (int i = 0; i < floorButtonRect.length; i++) {
                 paint.setColor(Color.BLUE);
                 if (currentFloor - 1 == i)
                     paint.setColor(Color.GREEN);
                 canvas.drawRect(floorButtonRect[i], paint);
             }

             myCanvas = canvas;
         }

        protected void drawAccessPoint(int x, int y, Canvas canvas) {
            paint.setColor(Color.BLUE);
            canvas.drawCircle(x, y, 8, paint);
        }

        protected void drawClosestAccessPoint(AccessPointLocation accessPointLocation, String BSSID, Canvas canvas) {
            int cx = accessPointLocation.getCanvasX();
            int cy = accessPointLocation.getCanvasY();

            // draw distance
            float areaRadius = (float) accessPointLocation.getCanvasDistance();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            canvas.drawCircle(cx, cy * -1, areaRadius, paint);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(1);

            // Change access point location colour to red
            paint.setColor(Color.RED);
            canvas.drawCircle(cx, cy * -1, 8.0f, paint);
        }

        protected void drawLocation(double[] coordinates, Canvas canvas) {
            if (coordinates != null) {
                float cx = ((float) coordinates[0]) * 7.4f;
                float cy = ((float) coordinates[1]) * 7.4f;
                paint.setColor(Color.GREEN);
                canvas.drawCircle(cy, cx * -1, 8.0f, paint);
            }
        }
    }
}
