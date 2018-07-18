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
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity {

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

    static MainActivity me;

    String idpromo = "", kodepromo = "", idvenue = "";

    PhotoViewAttacher mAttacher;

    private MaterialDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
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

        List<Device> srclst;

        switch (src) {
            case "gallery":
                srclst = Utils.gallery;
                break;
            case "acco":
                srclst = Utils.acco;
                break;
            case "promo":
                srclst = Utils.promo;
                break;
            default:
                srclst = Utils.acco;
                break;
        }

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

        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Processing...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false).build();

        final Device device = d;

        setTitle(device.getNama());

        final AppCompatButton btn = (AppCompatButton) findViewById(R.id.btnsave);
        final AppCompatButton btnUse = (AppCompatButton) findViewById(R.id.btnuse);

        for (Device dev : Utils.promo) {
            if (dev.getLocation().equals("" + newsid)) {
                idpromo = ""+dev.getId();
                kodepromo = dev.getCode();
                idvenue = ""+newsid;
                break;
            }
        }

        if (idpromo.isEmpty())
            btn.setVisibility(View.GONE);
        else
           btn.setVisibility(View.VISIBLE);

        //btnUse.setVisibility(btn.getVisibility());

        /*
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
        */
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(MainActivity.this, ActivityShowPromo.class);
                //long idx = Integer.valueOf(devices.get(position).getLocation());
                loginIntent.putExtra("newsid", idpromo);
                loginIntent.putExtra("catname", "Promo");
                loginIntent.putExtra("src", "acco");
                startActivity(loginIntent);
                finish();
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

        TextView txtcat = (TextView) findViewById(R.id.txtcatname);
        String catname = getIntent().getStringExtra("catname");
        txtcat.setText(catname);

        TextView address = (TextView) findViewById(R.id.txtaddress);
        if (device.getAddress().isEmpty()) {
            address.setVisibility(View.GONE);
            findViewById(R.id.lbladdress).setVisibility(View.GONE);
        } else
            address.setText(device.getAddress());

        HtmlTextView webView = (HtmlTextView) findViewById(R.id.txtdescription);
        webView.setHtml(device.getSnippet(),
                new HtmlHttpImageGetter(webView, null, true));

        ImageLoader mImageLoader = MySingleton.getInstance(this).getImageLoader();

        ImageView img = (ImageView) findViewById(R.id.imgvenue);
        if (device.getImage().isEmpty())
            img.setVisibility(View.GONE);
        else {
            img.setVisibility(View.VISIBLE);
            ImageLoader.ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(img,
                    R.mipmap.empty_photo, R.mipmap.empty_photo);
            mImageLoader.get(device.getImage(),
                    listener);
            mAttacher = new PhotoViewAttacher(img);
        }

        TextView facs = (TextView) findViewById(R.id.txtfacs);
        StringBuilder fcs = new StringBuilder();
        for (int i = 0; i < device.getFacs().length; i++) {
            if (i > 0) fcs.append(", ");
            fcs.append(device.getFacs()[i]);
        }
        if (fcs.toString().isEmpty()) {
            findViewById(R.id.lblfacs).setVisibility(View.GONE);
            facs.setVisibility(View.GONE);
        } else
            facs.setText(fcs.toString());

        TextView name = (TextView) findViewById(R.id.txtnama);
        if (device.getPic().isEmpty()) {
            name.setVisibility(View.GONE);
            findViewById(R.id.lblnama).setVisibility(View.GONE);
        } else
            name.setText(device.getPic());
        TextView email = (TextView) findViewById(R.id.txtemail);
        if (device.getEmail().isEmpty()) {
            email.setVisibility(View.GONE);
            findViewById(R.id.lblemail).setVisibility(View.GONE);
        } else
            email.setText(device.getEmail());
        TextView fax = (TextView) findViewById(R.id.txtfax);
        if (device.getFax().isEmpty()) {
            fax.setVisibility(View.GONE);
            findViewById(R.id.lblfax).setVisibility(View.GONE);
        } else
            fax.setText(device.getFax());

        TextView web = (TextView) findViewById(R.id.txtweb);
        if (device.getWebsite().isEmpty()) {
            web.setVisibility(View.GONE);
            findViewById(R.id.lblweb).setVisibility(View.GONE);
        } else
            web.setText(device.getWebsite());

        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = device.getWebsite();
                if (!url.startsWith("http"))
                {
                    url = "http://" + url;
                }
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        TextView telp = (TextView) findViewById(R.id.txtcall);

        if (device.getPhone().isEmpty()) {
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

        AppCompatButton map = (AppCompatButton) findViewById(R.id.btnpeta);
        if (device.getLatitude().isEmpty() || device.getLatitude().equals("0")) {
            map.setVisibility(View.GONE);
        }

        final HorizontalListView ssgrid = (HorizontalListView) findViewById(R.id.sslistview);

        String[][] gal = device.getGallery();
        if (gal.length>0)
        {
            final ArrayList<Device> alllist = new ArrayList<>();
            for (String[] aGal : gal) {
                Device g = new Device();
                g.setLink(aGal[1]);
                g.setBackground(aGal[0]);
                alllist.add(g);
            }
            if (alllist.size() > 0) {
                SSAdapter sslist = new SSAdapter(me, alllist);
                ssgrid.setAdapter(sslist);
                ssgrid.setOnItemClickListener(ssListener());
            }

        }
        else
            ssgrid.setVisibility(View.GONE);
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
