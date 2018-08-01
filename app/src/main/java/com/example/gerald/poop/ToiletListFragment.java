package com.example.gerald.poop;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ToiletListFragment extends Fragment {

    private View rootView;
    private ImageView ivCancel;
    private ListView lvToilets;
    private ToiletCustomAdapter toiletCustomAdapter;
    private ArrayList<Toilet> toilets;
    private DatabaseReference dbToilets;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    public static final String TAG = "ToiletListFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_toilet_list, container, false);
        ivCancel = rootView.findViewById(R.id.ivCancel);
        lvToilets = rootView.findViewById(R.id.lvToilet);
        dbToilets = FirebaseDatabase.getInstance().getReference("toilets");

        init();
        return rootView;
    }

    private void init() {
        Log.d(TAG, "init:start");
        initToiletCustomAdapter();
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "back button is pressed", Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }
        });

        lvToilets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.d(TAG, "toilet: " + toilets.get(pos).getToilet_title() + " is clicked");
                getToiletDetails(toilets.get(pos).getToilet_id());
            }
        });

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

    private void showToiletDetailsDialog(final Toilet toilet){
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
        tvRating.setText(toilet.getToilet_rating()+"/5");
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

    private void initToiletCustomAdapter() {
        Log.d(TAG, "initToiletCustomAdapter:start");
        toilets = new ArrayList<>();

        dbToilets.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                toilets.clear();
                for (DataSnapshot toiletSnapshot : dataSnapshot.getChildren()) {
                    Toilet toilet = toiletSnapshot.getValue(Toilet.class);
                    Log.d(TAG, "getAllToilets: toilet " + toilet.getToilet_id());
                    toilets.add(toilet);
                }
                //  getDeviceLocation();

                if(toiletCustomAdapter == null){
                    toiletCustomAdapter = new ToiletCustomAdapter(getActivity(), R.layout.toilet_list_item, toilets);
                    lvToilets.setAdapter(toiletCustomAdapter);
                    Log.d(TAG, "initToiletCustomAdapter: toilets length:" + toilets.size());
                    Log.d(TAG, "initToiletCustomAdapter: created new custom adapter");
                }
                else{
                    toiletCustomAdapter.notifyDataSetChanged();
                    Log.d(TAG, "initToiletCustomAdapter: custom adapter exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

 /*   public void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation:getting device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        try {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete:found location");
                            Location currentLocation = (Location) task.getResult();
                            Log.d(TAG, "current Location: " + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
                        } else {
                            Log.d(TAG, "onComplete:current location is null");
                            Toast.makeText(getContext(), "unable to get current location", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation:" + e.getMessage());
        }
    }*/

}