package com.example.gerald.poop;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;


public class MainActivity extends AppCompatActivity{

    private static final String TOILET_ID = "toilet_id";
    private static final String TOILET_TITLE = "toilet_title";
    private static final String TOILET_RATING = "toilet_rating";
    private static final String TOILET_DESCRIPTIOM = "toilet_description";
    private static final String TOILET_ADDRESS = "toilet_address";
    private static final String TOILET_LAT = "toilet_lat";
    private static final String TOILET_LNG = "toilet_lng";

    public static final String TAG = "MainActivity";

    private AddToiletFragment addToiletFragment;
    private FindToiletFragment findToiletFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView btnNavView = findViewById(R.id.botNav);

        if(findViewById(R.id.frameLayout) != null){
            if(savedInstanceState != null){
                return;
            }
            else{
                addToiletFragment = new AddToiletFragment();
                findToiletFragment = new FindToiletFragment();
                addToiletFragment.setArguments(getIntent().getExtras());
                findToiletFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, findToiletFragment).commit();
                getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, addToiletFragment).commit();
                getSupportFragmentManager().beginTransaction().show(findToiletFragment).commit();
                getSupportFragmentManager().beginTransaction().hide(addToiletFragment).commit();
            }
        }

        btnNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.find_toliet:
                        Toast.makeText(getApplicationContext(), "find toilet clicked", Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().show(findToiletFragment).commit();
                        getSupportFragmentManager().beginTransaction().hide(addToiletFragment).commit();
                        break;
                    case R.id.add_toliet:
                        Toast.makeText(getApplicationContext(), "add toilet clicked", Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().show(addToiletFragment).commit();
                        getSupportFragmentManager().beginTransaction().hide(findToiletFragment).commit();
                        break;
                }
                return true;
            }
        });
    }

}
