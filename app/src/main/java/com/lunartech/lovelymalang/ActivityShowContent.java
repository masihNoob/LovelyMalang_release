package com.lunartech.lovelymalang;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ActivityShowContent extends BaseActivity {

    private static ImageLoader mImageLoader;

    PhotoViewAttacher mAttacher;
    ImageView image;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_imageview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ac = getSupportActionBar();
        if (ac != null)
        {
            ac.setDisplayHomeAsUpEnabled(true);
            ac.setDisplayShowTitleEnabled(true);
        }

        image = (ImageView) findViewById(R.id.imgview);

        final String url = getIntent().getStringExtra("url");

        mImageLoader = MySingleton.getInstance(this).getImageLoader();
        if (url.equals("tourismmap"))
        {
            setTitle("Peta Pariwisata");
            mImageLoader.get("http://www.lovelymalang.com/malang_map.jpg",
                    ImageLoader.getImageListener(image,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));
        }
        else
        {
            setTitle("Show Image");
            mImageLoader.get(url,
                    ImageLoader.getImageListener(image,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));
        }
        mAttacher = new PhotoViewAttacher(image);
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
