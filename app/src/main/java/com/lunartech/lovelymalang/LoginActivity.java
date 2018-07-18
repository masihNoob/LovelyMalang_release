package com.lunartech.lovelymalang;

import android.content.Intent;
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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mrengineer13.snackbar.SnackBar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;

    TextView register, forgot;

    LoginActivity me;
    private MaterialDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        me = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Set up the login form.
        mUsername = (EditText) findViewById(R.id.txtusernme);

        mPassword = (EditText) findViewById(R.id.txtpassword);
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        register = (TextView) findViewById(R.id.register);
        forgot = (TextView) findViewById(R.id.forgot);

        register.setPaintFlags(register.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgot.setPaintFlags(forgot.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(getApplicationContext(), DaftarActivity.class);
                startActivity(loginIntent);
            }
        });

        forgot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(getApplicationContext(), LupaActivity.class);
                startActivity(loginIntent);
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
        mUsername.setError(null);
        mPassword.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            doLogin(username, password);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 1;
    }

    public void showSnackbar(String message) {
        new SnackBar.Builder(LoginActivity.this)
                .withMessage(message)
                .show();
    }

    public void doLogin(String username, String password)
    {
        String url = Utils.HTTP_RPCURL + "/login";
        Log.d(Utils.TAG, "Loading: "+url);
        AsyncHttpClient client = new AsyncHttpClient();
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("?uname=").append(username).append("&passwd=").append(password);

        //client.addHeader("uname", username);
        //client.addHeader("passwd", password);
        mProgressDialog.show();
        client.get(sb.toString(), loginResponseHandler);
    }

    private AsyncHttpResponseHandler loginResponseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            mProgressDialog.dismiss();
            showSnackbar("Error connecting");
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            mProgressDialog.dismiss();
            if (arg2 == null) {
                showSnackbar("Error connecting");
            } else {
                String json = new String(arg2);
                updateFetchResultLogin(json);
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

    public void updateFetchResultLogin(String json) {
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
                JSONObject login = retval.getJSONObject("login");
                String apikey = login.getString("api_key");
                String user_id = login.getString("user_id");
                String priv = login.getString("user_privilege");
                Utils.setConfig(me, "api_key2", apikey);
                Utils.setConfig(me, "user_id2", user_id);
                Utils.setConfig(me, "api_key4", "");
                Utils.setConfig(me, "priv", priv);
                Utils.setConfig(me, "promoid", login.getString("promoid"));
                Utils.setConfig(me, "destid", login.getString("destid"));
                Utils.setConfig(me, "accoid", login.getString("destid"));
                Intent memberIntent = priv.equals("2") ? new Intent(me, VenueActivity.class):new Intent(me, AdminActivity.class);
                startActivity(memberIntent);
                finish();
            }
            else
            {
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
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }
}

