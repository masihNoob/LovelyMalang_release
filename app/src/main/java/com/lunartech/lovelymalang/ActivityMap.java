package com.lunartech.lovelymalang;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mrengineer13.snackbar.SnackBar;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;

import org.sufficientlysecure.htmltextview.HtmlAssetsImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Design and developed by simetri.com
 * <p>
 * ActivityDirection is created to display direction to location from user position.
 * Created using AppCompatActivity.
 */
public class ActivityMap extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        GoogleMap.OnInfoWindowClickListener,
        View.OnClickListener {

    ActivityMap me;

    /**
     * Provides the entry point to Google Play services.
     */
    public static Location mCurrentLocation;

    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;
    // Constant used in the location settings dialog
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private boolean mFlagGranted = true, connecting = false;

    private MaterialDialog mProgressDialog;

    protected LocationRequest mLocationRequest;
    protected Boolean mRequestingLocationUpdates = false;
    protected LocationSettingsRequest mLocationSettingsRequest;

    private int[] colors = {
            R.color.col1,R.color.col2,R.color.col3,R.color.col4,R.color.col5,R.color.col6,R.color.col7,R.color.col8,R.color.col9,R.color.col10
    };

    // Create Google Direction objects
    private GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;

    public static boolean isDefault = true;
    int selected = 0;

    List<Device> devices = new ArrayList<>();

    FloatingActionMenu circleMenu;
    String subcat = "";
    Spinner cmbGenre;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        me = this;

        cmbGenre = (Spinner) findViewById(R.id.cmbGenre);

        // Get data that passed from previous activity and store them in the variables
        Intent i = getIntent();
        SupportMapFragment mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));

        mMapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ac = getSupportActionBar();
        if (ac != null)
        {
            ac.setDisplayHomeAsUpEnabled(true);
            ac.setDisplayShowTitleEnabled(true);
        }

        setTitle("Maps");

        ImageButton centerActionButton = (ImageButton) findViewById(R.id.btnCenter);

        LinearLayout btnHotel = setupBtn(R.drawable.ic_hotel, "Hotel");
        LinearLayout btnFood = setupBtn(R.drawable.ic_food, "Food");
        //LinearLayout btnMacito = setupBtn(R.drawable.ic_macito, "Macito");
        LinearLayout btnEntertain = setupBtn(R.drawable.ic_entertain, "Entertainment");
        LinearLayout btnShopping = setupBtn(R.drawable.ic_mall, "Shopping");
        LinearLayout btnSouvenir = setupBtn(R.drawable.ic_gift, "Oleh-oleh");
        //LinearLayout btnMoment = setupBtn(R.drawable.ic_gallerymlg, "Moment");
        LinearLayout btnDestination = setupBtn(R.drawable.ic_destination, "Destination");
        LinearLayout btnTravel = setupBtn(R.drawable.ic_travel, "Travel");

        FloatingActionMenu.Builder build = new FloatingActionMenu.Builder(me)
                .setStartAngle(180) // A whole circle!
                .setEndAngle(360)
                .setRadius((int)getResources().getDimension(R.dimen.range))
                .addSubActionView(btnDestination)
                .addSubActionView(btnShopping)
                .addSubActionView(btnHotel)
                .addSubActionView(btnFood)
                .addSubActionView(btnTravel)
                .addSubActionView(btnSouvenir)
                .addSubActionView(btnEntertain)
                .attachTo(centerActionButton);

        circleMenu = build.build();

        final RelativeLayout customTB = (RelativeLayout) findViewById(R.id.customtoolbar);

        circleMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu floatingActionMenu) {
                customTB.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuClosed(FloatingActionMenu floatingActionMenu) {
                customTB.setVisibility(View.GONE);
            }
        });

        btnHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                doLoadData("hotel,pondok_wisata,guest_house,homestay");
            }
        });
        btnFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                doLoadData("cafe/resto,coffeeshop,rumah_makan,jajanan");
            }
        });
        btnDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                doLoadData("museum,parks,heritage,gallery,kampong_tematik");
            }
        });
        btnShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                doLoadData("mall,plaza,bazaar");
            }
        });
        btnSouvenir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                doLoadData("handycraft,snack,souvenir");
            }
        });
        btnTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                doLoadData("tour_operator,ticket_agent,rent_car/bike");
            }
        });
        btnEntertain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                doLoadData("spa,karaoke,cinema,beautycenter,diskotik,sportcenter,tv,radio");
            }
        });
        // Show progress dialog in the beginning

        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Loading...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(true).build();
                //.show();

        buildGoogleApiClient();
    }

    private LinearLayout setupBtn(int image, String title) {
        LinearLayout lyr = new LinearLayout(me);

        ImageButton btn = new ImageButton(me);
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btn.setImageDrawable(ContextCompat.getDrawable(me, image));
        btn.setBackground(null);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.iconsize2), (int)getResources().getDimension(R.dimen.iconsize2));
        tvParams.setMargins(0, 0, 0, 0);
        btn.setPadding(0, 0, 0, 0);
        btn.setLayoutParams(tvParams);
        btn.setClickable(false);

        LinearLayout.LayoutParams lvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lvParams.setMargins(0, 0, 0, 0);
        lyr.setOrientation(LinearLayout.VERTICAL);
        lyr.setLayoutParams(lvParams);
        lyr.setGravity(Gravity.CENTER);

        TextView text = new TextView(me);
        text.setText(title);
        text.setGravity(Gravity.CENTER);
        text.setLayoutParams(lvParams);
        text.setTextColor(Color.WHITE);
        text.setClickable(false);
        //text.setBackgroundColor(Color.parseColor("#99000000"));

        lyr.addView(btn);
        lyr.addView(text);

        lyr.setClickable(true);
        return lyr;
    }

    public void doLoadData(String cat) {
        if (connecting) return;
        //updateState();

        subcat = "";

        final android.widget.ArrayAdapter<String> madapter = new android.widget.ArrayAdapter<>(this, R.layout.spinneritem);
        //madapter.add("All");

        devices = new ArrayList<>();

        final String[] cats = cat.split("\\,");

        for(int i=0; i<cats.length; i++)
            for(Device d : Utils.destcats)
            {
                if (d.getGuid().equals(cats[i]))
                {
                    if (subcat.isEmpty()) subcat = cats[i];
                    for(Device dev : Utils.acco) {
                        if (dev.getLatitude().equals("0")||dev.getLongitude().equals("0")) continue;
                        if (dev.getCatid().equals(subcat) && dev.isActive())
                        {
                            devices.add(dev);
                            //break;
                        }
                    }

                    madapter.add(d.getNama());
                    break;
                }
            }

        cmbGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                devices = new ArrayList<>();
                subcat = cats[position];

                for(Device dev : Utils.acco) {
                    if (dev.getLatitude().equals("0")||dev.getLongitude().equals("0")) continue;
                    if (dev.getCatid().equals(subcat) && dev.isActive())
                    {
                        devices.add(dev);
                        //break;
                    }
                }

                setupMarker();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cmbGenre.setAdapter(madapter);
    }

    // Method to show snackbar message
    public void showSnackbar(String message) {
        // Inform user that they will use default position using snackbar
        new SnackBar.Builder(ActivityMap.this)
                .withMessage(message)
                .withVisibilityChangeListener(new SnackBar.OnVisibilityChangeListener() {
                    @Override
                    public void onShow(int i) {
                        // While snackbar is visible, change fab buttons position above
                        //ViewPropertyAnimator.animate(mFabStop).cancel();
                        //ViewPropertyAnimator.animate(mFabStop).translationY(-80)
                        //       .setDuration(200).start();
                    }

                    @Override
                    public void onHide(int i) {
                        // While snackbar is not visible, set fab buttons position to default
                        //ViewPropertyAnimator.animate(mFabStop).cancel();
                        //ViewPropertyAnimator.animate(mFabStop).translationY(0)
                        //      .setDuration(200).start();
                    }
                })
                .show();
    }

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }
    // Method to set up location markers
    private void setupMarker() {
        // Clear map before displaying marker
        if (mMap==null) return;
        mMap.clear();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        boolean added = false;

        // Add marker to all locations
        //for(int i = 0; i< mLocationLatitudes.size(); i++){
        int idx = 0;
        for (Device dev : devices) {
            if (dev.getLongitude().equals("0")||dev.getLatitude().equals("0")) continue;
            if (dev.getLongitude().equals("")||dev.getLatitude().equals("")) continue;
            if (dev.lat<-90||dev.lat>90) continue;
            if (dev.lng<-180||dev.lng>180) continue;
            //int marker = getResources().getIdentifier(mLocationMarkers.get(i), "mipmap", getPackageName());

            View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);
            TextView numTxt = (TextView) marker.findViewById(R.id.desttitle);
            numTxt.setText(dev.getNama());
            numTxt.setBackgroundColor(colors[idx % 10]);
            idx++;

            MarkerOptions m = new MarkerOptions()
                    .position(new LatLng(Double.valueOf(dev.getLatitude()),
                            Double.valueOf(dev.getLongitude())))
                    //.icon(bmp) //BitmapDescriptorFactory.fromResource(marker)
                    //.icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    .snippet(dev.getAddress())
                    .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)))
                    .title(dev.getNama());

            Marker locationMarker = mMap.addMarker(m);

            //mLocationIdsOnMarkers.put(locationMarker.getId(), dev.getDeviceid());

            builder.include(locationMarker.getPosition());
            added = true;
        }

        if (added)
        {
            //LatLngBounds allBounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
// Calls moveCamera passing screen size as parameters
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 120));

            //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(allBounds, 80);
            //mMap.animateCamera(cu);
        }
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
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        //mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Utils.ARG_DEFAULT_LATITUDE, Utils.ARG_DEFAULT_LONGITUDE), Utils.ARG_DEFAULT_MAP_ZOOM_LEVEL));

        mMap.setOnInfoWindowClickListener(this);
        //if (ActivityHome.isDefault) return;
        mMap.clear();
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        doLoadData("hotel,pondok_wisata,guest_house,homestay");
        // Clear map first before redirecting location
        //mStart = new LatLng(ActivityHome.mCurrentLocation.getLatitude(), ActivityHome.mCurrentLocation.getLongitude());

        //getDirection(mStart);

        //buildGoogleApiClient();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnHotel:
                selected = 0;
                doLoadData("hotel,pondok_wisata,guest_house,homestay"); break;
        }
    }

    // Method to handle click on info window
    @Override
    public void onInfoWindowClick(Marker marker) {
        if (mCurrentLocation==null)
        {
            Utils.showMessage(this, "This device location not available yet");
            return;
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
            {
                Log.d(Utils.TAG, "Not permitted");
            }
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

    //**** End: Setting Location ****//


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.d(Utils.TAG, "GOT NEW LOCATION:" + location.getLatitude() + "," + location.getLongitude());
        stopLocationUpdates();
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
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
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

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mContents;

        CustomInfoWindowAdapter() {
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {

            String title = marker.getTitle();
            Device dev = null;

            if (devices != null) {
                for (int i = 0; i < devices.size(); i++) {
                    Device d = devices.get(i);
                    if (d.getNama().equals(title)) {
                        dev = d;
                        break;
                    }
                }
            }

            if (dev == null) {
                return;
            }

            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            HtmlTextView snippetUi = ((HtmlTextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                // snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, snippet.length(), 0);
                snippetUi.setHtml(snippet, new HtmlAssetsImageGetter(snippetUi));
            } else {
                snippetUi.setText("");
            }
        }
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
                    {
                        Log.d(Utils.TAG, "Location settings not satisfied.");
                    }
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
                    status.startResolutionForResult(ActivityMap.this, REQUEST_CHECK_SETTINGS);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        else
        if (id == R.id.tourism_map) {
            Intent memberIntent = new Intent(this, ActivityShowContent.class);
            memberIntent.putExtra("url", "tourismmap");
            startActivity(memberIntent);
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


}