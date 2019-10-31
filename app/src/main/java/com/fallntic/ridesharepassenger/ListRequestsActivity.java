package com.fallntic.ridesharepassenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.android.rideshareclient.R;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.fallntic.ridesharepassenger.DataHolder.navSelected;

//all lists are displayed in this activity

public class ListRequestsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private User mUser;
    private TextView mListTitle, textViewNavUserName;

    private RequestQueue mRequestQueue;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private View navHeader;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_requests);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //******************* Init the drawer menu ******************************
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navHeader = navigationView.getHeaderView(0);
        textViewNavUserName = navHeader.findViewById(R.id.textView_navUserName);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        //**************** Set the drawer menu ************************************
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //**************************************************************************

        textViewNavUserName.setText(DataHolder.mUser.getName());

        mListTitle = (TextView) findViewById(R.id.list_title);

        if (navSelected == null) {
            mListTitle.setText("My Rides");
            navSelected = "myRides";
            RideListHandler.getInstance().updateList(this, "/api/client-requests?id=" +
                    DataHolder.mUser.getId(), "MyRides", DataHolder.mUser.getName());
        }
        else if (navSelected.equals("myRides")){
            mListTitle.setText("My Rides");
        }
        else if (navSelected.equals("history")){
            mListTitle.setText("History");
        }
        if (navSelected != null)
            showListItems();
    }

    public void showListItems(){
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        RecyclerSectionItemDecoration sectionItemDecoration =
                new RecyclerSectionItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height),
                        true, getSectionCallback(RideListHandler.getInstance().getList()));
        mRecyclerView.addItemDecoration(sectionItemDecoration);

        if (RideListHandler.getInstance().getList().isEmpty() && navSelected.equals("myRides"))
            mListTitle.setText("No rides recorded.");
        else if (RideListHandler.getInstance().getList().isEmpty() && navSelected.equals("history"))
            mListTitle.setText("History empty.");
        DataHolder.dismissProgressDialog();
    }

    private RecyclerSectionItemDecoration.SectionCallback getSectionCallback(final List<DisplayListItem> thelist) {
        return new RecyclerSectionItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                String dateOne = thelist.get(Math.min(position, thelist.size() - 1)).getPickUpDate();
                String dateTwo = thelist.get(Math.min(position + 1, thelist.size() - 1)).getPickUpDate();
                return (position == 0 || (dateOne != dateTwo) || position == thelist.size() - 1);
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                return thelist.get(Math.min(position, thelist.size() - 1)).getPickUpDate();
            }
        };
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item))
            return true;

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_clientRides:
                navSelected = "myRides";
                RideListHandler.getInstance().updateList(this, "/api/client-requests?id=" + DataHolder.mUser.getId(), "MyRides", DataHolder.mUser.getName());
                break;

            case R.id.nnav_newRideRequest:
                startActivity(new Intent(this, CreateRequestActivity.class));
                break;

            case R.id.nav_clientHistory:
                navSelected = "history";
                RideListHandler.getInstance().getHistory(this, "/api/client-history?id=" + DataHolder.mUser.getId(), "History");
                //RideListHandler.getInstance().updateList(this, "/api/client-history?id=" + DataHolder.mUser.getId(), "History");
                break;

            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.nav_logout:
                logout();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public  void logout(){
        SharedPreferences sharedpreferences = getSharedPreferences(MainActivity.MYPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
        parseJSON(false);
    }

    public void parseJSON(final boolean shutdown) {
        DataHolder.showProgressDialog(this);
        mRequestQueue = Volley.newRequestQueue(this);
        String url = "https://apps.ericvillnow.com/rideshare/api/logout?email=" + DataHolder.mUser.getEmail();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                DataHolder.dismissProgressDialog();
                try {

                    boolean logout = response.getBoolean("logout");
                    if (logout) {
                        toastMessage("Logout successful!");
                        if (!shutdown) {
                            Intent intent = new Intent(ListRequestsActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        toastMessage("Couldn't log out.");
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DataHolder.dismissProgressDialog();
                toastMessage("Error: no response from the server");
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

    public void toastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataHolder.dismissProgressDialog();
        parseJSON(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataHolder.dismissProgressDialog();
    }
}

