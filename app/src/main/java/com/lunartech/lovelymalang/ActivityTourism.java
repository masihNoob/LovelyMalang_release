package com.lunartech.lovelymalang;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.zxing.Result;

public class ActivityTourism extends AppCompatActivity implements View.OnClickListener {

    static ActivityTourism me;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tourism);

        me = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1)
            verifyStoragePermissions(me);

    }


    public void doLoadData(String cat, String catname)
    {
        Intent loginIntent = new Intent(ActivityTourism.this, ActivityList.class);
        loginIntent.putExtra("cat", cat);
        loginIntent.putExtra("src", "venue");
        loginIntent.putExtra("catname", catname);
        startActivity(loginIntent);
    }

    public void doLoadGallery(String cat, String catname)
    {
        Intent loginIntent = new Intent(ActivityTourism.this, ActivityList.class);
        loginIntent.putExtra("cat", cat);
        loginIntent.putExtra("src", "gallery");
        loginIntent.putExtra("catname", catname);
        startActivity(loginIntent);
    }

    public void doLoadNews(String cat, String catname)
    {
        Intent loginIntent = new Intent(ActivityTourism.this, ActivityNews.class);
        loginIntent.putExtra("cat", cat);
        loginIntent.putExtra("catname", catname);
        startActivity(loginIntent);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnSejarah:
                doLoadData("sejarah", "Wisata Sejarah"); break;
            case R.id.btnGallery:
                doLoadGallery("20", "Gallery"); break;
            case R.id.btnKolosal:
                doLoadData("kolosal", "Kolosal"); break;
            case R.id.btnMall:
                doLoadData("mall", "Wisata Mall"); break;
            case R.id.btnMacyto:
                doLoadNews("7", "Malang City Tour"); break;
            case R.id.btnTrakking:
                doLoadData("trakking", "Trakking"); break;
            //case R.id.btnTrip: doLoadNews("17", "Trip"); break;
            case R.id.btnGoverment:
                doLoadData("goverment", "Tourism TAGs"); break;
            /*
            case R.id.btnQRCode:
                mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view<br />
                setContentView(mScannerView);
                ArrayList<BarcodeFormat> format = new ArrayList<>();
                format.add(BarcodeFormat.QR_CODE);
                mScannerView.setFormats(format);
                mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.<br />
                mScannerView.startCamera();
                break;
                */
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Call transition when physical back button pressed
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
