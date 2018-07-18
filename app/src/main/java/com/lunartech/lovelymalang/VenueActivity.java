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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
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

public class VenueActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    static VenueActivity me;
    Device venue = null;
    //Device acco = null;
    Device promo = null;
    public ImageLoader mImageLoader;
    private MaterialDialog mProgressDialog;

    static String bgpos = "voucher1";

    // Storage Permissions
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotor);

        me = this;

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        setTitle("Setup Destination");

        String destidstr = Utils.getConfig(me, "accoid");
        //String accoidstr = Utils.getConfig(me, "accoid");
        String promostr = Utils.getConfig(me, "promoid");

        if (!destidstr.isEmpty()) {
            int destid = Integer.valueOf(destidstr);
            for (Device dev : Utils.acco) {
                if (dev.getId()==destid) {
                    venue = dev;

                    for(Device c : Utils.destcats)
                    {
                        if (venue.getCatid().equals(c.getGuid()))
                        {
                            venue.setCatid(""+c.getId());
                            venue.setCatname(c.getNama());
                            break;
                        }
                    }
                    Utils.setConfig(me, "guid", dev.getGuid());
                    break;
                }
            }
        }
/*
        if (!accoidstr.isEmpty()) {
            int accoid = Integer.valueOf(accoidstr);
            for (Device dev : Utils.acco) {
                if (dev.getId()==accoid) {
                    acco = dev;

                    for(Device c : Utils.accocats)
                    {
                        if (c.getGuid().equals(acco.getCatname()))
                        {
                            acco.setCatid(""+c.getId());
                            acco.setCatname(c.getNama());
                            break;
                        }
                    }
                    break;
                }
            }
        }
**/
        if (!promostr.isEmpty()) {
            int promoid = Integer.valueOf(promostr);
            for (Device dev : Utils.promo) {
                if (dev.getId()==promoid) {
                    promo = dev;
                    break;
                }
            }
        }

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(VenueActivity.this)
                        .content("Jika diklik Venue ini akan dishare ke Sosial Media")
                        .positiveText("OK")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                            }
                        })
                        .show();
            }
        });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            verifyStoragePermissions(me);

        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Loading...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false).build();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_promotor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_password) {
            new MaterialDialog.Builder(me)
                    .title("Ganti Password")
                    .customView(R.layout.input_password, true)
                    .positiveText("Ganti")
                    .negativeText("Batal")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            View v = dialog.getCustomView();
                            if (v == null) return;
                            AppCompatEditText oldpassword = (AppCompatEditText) v.findViewById(R.id.txtOldPassword);
                            AppCompatEditText password1 = (AppCompatEditText) v.findViewById(R.id.txtPassword);
                            AppCompatEditText password2 = (AppCompatEditText) v.findViewById(R.id.txtPassword2);

                            String oldpwd = oldpassword.getText().toString().trim();
                            String pwd1 = password1.getText().toString().trim();
                            String pwd2 = password2.getText().toString().trim();

                            if (oldpwd.isEmpty() || pwd1.isEmpty()) {
                                Utils.showMessage(me, "Data tidak lengkap");
                                return;
                            }
                            if (!pwd1.equals(pwd2)) {
                                Utils.showMessage(me, "Kedua password baru tidak sama");
                                return;
                            }
                            changePassword(oldpwd, pwd1);
                        }
                    })
                    .build().show();
        }
        else
        if (id == R.id.action_logout) {

            new MaterialDialog.Builder(VenueActivity.this)
                    .content("Sure to Logout?")
                    .positiveText("Yes")
                    .negativeText("No")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Utils.setConfig(me, "api_key2", "");
                            finish();
                            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(loginIntent);
                        }
                    })
                    .show();

            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private EditText lat, lng, nama, address, picname, picphone, email, website, fax, persen, amount, number, start, end, minacco, maxacco;
        RichEditor description;

        private PlaceholderFragment mex;

        private ImageView img, imgbanner;

        Spinner spn;

        String nf = "";

        PhotoViewAttacher mAttacher;
        int initiator = 0;

        MultiSpinner mspn;
        private ArrayAdapter<String> madapter;
        ArrayList<Device> facs;

        private final TakePhotoUtils tpu = TakePhotoUtils.getInstance();
        HorizontalListView ssgrid;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        void setupFacs(String catid, String selected)
        {
            madapter = new ArrayAdapter<>(me, android.R.layout.simple_spinner_item);
            facs = new ArrayList<>();
            int num = Math.round(Utils.fac.size()/Utils.destcats.size());
            boolean[] sel = new boolean[num];
            int idx = 0;
            for(Device d : Utils.fac)
            {
                if (d.getCatid().equals(catid))
                {
                    facs.add(d);
                    sel[idx] = selected.contains(d.getNama());
                    idx++;
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
                    if (mAttacher != null)
                        mAttacher.update();
                }
                else
                {
                    String nf = tpu.getFile().getAbsolutePath();
                    Intent memberIntent = new Intent(me, ActivitySubmit.class);
                    memberIntent.putExtra("nf", nf);
                    memberIntent.putExtra("destid", ""+me.venue.getId());
                    memberIntent.putExtra("src", "acco");
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
            else if (requestCode==530) {
                int pos = data.getIntExtra("pos", 0);
                String url = Utils.promobg.get(pos).getBackground();
                bgpos = Utils.promobg.get(pos).getGuid();
                me.mImageLoader.get(url,
                        com.android.volley.toolbox.ImageLoader.getImageListener(imgbanner,
                                R.mipmap.empty_photo, R.mipmap.empty_photo));
            } else if (requestCode == GET_GALLERY) {
                onTakeResultFromGallery(data.getData());
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            mex = this;

            int num = getArguments().getInt(ARG_SECTION_NUMBER);
            /*
            if (num == -1) {
                rootView = inflater.inflate(R.layout.fragment_editprofile, container, false);
                List<String> list = new ArrayList<>();
                for (Device dev : Utils.destcats) {
                    list.add(dev.getNama());
                }

                nama = (EditText) rootView.findViewById(R.id.txtnama);
                address = (EditText) rootView.findViewById(R.id.txtaddress);
                description = (RichEditor) rootView.findViewById(R.id.txtdescription);
                picname = (EditText) rootView.findViewById(R.id.txtpicname);
                picphone = (EditText) rootView.findViewById(R.id.txtpicphone);
                email = (EditText) rootView.findViewById(R.id.txtpicemail);
                website = (EditText) rootView.findViewById(R.id.txtpicweb);
                fax = (EditText) rootView.findViewById(R.id.txtpicfax);

                lat = (EditText) rootView.findViewById(R.id.txtlatitude);
                lng = (EditText) rootView.findViewById(R.id.txtlongitude);

                minacco = (EditText) rootView.findViewById(R.id.txtminacco);
                maxacco = (EditText) rootView.findViewById(R.id.txtmaxacco);

                img = (ImageView) rootView.findViewById(R.id.imgvenue);

                spn = (Spinner) rootView.findViewById(R.id.spncat);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(me,
                        android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spn.setAdapter(dataAdapter);
                spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        Device cat = Utils.destcats.get(i);
                        setupFacs("" + cat.getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                mspn = (MultiSpinner) rootView.findViewById(R.id.spnfac);
                setupFacs("");

                if (me.venue != null) {
                    int idx = 0;
                    for (String str : list) {
                        if (str.equals(me.venue.getCatname())) {
                            spn.setSelection(idx);
                            break;
                        }
                        idx++;
                    }
                    nama.setText(me.venue.getNama());
                    address.setText(me.venue.getAddress());
                    description.setHtml(me.venue.getSnippet());
                    picname.setText(me.venue.getPic());
                    picphone.setText(me.venue.getPhone());
                    email.setText(me.venue.getEmail());
                    website.setText(me.venue.getWebsite());

                    fax.setText(me.venue.getFax());
                    lat.setText(me.venue.getLatitude());
                    lng.setText(me.venue.getLongitude());

                    minacco.setText(me.venue.getMinacco());
                    maxacco.setText(me.venue.getMaxacco());

                    setupFacs(me.venue.getCatid());

                    if (!me.venue.getImage3().isEmpty())
                        me.mImageLoader.get(me.venue.getImage3(),
                                com.android.volley.toolbox.ImageLoader.getImageListener(img,
                                        R.mipmap.empty_photo, R.mipmap.empty_photo));
                }

                AppCompatButton save = (AppCompatButton) rootView.findViewById(R.id.btnsave);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doSaveDestination();
                    }
                });

                AppCompatButton btnpic = (AppCompatButton) rootView.findViewById(R.id.btnpic);
                btnpic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tpu.launchGalleryFrame(mex);
                    }
                });

                AppCompatButton map = (AppCompatButton) rootView.findViewById(R.id.btnpeta);
                map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(me, ActivityBrowse.class);
                        startActivityForResult(intent, 526);
                    }
                });

            }
            else
            */
            if (num == 1) {
                rootView = inflater.inflate(R.layout.fragment_editacco, container, false);
                List<String> list = new ArrayList<>();
                for (Device dev : Utils.destcats) {
                    list.add(dev.getNama());
                }

                nama = (EditText) rootView.findViewById(R.id.txtnama);
                address = (EditText) rootView.findViewById(R.id.txtaddress);
                description = (RichEditor) rootView.findViewById(R.id.txtdescription);
                picname = (EditText) rootView.findViewById(R.id.txtpicname);
                picphone = (EditText) rootView.findViewById(R.id.txtpicphone);
                email = (EditText) rootView.findViewById(R.id.txtpicemail);
                website = (EditText) rootView.findViewById(R.id.txtpicweb);
                fax = (EditText) rootView.findViewById(R.id.txtpicfax);

                lat = (EditText) rootView.findViewById(R.id.txtlatitude);
                lng = (EditText) rootView.findViewById(R.id.txtlongitude);

                minacco = (EditText) rootView.findViewById(R.id.txtminacco);
                maxacco = (EditText) rootView.findViewById(R.id.txtmaxacco);

                img = (ImageView) rootView.findViewById(R.id.imgvenue);

                spn = (Spinner) rootView.findViewById(R.id.spncat);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(me,
                        android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spn.setAdapter(dataAdapter);
                spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        Device cat = Utils.destcats.get(i);
                        setupFacs(""+cat.getId(), me.venue==null?"": TextUtils.join(",", me.venue.getFacs()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                mspn = (MultiSpinner) rootView.findViewById(R.id.spnfac);
                //setupFacs("");

                ssgrid = (HorizontalListView) rootView.findViewById(R.id.sslistview);

                if (me.venue != null) {
                    int idx = 0;
                    for (String str : list) {
                        if (str.equals(me.venue.getCatname())) {
                            spn.setSelection(idx);
                            break;
                        }
                        idx++;
                    }
                    nama.setText(me.venue.getNama());
                    address.setText(me.venue.getAddress());
                    description.setHtml(me.venue.getSnippet());
                    picname.setText(me.venue.getPic());
                    picphone.setText(me.venue.getPhone());
                    email.setText(me.venue.getEmail());
                    website.setText(me.venue.getWebsite());

                    fax.setText(me.venue.getFax());
                    lat.setText(me.venue.getLatitude());
                    lng.setText(me.venue.getLongitude());

                    minacco.setText(me.venue.getMinacco());
                    maxacco.setText(me.venue.getMaxacco());

                    //setupFacs(me.venue.getCatid(), me.venue.getFacs().toString());

                    if (!me.venue.getImage3().isEmpty())
                        me.mImageLoader.get(me.venue.getImage3(),
                                com.android.volley.toolbox.ImageLoader.getImageListener(img,
                                        R.mipmap.empty_photo, R.mipmap.empty_photo));

                    if (ssgrid!=null)
                    {
                        String[][] gal = me.venue.getGallery();
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

                }

                mAttacher = new PhotoViewAttacher(img);

                AppCompatButton save = (AppCompatButton) rootView.findViewById(R.id.btnsave);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doSaveAccomodation();
                    }
                });

                AppCompatButton btnpic = (AppCompatButton) rootView.findViewById(R.id.btnpic);
                btnpic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initiator = 0;
                        tpu.launchGalleryFrame(mex);
                    }
                });

                AppCompatButton map = (AppCompatButton) rootView.findViewById(R.id.btnpeta);
                map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(me, ActivityBrowse.class);
                        startActivityForResult(intent, 526);
                    }
                });

                final AppCompatButton addgal = (AppCompatButton) rootView.findViewById(R.id.btnaddgallery);
                if (addgal != null)
                {
                    addgal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initiator = 1;
                            tpu.launchGalleryFrame(mex);
                        }
                    });
                    if (me.venue == null) addgal.setVisibility(View.GONE);
                }

            } else if (num == 5) {
                rootView = inflater.inflate(R.layout.fragment_doverify, container, false);

                AppCompatButton map = (AppCompatButton) rootView.findViewById(R.id.btncek);
                map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(me)
                                //.content("Mengecek status kode promo yang dimasukkan:\n- apakah sudah pernah dipergunakan\n- apakah dari venue ini\n-apakah sudah kadaluwarsa")
                                .content("Unimplemented")
                                .negativeText("Ok")
                                .show();
                    }
                });
            } else if (num == 3) {
                rootView = inflater.inflate(R.layout.fragment_qrcode, container, false);

                ImageView myImage = (ImageView) rootView.findViewById(R.id.qrcode);
                //String guid = Utils.getConfig(me, "guid");
                String link = "";
                if (me.venue!=null) link = me.venue.getLink();
                else
                if (me.venue!=null) link = me.venue.getLink();
                if (link.isEmpty())
                {
                    myImage.setVisibility(View.GONE);
                    AppCompatButton map = (AppCompatButton) rootView.findViewById(R.id.btncek);
                    map.setText("No Accomodation defined");
                }
                else
                {
                    final Bitmap myBitmap = QRCode.from(link).bitmap();
                    myImage.setImageBitmap(myBitmap);
                    myImage.setVisibility(View.VISIBLE);

                    AppCompatButton map = (AppCompatButton) rootView.findViewById(R.id.btncek);
                    map.setText("Share QRCode");
                    map.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                                return;
                            }
                            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + f.getAbsolutePath()));
                            startActivity(Intent.createChooser(share, "Send QRCode"));
                        }
                    });
                }

            } else {
                rootView = inflater.inflate(R.layout.fragment_promote, container, false);

                nama = (EditText) rootView.findViewById(R.id.txttitle);
                description = (RichEditor) rootView.findViewById(R.id.txtdescription);
                picname = (EditText) rootView.findViewById(R.id.txtpicname);
                picphone = (EditText) rootView.findViewById(R.id.txtpicphone);
                email = (EditText) rootView.findViewById(R.id.txtpicemail);
                fax = (EditText) rootView.findViewById(R.id.txtpicfax);

                amount = (EditText) rootView.findViewById(R.id.txtamount);
                persen = (EditText) rootView.findViewById(R.id.txtpersen);
                number = (EditText) rootView.findViewById(R.id.txtnumber);

                start = (EditText) rootView.findViewById(R.id.txtstart);
                end = (EditText) rootView.findViewById(R.id.txtend);

                img = (ImageView) rootView.findViewById(R.id.imgvenue);
                imgbanner = (ImageView) rootView.findViewById(R.id.imgbanner);

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
                        newFragment.show(me.getSupportFragmentManager(), "datePicker");
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
                        newFragment.show(me.getSupportFragmentManager(), "datePicker");
                    }
                });

                AppCompatButton btn = (AppCompatButton) rootView.findViewById(R.id.btnbg);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(me, ActivityPromoBackground.class);
                        startActivityForResult(intent, 530);
                    }
                });

                if (me.promo != null) {
                    nama.setText(me.promo.getNama());
                    description.setHtml(me.promo.getSnippet());
                    picname.setText(me.promo.getPic());
                    picphone.setText(me.promo.getPhone());
                    email.setText(me.promo.getEmail());
                    fax.setText(me.promo.getFax());
                    amount.setText(me.promo.getNominal());
                    persen.setText(me.promo.getPersen());
                    number.setText(me.promo.getNumber());
                    start.setText(me.promo.getStart());
                    end.setText(me.promo.getEnd());

                    me.mImageLoader.get(me.promo.getImage3(),
                            com.android.volley.toolbox.ImageLoader.getImageListener(img,
                                    R.mipmap.empty_photo, R.mipmap.empty_photo));
                    me.mImageLoader.get(me.promo.getBackground(),
                            com.android.volley.toolbox.ImageLoader.getImageListener(imgbanner,
                                    R.mipmap.empty_photo, R.mipmap.empty_photo));
                }

                AppCompatButton btnpic = (AppCompatButton) rootView.findViewById(R.id.btnpic);
                btnpic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tpu.launchGalleryFrame(mex);
                    }
                });

                AppCompatButton map = (AppCompatButton) rootView.findViewById(R.id.btncek);
                map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (me.venue == null) {
                            Utils.showMessage(me, "Please entry a destination profile first");
                            return;
                        }
                        doSavePromo();
                    }
                });
            }
            return rootView;
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

        public void showSnackbar(String message) {
            new SnackBar.Builder(me)
                    .withMessage(message)
                    .show();
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
                if (description != null)
                {
                    if (description.getHtml() != null)
                        params.put("post_content", description.getHtml().trim());
                    else
                        params.put("post_content", "");
                }
                else
                    params.put("post_content", "");
                params.put("promo_code", "PRM-" + sdf1.format(new Date()));
                params.put("promo_location", Utils.getConfig(me, "accoid"));
                params.put("promo_background", bgpos);
                params.put("promo_layout", "layout1");
                params.put("promo_disc_nominal", amount.getText().toString().trim());
                params.put("promo_disc_persen", persen.getText().toString().trim());
                params.put("promo_start", start.getText().toString().trim());
                params.put("promo_end", end.getText().toString().trim());
                params.put("promo_number", number.getText().toString().trim());
                params.put("promo_person_name", picname.getText().toString().trim());
                params.put("promo_person_phone", picphone.getText().toString().trim());
                params.put("promo_person_fax", fax.getText().toString().trim());
                params.put("promo_person_email", email.getText().toString().trim());
                params.put("promo_id", me.promo==null?"":me.promo.getId());
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
                me.mProgressDialog.dismiss();
                showSnackbar("Error connecting");
            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                me.mProgressDialog.dismiss();
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
                    String promoid = retval.getString("promo_id");
                    Utils.setConfig(me, "promoid", promoid);
                    Utils.reLoadDataPromo(me);
                    Utils.showMessage(me, "Uploaded successfully", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            me.finish();
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
                String html = description.getHtml();
                params.put("post_content", html==null?"":html.trim());
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
                params.put("accomodation_id", me.venue==null?"":me.venue.getId());
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
                me.mProgressDialog.dismiss();
                showSnackbar("Error connecting");
            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                me.mProgressDialog.dismiss();
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
                    String destid = retval.getString("accomodation_id");
                    Utils.setConfig(me, "accoid", destid);
                    Utils.reLoadDataAcco(me);
                    Utils.showMessage(me, "Uploaded successfully", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            me.finish();
                        }
                    });
                } else {

                    JSONObject error = null;
                    if (retval.has("errors"))
                        error = retval.getJSONObject("errors");
                    else if (retval.has("error"))
                        error = retval.getJSONObject("error");
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

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case -1:
                    return "Destination";
                case 0:
                    return "Description";
                case 1:
                    return "Promotion";
                case 2:
                    return "QRCode";
                case 4:
                    return "Verify";
            }
            return null;
        }
    }

    public void changePassword(String oldpwd, String newpwd) {
        String url = Utils.HTTP_RPCURL + "/changepwd";
        Log.d(Utils.TAG, "Loading: " + url);
        JSONObject params = new JSONObject();
        try {
            params.put("oldpwd", oldpwd);
            params.put("newpwd", newpwd);
        } catch (JSONException e) {
            showSnackbar("Error connecting: " + e.getMessage());
            return;
        }
        String json = params.toString();
        String key = Utils.getConfig(me, "api_key2");
        final HashCode hashCode = Hashing.sha1().hashString(key + json, Charset.defaultCharset());
        mProgressDialog.show();
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
            client.post(url, params2, requestResponseHandler);
    }

    private AsyncHttpResponseHandler requestResponseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            me.mProgressDialog.dismiss();
            showSnackbar("Error connecting");
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            me.mProgressDialog.dismiss();
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
                Utils.showMessage(me, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            } else {

                if (msg.isEmpty()) msg = "Undefined error";
                showSnackbar(msg);
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
        }
    }

    public void showSnackbar(String message) {
        new SnackBar.Builder(VenueActivity.this)
                .withMessage(message)
                .show();
    }
}
