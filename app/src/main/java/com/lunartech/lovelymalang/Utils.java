package com.lunartech.lovelymalang;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyStore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Design and developed by simetri.com
 *
 * Utils is created to set application configuration, from database path, ad visibility.
 */
public class Utils {

    // Debugging tag
    public static final String TAG = "BM:";

    // Key values for passing data between activities
    public static final String ARG_LOCATION_NAME       = "location_name";
    public static final String ARG_LOCATION_ADDRESSES  = "location_addresses";
    public static final String ARG_LOCATION_LONGITUDE  = "location_longitude";
    public static final String ARG_LOCATION_LATITUDE   = "location_latitude";
    public static final String ARG_LOCATION_MARKER     = "location_marker";

    public static final int ARG_DEFAULT_MAP_ZOOM_LEVEL = 4;

    public static final Double ARG_DEFAULT_LATITUDE  = -7.9818162;
    public static final Double ARG_DEFAULT_LONGITUDE = 112.6270804;

    public static final Integer ARG_TIMEOUT_MS  = 4000;

    public static final String HTTP_RPCURL = "http://www.malangmenyapa.com/api/master";
    public static boolean openbc = false;

    public static List<Device> news = new ArrayList<>(), today = new ArrayList<>();
    public static List<Device> promo = new ArrayList<>(), promotext = new ArrayList<>(), promobg = new ArrayList<>(), board = new ArrayList<>();
    public static List<Device> destcats = new ArrayList<>();

    public static List<Device> gallery = new ArrayList<>();
    public static List<Device> acco = new ArrayList<>();
    public static List<Device> fac = new ArrayList<>();

    public static SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    public static SimpleDateFormat dateFormatShort = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.ENGLISH);
    public static SimpleDateFormat dateMonth = new SimpleDateFormat(
            "MMMM, yyyy", Locale.ENGLISH);
    public static SimpleDateFormat indoFormat = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);

    public static AsyncHttpClient client = new AsyncHttpClient();
    public static MySSLSocketFactory socketFactory;

    public static void setSslSocketFactory() {
        if (socketFactory == null) {
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                socketFactory = new MySSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setSSLSocketFactory(socketFactory);
                client.setCookieStore(new BlackholeCookieStore());
            } catch (Exception e) {
                //L.d("SSL: %s", e.getMessage());
            }
        }
    }

    public static void showMessage(Context ctx, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(message).setCancelable(true).setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showMessage(Context ctx, String message, DialogInterface.OnClickListener next) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(message).setCancelable(true).setNegativeButton("Ok", next);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showConfirm(Context ctx, String message, DialogInterface.OnClickListener yes, DialogInterface.OnClickListener no) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(message).setCancelable(true).setNegativeButton("No", no).setPositiveButton("Yes", yes);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static String getConfig(Context ctx, String key)
    {
        final SharedPreferences prefs = ctx.getSharedPreferences(Utils.TAG, 0);
        return prefs.getString(key, "");
    }

    public static void setConfig(Context ctx, String key, String value)
    {
        final SharedPreferences prefs = ctx.getSharedPreferences(Utils.TAG, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static ArrayList<String> getPromo(Context ctx)
    {
        ArrayList<String> promolist = new ArrayList<>();
        String json = getConfig(ctx, "promolist");
        if (!json.isEmpty())
        {
            try {
                JSONArray arr = new JSONArray(json);
                for (int i=0; i<arr.length(); i++)
                    promolist.add(arr.getString(i));
            }
            catch(JSONException e)
            {

            }
        }
        return promolist;
    }

    public static void addPromo(Context ctx, String code) {
        if (code.isEmpty()) return;
        ArrayList<String> promolist = getPromo(ctx);
        if (!promolist.contains(code))
            promolist.add(code);
        JSONArray arr = new JSONArray(promolist);
        setConfig(ctx, "promolist", arr.toString());
    }

    public static void delPromo(Context ctx, String code) {
        if (code.isEmpty()) return;
        ArrayList<String> promolist = getPromo(ctx);
        if (!promolist.contains(code))
        {
            promolist.remove(code);
        }
        JSONArray arr = new JSONArray(promolist);
        setConfig(ctx, "promolist", arr.toString());
    }

    public static String changeDate(String date)
    {
        try {
            return indoFormat.format(dateFormat.parse(date));
        }
        catch (ParseException e)
        {
            return "-";
        }
    }
    public static void reLoadDataAcco(final Context me) {
        String url = Utils.HTTP_RPCURL + "/accomodation";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                String json = Utils.getConfig(me, "cacheacco");
                if (!json.isEmpty())
                    updateFetchResultAcco(json);
            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                if (arg2 == null) {
                    String json = Utils.getConfig(me, "cacheacco");
                    if (!json.isEmpty())
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
                Log.d("PROGRESS", bytesWritten + " " + totalSize);
            }
        });
    }

    public  static void updateFetchResultAcco(String json) {
        if (json == null || json == "") {
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
                        dev.setCatname(obj.getString("category"));
                    } catch (JSONException e) {
                        Log.d(Utils.TAG, "JSON Error: " + e.getMessage());
                        continue;
                    }
                    devices.add(dev);
                }
                Utils.acco = devices;
            }
        } catch (JSONException e) {
            Log.d("failed", "Msg: " + e.getMessage());
        }
    }

    public static void reLoadDataPromo(final Context me) {
        String url = Utils.HTTP_RPCURL + "/promo";
        Log.d(Utils.TAG, "Loading: "+url);
        Utils.client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                String json = Utils.getConfig(me, "cachepromo");
                if (!json.isEmpty())
                    updateFetchResultPromo(json);
            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                if (arg2 == null) {
                    String json = Utils.getConfig(me, "cachepromo");
                    if (!json.isEmpty())
                        updateFetchResultPromo(json);
                } else {
                    String json = new String(arg2);
                    Utils.setConfig(me, "cachepromo", json);
                    updateFetchResultPromo(json);
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                Log.d("PROGRESS", bytesWritten + " " + totalSize);
            }
        });
    }

    public  static void updateFetchResultPromo(String json) {
        if (json == null || json == "") {
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
            }
        } catch (JSONException e) {
            Log.d("failed", "Msg: " + e.getMessage());
        }
    }

    public static String showEventDate(String start, String end)
    {
        try {
            if (start.equals(end)) {
                SimpleDateFormat sameFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
                return sameFormat.format(dateFormatShort.parse(start));
            }
            else
            if (start.startsWith(end.substring(0, 7)))
            {
                SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                return dayFormat.format(dateFormatShort.parse(start)) + " - " + dayFormat.format(dateFormatShort.parse(end)) + " " + monthFormat.format(dateFormatShort.parse(start));
            }
            else
            if (start.startsWith(end.substring(0, 5)))
            {
                SimpleDateFormat dayFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
                SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                return dayFormat.format(dateFormatShort.parse(start)) + " - " + dayFormat.format(dateFormatShort.parse(end)) + " " + monthFormat.format(dateFormatShort.parse(start));
            }
            else
            {
                SimpleDateFormat yearFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
                return yearFormat.format(dateFormatShort.parse(start)) + " - " + yearFormat.format(dateFormatShort.parse(end));
            }
        }
        catch (ParseException pe)
        {
            return start + " - " + end;
        }
    }


    public static String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }
}
