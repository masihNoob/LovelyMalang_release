package com.lunartech.lovelymalang;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.github.mrengineer13.snackbar.SnackBar;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ActivityMember extends BaseActivity implements OnDismissCallback {

    private static final int INITIAL_DELAY_MILLIS = 300;

    private LocalListAdapter adapter;
    private static ImageLoader mImageLoader;
    List<Device> devices;

    ActivityMember me;

    ListView listView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_member);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        String nama = Utils.getConfig(this, "username");
        String pp = Utils.getConfig(this, "userpic");

        mImageLoader = MySingleton.getInstance(this).getImageLoader();
        RoundedImageView img = (RoundedImageView) findViewById(R.id.imgprofile);

        if (!pp.isEmpty())
        {
            mImageLoader.get(pp,
                    com.android.volley.toolbox.ImageLoader.getImageListener(img,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));
        }
        else
            img.setVisibility(View.GONE);

        TextView txt = (TextView) findViewById(R.id.txtprofile);
        txt.setText(nama);

        me = this;

        listView = (ListView) findViewById(R.id.activity_listview);

        adapter = new LocalListAdapter(getApplicationContext());

        devices = new ArrayList<>();
        ArrayList<String> srclst = Utils.getPromo(me);

        for(String code : srclst) {
            for(Device dev : Utils.promo)
                if (dev.getCode().equals(code)) {
                    devices.add(dev);
                    adapter.add(1);
                    break;
                }
        }

        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, this));
        swingBottomInAnimationAdapter.setAbsListView(listView);

        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

        //listView.setAdapter(swingBottomInAnimationAdapter);

        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent loginIntent = new Intent(ActivityMember.this, ActivityShowPromo.class);
                //long idx = Integer.valueOf(devices.get(position).getLocation());
                loginIntent.putExtra("newsid", ""+devices.get(position).getId());
                loginIntent.putExtra("catname", devices.get(position).getCatname());
                loginIntent.putExtra("src", "acco");
                startActivity(loginIntent);
                finish();
            }
        });

        doLoadPromo();
    }

    class LocalListAdapter extends ArrayAdapter<Integer> {

        private final Context mContext;

        LocalListAdapter(final Context context) {
            mContext = context;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            LocalListAdapter.ViewHolder viewHolder;
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);

                viewHolder = new LocalListAdapter.ViewHolder();
                viewHolder.textView = (TextView) view.findViewById(R.id.card_textview);
                view.setTag(viewHolder);
                viewHolder.textJarak = (TextView) view.findViewById(R.id.card_category);
                viewHolder.txtAmount= (TextView) view.findViewById(R.id.txtamount);
                viewHolder.txtRemains = (TextView) view.findViewById(R.id.txtremains);
                viewHolder.txtDiskon = (TextView) view.findViewById(R.id.txtdiskon);

                viewHolder.imageView = (ImageView) view.findViewById(R.id.card_imageview);
                viewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                viewHolder = (LocalListAdapter.ViewHolder) view.getTag();
            }

            Device dev = devices.get(position);

            viewHolder.textView.setText(dev.getNama());

            int number = Integer.parseInt(dev.getNumber());
            int used = Integer.parseInt(dev.getUsed());
            if (used>number) used = number;
            viewHolder.textJarak.setText(Utils.showEventDate(dev.getStart(), dev.getEnd()));
            viewHolder.txtAmount.setText(dev.getNominal());
            viewHolder.txtRemains.setText("Remains: " + (number-used) + "/" + number);

            viewHolder.txtRemains.setVisibility(View.VISIBLE);
            viewHolder.txtAmount.setVisibility(View.VISIBLE);
            viewHolder.txtDiskon.setVisibility(View.VISIBLE);

            mImageLoader.get(dev.getBackground(),
                    com.android.volley.toolbox.ImageLoader.getImageListener(viewHolder.imageView,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));

            return view;
        }

        @SuppressWarnings({"PackageVisibleField", "InstanceVariableNamingConvention"})
        private class ViewHolder {
            TextView textView, textJarak, txtAmount, txtRemains, txtDiskon;
            ImageView imageView;
        }
    }

    public void doLoadPromo() {
        showSnackbar("Loading...");
        String url = Utils.HTTP_RPCURL + "/promo";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlerpromo);
    }

    private AsyncHttpResponseHandler requestResponseHandlerpromo = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachepromo");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultpromo(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachepromo");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultpromo(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachepromo", json);
                updateFetchResultpromo(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            //progress.setText("Loading Promo... "+prg+"kB");
        }
    };

    public void updateFetchResultpromo(String json) {
        if (json == null || json == "") {
            //showSnackbar("Connection failed");
            return;
        }
        Log.d("http", "json: " + json);
        try {
            JSONObject retval = new JSONObject(json);
            //int total = retval.getInt("total");
            //if (total > 0) {
            JSONArray items = retval.getJSONArray("rows");
            if (items.length()>0) {
                List<Device> devices = new ArrayList<>();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject obj = items.getJSONObject(i);
                    Device dev = new Device();
                    dev.setId(obj.getInt("id"));
                    dev.setNama(obj.getString("post_title"));
                    dev.setTanggal(obj.getString("post_date"));
                    dev.setSnippet(obj.getString("post_content"));
                    dev.setLink(obj.getString("post_link"));
                    try {
                        //dev.setAddress(obj.getString("permalink"));
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
                        JSONObject person = obj.getJSONObject("contact_person");
                        dev.setPic(person.getString("promo_person_name"));
                        dev.setPhone(person.getString("promo_person_phone"));
                        dev.setFax(person.getString("promo_person_fax"));
                        dev.setEmail(person.getString("promo_person_email"));

                        //dev.setGuid(obj.getString("guid"));
                        //dev.setMime(obj.getString("post_mime_type"));

                        dev.setCode(obj.getString("promo_code"));
                        dev.setLocation(obj.getString("promo_location_id"));
                        dev.setBackground(obj.getString("promo_images"));
                        //dev.setLayout(obj.getString("promo_layout"));
                        dev.setNominal(obj.getString("promo_disc_nominal"));
                        dev.setPersen(obj.getString("promo_disc_persen"));
                        dev.setNumber(obj.getString("promo_number"));
                        dev.setUsed(obj.getString("promo_used"));
                        dev.setStart(obj.getString("promo_start"));
                        dev.setEnd(obj.getString("promo_end"));

                        dev.setCatid(obj.getString("categoryid"));
                        dev.setCatname(obj.getString("categoryname"));

                    } catch (JSONException e) {
                        Log.d(Utils.TAG, "JSON Error: " + e.getMessage());
                        continue;
                    }
                    devices.add(dev);
                }
                Utils.promo = devices;
            } else {
                //showSnackbar("No destination found");
            }

            adapter = new LocalListAdapter(getApplicationContext());
            devices = new ArrayList<>();
            int i=0;
            ArrayList<String> srclst = Utils.getPromo(me);

            for(String code : srclst) {
                for(Device dev : Utils.promo)
                    if (dev.getCode().equals(code)) {
                        devices.add(dev);
                        adapter.add(1);
                        break;
                    }
            }

            final ListView listView = (ListView) findViewById(R.id.activity_listview);
            SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, this));
            swingBottomInAnimationAdapter.setAbsListView(listView);

            assert swingBottomInAnimationAdapter.getViewAnimator() != null;
            swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

            listView.setAdapter(swingBottomInAnimationAdapter);

        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }

    public void showSnackbar(String message) {
        // Inform user that they will use default position using snackbar
        new SnackBar.Builder(ActivityMember.this)
                .withMessage(message)
                .withVisibilityChangeListener(new SnackBar.OnVisibilityChangeListener() {
                    @Override
                    public void onShow(int i) {
                    }

                    @Override
                    public void onHide(int i) {

                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_promotor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_logout:
                Utils.setConfig(this, "api_key4", "");
                finish();
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
            String kode = devices.get(position).getCode();
            Utils.delPromo(me, kode);
            adapter.remove(position);
            devices.remove(position);
        }
    }
}
