package com.lunartech.lovelymalang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ActivityNewsContent extends BaseActivity  {

    private static ImageLoader mImageLoader;

    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_webview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ac = getSupportActionBar();
        if (ac != null)
        {
            ac.setDisplayHomeAsUpEnabled(true);
            ac.setDisplayShowTitleEnabled(true);
        }
        HtmlTextView webView = (HtmlTextView) findViewById(R.id.activity_webview);

        long newsid = getIntent().getLongExtra("newsid", 0);

        Device d = null;

        for(Device dev : Utils.news) {
                if (dev.getId()==newsid) {
                    d = dev;
                    break;
                }
        }
        if (d == null)
        {
            finish();
            return;
        }

        final Device device = d;

        String catname = getIntent().getStringExtra("catname");
        setTitle(catname);

        TextView title = (TextView) findViewById(R.id.txttitle);
        title.setText(device.getNama());

        TextView tanggal = (TextView) findViewById(R.id.txttanggal);
        tanggal.setText(Utils.changeDate(device.getTanggal()));

        webView.setHtml(device.getSnippet(),
                new HtmlHttpImageGetter(webView, "", true));
        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        ImageView img = (ImageView) findViewById(R.id.imgeatured);
        if (device.getImage().isEmpty())
            img.setVisibility(View.GONE);
        else
        {
            img.setVisibility(View.VISIBLE);
            mImageLoader.get(device.getImage(),
                    com.android.volley.toolbox.ImageLoader.getImageListener(img,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));
            mAttacher = new PhotoViewAttacher(img);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabShare);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, device.getNama());
                shareIntent.putExtra(Intent.EXTRA_TEXT, device.getSnippet());
                startActivity(Intent.createChooser(shareIntent, "Share to..."));
            }
        });
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
