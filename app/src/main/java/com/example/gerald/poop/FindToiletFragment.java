package com.example.gerald.poop;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FindToiletFragment extends Fragment implements OnMapReadyCallback {

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getContext(), "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            //top right myLocation button disabled
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
    }

    private static final String TOILET_ID = "toilet_id";
    private static final String TOILET_TITLE = "toilet_title";
    private static final String TOILET_RATING = "toilet_rating";
    private static final String TOILET_DESCRIPTIOM = "toilet_description";
    private static final String TOILET_ADDRESS = "toilet_address";
    private static final String TOILET_LAT = "toilet_lat";
    private static final String TOILET_LNG = "toilet_lng";

    public static final String TAG = "FindToiletFragment";

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 17f;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Geocoder geocoder;
    private View rootView;
    private ImageView ivGps;
    private ImageView ivList;
    private List<Address> addresses;
    RelativeLayout closestToiletRel;
    private String closestId;
    private TextView tvTitle, tvDistance;
    private ArrayList<Marker> markers = new ArrayList<>();
    private LocationManager lm;

    DatabaseReference dbToilets;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_find_toilet, container, false);
        getLocationPermission();

        dbToilets = FirebaseDatabase.getInstance().getReference("toilets");

        //   markers = new ArrayList<>();

        ivGps = rootView.findViewById(R.id.ivGps);
        ivList = rootView.findViewById(R.id.ivList);
        closestToiletRel = rootView.findViewById(R.id.closetToiletRelLayout);

        // Inflate the layout for this fragment
        return rootView;
    }

    private void init() {
        tvTitle = rootView.findViewById(R.id.tvTitle);
        tvDistance = rootView.findViewById(R.id.tvDistance);

        Log.d(TAG, "init:called");
        ivGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "gps clicked", Toast.LENGTH_SHORT).show();
                getDeviceLocation();
            }
        });

        ivList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "List View of toilet function coming soon", Toast.LENGTH_SHORT).show();
                ToiletListFragment toiletListFragment = new ToiletListFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.listFrameLayout, toiletListFragment)
                        .addToBackStack(null)
                        .setCustomAnimations(R.animator.slide_down, R.animator.slide_up, R.animator.slide_down, R.animator.slide_down)
                        .commit();

                Log.d(TAG, "ivList:init");
            }
        });

        //  addToilets();

        dbToilets.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear();
                for (DataSnapshot toiletSnapshot : dataSnapshot.getChildren()) {
                    Toilet toilet = toiletSnapshot.getValue(Toilet.class);
                    if(toilet.getToilet_thumb_down() >= 5){
                        Log.d(TAG, "more than 5 thumb down: " + toilet.getToilet_thumb_down());
                        toiletSnapshot.getRef().removeValue();
                    }

                        Log.d(TAG, "onDataChange:" + TOILET_TITLE + ":" + toilet.getToilet_title() + TOILET_ID + ":" + toilet.getToilet_id());
                        Log.d(TAG, "onDataChange: lat: " + toilet.getToilet_lat() + "lng: " + toilet.getToilet_lng());
                        addMarker(new LatLng(toilet.getToilet_lat(), toilet.getToilet_lng()), toilet.getToilet_title(), toilet.getToilet_id());

                }

                initClosestToilet();
                markers.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getContext(), "marker " + marker.getTag() + " clicked", Toast.LENGTH_SHORT).show();
                String tag = marker.getTag().toString();
                getToiletDetails(tag);
                Log.d(TAG, " markers size: " + markers.size());
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d(TAG, "initMap:called");
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
                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                            currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "My Location");
                            geoLocate(currentLocation.getLatitude(), currentLocation.getLongitude());
                            Log.d(TAG, "current address:" + addresses.get(0).getAddressLine(0));
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

    private void initClosestToilet() {
       // Log.d(TAG, "findClosestToilet: init markers size: " + markers.size());
        LatLng currentLatLng = getDeviceLocationLatLng();
        Location deviceLocation = new Location("device");
        deviceLocation.setLatitude(currentLatLng.latitude);
        deviceLocation.setLongitude(currentLatLng.longitude);
        Log.d(TAG, "getDeviceLatLng: " + deviceLocation.getLatitude() + ", " + deviceLocation.getLongitude());

        Marker closestMarker = mMap.addMarker(new MarkerOptions().position(
                new LatLng(0, 0))
                .visible(false));
        double closestDistance = 0;

        for (int i = 0; i < markers.size(); i++) {
            Log.d(TAG, "findClosestToilet toilet markers: " + markers.get(i).getTitle());
            Location markerLocation = new Location("marker");
            LatLng markerPosition = markers.get(i).getPosition();
            markerLocation.setLatitude(markerPosition.latitude);
            markerLocation.setLongitude(markerPosition.longitude);
            double distance = deviceLocation.distanceTo(markerLocation);
            Log.d(TAG, "findClosestToilet distance between: " + distance);

            if (closestDistance == 0 || closestDistance > distance) {
                closestDistance = distance;
                closestMarker = markers.get(i);
            }
        }
        Log.d(TAG, "findClosestToilet: closestToilet: " + closestMarker.getTitle());
        tvTitle.setText(closestMarker.getTitle());
        tvDistance.setText(String.format("%.2f meters away", closestDistance));
        //todo:final String id keep crashing when theres change of data
        closestId = closestMarker.getTag().toString();
        //Log.d(TAG, "findClosestToilet: " + id);

        closestToiletRel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "relLayout clicked");
                getToiletDetails(closestId);
            }
        });
    }


    private LatLng getDeviceLocationLatLng() {
        LocationManager lm = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        Log.d(TAG, "getDeviceLatLng: " + longitude + ", " + latitude);
        return new LatLng(latitude, longitude);
    }


    private void moveCamera(LatLng latlng, float zoom, String title) {
        Log.d(TAG, "moveCamera:move camera to: lat:" + latlng.latitude + " lang" + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    private void addMarker(LatLng latlng, String title, String id) {
        Log.d(TAG, "addMarker: id:" + id);
        MarkerOptions options = new MarkerOptions()
                .position(latlng)
                .title(title);
        Marker marker = mMap.addMarker(options);
        marker.setTag(id);
        markers.add(marker);
        Log.d(TAG, "marker size: " + markers.size());
    }

    private void geoLocate(double lat, double lng) {
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOEXCEPTION:" + e.getMessage());
        }
    }

    private void getToiletDetails(String id) {
        dbToilets.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Toilet toilet = dataSnapshot.getValue(Toilet.class);
                Log.e(TAG, "getToiletDetails:" + toilet.getToilet_id() + " " + toilet.getToilet_title());
                showToiletDetailsDialog(toilet);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showToiletDetailsDialog(final Toilet toilet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.toilet_details_dialog, null);
        dialogView.invalidate();
        builder.setView(dialogView);

        final TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        final TextView tvAddress = dialogView.findViewById(R.id.tvAddress);
        final TextView tvRating = dialogView.findViewById(R.id.tvRating);
        final TextView tvDest = dialogView.findViewById(R.id.tvDescription);
        final TextView tvThumbUp = dialogView.findViewById(R.id.tvThumbUp);
        final TextView tvThumbDown = dialogView.findViewById(R.id.tvThumbDown);

        // builder.setTitle("Update Artist :" + artistName);
        tvTitle.setText(toilet.getToilet_title());
        tvAddress.setText(toilet.getToilet_address());
        tvRating.setText(toilet.getToilet_rating() + "/5");
        tvDest.setText(toilet.getToilet_description());
        tvThumbUp.setText(toilet.getToilet_thumb_up() + "");
        tvThumbDown.setText(toilet.getToilet_thumb_down() + "");

        builder.setPositiveButton("Go!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getContext(), "go is clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), DirectionActivity.class);
                intent.putExtra("TOILET", toilet);
                startActivity(intent);
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getContext(), "cancel is clicked", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getContext(), "next is clicked", Toast.LENGTH_SHORT).show();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void addToilets() {
        String id1 = dbToilets.push().getKey();
        Toilet toilet1 = new Toilet(id1, "Republic Polytechnic", "9 Woodlands Avenue 9, 738964", "Toilet in W1! Its at the first floor and have both male and female toilet!", 4, 103.785480, 1.443389, 24, 3);
        dbToilets.child(id1).setValue(toilet1);

       /* String id2 = dbToilets.push().getKey();
        Toilet toilet2 = new Toilet(id2, "End Primary", "end pri address", "dest end", 4, 103.817178, 1.454565);
        dbToilets.child(id2).setValue(toilet2);

        String id3 = dbToilets.push().getKey();
        Toilet toilet3 = new Toilet(id3, "Can Primary", "can pri address", "dest can", 3, 103.815540, 1.450997);
        dbToilets.child(id3).setValue(toilet3);*/
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
                initMap();
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }
}
