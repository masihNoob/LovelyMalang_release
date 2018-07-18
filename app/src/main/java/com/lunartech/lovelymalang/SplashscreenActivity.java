package com.lunartech.lovelymalang;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mrengineer13.snackbar.SnackBar;
import com.google.common.base.Utf8;
import com.google.firebase.messaging.FirebaseMessaging;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SplashscreenActivity extends AppCompatActivity {

    //ImageView logo;
    TextView progress;
    SplashscreenActivity me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

        Utils.setSslSocketFactory();

        me = this;

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //logo = (ImageView) findViewById(R.id.fullscreen_content);
        progress = (TextView) findViewById(R.id.dummy_button);

        FirebaseMessaging.getInstance().subscribeToTopic("broadcast");
        String openbc = getIntent().getStringExtra("openbc");
        if (openbc != null) Utils.openbc = true;

        doLoadNews();
    }

    // Asynctask class to process loading in background
    public class Sliding extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Thread.sleep(1000);
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
            Intent loginIntent = new Intent(getApplicationContext(), Main2Activity.class);
            startActivity(loginIntent);
            //overridePendingTransition(R.anim.open_next, R.anim.close_main);

        }
    }

    // Method to show snackbar message
    public void showSnackbar(String message) {
        // Inform user that they will use default position using snackbar
        new SnackBar.Builder(SplashscreenActivity.this)
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
/*
    public void doLoadData() {
        String url = Utils.HTTP_RPCURL + "/destination";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandler);
    }

    private AsyncHttpResponseHandler requestResponseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachedest");
            if (json.isEmpty())
                finish();
            else
                updateFetchResult(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachedest");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResult(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachedest", json);
                updateFetchResult(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading Destinations... "+prg+"kB");
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
                    dev.setGuid(obj.getString("post_name"));
                    dev.setTanggal(obj.getString("post_date"));
                    dev.setSnippet(obj.getString("post_content"));
                    dev.setLink(obj.getString("post_link"));
                    try {
                        JSONObject data = obj.getJSONObject("additional_data");
                        dev.setAddress(data.getString("destination_address"));
                        dev.setCode(data.getString("destination_code"));
                        dev.setLatitude(data.getString("destination_latitude"));
                        dev.setLongitude(data.getString("destination_longitude"));
                        try {
                            dev.setLat(data.getDouble("destination_latitude"));
                            dev.setLng(data.getDouble("destination_longitude"));
                        }
                        catch(JSONException e)
                        {

                        }
                        dev.setMinacco(data.getString("destination_accomodation_from"));
                        dev.setMaxacco(data.getString("destination_accomodation_to"));

                        JSONObject person = data.getJSONObject("contact_person");
                        dev.setPic(person.getString("destination_person_name"));
                        dev.setPhone(person.getString("destination_person_phone"));
                        dev.setFax(person.getString("destination_person_fax"));
                        dev.setEmail(person.getString("destination_person_email"));
                        String f = "";
                        if (obj.has("facilities"))
                            f = obj.getString("facilities");
                        if (!f.isEmpty())
                        {
                            JSONArray facs = new JSONArray(f);
                            String[] fcs = new String[facs.length()];
                            for(int j=0; j<facs.length(); j++)
                                fcs[j] = facs.getString(j);
                            dev.setFacs(fcs);
                        }
                        else
                            dev.setFacs(new String[]{});
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
                        if (obj.has("gallery"))
                        {
                            try {
                                JSONArray gallery = obj.getJSONArray("gallery");
                                String[][] garr = new String[gallery.length()][];
                                for(int j=0; j<gallery.length(); j++)
                                {
                                    String[] gal = {"", "", ""};
                                    JSONObject g = gallery.getJSONObject(j);
                                    gal[0] = g.getString("original");
                                    gal[1] = g.getString("small");
                                    gal[2] = g.getString("medium");
                                    garr[j] = gal;
                                }
                                dev.setGallery(garr);
                            }
                            catch(JSONException e)
                            {

                            }
                        }
                        dev.setCatid(obj.getString("category"));
                        dev.setCatname(obj.getString("category"));
                    } catch (JSONException e) {
                        Log.d(Utils.TAG, "JSON Error: " + e.getMessage());
                        continue;
                    }
                    devices.add(dev);
                }
                Utils.acco = devices;
                doLoadNews();
            } else {
                showSnackbar("No destination found");
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }
*/
    public void doLoadNews() {
        String url = Utils.HTTP_RPCURL + "/news";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlernews);
    }

    private AsyncHttpResponseHandler requestResponseHandlernews = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachenews");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultnews(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachenews");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultnews(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachenews", json);
                updateFetchResultnews(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading News... "+prg+"kB");
        }
    };

    public void updateFetchResultnews(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
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
                    dev.setGuid(obj.getString("post_name"));
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
                                Log.d(Utils.TAG, "JSON Error: " + e.getMessage());
                            }
                        }
                        try {
                            JSONArray cats = obj.getJSONArray("news_category");
                            if (cats.length()>0)
                            {
                                dev.setCatid(cats.getJSONObject(0).getString("category_id"));
                                dev.setCatname(cats.getJSONObject(0).getString("name"));
                            }
                            else
                            {
                                dev.setCatid(obj.getString("category_id"));
                                dev.setCatname(obj.getString("category_name"));
                            }
                        }
                        catch (JSONException e)
                        {
                            Log.d(Utils.TAG, "JSON Error: " + e.getMessage());
                        }
                    devices.add(dev);
                }
                Utils.news = devices;
            } else {
                //showSnackbar("No destination found");
            }
            doLoadPromo();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }

    public void doLoadPromo() {
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
            progress.setText("Loading Promo... "+prg+"kB");
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
            doLoadBoard();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }

    public void doLoadBoard() {
        String url = Utils.HTTP_RPCURL + "/board";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlerboard);
    }

    private AsyncHttpResponseHandler requestResponseHandlerboard = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cacheboard");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultboard(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cacheboard");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultboard(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cacheboard", json);
                updateFetchResultboard(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading Board... "+prg+"kB");
        }
    };

    public void updateFetchResultboard(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
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

                        dev.setLocation(obj.getString("promo_location_id"));
                        dev.setStart(obj.getString("promo_start"));
                        dev.setEnd(obj.getString("promo_end"));

                    } catch (JSONException e) {
                        Log.d(Utils.TAG, "JSON Error: " + e.getMessage());
                        continue;
                    }
                    devices.add(dev);
                }
                Utils.board = devices;
            } else {
                //showSnackbar("No destination found");
            }
            doLoadCats();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }

    public void doLoadCats() {
        String url = Utils.HTTP_RPCURL + "/destination_category";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlercats);
    }

    private AsyncHttpResponseHandler requestResponseHandlercats = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachecats");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultcats(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachecats");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultcats(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachecats", json);
                updateFetchResultcats(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading Categories... "+prg+"kB");
        }
    };

    public void updateFetchResultcats(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
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
                    dev.setId(obj.getInt("category_id"));
                    dev.setNama(obj.getString("category_name"));
                    dev.setGuid(obj.getString("category_slug"));
                    devices.add(dev);
                }
                Utils.destcats = devices;
                //doLoadCats2();
            } else {
                //showSnackbar("No category found");
            }
            doLoadGallery();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }
