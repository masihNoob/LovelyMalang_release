package com.lunartech.lovelymalang;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.toolbox.ImageLoader;
import com.github.mrengineer13.snackbar.SnackBar;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivitySubmit extends AppCompatActivity {

    static ActivitySubmit me;

    private static ImageLoader mImageLoader;
    String nf, postid, src;
    TextView title, description;

    private MaterialDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_moments);

        me = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Processing...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false).build();

        nf = getIntent().getStringExtra("nf");
        postid = getIntent().getStringExtra("destid");
        src = getIntent().getStringExtra("src");

        if (src.equals("moment"))
            setTitle("Submit Moment");
        else
        if (src.equals("dest"))
            setTitle("Submit Destination Gallery");
        else
            setTitle("Submit Accomodation Gallery");

        ImageView img = (ImageView) findViewById(R.id.imgmoment);

        title = (TextView) findViewById(R.id.txttitle);
        description = (TextView) findViewById(R.id.txtdescription);

        img.setImageBitmap(BitmapFactory.decodeFile(nf));

        AppCompatButton btn = (AppCompatButton) findViewById(R.id.btnsave);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stitle = title.getText().toString().trim();
                String sdesc = description.getText().toString().trim();
                if (stitle.isEmpty()) {
                    title.setError("Invalid title");
                    return;
                }
                doSubmit(stitle, sdesc);
            }
        });

    }

    public void doSubmit(String title, String desc) {
        String url = Utils.HTTP_RPCURL + "/gallery";
        Log.d(Utils.TAG, "Loading: " + url);
        JSONObject params = new JSONObject();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            params.put("post_id", "");
            JSONArray arr = new JSONArray();
            if (src.equals("dest"))
            {
                params.put("destination_id", postid);
                arr.put("6");
            }
            else
            if (src.equals("acco"))
            {
                params.put("accomodation_id", postid);
                arr.put("6");
            }
            else
            {
                arr.put("19");
            }

            params.put("post_date", sdf.format(new Date()));
            params.put("post_title", title);
            params.put("post_content", desc);
            params.put("gallery_category", arr);
        } catch (JSONException e) {
            showSnackbar("Error connecting: " + e.getMessage());
            return;
        }
        String json = params.toString();
        String key = Utils.getConfig(me, "api_key4");
        if (key.isEmpty())
            key = Utils.getConfig(me, "api_key2");
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
        File myFile = new File(nf);
        try {
            params2.put("feature_image", myFile);
            client.post(url, params2, requestResponseHandler);
        } catch (FileNotFoundException e) {
            showSnackbar("Image is gone");
            mProgressDialog.dismiss();
        }
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
                Utils.showMessage(me, "Uploaded successfully", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK);
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
        new SnackBar.Builder(ActivitySubmit.this)
                .withMessage(message)
                .show();
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

}

