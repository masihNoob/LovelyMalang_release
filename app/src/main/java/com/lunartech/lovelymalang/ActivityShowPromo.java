package com.lunartech.lovelymalang;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.toolbox.ImageLoader;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ActivityShowPromo extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    static ActivityShowPromo me;

    String idpromo = "", kodepromo = "", idvenue = "";
    Device venue;

    PhotoViewAttacher mAttacher;

    private MaterialDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_promo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        me = this;

        ActionBar ac = getSupportActionBar();

        if (ac != null)
        {
            ac.setDisplayHomeAsUpEnabled(true);
            ac.setDisplayShowTitleEnabled(true);
        }

        String locid = getIntent().getStringExtra("newsid");
        if (locid == null || locid.isEmpty() || locid.equals("null")) locid = "0";
        long newsid = Integer.valueOf(locid);
        String src = getIntent().getStringExtra("src");

        if (src == null) src = "";
        Device d = null;

        List<Device> srclst = Utils.promo;
        for (Device dev : srclst) {
            if (dev.getId() == newsid) {
                d = dev;
                break;
            }
        }
        if (d == null) {
            finish();
            return;
        }

        ArrayList<String> savedlst = Utils.getPromo(me);

        boolean found = false;
        for(String code : savedlst) {
            if (d.getCode().equals(code)) {
                found = true;
                break;
            }
        }

        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Processing...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false).build();

        final Device device = d;
        idpromo = ""+d.getId();
        kodepromo = d.getCode();
        idvenue = d.getLocation();

        setTitle(device.getNama());

        final AppCompatButton btn = (AppCompatButton) findViewById(R.id.btnsave);
        final AppCompatButton btnUse = (AppCompatButton) findViewById(R.id.btnuse);

        long venueid = Integer.valueOf(idvenue);

        for (Device dev : Utils.acco) {
            if (dev.getId()==venueid) {
                venue = dev;
                break;
            }
        }

        if (found)
            btn.setVisibility(View.GONE);
        else
           btn.setVisibility(View.VISIBLE);

        btnUse.setVisibility(View.VISIBLE);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showConfirm(me, "Save this Promo?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String api_key = Utils.getConfig(me, "api_key4");
                        if (api_key.isEmpty()) {
                            Intent memberIntent = new Intent(me, ActivityRegister.class);
                            memberIntent.putExtra("sendresult", 1);
                            startActivityForResult(memberIntent, 527);
                        } else {
                            Utils.addPromo(me, kodepromo);
                            Intent memberIntent = new Intent(me, ActivityMember.class);
                            startActivity(memberIntent);
                            finish();
                        }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
            }
        });

        btnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showConfirm(me, "Use this Promo?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String api_key = Utils.getConfig(me, "api_key4");
                        if (api_key.isEmpty()) {
                            Intent memberIntent = new Intent(me, ActivityRegister.class);
                            memberIntent.putExtra("sendresult", 1);
                            startActivityForResult(memberIntent, 529);
                        } else {
                            useVoucher();
                        }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
            }
        });

        TextView txtcat = (TextView) findViewById(R.id.txttitle);
        txtcat.setText(device.getNama());

        HtmlTextView webView = (HtmlTextView) findViewById(R.id.txtdescription);
        webView.setHtml(device.getSnippet(),
                new HtmlHttpImageGetter(webView, null, true));

        ImageLoader mImageLoader = MySingleton.getInstance(this).getImageLoader();

        ImageView img = (ImageView) findViewById(R.id.imgvenue);
        if (device.getImage().isEmpty())
            img.setVisibility(View.GONE);
        else {
            img.setVisibility(View.VISIBLE);
            ImageLoader.ImageListener listener = ImageLoader.getImageListener(img,
                    R.mipmap.empty_photo, R.mipmap.empty_photo);
            mImageLoader.get(device.getImage(),
                    listener);
            mAttacher = new PhotoViewAttacher(img);
        }

        TextView diskon = (TextView) findViewById(R.id.txtdiskon);
        diskon.setText(device.getNominal());

        TextView promodate = (TextView) findViewById(R.id.txtpromodate);
        promodate.setText(Utils.showEventDate(device.getStart(), device.getEnd()));

        if (venue != null)
        {
            TextView txt = (TextView) findViewById(R.id.lblvenue);
            txt.setVisibility(View.VISIBLE);
            txt = (TextView) findViewById(R.id.txtvenue);
            txt.setVisibility(View.VISIBLE);
            txt.setText(venue.getNama());

            AppCompatButton map = (AppCompatButton) findViewById(R.id.btnvenue);
            map.setVisibility(View.VISIBLE);
            map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent loginIntent = new Intent(ActivityShowPromo.this, MainActivity.class);
                    loginIntent.putExtra("newsid", ""+venue.getId());
                    loginIntent.putExtra("catname", "Venue");
                    loginIntent.putExtra("src", "acco");
                    startActivity(loginIntent);
                    finish();
                }
            });

        }

        TextView name = (TextView) findViewById(R.id.txtnama);
        if (device.getPic().isEmpty()||device.getPic().equals("null")) {
            name.setVisibility(View.GONE);
            findViewById(R.id.lblnama).setVisibility(View.GONE);
        } else
            name.setText(device.getPic());
        TextView email = (TextView) findViewById(R.id.txtemail);
        if (device.getEmail().isEmpty()||device.getEmail().equals("null")) {
            email.setVisibility(View.GONE);
            findViewById(R.id.lblemail).setVisibility(View.GONE);
        } else
            email.setText(device.getEmail());
        TextView fax = (TextView) findViewById(R.id.txtfax);
        if (device.getFax().isEmpty()||device.getFax().equals("null")) {
            fax.setVisibility(View.GONE);
            findViewById(R.id.lblfax).setVisibility(View.GONE);
        } else
            fax.setText(device.getFax());

        TextView telp = (TextView) findViewById(R.id.txtcall);

        if (device.getPhone().isEmpty()||device.getPhone().equals("null")) {
            telp.setVisibility(View.GONE);
            findViewById(R.id.lblcall).setVisibility(View.GONE);
        }
        else
        {
            telp.setText(device.getPhone());
            findViewById(R.id.lblcall).setVisibility(View.VISIBLE);
        }

        telp.setPaintFlags(telp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        telp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 8) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + device.getPhone()));
                    try {
                        startActivity(callIntent);
                    } catch (SecurityException e) {
                        //Log.d(Utils.TAG + TAG, "" + e.toString());
                    }
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabShare);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, device.getNama());
                shareIntent.putExtra(Intent.EXTRA_TEXT, device.getLink());
                startActivity(Intent.createChooser(shareIntent, "Share to..."));
            }
        });

    }

    private AdapterView.OnItemClickListener ssListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                Device gal = (Device) l.getItemAtPosition(position);
                if (gal == null)
                    return;
                Intent memberIntent = new Intent(me, ActivityShowContent.class);
                memberIntent.putExtra("url", gal.getBackground());
                startActivity(memberIntent);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == 527) {
            Utils.addPromo(me, kodepromo);
            Intent memberIntent = new Intent(me, ActivityMember.class);
            startActivity(memberIntent);
            finish();
        }
        else
        if (requestCode == 529) {
            useVoucher();
        }
    }

    public void useVoucher()
    {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Use Voucher")
                .customView(R.layout.input_pin, true)
                .positiveText("Gunakan")
                .negativeText("Batal")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View v = dialog.getCustomView();
                        if (v == null) return;
                        AppCompatEditText password = (AppCompatEditText) v.findViewById(R.id.txtpassword);
                        dialog.dismiss();

                        String kodestr = password.getText().toString().trim();

                        if (kodestr.isEmpty())
                        {
                            Utils.showMessage(me, "Invalid password");
                            return;
                        }

                        doUseVoucher(kodestr);
                    }
                })
                .build();

        dialog.show();
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

    public void doUseVoucher(String password)
    {
        String url = Utils.HTTP_RPCURL + "/usevoucher";
        String user_id = Utils.getConfig(me, "user_id4");
        Log.d(Utils.TAG, "Loading: "+url);
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("?password=").append(password).append("&idvenue=").append(idvenue);
        sb.append("&iduser=").append(user_id).append("&idpromo=").append(idpromo);
        Log.d(Utils.TAG, "Posting: "+sb.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        mProgressDialog.show();
        client.get(sb.toString(), requestResponseHandler);
    }

    private AsyncHttpResponseHandler requestResponseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            mProgressDialog.dismiss();
            Utils.showMessage(me, "Error connecting");
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            mProgressDialog.dismiss();
            if (arg2 == null) {
                Utils.showMessage(me, "Error connecting");
            } else {
                String json = new String(arg2);
                updateFetchResult(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            mProgressDialog.setContent("Loaded "+prg+"kB");
        }
    };

    public void updateFetchResult(String json) {
        if (json == null || json.isEmpty()) {
            Utils.showMessage(me, "Connection failed");
            return;
        }
        Log.d("http", "json: " + json);
        try {
            JSONObject retval = new JSONObject(json);
            int success = retval.getInt("success");
            String msg = retval.getString("msg");
            if (success>0) {
                Utils.showMessage(me, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            } else {
                Utils.showMessage(me, msg);
            }
        } catch (JSONException e) {
            Utils.showMessage(me, "Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
        }
    }
}
