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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Handles tasks found in the "Access Points" tab
 * The "Access Points" tab displays a list of AP's including their:
 *      * SSID
 *      * BSSID
 *      * Distance
 *      * Signal strength
 *
 * The general purpose of this tab is for fingerprinting AP's for later
 * use in indoor positioning
 */
public class AccessPointFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ListView listView;
    private AccessPointHandler accessPointHandler;

    public AccessPointFragment() {}

    public static AccessPointFragment newInstance() {
        AccessPointFragment fragment = new AccessPointFragment();
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

        // update access point list every 100ms
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // make sure that this fragment is attached to the activity
                if (isAdded() == true) {
                    // get access point list
                    accessPointHandler = new AccessPointHandler(getContext());
                    ArrayList<Router> routers = accessPointHandler.getAccessPoints();

                    // convert router data into a string format
                    String[] values = new String[routers.size()];
                    for (int i = 0; i < routers.size(); i++)
                        if (routers.get(i) != null) values[i] = routers.get(i).toString();

                    // display Router data in ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(rootView.getContext(),
                            android.R.layout.simple_list_item_1, android.R.id.text1, values);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                }
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
}
