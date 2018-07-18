package com.lunartech.lovelymalang;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Design and developed by simetri.com
 * <p>
 * ActivityDirection is created to display direction to location from user position.
 * Created using AppCompatActivity.
 */
public class ActivityBrowse extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        View.OnClickListener {

    /**
     * Provides the entry point to Google Play services.
     */
    protected Location mCurrentLocation;

    private MaterialDialog mProgressDialog;

    // Create Google Direction objects
    private GoogleMap mMap;

    private boolean mFlagGranted = true, connecting = false;

    protected LocationRequest mLocationRequest;
    protected Boolean mRequestingLocationUpdates = false;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    public static boolean isDefault = true;

    long defpos;

    Marker marker;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        SupportMapFragment mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));

        mMapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Show progress dialog in the beginning
        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Detecting position...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(true)
                .show();

        buildGoogleApiClient();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == android.R.id.home) {
            onBackPressed();
        }
        else
        if (id == R.id.map_map) {
            mMap.setMapType(1);
        }
        else
        if (id == R.id.map_satellite) {
            mMap.setMapType(2);
        }
        else
        if (id == R.id.map_hybrid) {
            mMap.setMapType(4);
        }
        else
        if (id == R.id.map_terrain) {
            mMap.setMapType(3);
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to handle physical back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Call transition when physical back button pressed
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

    // Method to set up map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mProgressDialog.hide();
        mMap.clear();

        LatLng def = new LatLng(Utils.ARG_DEFAULT_LATITUDE, Utils.ARG_DEFAULT_LONGITUDE);
        MarkerOptions m = new MarkerOptions()
                .position(def)
                .snippet("")
                .title("");

        marker = mMap.addMarker(m);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(def, 10));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                marker.setPosition(latLng);
            }
        });

