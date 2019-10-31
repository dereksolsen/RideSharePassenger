package com.fallntic.ridesharepassenger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.rideshareclient.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private Button mSubmit;


    public String TAG = "MapsActivityClient";
    public static final String EXTRA_WHAT_LOCATION = "com.android.ridesharepassenger.what_location";
    public static final String EXTRA_LOCATION = "com.android.ridesharepassenger.location";
    private final int AUTOCOMPLETE_REQUEST_CODE = 1001;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mCurrentLocation;


    //Variables to get my current location
    private Polyline currentPolyline;
    private Boolean mLocationPermissionsGranted = false;
    private boolean pickUp;
    private boolean dropOff;
    private boolean addressSelected = false;
    private ImageView mGps;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;


    RelativeLayout rel_pick_up;
    TextView textView_pickUp;

    RelativeLayout rel_drop_off;
    TextView textView_dropOff;
    private Button mBackButton;
    private String mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //Get permission to use our location
        getLocationPermission();

        getDeviceLocation();

        //GPS icon which will bring us to our current location
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                //Position the map on our current location
                getDeviceLocation();
            }
        });

        //Initialize Places
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAefQArjnf5orqkbWJj7aXTzAxES_YVErM");
        }

        // Pick up field
        rel_pick_up = (RelativeLayout) findViewById(R.id.rel_pick_up);
        textView_pickUp = (TextView) findViewById(R.id.txt_pick_up);
        rel_pick_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Boolean variables allow us to know which field
                //is clicked: pick up address or drop off address
                pickUp = true;
                dropOff = false;
                showAutocomplete();
            }
        });

        // Drop off field
        rel_drop_off = (RelativeLayout) findViewById(R.id.rel_drop_off);
        textView_dropOff = (TextView) findViewById(R.id.txt_drop_off);
        rel_drop_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropOff = true;
                pickUp = false;
                showAutocomplete();
            }
        });

        //Submit button
        mSubmit = (Button) findViewById(R.id.submit_button_maps);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isEmpty(DataHolder.mPickUpAddress, DataHolder.mDropOffAddress)) {
                    setAnswerShownResult();
                } else
                    toastMessage("Please provide a pick up and a drop off location");
            }
        });

        mBackButton = (Button) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, CreateRequestActivity.class);
                startActivity(intent);
            }
        });
    }

    public void showAutocomplete() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setCountry("US")
                .setHint("Enter Address/Place")
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());

                if (pickUp) {
                    //Get the Latlng
                    mMap.clear();
                    DataHolder.mDropOffAddress = "";
                    textView_dropOff.setText("");
                    DataHolder.mPickUp_latlng = place.getLatLng();
                    //Get the address in a string
                    DataHolder.mPickUpAddress = place.getAddress();
                    //Set the address name into the textView
                    textView_pickUp.setText(DataHolder.mPickUpAddress);
                    addMarker(DataHolder.mPickUp_latlng, "Pick up location");
                }

                if (dropOff) {
                    //Get the Latlng
                    DataHolder.mDropOff_latlng = place.getLatLng();
                    //Get the address in a string
                    DataHolder.mDropOffAddress = place.getAddress();
                    //Set the address name into the textView
                    textView_dropOff.setText(DataHolder.mDropOffAddress);
                    addMarker(DataHolder.mDropOff_latlng, "Drop off location");
                }

                if (!DataHolder.mPickUpAddress.equals("") && !DataHolder.mDropOffAddress.equals("")) {
                    //A boolean that will allow us to know if user has provided the two addresses.
                    DataHolder.addressSelected = true;
                    mMap.clear();
                    addMarker(DataHolder.mPickUp_latlng, "Pick up location");
                    addMarker(DataHolder.mDropOff_latlng, "Drop off location");
                    addPolyline(DataHolder.mPickUp_latlng, DataHolder.mDropOff_latlng);
                }

                moveCamera(place.getLatLng(), DEFAULT_ZOOM, place.getAddress());

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        hideSoftKeyboard();
    }

    private void setAnswerShownResult() {
        Intent data = new Intent(this, CreateRequestActivity.class);
        //Intent data = new Intent(this, DriverMapsActivity.class);
        startActivity(data);
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            //Get Location
                            mCurrentLocation = (Location) task.getResult();
                            //Get LatLng
                            DataHolder.mCurrent_latLng = (new LatLng(mCurrentLocation.getLatitude(),
                                    mCurrentLocation.getLongitude()));
                            //Get address name
                            DataHolder.mCurrentAddress = getAddress(DataHolder.mCurrent_latLng.latitude,
                                    DataHolder.mCurrent_latLng.longitude);

                            moveCamera(DataHolder.mCurrent_latLng, DEFAULT_ZOOM, "My Location");

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            toastMessage("unable to get current location");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        hideSoftKeyboard();
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
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
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" +
                parameters + "&key=" + getString(R.string.google_maps_key);

        return url;
    }

    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0));
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    @Override
    public void onTaskDone(String string, Object... values) {
        //Check if device has internet
        if (isConnected()) {
            if (currentPolyline != null)
                currentPolyline.remove();
            currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
            toastMessage("Estimated Time: " + string);
        } else
            toastMessage("No internet connection!");
    }

    public boolean isEmpty(String pickUp, String dropOff) {
        if (pickUp.equals("") || dropOff.equals("")) {
            return true;
        } else
            return false;
    }

    public void toastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void addMarker(LatLng latLng, String title) {
        MarkerOptions marker = new MarkerOptions();
        marker.position(latLng);
        marker.title(title);
        mMap.addMarker(marker).showInfoWindow();
    }

    public void addPolyline(LatLng latLng1, LatLng latLng2) {
        //Check if device is connected to internet
        if (isConnected()) {
            //Draw Polyline
            new FetchURL(MapsActivity.this).execute(getUrl(latLng1,
                    latLng2, "driving"), "driving");
        } else
            toastMessage("No internet connection!");
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

            return connected;

        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }

        return connected;
    }

}