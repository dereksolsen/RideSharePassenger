package com.fallntic.ridesharepassenger;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.rideshareclient.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//The current displaying rides list (either "My Rides" or "History") will be held here
//It is static so anywhere in the app you can retrieve any ride in the list so long as you have the ride id for it

public class RideListHandler {

    private static RideListHandler instance = null;

    private static List<DisplayListItem> mRidesList;

    public static RideListHandler getInstance() {
        if (instance == null) {
            instance = new RideListHandler();
        }
        return instance;
    }

    private RideListHandler() {
        mRidesList = new ArrayList<>();
    }

    public List<DisplayListItem> getList() {
        return mRidesList;
    }

    public DisplayListItem getItemById(String id) {
        for (int i = 0; i < mRidesList.size() - 1; i++) {
            if (mRidesList.get(i).getId().equals(id)) {
                return mRidesList.get(i);
            }
        }
        return null;
    }

    public void updateList(final Context context, String endpoint, final String listType, final String clientName) { //type of list: Accepted, Pending, History
        mRidesList = new ArrayList<>(); //empty previous contents

        DataHolder.showProgressDialog(context);
        RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        String url = context.getResources().getString(R.string.server_addr) + endpoint;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++){
                        JSONObject item = response.getJSONObject(i);
                        String driverEarnedRating;
                        String status;

                        if (listType.equals("History")) { //history items don't have a status, so we will use 2 to identify that
                            JSONObject rating = item.getJSONObject("rating");
                            driverEarnedRating = rating.getString("client_rating");
                            status = "2";
                        } else {
                            driverEarnedRating = null;
                            status = item.getString("status");
                        }


                        //String clientName = item.getString("name");
                        String id = item.getString("id");
                        //String client_id = item.getString("client_id");
                        //String driver_id = item.getString("driver_id");
                        String dropOff = item.getString("destination_address");
                        String pickUp = item.getString("pick_up_address");
                        pickUp = pickUp.replaceAll("\\\\n", "\n");
                        String estimatedLength = item.getString("estimated_length");
                        String pickUpTime = item.getString("time");
                        String pickUpDate = item.getString("date");
                        //String created_at = item.getString("created_at");
                        //String updated_at = item.getString("updated_at");
                        Log.d("rating", "" + driverEarnedRating);
                        DisplayListItem listitem = new DisplayListItem(clientName, pickUp, dropOff, pickUpTime, pickUpDate, estimatedLength, status, id, driverEarnedRating);
                        mRidesList.add(listitem);
                    }

                    DataHolder.dismissProgressDialog();
                    if (DataHolder.navSelected.equals("myRides")) {
                        context.startActivity(new Intent(context, ListRequestsActivity.class));
                    }

                } catch (JSONException e) {
                    DataHolder.dismissProgressDialog();
                    e.printStackTrace();
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


    public void getHistory(final Context context, String endpoint, final String listType) { //type of list: Accepted, Pending, History
        DataHolder.showProgressDialog(context);
        final boolean isPendingList = listType.equals("Pending");
        mRidesList = new ArrayList<>(); //empty previous contents
        mRidesList.clear();

        RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        String url = context.getResources().getString(R.string.server_addr) + endpoint;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++){
                        JSONObject item = response.getJSONObject(i);
                        String status;
                        String clientEarnedRating;

                        if (listType.equals("History")) { //history items don't have a status, so we will use 2 to identify that
                            status = "2";
                            JSONObject rating = item.getJSONObject("rating");
                            clientEarnedRating = rating.getString("client_rating");
                        } else {
                            status = item.getString("status");
                            clientEarnedRating = null;
                        }

                        if (!(isPendingList) || status.equals("0")) { //when we want the pending list of rides, we only want unaccepted ones. Otherwise we want all of them
                            JSONObject client = item.getJSONObject("driver");

                            String clientName = client.getString("name");
                            //String rating = client.getString("rating");
                            String id = item.getString("id");
                            //String client_id = item.getString("client_id");
                            //String driver_id = item.getString("driver_id");
                            String dropOff = item.getString("destination_address");
                            String pickUp = item.getString("pick_up_address");
                            String estimatedLength = item.getString("estimated_length");
                            String pickUpTime = item.getString("time");
                            String pickUpDate = item.getString("date");
                            //String created_at = item.getString("created_at");
                            //String updated_at = item.getString("updated_at");

                            //insert new line character in addresses


                            DisplayListItem listitem = new DisplayListItem(clientName, pickUp, dropOff, pickUpTime, pickUpDate, estimatedLength, status, id, clientEarnedRating);
                            mRidesList.add(listitem);
                        }
                    }
                    DataHolder.dismissProgressDialog();
                    context.startActivity(new Intent(context, ListRequestsActivity.class));
                } catch (JSONException e) {
                    DataHolder.dismissProgressDialog();
                    e.printStackTrace();
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

    public void sortList() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm");
        DisplayListItem curItem;
        DisplayListItem nextItem;
        try {
            for (int i = 0; i < mRidesList.size() - 1; i++) {
                for (int j = 0; j < (mRidesList.size() - 1 - i); j++) {
                    curItem = mRidesList.get(j);
                    nextItem = mRidesList.get(j + 1);
                    Date curItemDate = sdf.parse(curItem.getPickUpDate() + " " + curItem.getPickUpTime());
                    Date nextItemDate = sdf.parse(nextItem.getPickUpDate() + " " + nextItem.getPickUpTime());
                    if (curItemDate.after(nextItemDate)) {
                        mRidesList.set(j, nextItem);
                        mRidesList.set(j + 1, curItem);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}