//        buildGoogleApiClient();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnChoose:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("lat", ""+marker.getPosition().latitude);
                returnIntent.putExtra("lng", ""+marker.getPosition().longitude);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
                break;

        }

    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // When request permission mFlagGranted false
        mFlagGranted = false;
        if (!mayRequestLocations()) {
            return;
        }
        if (!mGoogleApiClient.isConnected()) return;
        if (Build.VERSION.SDK_INT >= 8) {
            // It can prosses after get permission(Marshmallow)
            try {
                mMap.setMyLocationEnabled(true);
                // after get permission mFlagGranted true
                mFlagGranted = true;

                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient,
                        mLocationRequest,
                        this
                ).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        mRequestingLocationUpdates = true;
                    }
                });
                Log.d(Utils.TAG, "startLocationUpdates");
            }
            catch (SecurityException e)
            {}
        }
    }

    // Implement permissions requests on apps that target API level 23 or higher, and are
    // running on a device that's running Android 6.0 (API level 23) or higher.
    // If the device or the app's targetSdkVersion is 22 or lower, the system prompts the user
    // to grant all dangerous permissions when they install or update the app.
    private boolean mayRequestLocations() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        // If already choose deny once
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            askPermissionDialog();
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        }
        return false;

    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // location-related task you need to do.
                startLocationUpdates();
                Log.d(Utils.TAG, "Request Location Allowed");
            } else {
                // permission was not granted
                if (getApplicationContext() == null) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        startLocationUpdates();
                    } else {
                        permissionSettingDialog();
                    }
                }
            }
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

        if (mCurrentLocation == null)
        {
            mCurrentLocation = location;

            LatLng ll = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            Log.d(Utils.TAG, "GOT NEW LOCATION:" + location.getLatitude() + "," + location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));

            if (marker != null)
                marker.setPosition(ll);
        }

        // Condition after get current location it not search again
        stopLocationUpdates();
        //}
    }


    // Method to display share dialog
    private void askPermissionDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .backgroundColorRes(R.color.md_material_blue_600)
                .titleColorRes(R.color.cardview_light_background)
                .contentColorRes(R.color.cardview_light_background)
                .positiveColorRes(R.color.colorAccent)
                .negativeColorRes(R.color.colorPrimary)
                .title("Request Permission")
                .content("Your device location")
                .positiveText("Enable")
                .negativeText("Disable")
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                                REQUEST_ACCESS_FINE_LOCATION);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        // Close dialog when Cancel button clicked
                        finish();
                    }
                }).build();
        // Show dialog
        dialog.show();
    }

    // Method to display setting dialog
    private void permissionSettingDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .backgroundColorRes(R.color.md_material_blue_600)
                .titleColorRes(R.color.cardview_light_background)
                .contentColorRes(R.color.cardview_light_background)
                .positiveColorRes(R.color.colorAccent)
                .negativeColorRes(R.color.colorPrimary)
                .title("Request Permission")
                .content("Your device location")
                .positiveText("Enable")
                .negativeText("Disable")
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        // Close dialog when Cancel button clicked
                        finish();
                    }
                }).build();
        // Show dialog
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(Utils.TAG, "onStart");
        if (checkGooglePlayService()) {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Utils.TAG, "onPause");

        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (checkGooglePlayService()) {
            // Stop location updates to save battery,
            // but don't disconnect the GoogleApiClient object.
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    stopLocationUpdates();
                }
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(Utils.TAG, "onResume");

        if (checkGooglePlayService()) {
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected() && mFlagGranted) {
                    startLocationUpdates();
                }
            }
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(Utils.TAG, "onStop");
        if (checkGooglePlayService()) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(Utils.TAG + TAG, "onDestroy");
        //mDBHelper.close();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(Utils.TAG, "Wait until user position");
    }

    //Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null && checkGooglePlayService()) {
            Log.d(Utils.TAG, "Building GoogleApiClient");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            /**
             * Sets up the location request. Android has two location request settings:
             * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings
             * control the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION,
             * as defined in the AndroidManifest.xml.
             * <p/>
             * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
             * interval (5 seconds), the Fused Location Provider API returns location updates
             * that are accurate to within a few feet.
             * <p/>
             * These settings are appropriate for mapping applications that show real-time location
             * updates.
             */
            mLocationRequest = new LocationRequest();

            // Sets the desired interval for active location updates. This interval is
            // inexact. You may not receive updates at all if no location sources are available, or
            // you may receive them slower than requested. You may also receive updates faster than
            // requested if other applications are requesting location at a faster interval.
            mLocationRequest.setInterval(15000);

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates faster than this value.
            mLocationRequest.setFastestInterval(15000);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            /**
             * Uses a {@link LocationSettingsRequest.Builder} to build
             * a {@link LocationSettingsRequest} that is used for checking
             * if a device has the needed location settings.
             */
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            builder.setAlwaysShow(true);
            mLocationSettingsRequest = builder.build();

            /**
             * Check if the device's location settings are adequate for the app's needs using the
             * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings
             * (GoogleApiClient,LocationSettingsRequest)} method,
             *  with the results provided through a {@code PendingResult}.
             */
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(
                            mGoogleApiClient,
                            mLocationSettingsRequest
                    );
            result.setResultCallback(this);

        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(0, this, 0);
            if (dialog != null) {
                FragmentDialogError errorFragment = new FragmentDialogError();
                errorFragment.setDialog(dialog);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(errorFragment, null);
                ft.commitAllowingStateLoss();
            }
        }

    }

    //**** Start: Setting Location ****//
    // Check google play service if available run map
    private boolean checkGooglePlayService() {
        /**
         * verify that Google Play services is available before making a request.
         *
         * @return true if Google Play services is available, otherwise false
         */
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(Utils.TAG, "Available");
            return true;

            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                FragmentDialogError errorFragment = new FragmentDialogError();
                errorFragment.setDialog(dialog);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(errorFragment, null);
                ft.commitAllowingStateLoss();
            }
            return false;
        }
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {

        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.d(Utils.TAG, "All location settings are satisfied.");
                if (isDefault) {
                    //Log.d(Utils.TAG, "onResult SUCCESS mCurrentLocation == null");
                    try {
                        mCurrentLocation = LocationServices.FusedLocationApi
                                .getLastLocation(mGoogleApiClient);
                        if (mCurrentLocation != null) {
                            isDefault = false;
                            //Log.d(Utils.TAG , "First Location: " + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
                            //new SyncGetLocations().execute();
                            //showFab();

                        }
                    }
                    catch (SecurityException e)
                    {}
                }
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.d(Utils.TAG,
                        "Location settings are not satisfied. Show the user a dialog to" +
                                "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(ActivityBrowse.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.d(Utils.TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.d(Utils.TAG,
                        "Location settings are inadequate, and cannot be fixed here. Dialog " +
                                "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //mLocationResultStatus = resultCode;
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            // If OK selected, then update user location, else if no button selected use default
            // location.
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // If GPS enabled, start location update to get user position
                        Log.d(Utils.TAG,
                                "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d(Utils.TAG,
                                "Deteksi lokasi dibatalkan");
                        // If GPS not enabled, set default user position
                        mCurrentLocation = new Location("");
                        mCurrentLocation.setLatitude(Utils.ARG_DEFAULT_LATITUDE);
                        mCurrentLocation.setLongitude(Utils.ARG_DEFAULT_LONGITUDE);
                        //mCurrentLatitude = Utils.ARG_DEFAULT_LATITUDE;
                        //mCurrentLongitude = Utils.ARG_DEFAULT_LONGITUDE;

                        //getUserPosition(mCurrentLatitude, mCurrentLongitude);
                        //new SyncGetLocations().execute();
                        //showFab();
                        break;
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(Utils.TAG, "Connection suspended");
    }

    /*
     * called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(Utils.TAG, "onConnectionFailed");
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        9000);

                /*
                * thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
                Log.i("onConnectionFailed", "" + e);
            }
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(0, this, 0);
            if (dialog != null) {
                FragmentDialogError errorFragment = new FragmentDialogError();
                errorFragment.setDialog(dialog);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(errorFragment, null);
                ft.commitAllowingStateLoss();
            }
        }
    }

}