package com.lunartech.lovelymalang;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.Utility;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.AccountService;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

/**
 * A login screen that offers login via email/password.
 */
public class ActivityRegister extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TWITTER_KEY = "";
    private static final String TWITTER_SECRET = "";
    private static final int RC_SIGN_IN = 200;

    CallbackManager callbackManager;
    LoginButton loginButton;

    ActivityRegister me;

    TwitterLoginButton twloginButton;
    GoogleApiClient mGoogleApiClient;

    TwitterSession session;

    private MaterialDialog mProgressDialog;

    String email, nama;

    int sendresult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_register);

        me = this;

        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Processing...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false).build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        setTitle("Registration");

        sendresult = getIntent().getIntExtra("sendresult", 0);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.fblogin_button);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String token = loginResult.getAccessToken().getToken();
                //Profile profile = loginResult.getAccessToken().
                String uid = loginResult.getAccessToken().getUserId();
                //Profile profile = loginResult.
                Log.d("LOGGEDIN", uid);
                Log.d("TOKEN", token);

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                try {
                                    String email = object.getString("email");
                                    String name = object.getString("name");
                                    String pp = "";
                                    if (object.has("picture")) {
                                        pp = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    }

                                    //Utils.showMessage(me, "Name: " + name + " & Email: " + email + " pp: " + pp);
                                    Utils.setConfig(me, "useremail", email);
                                    Utils.setConfig(me, "username", name);
                                    Utils.setConfig(me, "userpic", pp);

                                    doLogin(email, name);
                                } catch (JSONException e) {

                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,picture");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("ERROR", error.getMessage());
                Utils.showMessage(me, "Cannot retrieve email from Facebook");
            }
        });

        LoginManager.getInstance().logOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        twloginButton = (TwitterLoginButton) findViewById(R.id.login_button);

        twloginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {

                session = Twitter.getSessionManager().getActiveSession();

                AccountService ac = Twitter.getApiClient(session).getAccountService();

                ac.verifyCredentials(true, false, new Callback<User>() {

                            @Override
                            public void success(Result<User> userResult) {

                                final User user = userResult.data;

                                final String name = user.name;
                                final String pp = user.profileImageUrl;

                                TwitterAuthClient authClient = new TwitterAuthClient();
                                authClient.requestEmail(session, new Callback<String>() {
                                    @Override
                                    public void success(Result<String> result) {
                                        // Do something with the result, which provides the email address
                                        String email = result.data;
                                        Utils.setConfig(me, "useremail", email);
                                        Utils.setConfig(me, "username", name);
                                        Utils.setConfig(me, "userpic", pp);

                                        doLogin(email, name);
                                    }

                                    @Override
                                    public void failure(TwitterException exception) {
                                        Utils.showMessage(me, "Cannot retrieve email from Twitter");
                                    }
                                });
                                //Utils.showMessage(me, "Name: " + name + " & Email: " + email + " pp: " + pp);

                            }

                            @Override
                            public void failure(TwitterException e) {
                                Utils.showMessage(me, "Cannot retrieve email from Twitter");
                            }

                        });

            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
                Utils.showMessage(me, "Cannot retrieve email from Twitter");
            }


        });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Utils.showMessage(me, "Cannot retrieve email");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }

    }

    private void signOut() {
        try {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // ...
                        }
                    });
        }
        catch (IllegalStateException e)
        {

        }
    }

    private void signIn() {
        try {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        catch (IllegalStateException e)
        {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            twloginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("LOGIN G+", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            String name = acct.getDisplayName();
            String email = acct.getEmail();
            Uri url = acct.getPhotoUrl();
            String pp = "";
            if (url != null)
                pp = url.toString();
            //Utils.showMessage(me, "Name: " + name + " & Email: " + email + " pp: " + pp);
            Utils.setConfig(me, "useremail", email);
            Utils.setConfig(me, "username", name);
            Utils.setConfig(me, "userpic", pp);
            signOut();

            doLogin(email, name);
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
            Utils.showMessage(me, "Cannot retrieve email from Google+");
        }
    }

    public void showSnackbar(String message) {
        new SnackBar.Builder(ActivityRegister.this)
                .withMessage(message)
                .show();
    }

    public void doLogin(String email, String nama)
    {
        this.email = email;
        this.nama = nama;

        final HashCode hashCode = Hashing.sha1().hashString(email, Charset.defaultCharset());
        String url = Utils.HTTP_RPCURL + "/login";
        Log.d(Utils.TAG, "Loading: "+url);
        String passwd = hashCode.toString();

        String uname = email.replace("@", "-").replace(".", "-");
        AsyncHttpClient client = new AsyncHttpClient();
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("?uname=").append(uname).append("&passwd=").append(passwd).append("&token=").append(Utils.getConfig(me, "fcmtoken"));

        Log.d("LOGIN", "uname: " + uname + " nama: " + nama + " passwd: " + passwd);

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
            if (success>0) {
                JSONObject login = retval.getJSONObject("login");
                String apikey = login.getString("api_key");
                String user_id = login.getString("user_id");
                Utils.setConfig(me, "api_key4", apikey);
                Utils.setConfig(me, "user_id4", user_id);
                Utils.setConfig(me, "api_key4", apikey);
                Utils.setConfig(me, "api_key2", "");

                if (sendresult==1)
                {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("api_key", apikey);
                    setResult(Activity.RESULT_OK,returnIntent);
                }
                else
                {
                    Intent memberIntent = new Intent(me, ActivityMember.class);
                    startActivity(memberIntent);
                }
                finish();
            } else {
                doRegister(email, nama);
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
        }
    }

    public void doRegister(String email, String nama)
    {
        final HashCode hashCode = Hashing.sha1().hashString(email, Charset.defaultCharset());
        String url = Utils.HTTP_RPCURL + "/users";
        Log.d(Utils.TAG, "Loading: "+url);
        String fname = nama;
        String lname = "";
        if (nama.contains(" "))
        {
            fname = nama.substring(0, nama.indexOf(" "));
            lname = nama.substring(nama.indexOf(" ")+1);
        }
        String passwd = hashCode.toString();

        JSONObject params = new JSONObject();
        try {
            params.put("username", email.replace("@", "-").replace(".", "-"));
            params.put("firstname", fname);
            params.put("lastname", lname);
            params.put("email", email);
            params.put("password", passwd);
            params.put("confirm_password", passwd);
            params.put("privilege", "4");
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
            Log.d("REGISTER", "json: " + params.toString());
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
                doLogin(email, nama);
            } else {
                if (msg.isEmpty()) msg = "Undefined error";
                showSnackbar(msg);
            }
        } catch (JSONException e) {
            showSnackbar("Connect failed: " + e.getMessage());
            Log.d("failed", "Msg: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Call transition when physical back button pressed
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
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

}

