package com.example.indoorpositionassignment;


import android.content.Context;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class AccessPoints extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ListView listView;

    public AccessPoints() { /* Required empty public constructor */ }

    public static AccessPoints newInstance() {
        AccessPoints fragment = new AccessPoints();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = getActivity().findViewById(R.id.access_point_list);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_access_points, container, false);

        listView = rootView.findViewById(R.id.access_point_list);

        // Update access point list every 100ms
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Make sure that this fragment is attached to the activity
                if (isAdded() == true) generateAccessPointList(rootView);
                handler.postDelayed(this, 100);
            }
        }, 100);

        return rootView;
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

    public void generateAccessPointList(View rootView) {
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        List<ScanResult> results = wifiManager.getScanResults();

        // Filter data from scan results
        ArrayList<AccessPoint> accessPointList = new ArrayList<>();
        if (results != null) {
            final int size = results.size();
            if (size == 0) { /* "No results" handling goes here */ }
            else {
                for (int i = 0; i < size; i++) {
                    ScanResult result = results.get(i);
                    if (result.SSID.contains("")) {
                        double distance = getDistance(result.level, result.frequency);
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

                // Convert AccessPoint objects into String's
                String[] values = new String[accessPointList.size()];
                for (int i = 0; i < accessPointList.size(); i++) {
                    if (accessPointList.get(i) != null)
                        values[i] = accessPointList.get(i).toString();
                }

                // Set data up for ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(rootView.getContext(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, values);

                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
            }
        }
    }

    public double getDistance(double level, double freq) {
        // https://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
        // http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
        double exp = (27.55 - (20 * Math.log10(freq)) + Math.abs(level)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
