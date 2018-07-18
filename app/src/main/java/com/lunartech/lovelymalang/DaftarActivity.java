package com.lunartech.lovelymalang;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mrengineer13.snackbar.SnackBar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class DaftarActivity extends AppCompatActivity {

    // UI references.
    private EditText mUsername, mEmailView, mNamaView;
    private EditText mPasswordView, mPasswordView2, mPhone;

    DaftarActivity me;
    private MaterialDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        me = this;

        mNamaView = (EditText) findViewById(R.id.txtnama);
        mUsername = (EditText) findViewById(R.id.txtusernme);
        mEmailView = (EditText) findViewById(R.id.txtemail);

        mPasswordView = (EditText) findViewById(R.id.txtpassword);
        mPasswordView2 = (EditText) findViewById(R.id.txtpassword2);

        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mPhone = (EditText) findViewById(R.id.txtphone);

        final Spinner s = (Spinner) findViewById(R.id.cmbGenre);
        final android.widget.ArrayAdapter<String> madapter = new android.widget.ArrayAdapter<>(this, R.layout.spinneritem);
        for(Device d : Utils.destcats)
        {
            madapter.add(d.getNama());
        }
        s.setAdapter(madapter);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        TextView batal = (TextView) findViewById(R.id.batal);
        batal.setPaintFlags(batal.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        batal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Processing...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false).build();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mNamaView.setError(null);
        mUsername.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordView2.setError(null);

        // Store values at the time of the login attempt.
        String nama = mNamaView.getText().toString().trim();
        String username = mUsername.getText().toString().trim();
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();
        String password2 = mPasswordView2.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();

        final Spinner s = (Spinner) findViewById(R.id.cmbGenre);
        String usaha = s.getSelectedItem().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(nama)) {
            mNamaView.setError(getString(R.string.error_field_required));
            focusView = mNamaView;
            cancel = true;
        }
        if (TextUtils.isEmpty(username)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        }

        Pattern p = Pattern.compile("[^a-zA-Z0-9]");

        if (p.matcher(username).find()) {
            mUsername.setError(getString(R.string.error_field_alpha));
            focusView = mUsername;
            cancel = true;
        }
        if (TextUtils.isEmpty(phone)) {
            mPhone.setError(getString(R.string.error_field_required));
            focusView = mPhone;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email) || !isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Invalid password");
            focusView = mPasswordView;
            cancel = true;
        }

        if (!password.equals(password2)) {
            mPasswordView2.setError("Kedua password harus sama");
            focusView = mPasswordView2;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            mProgressDialog.show();
            doRegister(username, nama, email, password, phone, usaha);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public void showSnackbar(String message) {
        new SnackBar.Builder(DaftarActivity.this)
                .withMessage(message)
                .show();
    }

    public void doRegister(String username, String nama, String email, String password, String phone, String usaha)
    {
        String url = Utils.HTTP_RPCURL + "/users";
        Log.d(Utils.TAG, "Loading: "+url);
        //RequestParams params = new RequestParams();
        String fname = nama;
        String lname = "";
        if (nama.contains(" "))
        {
            fname = nama.substring(0, nama.indexOf(" "));
            lname = nama.substring(nama.indexOf(" ")+1);
        }
        JSONObject params = new JSONObject();
        try {
            params.put("username", username);
            params.put("firstname", fname);
            params.put("lastname", lname);
            params.put("email", email);
            params.put("password", password);
            params.put("confirm_password", password);
            params.put("privilege", "2");
            params.put("phone", phone);
            params.put("usaha", usaha);
        }
        catch (JSONException e)
        {
            showSnackbar("Error connecting: " + e.getMessage());
            return;
        }
        try {
            StringEntity ent = new StringEntity(params.toString());
            AsyncHttpClient client = new AsyncHttpClient();
            mProgressDialog.show();
            client.post(me, url, ent, "application/json", requestResponseHandler);
        }
        catch (UnsupportedEncodingException e)
        {
            showSnackbar("Error connecting: " + e.getMessage());
        }
    }

    private AsyncHttpResponseHandler requestResponseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            mProgressDialog.hide();
            showSnackbar("Error connecting");
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            mProgressDialog.hide();
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
            int prg = Math.round(bytesWritten/1024);
            Log.d("PROGRESS", bytesWritten + " " + totalSize);
            mProgressDialog.setContent("Loaded "+prg+"kB");
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
            if (success>0) {
                Utils.showMessage(me, "Pendaftaran berhasil, silakan tunggu email pemberitahuan setelah account diaktifkan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            } else {

                JSONObject error = null;
                if (retval.has("errors"))
                    retval.getJSONObject("errors");
                else
                if (retval.has("error"))
                    retval.getJSONObject("error");
                if (error != null)
                {
                    String[] flds = {"username", "firstname", "email", "password", "confirm_password"};
                    for(String f : flds)
                    {
                        if (error.has(f))
                            msg += "\n" + error.getString(f);
                    }
                }
                else
                {
                    if (msg.isEmpty()) msg = "Undefined error";
                }
                showSnackbar(msg);
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
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
}

