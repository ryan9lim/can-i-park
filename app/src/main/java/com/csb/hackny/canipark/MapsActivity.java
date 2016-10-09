package com.csb.hackny.canipark;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMyLocationButtonClickListener,
        OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback  {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setTitle("Can I Park Here?");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final Button clickButton = (Button) findViewById(R.id.my_button);
        clickButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = -91; //error markers
                double lng = -181;
                if(mMarker != null){
                    LatLng pos =  mMarker.getPosition();
                    lat = pos.latitude;
                    lng = pos.longitude;
                }
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
                String url ="http://172.30.20.17:8081/";
                if(lat > -91 && lng > -181){
                   url += "?lat="+lat+"&lng="+lng;
                    Log.d("LatLng",lat+" "+lng);
                }
                final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);
                dialog.show();
                clickButton.setEnabled(false);
                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                clickButton.setEnabled(true);
                                dialog.dismiss();
                                double responseNum = Double.parseDouble(response); //temp value
                                String styledText = "Please place marker near a street in New York";
                                String titleText = "Can I Park Here";
                                if(responseNum < .10){
                                    styledText = String.format("There is a <font color='#009900'>%d%%</font> chance of parking ticket.", (int) (responseNum*100));
                                    titleText = "<font color='#009900'>Park is safe</font>";
                                } else if (responseNum < .25){
                                    styledText = String.format("There is a <font color='#ffcc00'>%d%%</font> chance of parking ticket.", (int) (responseNum*100));
                                    titleText = "<font color='#ffcc00'>Park at your own risk</font>";
                                } else if (responseNum < .50){
                                    styledText = String.format("There is a <font color='#ffa500'>%d%%</font> chance of parking ticket.",(int) (responseNum*100));
                                    titleText = "<font color='#ffa500'>Parking not recommended</font>";
                                } else {
                                    styledText = String.format("There is a <font color='#cc0000'>%d%%</font> chance of parking ticket.",(int) (responseNum*100));
                                    titleText = "<font color='#cc0000'>Do not park here</font>";
                                }
                                new AlertDialog.Builder(MapsActivity.this)
                                        .setTitle(Html.fromHtml(titleText))
                                        .setMessage(Html.fromHtml(styledText))
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete
                                            }
                                        })
                                        .show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        clickButton.setEnabled(true);
                        dialog.dismiss();
                        Log.e("Connect Error",""+error.getMessage());
                        new AlertDialog.Builder(MapsActivity.this)
                                .setTitle("Can I Park Here?")
                                .setMessage("Please place marker on the street.")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                    }
                                })
                                .show();
                    }
                });
                // Add the request to the RequestQueue.
                queue.add(stringRequest);

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();/*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        LocationManager  manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria mCriteria = new Criteria();
        String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location mLocation = manager.getLastKnownLocation(bestProvider);
            if (mLocation != null) {
                Log.e("TAG", "GPS is on");
                final double currentLatitude = mLocation.getLatitude();
                final double currentLongitude = mLocation.getLongitude();
                LatLng loc1 = new LatLng(currentLatitude, currentLongitude);
                mMap.clear();
                mMarker = mMap.addMarker(new MarkerOptions().position(loc1).title("Your Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 19));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
            }
        }
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMap.clear();
                mMarker = mMap.addMarker( new MarkerOptions()
                        .position( cameraPosition.target )
                        .title( cameraPosition.toString() )
                );
            }

        });

        /*googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng position=marker.getPosition();
                String filterAddress = "";
                Geocoder geoCoder = new Geocoder(
                        getBaseContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geoCoder.getFromLocation(
                            position.latitude,
                            position.longitude, 1);

                    if (addresses.size() > 0) {
                        for (int index = 0;
                             index < addresses.get(0).getMaxAddressLineIndex(); index++)
                            filterAddress += addresses.get(0).getAddressLine(index) + " ";
                    }
                }catch (IOException ex) {
                    ex.printStackTrace();
                }
                Log.d(getClass().getSimpleName(),
                        String.format("Dragging to %f:%f", position.latitude,
                                position.longitude));
                TextView myTextView = (TextView) findViewById(R.id.test);
                myTextView.setText("Address is: " + filterAddress);
                return false;
            }
        });*/
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Back to current position", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}
