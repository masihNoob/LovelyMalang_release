package com.lunartech.lovelymalang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.ImageLoader;

public class ActivityAcco extends AppCompatActivity implements View.OnClickListener {

    static ActivityAcco me;
    private static ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String src = getIntent().getStringExtra("src");

        switch (src)
        {
            case "hotel":
                setContentView(R.layout.fragment_hotel);
                break;
            case "food":
                setContentView(R.layout.fragment_food);
                break;
            case "shopping":
                setContentView(R.layout.fragment_shopping);
                break;
            case "travel":
                setContentView(R.layout.fragment_travel);
                break;
            case "souvenir":
                setContentView(R.layout.fragment_souvenir);
                break;
            case "destination":
                setContentView(R.layout.fragment_destination);
                break;
            case "entertain":
                setContentView(R.layout.fragment_entertain);
                break;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        me = this;

        ActionBar ac = getSupportActionBar();
        if (ac != null)
        {
            ac.setDisplayHomeAsUpEnabled(true);
            ac.setDisplayShowTitleEnabled(true);
        }

        String catname = getIntent().getStringExtra("title");
        setTitle(catname);

        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        ImageView bg = (ImageView) findViewById(R.id.mainbg);
        if (bg != null)
        {
            for(Device dev : Utils.board)
            {
                if (dev.getLocation().equals("49"))
                {
                    mImageLoader.get(dev.getImage(),
                            com.android.volley.toolbox.ImageLoader.getImageListener(bg,
                                    R.drawable.gedek, R.drawable.gedek));
                }
            }
        }

    }

    public void doLoadData(String cat, String catname)
    {
        Intent loginIntent = new Intent(ActivityAcco.this, ActivityList.class);
        loginIntent.putExtra("cat", cat);
        loginIntent.putExtra("src", "acco");
        loginIntent.putExtra("catname", catname);
        startActivity(loginIntent);
    }

    public void doLoadGallery(String cat, String catname)
    {
        Intent loginIntent = new Intent(ActivityAcco.this, ActivityList.class);
        loginIntent.putExtra("cat", cat);
        loginIntent.putExtra("src", "gallery");
        loginIntent.putExtra("catname", catname);
        startActivity(loginIntent);
    }

    public void doLoadNews(String cat, String catname)
    {
        Intent loginIntent = new Intent(ActivityAcco.this, ActivityNews.class);
        loginIntent.putExtra("cat", cat);
        loginIntent.putExtra("catname", catname);
        startActivity(loginIntent);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnHotel:
                doLoadData("hotel", "Hotel"); break;
            case R.id.btnHomestay:
                doLoadData("homestay", "Home Stay"); break;
            case R.id.btnPondokwisata:
                doLoadData("pondok_wisata", "Pondok Wisata"); break;
            case R.id.btnGuesthouse:
                doLoadData("guest_house", "Guest House"); break;

            case R.id.btnCafe:
                doLoadData("cafe/resto", "Cafe / Resto"); break;
            case R.id.btnCoffeeshop:
                doLoadData("coffeeshop", "Coffee Shop"); break;
            case R.id.btnRM:
                doLoadData("rumah_makan", "Restaurant"); break;
            case R.id.btnJajanan:
                doLoadData("jajanan", "Jajanan"); break;

            case R.id.btnMall:
                doLoadData("mall", "Mall"); break;
            case R.id.btnPlaza:
                doLoadData("plaza", "Plaza"); break;
            case R.id.btnBazaar:
                doLoadData("bazaar", "Bazaar"); break;

            case R.id.btnHandCraft:
                doLoadData("handycraft", "Handy Craft"); break;
            case R.id.btnSnack:
                doLoadData("snack", "Snack"); break;
            case R.id.btnSouvenir:
                doLoadData("souvenir", "Souvenir"); break;

            case R.id.btnMuseum:
                doLoadData("museum", "Museum"); break;
            case R.id.btnParfes:
                doLoadData("parks", "Parks"); break;
            case R.id.btnHeritage:
                doLoadData("heritage", "Heritage"); break;
            case R.id.btnGallery:
                doLoadData("gallery", "Gallery"); break;
            case R.id.btnKampung:
                doLoadData("kampong_tematik", "Kampong Tematik"); break;

            case R.id.btnTour:
                doLoadData("tour_operator", "Tour Operator"); break;
            case R.id.btnTicket:
                doLoadData("ticket_agent", "Ticket Agent"); break;
            case R.id.btnRentcar:
                doLoadData("rent_car/bike", "Rent Car / Bike"); break;

            case R.id.btnSpa:
                doLoadData("spa", "Spa"); break;
            case R.id.btnKaraoke:
                doLoadData("karaoke", "Karaoke"); break;
            case R.id.btnCinema:
                doLoadData("cinema", "Cinema"); break;
            case R.id.btnBeautycenter:
                doLoadData("beautycenter", "Beauty Shop"); break;
            case R.id.btnDiskotik:
                doLoadData("diskotik", "Discotheque"); break;
            case R.id.btnSportcenter:
                doLoadData("sportcenter", "Sport Center"); break;
            case R.id.btnTV:
                doLoadData("tv", "Television"); break;
            case R.id.btnRadio:
                doLoadData("radio", "Radio"); break;
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