/*
    public void doLoadCats2() {
        String url = Utils.HTTP_RPCURL + "/accomodation_category";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlercats2);
    }

    private AsyncHttpResponseHandler requestResponseHandlercats2 = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachecats2");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultcats2(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachecats2");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultcats2(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachecats2", json);
                updateFetchResultcats2(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading Categories... "+prg+"kB");
        }
    };

    public void updateFetchResultcats2(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
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
                    dev.setId(obj.getInt("category_id"));
                    dev.setNama(obj.getString("category_name"));
                    dev.setGuid(obj.getString("category_slug"));
                    devices.add(dev);
                }
                Utils.accocats = devices;
                doLoadGallery();
            } else {
                showSnackbar("No category found");
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }
*/

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
            progress.setText("Loading Gallery... "+prg+"kB");
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
                List<Device> devices = new ArrayList<>();
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
                    dev.setLink(obj.getString("post_link"));
                    dev.setActive(obj.getString("active").equals("1"));
                    devices.add(dev);
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
                            devices.add(newdev);
                        }
                    }
                }
                Utils.gallery = devices;
            } else {
                //showSnackbar("No gallery found");
            }
            doLoadDataAcco();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }

    public void doLoadDataAcco() {
        String url = Utils.HTTP_RPCURL + "/accomodation";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlerAcco);
    }

    private AsyncHttpResponseHandler requestResponseHandlerAcco = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cacheacco");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultAcco(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cacheacco");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultAcco(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cacheacco", json);
                updateFetchResultAcco(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading Accomodations... "+prg+"kB");
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
                    dev.setGuid(obj.getString("post_name"));
                    dev.setTanggal(obj.getString("post_date"));
                    dev.setSnippet(obj.getString("post_content"));
                    dev.setKeterangan(obj.getString("post_excerpt"));
                    dev.setBerbintang(obj.getString("berbintang"));
                    dev.setToday(obj.getString("today"));
                    dev.setActive(obj.getString("active").equals("1"));
                    dev.setLink(obj.getString("post_link"));
                    try {
                        JSONObject data = obj.getJSONObject("additional_data");
                        dev.setAddress(data.getString("accomodation_address"));
                        dev.setCode(data.getString("accomodation_code"));
                        dev.setLatitude(data.getString("accomodation_latitude"));
                        dev.setLongitude(data.getString("accomodation_longitude"));
                        try {
                            dev.setLat(data.getDouble("accomodation_latitude"));
                            dev.setLng(data.getDouble("accomodation_longitude"));
                        }
                        catch(JSONException e)
                        {

                        }
                        dev.setMinacco(data.getString("accomodation_from"));
                        dev.setMaxacco(data.getString("accomodation_to"));

                        if (data.has("contact_person"))
                        {
                            JSONObject person = data.getJSONObject("contact_person");
                            dev.setPic(person.getString("accomodation_person_name"));
                            dev.setPhone(person.getString("accomodation_person_phone"));
                            dev.setFax(person.getString("accomodation_person_fax"));
                            dev.setEmail(person.getString("accomodation_person_email"));
                            dev.setWebsite(person.getString("accomodation_person_web"));
                        }
                        String f = "";
                        if (obj.has("facilities"))
                            f = obj.getString("facilities");
                        if (!f.isEmpty())
                        {
                            JSONArray facs = new JSONArray(f);
                            String[] fcs = new String[facs.length()];
                            for(int j=0; j<facs.length(); j++)
                                fcs[j] = facs.getString(j);
                            dev.setFacs(fcs);
                        }
                        else
                            dev.setFacs(new String[]{});
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
                        if (obj.has("gallery"))
                        {
                            try {
                                JSONArray gallery = obj.getJSONArray("gallery");
                                String[][] garr = new String[gallery.length()][];
                                for(int j=0; j<gallery.length(); j++)
                                {
                                    String[] gal = {"", "", ""};
                                    JSONObject g = gallery.getJSONObject(j);
                                    gal[0] = g.getString("original");
                                    gal[1] = g.getString("small");
                                    gal[2] = g.getString("medium");
                                    garr[j] = gal;
                                }
                                dev.setGallery(garr);
                            }
                            catch(JSONException e)
                            {

                            }
                        }
                        dev.setCatid(obj.getString("category"));
                        dev.setCatname(obj.getString("categoryname"));
                    } catch (JSONException e) {
                        Log.d(Utils.TAG, "JSON Error: " + e.getMessage());
                        continue;
                    }
                    devices.add(dev);
                }
                Utils.acco = devices;
            } else {
                //showSnackbar("No accomodation found");
            }
            doLoadDataFac();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }

    public void doLoadDataFac() {
        String url = Utils.HTTP_RPCURL + "/facilities";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlerFac);
    }

    private AsyncHttpResponseHandler requestResponseHandlerFac = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachefac");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultFac(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachefac");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultFac(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachefac", json);
                updateFetchResultFac(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading Facilities... "+prg+"kB");
        }
    };

    public void updateFetchResultFac(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
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
                    dev.setId(obj.getInt("facility_id"));
                    dev.setNama(obj.getString("facility_name"));
                    dev.setGuid(obj.getString("facility_slug"));
                    dev.setSnippet(obj.getString("facility_desc"));

                    dev.setCatid(obj.getString("destination_category_id"));
                    dev.setCatname(obj.getString("destination_category"));
                    devices.add(dev);
                }
                Utils.fac = devices;
            } else {
                //showSnackbar("No facilities found");
            }
            doLoadDataPromobg();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }

    public void doLoadDataPromobg() {
        String url = Utils.HTTP_RPCURL + "/promo_background";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlerPromobg);
    }

    private AsyncHttpResponseHandler requestResponseHandlerPromobg = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachepromobg");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultPromobg(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachepromobg");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultPromobg(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachepromobg", json);
                updateFetchResultPromobg(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading Facilities... "+prg+"kB");
        }
    };

    public void updateFetchResultPromobg(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
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
                    dev.setGuid(obj.getString("background_id"));
                    dev.setBackground(obj.getString("image_path"));
                    devices.add(dev);
                }
                Utils.promobg = devices;
            } else {
                //showSnackbar("No facilities found");
            }
            doLoadToday();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }

    public void doLoadToday() {
        String url = Utils.HTTP_RPCURL + "/today";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlerToday);
    }

    private AsyncHttpResponseHandler requestResponseHandlerToday = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachetoday");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultToday(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachetoday");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultToday(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachetoday", json);
                updateFetchResultToday(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading Facilities... "+prg+"kB");
        }
    };

    public void updateFetchResultToday(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
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
                    dev.setGuid(obj.getString("post_name"));
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
                            Log.d(Utils.TAG, "JSON Error: " + e.getMessage());
                        }
                    }
                    try {
                        JSONArray cats = obj.getJSONArray("news_category");
                        if (cats.length()>0)
                        {
                            dev.setCatid(cats.getJSONObject(0).getString("category_id"));
                            dev.setCatname(cats.getJSONObject(0).getString("name"));
                        }
                        else
                        {
                            dev.setCatid(obj.getString("category_id"));
                            dev.setCatname(obj.getString("category_name"));
                        }
                    }
                    catch (JSONException e)
                    {
                        Log.d(Utils.TAG, "JSON Error: " + e.getMessage());
                    }
                    devices.add(dev);
                }
                Utils.today = devices;
            } else {
                //showSnackbar("No destination found");
            }
            doLoadPromoText();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }

    public void doLoadPromoText() {
        String url = Utils.HTTP_RPCURL + "/promotext";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, requestResponseHandlerpromotext);
    }

    private AsyncHttpResponseHandler requestResponseHandlerpromotext = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            showSnackbar("Error connecting");
            String json = Utils.getConfig(me, "cachepromotext");
            if (json.isEmpty())
                finish();
            else
                updateFetchResultpromotext(json);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (arg2 == null) {
                showSnackbar("Error connecting");
                String json = Utils.getConfig(me, "cachepromotext");
                if (json.isEmpty())
                    finish();
                else
                    updateFetchResultpromotext(json);
            } else {
                String json = new String(arg2);
                Utils.setConfig(me, "cachepromotext", json);
                updateFetchResultpromotext(json);
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            progress.setText("Loading Promo... "+prg+"kB");
        }
    };

    public void updateFetchResultpromotext(String json) {
        if (json == null || json == "") {
            showSnackbar("Connection failed");
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
                Utils.promotext = devices;
            } else {
                //showSnackbar("No destination found");
            }
            new Sliding().execute();
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
            finish();
        }
    }
}
