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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Map extends Fragment {

    private OnFragmentInteractionListener mListener;

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
        return new MyView(getActivity());
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

             int offsetX = 60;
             int offsetY = 30;

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

             drawAccessPoint(0, 0, offsetX, offsetY, canvas);
         }

         protected void drawAccessPoint(int x, int y, int offsetX, int offsetY, Canvas canvas) {
             int cx = x + offsetX;
             int cy = x + offsetY;
             int radius = 20;
             paint.setColor(Color.BLUE);
             canvas.drawCircle(cx, cy, radius, paint);
         }
    }
}
