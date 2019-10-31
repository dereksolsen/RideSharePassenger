package com.fallntic.ridesharepassenger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private List<DisplayListItem>  mDisplayList;
    private RequestQueue mRequestQueue;

    public MyAdapter() {
        RideListHandler.getInstance().sortList();
        this.mDisplayList = RideListHandler.getInstance().getList();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pretty_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DisplayListItem displayListItem = mDisplayList.get(position);

        Log.d("Status", displayListItem.getStatus());
        Log.d("Rating", "" + displayListItem.getRating());

        holder.mClient.setText(displayListItem.getClientName());
        holder.mPickup.setText(displayListItem.getPickup());
        holder.mDropOff.setText(displayListItem.getDropOff());
        holder.mPickUpTime.setText(displayListItem.getPickUpTime());
        holder.mEstimatedLength.setText(displayListItem.getEstimatedLength());
        holder.mId.setText(displayListItem.getId());
        holder.mStatus.setText(displayListItem.getStatus());
        holder.mRating.setText(displayListItem.getRating());
        if (displayListItem.getStatus().equals("0")) {
            holder.mStatusColor.setBackgroundColor(Color.parseColor("#ffffc107"));
            holder.mStatusImage.setImageResource(R.drawable.ic_hourglass_half_solid);
        } else if (displayListItem.getStatus().equals("1")) {
            holder.mStatusColor.setBackgroundColor(Color.parseColor("#5BBFEC"));
            holder.mStatusImage.setImageResource(R.drawable.ic_check_circle_regular);
        } else if (displayListItem.getStatus().equals("2")) {
            //ToDo change this to display rating
            holder.mStatusColor.setBackgroundColor(Color.parseColor("#5BBFEC"));

            //get rating
            String rating = displayListItem.getRating();
            if (rating.equals("1")) {
                holder.mStatusImage.setVisibility(View.VISIBLE);
                holder.mStatusImage.setImageResource(R.drawable.ic_sentiment_very_dissatisfied_black_24dp);
            } else  if (rating.equals("2")) {
                holder.mStatusImage.setVisibility(View.VISIBLE);
                holder.mStatusImage.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_24dp);
            } else if (rating.equals("3")) {
                holder.mStatusImage.setVisibility(View.VISIBLE);
                holder.mStatusImage.setImageResource(R.drawable.ic_sentiment_neutral_black_24dp);
            }else if (rating.equals("4")) {
                holder.mStatusImage.setVisibility(View.VISIBLE);
                holder.mStatusImage.setImageResource(R.drawable.ic_sentiment_satisfied_black_24dp);
            }else if (rating.equals("5")) {
                holder.mStatusImage.setVisibility(View.VISIBLE);
                holder.mStatusImage.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_24dp);
            }else {
                holder.mStatusImage.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDisplayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mClient;
        public TextView mPickup;
        public TextView mDropOff;
        public TextView mPickUpTime;
        public TextView mEstimatedLength;
        public TextView mStatus;
        public View mStatusColor;
        public ImageView mStatusImage;
        public TextView mId;
        public TextView mRating;
        private final EditText editText;

        private final Context context;

        public ViewHolder(View itemView) {
            super(itemView);

            mClient = (TextView) itemView.findViewById(R.id.clientName);
            mPickup = (TextView) itemView.findViewById(R.id.pickup);
            mDropOff = (TextView) itemView.findViewById(R.id.dropoff);
            mPickUpTime = (TextView) itemView.findViewById(R.id.pickupTime);
            mEstimatedLength = (TextView) itemView.findViewById(R.id.estimatedLength);
            mStatus = (TextView) itemView.findViewById(R.id.status);
            mStatus.setVisibility(View.INVISIBLE);
            mStatusColor = (View) itemView.findViewById((R.id.StatusColor));
            mStatusImage = (ImageView) itemView.findViewById((R.id.StatusImage));
            mId = (TextView) itemView.findViewById(R.id.id);
            mId.setVisibility(View.INVISIBLE);
            mRating = (TextView) itemView.findViewById(R.id.rating);
            mRating.setVisibility(View.INVISIBLE);
            context = itemView.getContext();
            editText = new EditText(context);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            Log.d("onClickStatus", mStatus.getText().toString());
            Log.d("onClickRating", mRating.getText().toString());
            if (mStatus.getText().toString().equals("2") && (mRating.getText().toString().equals("null") || mRating.getText().equals("0"))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Rate this ride");
                builder.setMessage("Let us know how your experience was.");
                builder.setCancelable(false);
                builder.setView(editText);

                //BUG: Sometimes on the first attempt to rate, the image does not appear and the rating does not get changed.

                builder.setPositiveButton("Satisfactory", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendRatingToServer(mId.getText().toString(), "5", editText.getText().toString());

                        mRating.setText("5");
                        mStatusImage.setVisibility(View.VISIBLE);
                        mStatusImage.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_24dp);
                        Toast.makeText(context, "Thank you for rating this ride.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNeutralButton("Neutral", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendRatingToServer(mId.getText().toString(), "3", editText.getText().toString());

                        mRating.setText("3");
                        mStatusImage.setVisibility(View.VISIBLE);
                        mStatusImage.setImageResource(R.drawable.ic_sentiment_neutral_black_24dp);
                        Toast.makeText(context, "Thank you for rating this ride.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Unsatisfactory", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendRatingToServer(mId.getText().toString(), "1", editText.getText().toString());

                        mRating.setText("1");
                        mStatusImage.setVisibility(View.VISIBLE);
                        mStatusImage.setImageResource(R.drawable.ic_sentiment_very_dissatisfied_black_24dp);
                        Toast.makeText(context, "Thank you for rating this ride.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.show();
            } else {
                //Intent intent = new Intent(context, MapsActivity.class);
                //intent.putExtra("pickUp", mPickup.getText());
                //intent.putExtra("dropOff", mDropOff.getText());
                //context.startActivity(intent);
            }
        }
        private void sendRatingToServer(String id, String rating, String comment) {
            DataHolder.showProgressDialog(context);
            mRequestQueue = Volley.newRequestQueue(context);
            String url = context.getResources().getString(R.string.server_addr) + "/api/rate?request_id=" + id + "&is_driver=false&rating=" + rating + "&comment=" + comment;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    DataHolder.dismissProgressDialog();
                    JSONObject item = response;
                    try {
                        String clientID = response.getString("client_id");
                    } catch (JSONException e1) {
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
    }
}
