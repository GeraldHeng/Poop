package com.example.gerald.poop;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gerald.poop.modules.DirectionFinder;
import com.example.gerald.poop.modules.DirectionFinderListener;
import com.example.gerald.poop.modules.Route;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, DirectionFinderListener {

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            //top right myLocation button disabled
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            init();
        }
    }

    private static final String TAG = "DirectionActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionGranted = false;
    private ImageView ivBack;
    private TextView tvTitle, tvAddress, tvDest, tvRating;
    private ProgressDialog progressDialog;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private Toilet toilet;
    private Button btnReached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        ivBack = findViewById(R.id.ivBack);
        getLocationPermission();
    }

    private void init() {

        tvAddress = findViewById(R.id.addressLabel);
        tvTitle = findViewById(R.id.titleLabel);
        tvDest = findViewById(R.id.destLabel);
        tvRating = findViewById(R.id.ratingLabel);
        btnReached = findViewById(R.id.btnReached);
        //rbThumbDown = findViewById(R.id.rbThumbDown);
        // rbThumbUp = findViewById(R.id.rbThumbUp);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBackDialog();
            }
        });

        btnReached.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReachedDialog();
            }
        });

        Intent intent = getIntent();
        toilet = intent.getParcelableExtra("TOILET");

        tvTitle.setText(toilet.getToilet_title());
        tvAddress.setText(toilet.getToilet_address());
        tvRating.setText(toilet.getToilet_rating() + "/5");
        tvDest.setText(toilet.getToilet_description());

        Log.d(TAG, "init: get intent: title: " + toilet.getToilet_title());


    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(DirectionActivity.this);
        Log.d(TAG, "initMap:called");
    }

    private void showBackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DirectionActivity.this);
        builder.setTitle("Back");
        builder.setMessage("Do you want to cancel this journey to your toilet?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showReachedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DirectionActivity.this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.reached_toilet_dialog, null);
        final RadioGroup rgThumbs = dialogView.findViewById(R.id.rgThumbs);
        Toast.makeText(getApplicationContext(), "feedback function coming soon", Toast.LENGTH_SHORT).show();
        //dialogView.invalidate();
        builder.setView(dialogView);


        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int selectedRB = rgThumbs.getCheckedRadioButtonId();
                if (selectedRB == R.id.rbThumbUp) {
                    updateToiletThumbs(1);
                    finish();
                } else if (selectedRB == R.id.rbThumbDown) {
                    updateToiletThumbs(0);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please give a thumb!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "cancel is clicked", Toast.LENGTH_SHORT).show();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateToiletThumbs(int upOrDown) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("toilets").child(toilet.getToilet_id());
        //thumb down
        if (upOrDown == 0) {
            toilet.add_1_to_thumb_down();
        }
        //thumb up
        else if (upOrDown == 1) {
            toilet.add_1_to_thumb_up();
        }

        databaseReference.setValue(toilet);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void moveCamera(LatLng latlng, float zoom, String title) {
        Log.d(TAG, "moveCamera:move camera to: lat:" + latlng.latitude + " lang" + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latlng)
                    .title(title);
            mMap.addMarker(options);
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation:getting device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
                            sendDirectionRequest(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), new LatLng(toilet.getToilet_lat(), toilet.getToilet_lng()));
                        } else {
                            Log.d(TAG, "onComplete:current location is null");
                            Toast.makeText(DirectionActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation:" + e.getMessage());
        }
    }

    private void sendDirectionRequest(LatLng startPosition, LatLng endPosition) {
        //test for google directions
        //LatLng startPosition = new LatLng(1.369719, 103.849661);
        //LatLng endPosition = new LatLng(1.365738, 103.856894);
        Log.d(TAG, "sendDirectionRequest:init");
        String start = startPosition.latitude + "," + startPosition.longitude;
        String end = endPosition.latitude + "," + endPosition.longitude;
        Log.d(TAG, "sendDirectionRequest:start:" + start + " end:" + end);
        /*mMap.addMarker(new MarkerOptions()
                .position(startPosition)
                .title("Starting")
        );

        mMap.addMarker(new MarkerOptions()
                .position(endPosition)
                .title("Ending")
        );*/

      /*  mMap.addPolyline(new PolylineOptions().add(
                startPosition,
                endPosition
        )
                .width(10)
                .color(Color.BLUE));*/

        try {
            new DirectionFinder(this, start, end).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        Log.d(TAG, "onDirectionFinderStart:init");
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        Log.d(TAG, "onDirectionFinderSuccess:init");
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            //((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            //((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);
            Toast.makeText(getApplicationContext(), "Distance: " + route.distance.text + " Duration:" + route.duration.text, Toast.LENGTH_SHORT).show();
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title("Starting Place")
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(toilet.getToilet_address())
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
}
