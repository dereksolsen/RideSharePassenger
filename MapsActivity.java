package com.cs3370.findme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private final int REQUEST_LOCATION_PERMISSIONS = 0;

    private GoogleMap mMap;
    private FusedLocationProviderClient mClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private LatLng mOrigin, mDestination;
    private MarkerOptions mMarkerOrigin, mMarkerDestination;
    private Polyline currentPolyline;
    private double distance;
    private String url;
    private String travelMode;
    private TextView distanceBox;
    private boolean zoom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Create location callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        updateMap(location);
                    }
                }
            }
        };

        mClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void updateMap(Location location) {
        // Get current location
        mOrigin = new LatLng(location.getLatitude(),
                location.getLongitude());

        mDestination = new LatLng(47.478950, -94.834060);

        // Place a marker at the current location
        mMarkerOrigin = new MarkerOptions()
                .title("Here you are!")
                .position(mOrigin);

        mMarkerDestination = new MarkerOptions()
                .title("Here is Derick!")
                .position(mDestination);

        // Remove previous marker
        mMap.clear();

        // Add new marker
        mMarkerOrigin.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(mMarkerOrigin);

        mMarkerDestination.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(mMarkerDestination);

        distanceBox = (TextView) findViewById(R.id.distance);
        distanceBox.setText("The Distance to Derek: " + String.format("%.2f", distance) +" Miles");

        distance = getDistance(mOrigin, mDestination);
        String path;
        if (distance < 1) {
            travelMode = "walking";
            path = "Walking path";
        }
        else if(distance <= 3){
            travelMode = "bicycling";
            path = "Bicycling path";
        }
        else {
            travelMode = "driving";
            path = "Driving path";
        }

        url = getUrl(mOrigin, mDestination, travelMode);

        new FetchURL(MapsActivity.this).execute(getUrl(mMarkerOrigin.getPosition(), mMarkerDestination.getPosition(), travelMode), travelMode);


        // Move and zoom to current location at the street level
        if(!zoom) {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(mOrigin, 11);

            Toast toast = Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT);
            toast.show();
            mMap.animateCamera(update);

            zoom = true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }

    @Override
    public void onPause() {
        super.onPause();
        mClient.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

        if (hasLocationPermission()) {
            mClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private boolean hasLocationPermission() {

        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);

            return false;
        }

        return true;
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }


    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }


    public double getDistance(LatLng latlngA, LatLng latlngB) {
        Location locationA = new Location("point A");

        locationA.setLatitude(latlngA.latitude);
        locationA.setLongitude(latlngA.longitude);

        Location locationB = new Location("point B");

        locationB.setLatitude(latlngB.latitude);
        locationB.setLongitude(latlngB.longitude);

        float distance = locationA.distanceTo(locationB) / 1000;//To convert Meter in Kilometer

        //return String.format("%.2f", distance);

        return distance * 0.621371;
    }

}