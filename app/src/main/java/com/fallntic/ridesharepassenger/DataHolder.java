package com.fallntic.ridesharepassenger;

import android.app.ProgressDialog;
import android.content.Context;

public class DataHolder {
    public static User mUser;
    public static com.google.android.gms.maps.model.LatLng mDropOff_latlng;
    public static com.google.android.gms.maps.model.LatLng mPickUp_latlng;
    public static com.google.android.gms.maps.model.LatLng mCurrent_latLng;
    public static String mPickUpAddress = "";
    public static String mDropOffAddress = "";
    public static String mCurrentAddress = "";
    public static String distanceToPickUp;
    public static String distanceToDropOff;
    public static String navSelected = null;
    public static boolean addressSelected = false;
    public static boolean isClientPickedUp = false;
    public static boolean firstLoad = true;

    public static ProgressDialog progressDialog;

    public static void showProgressDialog(Context context) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(context);

        if (!progressDialog.isShowing()) {
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}