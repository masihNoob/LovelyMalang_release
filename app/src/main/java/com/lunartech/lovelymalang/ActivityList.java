package com.lunartech.lovelymalang;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.github.mrengineer13.snackbar.SnackBar;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.lunartech.lovelymalang.TakePhotoUtils.GET_CAMERA;
import static com.lunartech.lovelymalang.TakePhotoUtils.GET_GALLERY;


public class ActivityList extends BaseActivity implements OnDismissCallback, MaterialSearchBar.OnSearchActionListener {

    private static final int INITIAL_DELAY_MILLIS = 300;

    private LocalListAdapter adapter;
    private static ImageLoader mImageLoader;
    List<Device> devices;

    ActivityList me;

    ListView listView;

    String src = "";
    String cat = "";

    MaterialSearchBar searchBar;

    private final TakePhotoUtils tpu = TakePhotoUtils.getInstance();
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        me = this;

        listView = (ListView) findViewById(R.id.activity_listview);

        cat = getIntent().getStringExtra("cat");
        final String catname = getIntent().getStringExtra("catname");
        setTitle(catname);
        src = getIntent().getStringExtra("src");

        ActionBar ac = getSupportActionBar();
        if (ac != null)
        {
            ac.setDisplayHomeAsUpEnabled(true);
            ac.setDisplayShowTitleEnabled(true);
        }

        adapter = new LocalListAdapter(getApplicationContext());
        devices = new ArrayList<>();
        final List<Device> srclst;
        if (src.equals("gallery"))
            srclst = Utils.gallery;
        else
        if (src.equals("acco"))
            srclst = Utils.acco;
        else
            srclst = Utils.acco;

        if (srclst == null)
        {
            finish();
            return;
        }

        String[] cats = cat.split("\\,");
        for(Device dev : srclst) {
            for (int i = 0; i < cats.length; i++)
                if (dev.getCatid().contains(cats[i]) && dev.isActive()) {
                    devices.add(dev);
                    adapter.add(i);
                    break;
                }
        }

        final Spinner s = (Spinner) findViewById(R.id.cmbGenre);
        ArrayList<String> floor = new ArrayList<>();

        if (cat.equals("hotel"))
        {
            floor.add("Bintang");
            floor.add("Non-Bintang");
        }
        /*
        for(Device dev : Utils.destcats)
        {
            floor.add(dev.getNama());
        }
        */

