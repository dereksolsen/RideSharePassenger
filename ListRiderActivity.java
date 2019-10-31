package com.android.rideshare;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ListRiderActivity extends AppCompatActivity {
    private Button mBackButton;
    private RequestQueue mRequestQueue;
    private RequestItemArrayList mRequestItemArrayList;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;

    private void parseJSON(){
        String url = "rideshare.ddns.net/api/serviceable-requests";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++){
                        JSONObject item = response.getJSONObject(i);

                        String id = item.getString("id");
                        String client_id = item.getString("client_id");
                        String driver_id = item.getString("driver_id");
                        String status = item.getString("status");
                        String destination_address = item.getString("destination_address");
                        String pick_up_address = item.getString("pick_up_address");
                        String estimated_length = item.getString("estimated_length");
                        String time = item.getString("time");
                        String date = item.getString("date");
                        String created_at = item.getString("created_at");
                        String updated_at = item.getString("updated_at");
                        mRequestItemArrayList.add(new RequestItem(id, client_id, driver_id, status, destination_address, pick_up_address, estimated_length, time, date, created_at, updated_at));

                    }
                    mAdapter = new ViewAdapter(DriverHomePage.this, mRequestItemArrayList);
                    mRecyclerView.setAdapter(mAdapter);

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_rider);
        class Rider {
            public String id;
            public String destination;
            public int estTime;

            public Rider() {

            }
            public Rider(String id,  String destination, int estTime){
                this.id = id;
                this.destination= destination;
                this.estTime=estTime;
            }
            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }


            public String getDestination() {
                return destination;
            }

            public void setDestination(String destination) {
                this.destination=destination;
            }
            public int getEstTime() {
                return estTime;
            }

            public void setEstTime(int estTime) {
                this.estTime = estTime;
            }
        }


        class RiderAdapter extends RecyclerView.Adapter<RiderAdapter.ViewHolder>{
            private Context context;
            private List<Rider> list;

            public RiderAdapter(Context context, List<Rider> list){
                this.context=context;
                this.list =list;
            }
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
                View v = LayoutInflater.from(context).infalte(R.layout.single_item, parent, false);
                return new ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                Rider rider = list.get(position);

                holder.textName.setText(Rider.getId());
                holder.textDestination.setText(String.valueOf(Rider.getDestination()));
                holder.textEstTime.setText(String.valueOf(Rider.getEstTime()));

            }

            @Override
            public int getItemCount() {
                return list.size();
            }

            class ViewHolder extends RecyclerView.ViewHolder {
                public TextView textName, textDestination, textEstTime;

                public ViewHolder(View itemView) {
                    super(itemView);


                    textName = itemView.findViewById(R.id.main_id);
                    textDestination = itemView.findViewById(R.id.main_destination);
                    textEstTime = itemView.findViewById(R.id.main_estTime);
                }
            }

        }



        mBackButton = (Button) findViewById(R.id.back_button);
                mBackButton.setOnClickListener(new View.OnClickListener()); {

        }

@Override
public void onClick(View v) {
        onBackPressed();
        }
        }


