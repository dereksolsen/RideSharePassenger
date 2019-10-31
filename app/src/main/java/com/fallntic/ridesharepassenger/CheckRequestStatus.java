package com.fallntic.ridesharepassenger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

import java.util.HashSet;
import java.util.Set;

public class CheckRequestStatus extends Service {

    public static final long NOTIFY_INTERVAL = 3660 * 1000;
    private Handler handler = new Handler();
    private final Binder mBinder = new Binder();

    private int id = -1;

    private static String[] statusList;
    private static String[] oldStatusList;
    private String CHANNEL_ID = "com.android.ridesharepassenger.channel_id";

    public CheckRequestStatus() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp = getSharedPreferences("id", Context.MODE_PRIVATE);
        id = sp.getInt("clientID", -1);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                updateList(CheckRequestStatus.this, "/api/client-requests?id=" + id, "MyRides");
            }
        });
        return android.app.Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void updateList(Context context, String endpoint, final String listType) { //type of list: Accepted, Pending, History
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_clock_regular)
                .setContentTitle("RideShare")
                .setContentText("Your ride has been accepted!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        String url = context.getResources().getString(R.string.server_addr) + endpoint;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    statusList = new String[response.length()];
                    Set<String> set = new HashSet<>();
                    SharedPreferences sp = getSharedPreferences("id", Context.MODE_PRIVATE);
                    set = sp.getStringSet("status", null);
                    if(set != null) {
                        oldStatusList = set.toArray(new String[0]);
                    }

                    for (int i = 0; i < response.length(); i++) {
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
                            statusList[i] = status;
                            if (oldStatusList !=null) {
                                if (oldStatusList[i] != status && oldStatusList[i] == "0") {
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(CheckRequestStatus.this);

                                    // notificationId is a unique int for each notification that you must define
                                    notificationManager.notify(1, builder.build());
                                    statusList[i] = status;
                                }
                            }
                        }

                        //String clientName = item.getString("name");
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
                        Log.d("rating", "" + driverEarnedRating);

                    }
                    //Save status
                    Set<String> set2 = new HashSet<>();
                    for (String s : statusList){
                        set2.add(s);
                    }
                   SharedPreferences.Editor editor = sp.edit();
                    editor.putStringSet("status", set2);
                    editor.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mRequestQueue.add(request);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
