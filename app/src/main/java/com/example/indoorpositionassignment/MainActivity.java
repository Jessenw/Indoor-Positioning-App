package com.example.indoorpositionassignment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    int MY_PERMISSION_FINE_LOCATION = 0;

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
}