        android.widget.ArrayAdapter<String> sadapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, floor.toArray(new String[floor.size()]));
        sadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                devices = new ArrayList<>();
                adapter.clear();
                String checker = i == 0 ? "1" : "0";
                for(Device dev : srclst) {
                    if (dev.getCatid().equals("hotel") && dev.getBerbintang().equals(checker)) {
                        devices.add(dev);
                        adapter.add(i);
                    }
                }

                SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, me));
                swingBottomInAnimationAdapter.setAbsListView(listView);

                assert swingBottomInAnimationAdapter.getViewAnimator() != null;
                swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

                listView.setAdapter(swingBottomInAnimationAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        s.setAdapter(sadapter);
        if (!floor.isEmpty()) s.setVisibility(View.VISIBLE);

        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, this));
        swingBottomInAnimationAdapter.setAbsListView(listView);

        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

        listView.setAdapter(swingBottomInAnimationAdapter);

        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent loginIntent = new Intent(ActivityList.this, MainActivity.class);
                loginIntent.putExtra("newsid", ""+devices.get(position).getId());
                loginIntent.putExtra("catname", catname);
                loginIntent.putExtra("src", src);
                startActivity(loginIntent);
            }
        });

        if (catname.equals("Moments"))
        {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String api_key = Utils.getConfig(me, "api_key4");
                    if (api_key.isEmpty())
                    {
                        Intent memberIntent = new Intent(me, ActivityRegister.class);
                        memberIntent.putExtra("sendresult", 1);
                        startActivityForResult(memberIntent, 527);
                    }
                    else
                        tpu.launchGallery(me);
                }
            });
            fab.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1)
                verifyStoragePermissions(me);
        }

        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);
        searchBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void onSearchStateChanged(boolean b) {

    }

    @Override
    public void onSearchConfirmed(CharSequence charSequence) {
        String search = charSequence.toString().toLowerCase();

        adapter = new LocalListAdapter(getApplicationContext());
        devices = new ArrayList<>();
        final List<Device> srclst;
        if (src.equals("gallery"))
            srclst = Utils.gallery;
        else
        if (src.equals("acco"))
            srclst = Utils.acco;
        else
            srclst = Utils.acco;

        if (srclst == null)
        {
            //finish();
            return;
        }

        String[] cats = cat.split("\\,");
        for(Device dev : srclst) {
            for (int i = 0; i < cats.length; i++)
                if (dev.getCatid().equals(cats[i]) && dev.getNama().toLowerCase().contains(search) && dev.isActive()) {
                    devices.add(dev);
                    adapter.add(i);
                    break;
                }
        }

        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, this));
        swingBottomInAnimationAdapter.setAbsListView(listView);

        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

        listView.setAdapter(swingBottomInAnimationAdapter);

    }

    @Override
    public void onButtonClicked(int i) {

    }

    class LocalListAdapter extends ArrayAdapter<Integer> {

        private final Context mContext;

        LocalListAdapter(final Context context) {
            mContext = context;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            ViewHolder viewHolder;
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView) view.findViewById(R.id.card_textview);
                view.setTag(viewHolder);
                viewHolder.textJarak = (TextView) view.findViewById(R.id.card_category);

                viewHolder.imageView = (ImageView) view.findViewById(R.id.card_imageview);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            if (position<devices.size())
            {
                Device dev = devices.get(position);

                viewHolder.textView.setText(dev.getNama());
                viewHolder.textJarak.setText(dev.getCatname());

                mImageLoader.get(dev.getImage2(),
                        com.android.volley.toolbox.ImageLoader.getImageListener(viewHolder.imageView,
                                R.mipmap.empty_photo, R.mipmap.empty_photo));
            }

            return view;
        }

        @SuppressWarnings({"PackageVisibleField", "InstanceVariableNamingConvention"})
        private class ViewHolder {
            TextView textView, textJarak;
            ImageView imageView;
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
    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            adapter.remove(position);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == 527) {
            tpu.launchGallery(me);
        }
        else
        if (requestCode == 528) {
            doLoadGallery();
        }
        else
        if (requestCode == GET_CAMERA)
        {
            onTakeResultFromCamera();
        }
        else
        if (requestCode == GET_GALLERY)
        {
            onTakeResultFromGallery(data.getData());
        }
    }

    public void showSnackbar(String message) {
        new SnackBar.Builder(ActivityList.this)
                .withMessage(message)
                .show();
    }

    public void onTakeResultFromCamera() {
        if (!TakePhotoUtils.isMediaStorageMounted()) {
            tpu.launchGallery(this);
            return;
        }

        if (!tpu.checkMemory()) {
            showSnackbar("Not enough memory");
            return;
        }
        Bitmap imageBitmap;
        try {
            imageBitmap = tpu.savePhotoAndGetThumbnail(me);
        } catch (OutOfMemoryError e) {
            showSnackbar("Not enough memory");
            return;
        }

        setImage(imageBitmap);
    }

    private void setImage(Bitmap imageBitmap) {
        if (imageBitmap != null) {
            String nf = tpu.getFile().getAbsolutePath();
            Intent memberIntent = new Intent(me, ActivitySubmit.class);
            memberIntent.putExtra("nf", nf);
            memberIntent.putExtra("destid", "");
            memberIntent.putExtra("src", "moment");
            startActivityForResult(memberIntent, 528);
        } else {
            showSnackbar("Can't get image from camera");
        }
    }

    public void doLoadGallery() {
        String url = Utils.HTTP_RPCURL + "/gallery";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlergallery);
    }

    private AsyncHttpResponseHandler requestResponseHandlergallery = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachegal");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultgallery(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachegal");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultgallery(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachegal", json);
                updateFetchResultgallery(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            //progress.setText("Loading Gallery... "+prg+"kB");
        }
    };

    public void updateFetchResultgallery(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
            return;
        }
        Log.d("http", "json: " + json);
        try {
            JSONObject retval = new JSONObject(json);
            JSONArray items = retval.getJSONArray("rows");
            if (items.length()>0) {
                List<Device> devs = new ArrayList<>();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject obj = items.getJSONObject(i);
                    Device dev = new Device();
                    dev.setId(obj.getInt("gallery_id"));
                    dev.setNama(obj.getString("gallery_title"));
                    dev.setSnippet(obj.getString("gallery_description"));
                    if (obj.has("feature_image"))
                    {
                        try {
                            JSONObject image = obj.getJSONObject("feature_image");
                            dev.setImage(image.getString("original"));
                            dev.setImage2(image.getString("small"));
                            dev.setImage3(image.getString("medium"));
                        }
                        catch(JSONException e)
                        {

                        }
                    }
                    dev.setTanggal(obj.getString("publish_date"));
                    String catid = obj.getString("category_id");
                    String[] tmp = catid.split("\\,");
                    dev.setCatid(tmp[0]);
                    String catname = obj.getString("category_name");
                    String[] tmp2 = catname.split("\\,");
                    dev.setCatname(tmp2[0]);
                    //dev.setCatname(obj.getString("category_name"));
                    dev.setAuthor(obj.getString("post_author"));
                    dev.setPublisher(obj.getString("publisher"));
                    devs.add(dev);
                    if (tmp.length>1)
                    {
                        for(int j=1; j<tmp.length; j++)
                        {
                            Device newdev = new Device();
                            newdev.setCatid(tmp[j]);
                            newdev.setTanggal(dev.getTanggal());
                            newdev.setId(dev.getId());
                            newdev.setNama(dev.getNama());
                            newdev.setSnippet(dev.getSnippet());
                            newdev.setImage(dev.getImage());
                            newdev.setImage2(dev.getImage2());
                            newdev.setImage3(dev.getImage3());
                            newdev.setCatname(tmp2[j]);
                            newdev.setAuthor(dev.getAuthor());
                            newdev.setPublisher(dev.getPublisher());
                            devs.add(newdev);
                        }
                    }
                }
                Utils.gallery = devs;
                adapter = new LocalListAdapter(getApplicationContext());
                for(Device dev : Utils.gallery) {
                        if (dev.getCatid().equals("19")) {
                            devices.add(dev);
                            adapter.add(0);
                        }
                }
                SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, this));
                swingBottomInAnimationAdapter.setAbsListView(listView);

                assert swingBottomInAnimationAdapter.getViewAnimator() != null;
                swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

                listView.setAdapter(swingBottomInAnimationAdapter);
            } else {
                showSnackbar("No gallery found");
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
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
}
