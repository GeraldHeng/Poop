package com.example.gerald.poop;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;

public class ToiletCustomAdapter extends ArrayAdapter {
    Context parent_context;
    int layout_id;
    ArrayList<Toilet> toiletList;

    public static final String TAG = "ToiletCustomAdapter";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    Location userLocation = new Location("");

    public ToiletCustomAdapter(Context context, int resource, ArrayList<Toilet> objects) {
        super(context, resource, objects);

        parent_context = context;
        layout_id = resource;
        toiletList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) parent_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(layout_id, parent, false);
        final Toilet currentToilet = toiletList.get(position);
        final TextView tvTitle = rowView.findViewById(R.id.tvTitle);
        final TextView tvAddress = rowView.findViewById(R.id.tvAddress);
        final TextView tvDistance = rowView.findViewById(R.id.tvDistance);

        Location toiletLocation = new Location("");
        toiletLocation.setLatitude(currentToilet.getToilet_lat());
        toiletLocation.setLongitude(currentToilet.getToilet_lng());
        Log.d(TAG, "toilet name: " + currentToilet.getToilet_title());
        //getDeviceLocation();

        double distance = getDistanceBetween(currentToilet);
        tvTitle.setText(currentToilet.getToilet_title());
        tvAddress.setText(currentToilet.getToilet_address());
        tvDistance.setText(String.format("%.2f Meters Away", distance));
        return rowView;
    }

    public void bubble_srt(ArrayList<Toilet> toilets) {
        int n = toilets.size();
        int k;
        for (int m = n; m >= 0; m--) {
            double distance = getDistanceBetween(toilets.get(m));
            for (int i = 0; i < n - 1; i++) {
                k = i + 1;
                double secondDistance = getDistanceBetween(toilets.get(k));
                if (distance > secondDistance) {
                    swapNumbers(i, k, toilets);
                }
            }
            Log.d("bubble sort", toilets.get(m).getToilet_title());
        }
    }

    private void swapNumbers(int i, int j, ArrayList<Toilet> toilets) {

        Toilet temp;
        temp = toilets.get(i);
        toilets.get(i).setToilet(toilets.get(j));
        toilets.get(j).setToilet(temp);
    }

    private LatLng getDeviceLocationLatLng() {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        Log.d(TAG, "getDeviceLatLng: " + longitude + ", " + latitude);
        return new LatLng(latitude, longitude);
    }

    private Double getDistanceBetween(Toilet toilet) {
        Log.d(TAG, "findClosestToilet: init");
        LatLng currentLatLng = getDeviceLocationLatLng();
        Location deviceLocation = new Location("device");
        deviceLocation.setLatitude(currentLatLng.latitude);
        deviceLocation.setLongitude(currentLatLng.longitude);
        Log.d(TAG, "getDeviceLatLng: " + deviceLocation.getLatitude() + ", " + deviceLocation.getLongitude());

        Location toiletLocation = new Location("toilet");
        toiletLocation.setLongitude(toilet.getToilet_lng());
        toiletLocation.setLatitude(toilet.getToilet_lat());
        double distance = deviceLocation.distanceTo(toiletLocation);
        Log.d(TAG, "findClosestToilet distance between: " + distance);

        return distance;
    }
}
