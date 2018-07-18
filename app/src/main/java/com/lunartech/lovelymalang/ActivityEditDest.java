package com.lunartech.lovelymalang;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.toolbox.ImageLoader;
import com.github.mrengineer13.snackbar.SnackBar;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.glxn.qrgen.android.QRCode;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.lunartech.lovelymalang.TakePhotoUtils.GET_GALLERY;


public class ActivityEditDest extends BaseActivity  {

    private static ImageLoader mImageLoader;

    ActivityEditDest me;

    PhotoViewAttacher mAttacher;
    Device dev = null;

    private EditText lat, lng, nama, address, picname, picphone, email, website, fax, persen, amount, start, end, minacco, maxacco;
    RichEditor description;

    Spinner spn, spnacco;

    private ImageView img, imgbanner;
    String nf = "", bgpos="background1";

    MultiSpinner mspn;
    private ArrayAdapter<String> madapter;
    ArrayList<Device> facs;

    private MaterialDialog mProgressDialog;

    String src;

    int initiator = 0;

    List<Device> srclst;
    List<Device> srccat = null;

    HorizontalListView ssgrid;

    MenuItem qrmenu;

    private final TakePhotoUtils tpu = TakePhotoUtils.getInstance();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private DatePickerDialog.OnDateSetListener startListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            start.setText(year + "-" +  String.format("%02d", monthOfYear+1) + "-" + String.format("%02d", dayOfMonth));
        }
    };

    private DatePickerDialog.OnDateSetListener endListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            end.setText(year + "-" +  String.format("%02d", monthOfYear+1) + "-" + String.format("%02d", dayOfMonth));
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        src = getIntent().getStringExtra("src");

        if (src.equals("promo"))
            setContentView(R.layout.fragment_promote);
        else
        if (src.equals("acco"))
            setContentView(R.layout.fragment_editacco);
        else
            setContentView(R.layout.fragment_editprofile);

        me = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        long newsid = getIntent().getLongExtra("newsid", 0);

        if (src.equals("promo"))
        {
            srclst = Utils.promo;
        }
        else
        if (src.equals("acco"))
        {
            srclst = Utils.acco;
            srccat = Utils.destcats;
        }
        else
        {
            srclst = Utils.acco;
            srccat = Utils.destcats;
        }

        for(Device d : srclst) {
            if (d.getId()==newsid) {
                dev = d;
                break;
            }
        }

        String catname = getIntent().getStringExtra("catname");
        setTitle((dev == null?"New ":"Edit ")+catname);

        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        if (src.equals("promo"))
        {
            nama = (EditText) findViewById(R.id.txttitle);
            description = (RichEditor) findViewById(R.id.txtdescription);
            picname = (EditText) findViewById(R.id.txtpicname);
            picphone = (EditText) findViewById(R.id.txtpicphone);
            email = (EditText) findViewById(R.id.txtpicemail);
            fax = (EditText) findViewById(R.id.txtpicfax);

            amount = (EditText) findViewById(R.id.txtamount);
            persen = (EditText) findViewById(R.id.txtpersen);

            start = (EditText) findViewById(R.id.txtstart);
            end = (EditText) findViewById(R.id.txtend);

            img = (ImageView) findViewById(R.id.imgvenue);
            imgbanner = (ImageView) findViewById(R.id.imgbanner);

            start.setKeyListener(null);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerFragment newFragment = new DatePickerFragment();
                    newFragment.listener = startListener;
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);

                    Bundle extra = new Bundle();
                    extra.putInt("year", year);
                    extra.putInt("month", month);
                    extra.putInt("day", day);
                    newFragment.setArguments(extra);
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                }
            });
            end.setKeyListener(null);
            end.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerFragment newFragment = new DatePickerFragment();
                    newFragment.listener = endListener;
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);

                    Bundle extra = new Bundle();
                    extra.putInt("year", year);
                    extra.putInt("month", month);
                    extra.putInt("day", day);
                    newFragment.setArguments(extra);
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                }
            });

            TextView txtacco = (TextView) findViewById(R.id.txtacco);
            txtacco.setVisibility(View.VISIBLE);
            spnacco = (Spinner) findViewById(R.id.spnacco);
            spnacco.setVisibility(View.VISIBLE);
            List<String> list = new ArrayList<>();
            for (Device dev : Utils.acco) {
                list.add(dev.getNama());
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(me,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnacco.setAdapter(dataAdapter);

            AppCompatButton btn = (AppCompatButton) findViewById(R.id.btnbg);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(me, ActivityPromoBackground.class);
                    startActivityForResult(intent, 530);
                }
            });

            if (dev != null) {
                nama.setText(dev.getNama());
                //description.setText(dev.getSnippet());
                description.setHtml(dev.getSnippet());
                picname.setText(dev.getPic());
                picphone.setText(dev.getPhone());
                email.setText(dev.getEmail());
                fax.setText(dev.getFax());
                amount.setText(dev.getNominal());
                persen.setText(dev.getPersen());
                start.setText(dev.getStart());
                end.setText(dev.getEnd());

                int idx = 0;
                for (Device d : Utils.acco) {
                    if ((""+d.getId()).equals(dev.getLocation())) {
                        spnacco.setSelection(idx);
                        break;
                    }
                    idx++;
                }

                me.mImageLoader.get(dev.getImage3(),
                        com.android.volley.toolbox.ImageLoader.getImageListener(img,
                                R.mipmap.empty_photo, R.mipmap.empty_photo));
                me.mImageLoader.get(dev.getBackground(),
                        com.android.volley.toolbox.ImageLoader.getImageListener(imgbanner,
                                R.mipmap.empty_photo, R.mipmap.empty_photo));
            }

            final AppCompatButton save = (AppCompatButton) findViewById(R.id.btncek);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        doSavePromo();
                }
            });


            if (qrmenu != null)
                qrmenu.setVisible(false);
        }
        else {

            if (qrmenu != null)
                qrmenu.setVisible(dev != null);

            List<String> list = new ArrayList<>();
            for (Device dev : srccat) {
                list.add(dev.getNama());
            }

            nama = (EditText) findViewById(R.id.txtnama);
            address = (EditText) findViewById(R.id.txtaddress);
            description = (RichEditor) findViewById(R.id.txtdescription);
            picname = (EditText) findViewById(R.id.txtpicname);
            picphone = (EditText) findViewById(R.id.txtpicphone);
            email = (EditText) findViewById(R.id.txtpicemail);
            website = (EditText) findViewById(R.id.txtpicweb);
            fax = (EditText) findViewById(R.id.txtpicfax);

            lat = (EditText) findViewById(R.id.txtlatitude);
            lng = (EditText) findViewById(R.id.txtlongitude);

            minacco = (EditText) findViewById(R.id.txtminacco);
            maxacco = (EditText) findViewById(R.id.txtmaxacco);

            img = (ImageView) findViewById(R.id.imgvenue);

            spn = (Spinner) findViewById(R.id.spncat);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(me,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spn.setAdapter(dataAdapter);
            spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Device cat = srccat.get(i);
                    setupFacs("" + cat.getId(), dev == null ? "" : dev.getFacs().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            mspn = (MultiSpinner) findViewById(R.id.spnfac);

            if (dev != null) {
                int idx = 0;
                for (Device d : srccat) {
                    if (d.getGuid().equals(dev.getCatname())) {
                        spn.setSelection(idx);
                        break;
                    }
                    idx++;
                }
                nama.setText(dev.getNama());
                address.setText(dev.getAddress());
                //description.setText(dev.getSnippet());
                description.setHtml(dev.getSnippet());

                picname.setText(dev.getPic());
                picphone.setText(dev.getPhone());
                email.setText(dev.getEmail());
                website.setText(dev.getWebsite());

                fax.setText(dev.getFax());
                lat.setText(dev.getLatitude());
                lng.setText(dev.getLongitude());

                minacco.setText(dev.getMinacco());
                maxacco.setText(dev.getMaxacco());

                setupFacs(dev.getCatid(), dev.getFacs().toString());

                if (!dev.getImage3().isEmpty())
                    mImageLoader.get(dev.getImage3(),
                            com.android.volley.toolbox.ImageLoader.getImageListener(img,
                                    R.mipmap.empty_photo, R.mipmap.empty_photo));

/*
                LinearLayout lyr = (LinearLayout) findViewById(R.id.lyrQrcode);
                lyr.setVisibility(View.VISIBLE);

                lyr.setVisibility(View.GONE);
                ImageView myImage = (ImageView) findViewById(R.id.qrcode);
                String link = "";//dev.getLink();
                if (link.isEmpty())
                {
                    lyr.setVisibility(View.GONE);
                }
                else
                {
                    final Bitmap myBitmap = QRCode.from(link).bitmap();
                    myImage.setImageBitmap(myBitmap);
                    myImage.setVisibility(View.VISIBLE);

                    AppCompatButton map = (AppCompatButton) findViewById(R.id.btnQRCode);
                    map.setText("Share QRCode");
                    map.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("image/jpeg");
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
                            try {
                                f.createNewFile();
                                FileOutputStream fo = new FileOutputStream(f);
                                fo.write(bytes.toByteArray());
                                fo.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Utils.showMessage(me, "Cannot write to file: " + e.getMessage());
                                return;
                            }
                            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + f.getAbsolutePath()));
                            startActivity(Intent.createChooser(share, "Send QRCode"));
                        }
                    });
                }
*/

                ssgrid = (HorizontalListView) findViewById(R.id.sslistview);
                if (ssgrid!=null)
                {
                    String[][] gal = dev.getGallery();
                    if (gal.length>0)
                    {
                        final ArrayList<Device> alllist = new ArrayList<>();
                        for(int i=0; i< gal.length; i++)
                        {
                            Device g = new Device();
                            g.setLink(gal[i][1]);
                            g.setBackground(gal[i][0]);
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
            } else
                setupFacs("", "");

            final AppCompatButton save = (AppCompatButton) findViewById(R.id.btnsave);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (src.equals("acco"))
                        doSaveAccomodation();
                    else
                    if (src.equals("promo"))
                        doSavePromo();
                    else
                        doSaveDestination();
                }
            });

            final AppCompatButton addgal = (AppCompatButton) findViewById(R.id.btnaddgallery);
            if (addgal != null)
            {
                addgal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initiator = 1;
                        tpu.launchGallery(me);
                    }
                });
                if (dev == null) addgal.setVisibility(View.GONE);
            }
        }

        AppCompatButton btnpic = (AppCompatButton) findViewById(R.id.btnpic);
        if (btnpic!=null)
        btnpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiator = 0;
                tpu.launchGallery(me);
            }
        });

        AppCompatButton map = (AppCompatButton) findViewById(R.id.btnpeta);
        if (map!=null)
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(me, ActivityBrowse.class);
                startActivityForResult(intent, 526);
            }
        });

        if (dev != null)
        {
            mImageLoader.get(dev.getImage(),
                    ImageLoader.getImageListener(img,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));
        }
        mAttacher = new PhotoViewAttacher(img);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            verifyStoragePermissions(me);

        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Loading...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false).build();
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

    void setupFacs(String catid, String selected)
    {
        madapter = new ArrayAdapter<>(me, android.R.layout.simple_spinner_item);
        facs = new ArrayList<>();
        boolean[] sel = new boolean[Utils.fac.size()];
        int idx = 0;
        for(Device d : Utils.fac)
        {
            if (d.getCatid().equals(catid))
            {
                facs.add(d);
                sel[idx] = selected.contains(d.getNama());
                madapter.add(d.getNama());
            }
        }
        mspn.setAdapter(madapter, false, new MultiSpinner.MultiSpinnerListener() {
            @Override
            public void onItemsSelected(boolean[] selected) {

            }
        });
        mspn.setSelected(sel);
    }

    public void onTakeResultFromGallery(Uri data) {
        startScaleTask(data);
    }

    private void startScaleTask(Uri data) {
        new ScaleTask().execute(data);
    }

    class ScaleTask extends AsyncTask<Uri, Void, Bitmap> {
        Bitmap bitmap;

        @Override
        protected void onPreExecute() {

            //showProgressDialog();
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            try {
                InputStream stream = me.getContentResolver().openInputStream(params[0]);
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                bitmap = tpu.savePhotoAndGetThumbnail(me, bitmap);
            } catch (Exception e) {
                //L.e(e);
            } catch (OutOfMemoryError e) {
                System.gc();
                //L.e(e);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            //dismissProgressDialog();
            setImage(bitmap);
        }
    }

    private void setImage(Bitmap imageBitmap) {
        if (imageBitmap != null) {// file exists
            if (initiator == 0)
            {
                img.setImageBitmap(imageBitmap);
                nf = tpu.getFile().getAbsolutePath();
                mAttacher.update();
            }
            else
            {
                String nf = tpu.getFile().getAbsolutePath();
                Intent memberIntent = new Intent(me, ActivitySubmit.class);
                memberIntent.putExtra("nf", nf);
                memberIntent.putExtra("destid", dev.getId());
                memberIntent.putExtra("src", src);
                startActivityForResult(memberIntent, 528);
            }
            //ivAvatar.setImageDrawable(HelperImage.getRoundImage(imageBitmap));
            //ivAddPhoto.setVisibility(View.GONE);
        } else {
            //AppUtil.showLongToast("Can't get image from Gallery", Style.ALERT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == 526) {
            lat.setText(data.getStringExtra("lat"));
            lng.setText(data.getStringExtra("lng"));
        }
        else
        if (requestCode == 528) {
            if (src.equals("acco"))
                Utils.reLoadDataAcco(me);
            else
                Utils.reLoadDataAcco(me);
            Utils.showMessage(me, "Uploaded successfully", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
            else if (requestCode==530) {
                int pos = data.getIntExtra("pos", 0);
                String url = Utils.promobg.get(pos).getBackground();
                pos++;
                bgpos = "background" + pos;
                me.mImageLoader.get(url,
                        com.android.volley.toolbox.ImageLoader.getImageListener(imgbanner,
                                R.mipmap.empty_photo, R.mipmap.empty_photo));

        }
        else if (requestCode == GET_GALLERY) {
            onTakeResultFromGallery(data.getData());
        }
    }

    public void doSaveDestination() {
        String url = Utils.HTTP_RPCURL + "/destination";
        Log.d(Utils.TAG, "Loading: " + url);
        JSONObject params = new JSONObject();
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("MMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            params.put("destination_code", dev==null?"DST-" + sdf1.format(new Date()):dev.getCode());
            params.put("destination_address", address.getText().toString().trim());
            params.put("post_date", sdf2.format(new Date()));
            params.put("post_title", nama.getText().toString().trim());
            params.put("post_content", description.getHtml().toString().trim());
            params.put("destination_category", Utils.destcats.get(spn.getSelectedItemPosition()).getId());
            params.put("destination_latitude", lat.getText().toString().trim());
            params.put("destination_longitude", lng.getText().toString().trim());
            params.put("destination_person_name", picname.getText().toString().trim());
            params.put("destination_person_phone", picphone.getText().toString().trim());
            params.put("destination_person_fax", fax.getText().toString().trim());
            params.put("destination_person_email", email.getText().toString().trim());
            params.put("destination_person_website", website.getText().toString().trim());
            params.put("destination_min_accomodation", minacco.getText().toString().trim());
            params.put("destination_max_accomodation", maxacco.getText().toString().trim());
            params.put("destination_id", dev==null?"":dev.getId());
            params.put("destination_gallery", new JSONArray());
            JSONArray f = new JSONArray();
            boolean[] checked = mspn.getSelected();
            int idx = 0;
            for(Device c : facs)
            {
                if (checked[idx])
                    f.put(c.getId());
                idx++;
            }
            params.put("facilities", f);
        } catch (JSONException e) {
            showSnackbar("Error connecting: " + e.getMessage());
            return;
        }
        String json = params.toString();
        //Entity
        String key = Utils.getConfig(me, "api_key2");
        final HashCode hashCode = Hashing.sha1().hashString(key + json, Charset.defaultCharset());
        me.mProgressDialog.show();
        String secret = hashCode.toString();
        AsyncHttpClient client = new AsyncHttpClient();

        client.addHeader("API-KEY", key);
        client.addHeader("SECRET-KEY", secret);
        //client.addHeader("Content-Type", "multipart/form-data");
        Log.d("API-KEY", key);
        Log.d("SECRET-KEY", secret);
        Log.d("API-KEY", key);
        Log.d("JSON", json);
        RequestParams params2 = new RequestParams();
        params2.setForceMultipartEntityContentType(true);
        params2.put("params", json);
        if (!nf.isEmpty())
        {
            try {
                File myFile = new File(nf);
                params2.put("feature_image", myFile);
            }
            catch (FileNotFoundException e)
            {
                Log.d("File Not Found", e.getMessage());
            }
        }
        client.post(url, params2, requestResponseHandler);
    }

    private AsyncHttpResponseHandler requestResponseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            me.mProgressDialog.hide();
            showSnackbar("Error connecting");
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            me.mProgressDialog.hide();
            if (arg2 == null) {
                showSnackbar("Error connecting");
            } else {
                String json = new String(arg2);
                updateFetchResult(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten / 1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            me.mProgressDialog.setContent("Loaded " + prg + "kB");
        }
    };

    public void updateFetchResult(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
            return;
        }
        Log.d("http", "json: " + json);
        try {
            JSONObject retval = new JSONObject(json);
            int success = retval.getInt("success");
            String msg = retval.getString("msg");
            if (success > 0) {
                Utils.reLoadDataAcco(me);
                Utils.showMessage(me, "Uploaded successfully", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            } else {

                JSONObject error = null;
                if (retval.has("errors"))
                    retval.getJSONObject("errors");
                else if (retval.has("error"))
                    retval.getJSONObject("error");
                if (error != null) {
                    String[] flds = {"destination_code"};
                    for (String f : flds) {
                        if (error.has(f))
                            msg += "\n" + error.getString(f);
                    }
                } else {
                    if (msg.isEmpty()) msg = "Undefined error";
                }
                showSnackbar(msg);
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
        }
    }


    public void doSaveAccomodation() {
        String url = Utils.HTTP_RPCURL + "/accomodation";
        Log.d(Utils.TAG, "Loading: " + url);
        JSONObject params = new JSONObject();
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("MMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            //params.put("destination_code", me.acco==null?"DST-" + sdf1.format(new Date()):me.acco.getCode());
            params.put("accomodation_address", address.getText().toString().trim());
            params.put("post_date", sdf2.format(new Date()));
            params.put("post_title", nama.getText().toString().trim());
            params.put("post_content", description.getHtml().toString().trim());
            params.put("accomodation_category", Utils.destcats.get(spn.getSelectedItemPosition()).getId());
            params.put("accomodation_latitude", lat.getText().toString().trim());
            params.put("accomodation_longitude", lng.getText().toString().trim());
            params.put("accomodation_person_name", picname.getText().toString().trim());
            params.put("accomodation_person_phone", picphone.getText().toString().trim());
            params.put("accomodation_person_fax", fax.getText().toString().trim());
            params.put("accomodation_person_email", email.getText().toString().trim());
            params.put("accomodation_person_website", website.getText().toString().trim());
            params.put("accomodation_min", minacco.getText().toString().trim());
            params.put("accomodation_max", maxacco.getText().toString().trim());
            params.put("accomodation_id", dev==null?"":dev.getId());
            params.put("accomodation_gallery", new JSONArray());
            JSONArray f = new JSONArray();
            boolean[] checked = mspn.getSelected();
            int idx = 0;
            for(Device c : facs)
            {
                if (checked[idx])
                    f.put(c.getId());
                idx++;
            }
            params.put("facilities", f);
        } catch (JSONException e) {
            showSnackbar("Error connecting: " + e.getMessage());
            return;
        }
        String json = params.toString();
        //Entity
        String key = Utils.getConfig(me, "api_key2");
        final HashCode hashCode = Hashing.sha1().hashString(key + json, Charset.defaultCharset());
        me.mProgressDialog.show();
        String secret = hashCode.toString();
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("API-KEY", key);
        client.addHeader("SECRET-KEY", secret);
        Log.d("API-KEY", key);
        Log.d("SECRET-KEY", secret);
        Log.d("API-KEY", key);
        Log.d("JSON", json);
        RequestParams params2 = new RequestParams();
        params2.setForceMultipartEntityContentType(true);
        params2.put("params", json);
        if (!nf.isEmpty())
        {
            try {
                File myFile = new File(nf);
                params2.put("feature_image", myFile);
            }
            catch (FileNotFoundException e)
            {

            }
        }
        client.post(url, params2, requestResponseHandlerAcco);
    }

    private AsyncHttpResponseHandler requestResponseHandlerAcco = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            me.mProgressDialog.hide();
            showSnackbar("Error connecting");
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            me.mProgressDialog.hide();
            if (arg2 == null) {
                showSnackbar("Error connecting");
            } else {
                String json = new String(arg2);
                updateFetchResultAcco(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten / 1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            me.mProgressDialog.setContent("Loaded " + prg + "kB");
        }
    };

    public void updateFetchResultAcco(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
            return;
        }
        Log.d("http", "json: " + json);
        try {
            JSONObject retval = new JSONObject(json);
            int success = retval.getInt("success");
            String msg = retval.getString("msg");
            if (success > 0) {
                Utils.reLoadDataAcco(me);
                Utils.showMessage(me, "Uploaded successfully", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            } else {

                JSONObject error = null;
                if (retval.has("errors"))
                    retval.getJSONObject("errors");
                else if (retval.has("error"))
                    retval.getJSONObject("error");
                if (error != null) {
                    String[] flds = {"destination_code"};
                    for (String f : flds) {
                        if (error.has(f))
                            msg += "\n" + error.getString(f);
                    }
                } else {
                    if (msg.isEmpty()) msg = "Undefined error";
                }
                showSnackbar(msg);
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
        }
    }

    public void doSavePromo() {
        String url = Utils.HTTP_RPCURL + "/promo";
        Log.d(Utils.TAG, "Loading: " + url);
        JSONObject params = new JSONObject();
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("MMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            params.put("post_date", sdf2.format(new Date()));
            params.put("post_title", nama.getText().toString().trim());
            params.put("post_content", description.getHtml().toString().trim());
            params.put("promo_code", "PRM-" + sdf1.format(new Date()));
            params.put("promo_location", Utils.acco.get(spnacco.getSelectedItemPosition()).getId());
            params.put("promo_background", bgpos);
            params.put("promo_layout", "layout1");
            params.put("promo_disc_nominal", amount.getText().toString().trim());
            params.put("promo_disc_persen", persen.getText().toString().trim());
            params.put("promo_start", start.getText().toString().trim());
            params.put("promo_end", end.getText().toString().trim());
            params.put("promo_person_name", picname.getText().toString().trim());
            params.put("promo_person_phone", picphone.getText().toString().trim());
            params.put("promo_person_fax", fax.getText().toString().trim());
            params.put("promo_person_email", email.getText().toString().trim());
            params.put("promo_id", dev==null?"":dev.getId());
        } catch (JSONException e) {
            showSnackbar("Error connecting: " + e.getMessage());
            return;
        }

        String json = params.toString();
        //Entity
        String key = Utils.getConfig(me, "api_key2");
        final HashCode hashCode = Hashing.sha1().hashString(key + json, Charset.defaultCharset());
        me.mProgressDialog.show();
        String secret = hashCode.toString();
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("API-KEY", key);
        client.addHeader("SECRET-KEY", secret);
        Log.d("API-KEY", key);
        Log.d("SECRET-KEY", secret);
        Log.d("API-KEY", key);
        Log.d("JSON", json);
        RequestParams params2 = new RequestParams();
        params2.setForceMultipartEntityContentType(true);
        params2.put("params", json);
        if (!nf.isEmpty())
        {
            try {
                File myFile = new File(nf);
                params2.put("feature_image", myFile);
            }
            catch (FileNotFoundException e)
            {

            }
        }
        client.post(url, params2, requestResponseHandlerpromo);

    }

    private AsyncHttpResponseHandler requestResponseHandlerpromo = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            me.mProgressDialog.hide();
            showSnackbar("Error connecting");
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            me.mProgressDialog.hide();
            if (arg2 == null) {
                showSnackbar("Error connecting");
            } else {
                String json = new String(arg2);
                updateFetchResultpromo(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten / 1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            me.mProgressDialog.setContent("Loaded " + prg + "kB");
        }
    };

    public void updateFetchResultpromo(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
            return;
        }
        Log.d("http", "json: " + json);
        try {
            JSONObject retval = new JSONObject(json);
            int success = retval.getInt("success");
            String msg = retval.getString("msg");
            if (success > 0) {
                Utils.reLoadDataPromo(me);
                Utils.showMessage(me, "Uploaded successfully", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            } else {

                JSONObject error = null;
                if (retval.has("errors"))
                    retval.getJSONObject("errors");
                else if (retval.has("error"))
                    retval.getJSONObject("error");
                if (error != null) {
                    String[] flds = {"destination_code"};
                    for (String f : flds) {
                        if (error.has(f))
                            msg += "\n" + error.getString(f);
                    }
                } else {
                    if (msg.isEmpty()) msg = "Undefined error";
                }
                showSnackbar(msg);
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
        }
    }

    public void showSnackbar(String message) {
        new SnackBar.Builder(me)
                .withMessage(message)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_qrcode, menu);
        qrmenu = menu.findItem(R.id.action_qrcode);
        qrmenu.setVisible(dev != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_qrcode:
                if (dev == null)
                {
                    Utils.showMessage(me, "Not saved yet");
                    return super.onOptionsItemSelected(item);
                }
                final Bitmap myBitmap = QRCode.from(dev.getLink()).bitmap();
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "qrcode.jpg");
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Utils.showMessage(me, "Cannot write to file: " + e.getMessage());
                    return super.onOptionsItemSelected(item);
                }
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + f.getAbsolutePath()));
                startActivity(Intent.createChooser(share, "Send QRCode"));
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

}
