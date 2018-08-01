package com.example.gerald.poop;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class AddToiletFragment extends Fragment {

    private static final String TAG = "AddToiletFragment";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private EditText etTitle, etAddress, etDest;
    private SeekBar sbRating;
    private Button btnDone;
    private View rootView;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DatabaseReference dbToilets;
    private Boolean mLocationPermissionGranted = false;
    private Geocoder geocoder;
    private List<Address> addresses;
    private double lat;
    private double lng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_add_toilet, container, false);
        getLocationPermission();
        getDeviceLocation();
        etTitle = rootView.findViewById(R.id.etTitle);
        etAddress = rootView.findViewById(R.id.etAddress);
        etDest = rootView.findViewById(R.id.etDescription);
        sbRating = rootView.findViewById(R.id.sbRating);
        btnDone = rootView.findViewById(R.id.btnDone);

        dbToilets = FirebaseDatabase.getInstance().getReference("toilets");
        Log.d(TAG, "onCreateView: " + lat + ", " + lng);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToilet();
            }
        });

        return rootView;
    }

    public void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation:getting device location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete:found location");
                            Location currentLocation = (Location) task.getResult();
                            Log.d(TAG, "Current Location: " + currentLocation.getLatitude() + " " + currentLocation.getLatitude());
                            geoLocate(currentLocation.getLatitude(), currentLocation.getLongitude());
                           // Log.d(TAG, "current address:" + addresses.get(0).getAddressLine(0).toString());
                            etAddress.setText(addresses.get(0).getAddressLine(0).toString());
                            etAddress.setEnabled(false);
                            lat = currentLocation.getLatitude();
                            lng = currentLocation.getLongitude();

                        } else {
                            Log.d(TAG, "onComplete:current location is null");
                            Toast.makeText(getContext(), "unable to get current location", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation:" + e.getMessage());
        }
    }

    private void geoLocate(double lat, double lng){
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        try{
            addresses = geocoder.getFromLocation(lat, lng, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOEXCEPTION:" + e.getMessage());
        }
    }

    private void addToilet(){
        String title = etTitle.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dest = etDest.getText().toString().trim();
        int rating = sbRating.getProgress();
        double lng = this.lng;
        double lat = this.lat;

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(address) && !TextUtils.isEmpty(dest)){
            String id = dbToilets.push().getKey();
            Toilet toilet = new Toilet(id, title, address, dest, rating, lng, lat, 0,0);
            dbToilets.child(id).setValue(toilet);

            Snackbar.make(rootView, "toilet added", Snackbar.LENGTH_SHORT).show();

            etTitle.setText("");
            etDest.setText("");
            sbRating.setProgress(3);
        }else{
            Toast.makeText(getContext(), "You did not enter something", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(this.getContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

}
