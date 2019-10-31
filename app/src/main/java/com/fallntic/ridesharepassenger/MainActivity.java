package com.fallntic.ridesharepassenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class MainActivity extends AppCompatActivity {

    private Button mCancelButton;
    private Button mSubmitButton;
    private EditText mEditText_email;
    private EditText mEditText_password;
    private String mEmail;
    private String mPassword;
    private RequestQueue mRequestQueue;
    final Handler handler = new Handler();
    boolean isUserExist;
    private ProgressBar progressBar;
    private TextView mTextView_signingIn;
    private String CHANNEL_ID = "com.android.ridesharepassenger.channel_id";
    SQLiteDatabase myDatabase;

    //Auto login
    SharedPreferences sharedPreferences;
    public static final String MYPREFERENCES = "user_details";
    public static final String NAME = "nameKey";
    public static final String PHONE = "phoneKey";
    public static final String EMAIL = "emailKey";
    public static final String PASSWORD = "emailKey";
    public static final String ID = "idKey";
    public static final String AUTHORIZED = "authorizedKey";
    public static final String ROLE = "roleKey";
    public static final String CREATED_AT = "created_at_key";
    public static final String UPDATED_AT = "updated_at_key";
    public static final String AUTHENTICATED = "authenticatedKey";
    public static final String RATING = "rating";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //createNotificationChannel();

        mEditText_email = (EditText) findViewById(R.id.editText_email);
        mEditText_password = (EditText) findViewById(R.id.editText_pwd);
        mTextView_signingIn = (TextView) findViewById(R.id.signing_in);
        mTextView_signingIn.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(EMAIL) && sharedPreferences.contains(PASSWORD)) {
            getMyPreferences();
            Intent intent = new Intent(MainActivity.this, ListRequestsActivity.class);
            startActivity(intent);
            finish();
        }

        mSubmitButton = (Button) findViewById(R.id.submit_button_signIn);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    //Retrieve data into string variables
                    mEmail = mEditText_email.getText().toString();
                    mPassword = mEditText_password.getText().toString();
                    // Check if email and pwd are provided
                    if (mEmail != null && mPassword != null && !mEmail.equals("") && !mPassword.equals("")) {
                        //Connect into the database
                        parseJSON();
                    } else
                        toastMessage("Email and password field cannot be empty");
                } else
                    toastMessage("No internet connection!");
            }
        });

        //Close the app
        mCancelButton = (Button) findViewById(R.id.back_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NAME, DataHolder.mUser.getName());
        editor.putString(PHONE, DataHolder.mUser.getPhoneNumber());
        editor.putString(EMAIL, DataHolder.mUser.getEmail());
        editor.putString(PASSWORD, mPassword);
        editor.putInt(ID, DataHolder.mUser.getId());
        editor.putInt(AUTHENTICATED, DataHolder.mUser.isAuthenticated());
        editor.putString(CREATED_AT, DataHolder.mUser.getCreated_at());
        editor.putString(ROLE, DataHolder.mUser.getRole());
        editor.putString(UPDATED_AT, DataHolder.mUser.getUpdated_at());
        editor.putBoolean(AUTHORIZED, DataHolder.mUser.isAuthorized());
        editor.putString(RATING, DataHolder.mUser.getRating());
        editor.commit();
    }

    public void getMyPreferences() {
        boolean authorized = sharedPreferences.getBoolean(AUTHORIZED, false);
        String role = sharedPreferences.getString(ROLE, "");
        int id = sharedPreferences.getInt(ID, -1);
        String name = sharedPreferences.getString(NAME, "");
        String email = sharedPreferences.getString(EMAIL, "");
        String phone_number = sharedPreferences.getString(PHONE, "");
        String rating = sharedPreferences.getString(RATING, "");
        String created_at = sharedPreferences.getString(CREATED_AT, "");
        String updated_at = sharedPreferences.getString(UPDATED_AT, "");
        int authenticated = sharedPreferences.getInt(AUTHENTICATED, -1);
        DataHolder.mUser = new User(authorized, role, id, name, email, phone_number,
                rating, created_at, updated_at, authenticated);
    }

    public void toastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Connect into the dataBase and retrieve all user information
     * using a JSONObject.
     */
    public void parseJSON() {
        hideForm();
        mRequestQueue = Volley.newRequestQueue(this);
        String url = "https://apps.ericvillnow.com/rideshare/api/login?email=" +
                mEmail + "&password=" + mPassword;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {

                    boolean authorized = response.getBoolean("authorized");
                    String role = response.getString("role");

                    JSONObject user = response.getJSONObject("user");

                    int id = user.getInt("id");
                    String name = user.getString("name");
                    String email = user.getString("email");
                    String phone_number = user.getString("phone_number");
                    //String rating = user.getString("rating");
                    String rating = "0"; //User rating was removed from the database
                    String created_at = user.getString("created_at");
                    String updated_at = user.getString("updated_at");
                    int authenticated = user.getInt("authenticated");

                    if (authorized && role.equals("client")) {

                        DataHolder.mUser = (new User(authorized, role, id, name, email, phone_number,
                                rating, created_at, updated_at, authenticated));

                        savePreferences();

                        Intent intent = new Intent(MainActivity.this, ListRequestsActivity.class);
                        startActivity(intent);
                    }
                    else {
                        showForm();
                        toastMessage("Email address or password incorrect");
                    }
                } catch (JSONException e1) {
                    showForm();
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showForm();
                toastMessage("Error: no response from the server");
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
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

    public void hideForm() {
        mEditText_email.setVisibility(View.INVISIBLE);
        mEditText_password.setVisibility(View.INVISIBLE);
        mSubmitButton.setVisibility(View.INVISIBLE);
        mCancelButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        mTextView_signingIn.setVisibility(View.VISIBLE);
    }

    public void showForm() {
        mEditText_email.setVisibility(View.VISIBLE);
        mEditText_password.setVisibility(View.VISIBLE);
        mSubmitButton.setVisibility(View.VISIBLE);
        mCancelButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        mTextView_signingIn.setVisibility(View.INVISIBLE);
    }
}
