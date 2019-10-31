package com.fallntic.ridesharepassenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.rideshareclient.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fallntic.ridesharepassenger.DataHolder.navSelected;

public class CreateRequestActivity extends AppCompatActivity implements TaskLoadedCallback{
    private Button mSubmitButton;
    private Button mCancelButton;
    private Button mDateButton;
    private TimePicker mTimePicker;
    private TextView mTextView_pickUpLocation;
    private TextView mTextView_dropOffLocation;
    private TextView mTextView_clientName;
    private EditText mEditView_estTime;
    private Date mDate = Calendar.getInstance().getTime();
    private String url;
    private RequestQueue mRequestQueue;
    private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    private String estTime;
    final Handler handler = new Handler();

    private static final String DIALOG_DATE = "DialogDate";
    private final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final int REQUEST_DATE = 0;

    private static final String TAG = "CreateRequestActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private Button mLocationsButton;
    private TextView mTextView_riderTime;

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("james", "Starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        mLocationsButton = (Button) findViewById(R.id.btn_locations);
        mLocationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CreateRequestActivity.this, MapsActivity.class);
                startActivityForResult(i,0);
            }
        });

        mTextView_clientName = (TextView) findViewById(R.id.client_name);
        mTextView_clientName.setText(DataHolder.mUser.getName());

        mTextView_pickUpLocation = (TextView) findViewById(R.id.txt_pick_up);
        mTextView_dropOffLocation = (TextView) findViewById(R.id.txt_drop_off);
        mTextView_pickUpLocation.setEnabled(false);
        mTextView_dropOffLocation.setEnabled(false);


        /* If a user provided addresses
         */
        if(DataHolder.addressSelected) {
            mTextView_pickUpLocation.setVisibility(View.VISIBLE);
            mTextView_dropOffLocation.setVisibility(View.VISIBLE);
            mTextView_pickUpLocation.setText("Pick up address:\n" + DataHolder.mPickUpAddress + "\n");
            mTextView_dropOffLocation.setText("Drop off address:\n" + DataHolder.mDropOffAddress);
        }
        else {
            //Hide TextViews pick up and drop off.
            RelativeLayout.LayoutParams pickUp_param = (RelativeLayout.LayoutParams) mTextView_pickUpLocation.getLayoutParams();
            pickUp_param.addRule(RelativeLayout.BELOW, R.id.client_name);
            mTextView_pickUpLocation.setVisibility(View.INVISIBLE);

            RelativeLayout.LayoutParams dropOff_param = (RelativeLayout.LayoutParams) mTextView_dropOffLocation.getLayoutParams();
            dropOff_param.addRule(RelativeLayout.BELOW, R.id.client_name);
            mTextView_dropOffLocation.setVisibility(View.INVISIBLE);

            mTextView_riderTime = (TextView) (TextView) findViewById(R.id.riderTime);
            RelativeLayout.LayoutParams riderTime_param = (RelativeLayout.LayoutParams) mTextView_riderTime.getLayoutParams();
            dropOff_param.addRule(RelativeLayout.BELOW, R.id.btn_locations);
            mTextView_dropOffLocation.setVisibility(View.INVISIBLE);

        }

        mTimePicker = (TimePicker) findViewById(R.id.timePicker);

        mEditView_estTime = (EditText) findViewById(R.id.riderEditEst);

        mDateButton = (Button) findViewById(R.id.rider_date_button);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mDate);
                dialog.setTargetFragment(null, REQUEST_DATE);
                dialog.show(fragmentManager, DIALOG_DATE);
            }
        });

        mSubmitButton = (Button) findViewById(R.id.rider_submit_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    DataHolder.showProgressDialog(CreateRequestActivity.this);
                    if (DataHolder.mDropOff_latlng == null || DataHolder.mPickUp_latlng == null) {
                        DataHolder.dismissProgressDialog();
                        toastMessage("Please select where you want to go.");
                    }
                    else if(mEditView_estTime.getText().toString().isEmpty()) {
                        DataHolder.dismissProgressDialog();
                        toastMessage("Please fill out the amount of time you will be there.");
                        Log.d("james", "No est time.");
                    }else {
                        //Send all data to ListRequestsActivity

                        if (isConnected()) {
                            parseJSON();
                            navSelected = "myRides";
                            RideListHandler.getInstance().updateList(CreateRequestActivity.this, "/api/client-requests?id=" + DataHolder.mUser.getId(), "MyRides", DataHolder.mUser.getName());

                            /*
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    DataHolder.dismissProgressDialog();
                                    DataHolder.addressSelected = false;
                                    DataHolder.mDropOff_latlng = null;
                                    DataHolder.mPickUp_latlng = null;
                                    DataHolder.mPickUpAddress = "";
                                    DataHolder.mDropOffAddress = "";
                                    navSelected = "myRides";

                                    RideListHandler.getInstance().updateList(CreateRequestActivity.this, "/api/client-requests?id=" + DataHolder.mUser.getId(), "MyRides", DataHolder.mUser.getName());
                                }
                            }, 1000);

                             */
                            DataHolder.dismissProgressDialog();
                        } else {
                            DataHolder.dismissProgressDialog();
                            toastMessage("No internet or service.");
                        }

                    }
                }

        });

        mCancelButton = (Button) findViewById(R.id.rider_cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send all data to ListRequestsActivity
                Intent intent = new Intent(CreateRequestActivity.this, ListRequestsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateDate() {
        mDateButton.setText(formatter.format(mDate));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume(){
        super.onResume();
        Bundle bundle = this.getIntent().getExtras();

        if(bundle != null) {
            final String sender = bundle.getString("SENDER_KEY");

            if (sender != null) {
                Intent i = getIntent();
                Date date = (Date) i.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mDate = date;
                updateDate();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataHolder.dismissProgressDialog();
    }
    //Gets address from lat long.

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

    private void parseJSON(){
        String time;
        DataHolder.showProgressDialog(this);
        if(mTimePicker.getCurrentMinute() < 10){
            time = mTimePicker.getCurrentHour() + ":0" + mTimePicker.getCurrentMinute();
        }
        else {
            time = mTimePicker.getCurrentHour() + ":" + mTimePicker.getCurrentMinute();
        }

        mRequestQueue = Volley.newRequestQueue(this);
        url = "https://apps.ericvillnow.com/rideshare/api/create-request?client_id=" +
                DataHolder.mUser.getId() + "&destination_address=" +
                getAddress(DataHolder.mDropOff_latlng.latitude, DataHolder.mDropOff_latlng.longitude) +
                "&pick_up_address=" + getAddress(DataHolder.mPickUp_latlng.latitude, DataHolder.mPickUp_latlng.longitude) +
                "&estimated_length=" + mEditView_estTime.getText().toString() + "&time=" + time + "&date=" +
                formatter.format(mDate);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                DataHolder.dismissProgressDialog();
                JSONObject item = response;
                try {
                    String clientID = response.getString("client_id");
                    String id = response.getString("id");
                    String created_at = response.getString("created_at");
                    String updated_at = response.getString("updated_at");
                    String pickupAddress = response.getString("destination_address");
                    String destAddress = response.getString("destination_address");
                    String estTime = response.getString("estimated_length");
                    String date = response.getString("date");
                    String time = response.getString("time");
                    toastMessage("Request Sent Succesfully!");
                    Log.d("james", "Sent successfully!");
                } catch (JSONException e1) {
                    toastMessage("Request Failed.");
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DataHolder.dismissProgressDialog();
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

    @Override
    public void onTaskDone(String string, Object... values) {
        estTime = string;
        Log.d("james", "Est: " + estTime);
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

            return connected;

        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }

        return connected;
    }


    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, ListRequestsActivity.class));
    }
}