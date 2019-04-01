package com.example.indoorpositionassignment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements AccessPoints.OnFragmentInteractionListener, Map.OnFragmentInteractionListener {

    int MY_PERMISSION_FINE_LOCATION = 0;

    WifiManager wifiManager;

    ArrayList<AccessPointLocation> floorOneAccessPoints = new ArrayList<>(
            Arrays.asList(
                    new AccessPointLocation("70:b3:17:d5:34:40", "CO228", 0, 0, 2),
                    new AccessPointLocation("70:b3:17:d5:37:e0", "Outside CO228", 0, 0, 2),
                    new AccessPointLocation("70:6d:15:40:56:0f", "Outside CO232", 0, 0, 2)
            )
    );
    ArrayList<AccessPointLocation> floorTwoAccessPoints;
    ArrayList<AccessPointLocation> floorThreeAccessPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request location permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);

        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            System.out.println("ACCESS_FINE_LOCATION: PERMISSION_GRANTED");
        else
            System.out.println("ACCESS_FINE_LOCATION: PERMISSION_DENIED");

        // Create Wifi Manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.access_points_menu_item:
                        selectedFragment = AccessPoints.newInstance();
                        break;
                    case R.id.map_menu_item:
                        selectedFragment = Map.newInstance();
                        break;
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.commit();
                return true;
            }
        });

        // Display first fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, AccessPoints.newInstance());
        transaction.commit();

        // Accessing menu item programmatically
        // bottomNavigationView.getMenu().getItem(1).setChecked(true);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }
}
