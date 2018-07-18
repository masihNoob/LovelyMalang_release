package com.lunartech.lovelymalang;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import yweathergetter4a.WeatherInfo;
import yweathergetter4a.YahooWeather;
import yweathergetter4a.YahooWeatherExceptionListener;
import yweathergetter4a.YahooWeatherInfoListener;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, YahooWeatherInfoListener,
        YahooWeatherExceptionListener {

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    static Main2Activity me;

    static int menunum = 0;
    int currentPage = 0;

    private static ImageLoader mImageLoader;

    List<Device> slides = new ArrayList<>();

    TextView txtweather;
    ImageView imgweather;
    private YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, true);

    FloatingActionMenu circleMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        me = this;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (circleMenu.isOpen())
                    circleMenu.close(false);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == slides.size()) {
                    currentPage = 0;
                }
                mViewPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 3000, 3000);

        /*
        btn = (ImageButton) findViewById(R.id.btnTourism);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(me, ActivityTourism.class);
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });
        /*
        * /
        btn = (ImageButton) findViewById(R.id.btnAccomodation);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(me, ActivityAcco.class);
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });
        */
        ImageButton btn = (ImageButton) findViewById(R.id.btnPromo);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(me, ActivityPromo.class);
                intent.putExtra("cat", "1");
                intent.putExtra("catname", "Promo");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });

        btn = (ImageButton) findViewById(R.id.btnEvent);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(me, ActivityEvent.class);
                intent.putExtra("cat", "16");
                intent.putExtra("catname", "Event Calendar");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });

        LinearLayout btnHotel = setupBtn(R.drawable.ic_hotel, "Hotel");
        LinearLayout btnFood = setupBtn(R.drawable.ic_food, "Food");
        //LinearLayout btnMacito = setupBtn(R.drawable.ic_macito, "Macito");
        LinearLayout btnEntertain = setupBtn(R.drawable.ic_entertain, "Entertainment");
        LinearLayout btnShopping = setupBtn(R.drawable.ic_mall, "Shopping");
        LinearLayout btnSouvenir = setupBtn(R.drawable.ic_gift, "Oleh-oleh");
        //LinearLayout btnMoment = setupBtn(R.drawable.ic_gallerymlg, "Moment");
        LinearLayout btnDestination = setupBtn(R.drawable.ic_destination, "Destination");
        LinearLayout btnTravel = setupBtn(R.drawable.ic_travel, "Travel");

        ImageButton centerActionButton = (ImageButton) findViewById(R.id.btnCenter);
        //circleMenu
        //scanForActivity(centerActionButton.getRootView().getContext())

        FloatingActionMenu.Builder build = new FloatingActionMenu.Builder(me)
                .setStartAngle(120) // A whole circle!
                .setEndAngle(360)
                .setRadius((int)getResources().getDimension(R.dimen.range))
                .addSubActionView(btnDestination)
                .addSubActionView(btnShopping)
                .addSubActionView(btnHotel)
                .addSubActionView(btnFood)
                .addSubActionView(btnTravel)
                .addSubActionView(btnSouvenir)
                .addSubActionView(btnEntertain)
                .attachTo(centerActionButton);

        circleMenu = build.build();

        final RelativeLayout customTB = (RelativeLayout) findViewById(R.id.customtoolbar);

        circleMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu floatingActionMenu) {
                customTB.setBackground(ContextCompat.getDrawable(me, R.drawable.gedekinsider));
            }

            @Override
            public void onMenuClosed(FloatingActionMenu floatingActionMenu) {
                customTB.setBackground(ContextCompat.getDrawable(me, R.drawable.homebg));
            }
        });
        btnHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                Intent intent = new Intent(me, ActivityAcco.class);
                intent.putExtra("title", "Hotel");
                intent.putExtra("src", "hotel");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });
        btnFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                Intent intent = new Intent(me, ActivityAcco.class);
                intent.putExtra("title", "Foods");
                intent.putExtra("src", "food");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });
        btnDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                Intent intent = new Intent(me, ActivityAcco.class);
                intent.putExtra("title", "Destination");
                intent.putExtra("src", "destination");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });
        btnShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                Intent intent = new Intent(me, ActivityAcco.class);
                intent.putExtra("title", "Shopping");
                intent.putExtra("src", "shopping");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });
        btnSouvenir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                Intent intent = new Intent(me, ActivityAcco.class);
                intent.putExtra("title", "Oleh-oleh");
                intent.putExtra("src", "souvenir");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });
        btnTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                Intent intent = new Intent(me, ActivityAcco.class);
                intent.putExtra("title", "Travel");
                intent.putExtra("src", "travel");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });

        /*
        btnMoment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleMenu.close(false);
                Intent intent = new Intent(me, ActivityList.class);
                intent.putExtra("cat", "19");
                intent.putExtra("catname", "Moments");
                intent.putExtra("src", "gallery");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });
        */

        btnEntertain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleMenu.close(false);
                Intent intent = new Intent(me, ActivityAcco.class);
                intent.putExtra("title", "Entertainment");
                intent.putExtra("src", "entertain");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
        });

        txtweather = (TextView) findViewById(R.id.txtWeather);
        imgweather = (ImageView) findViewById(R.id.imgWeather);

        getData();

        mYahooWeather.setExceptionListener(this);
        searchByGPS();

        final TextView scroller = (TextView) findViewById(R.id.txtScrolling);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(Device dev : Utils.promotext) // .news
        {
            //if (dev.getCatid().equals("16"))
            if (!dev.getSnippet().isEmpty())
            {
                if (!first) sb.append(" | ");
                //sb.append(Utils.changeDate(dev.getTanggal())).append(" - ").append(dev.getNama());
                sb.append(dev.getSnippet());
                first = false;
            }
        }
        scroller.setText(sb.toString());
        scroller.setSelected(true);

        scroller.post(new Runnable() {
            @Override
            public void run() {
                scroller.setLayoutParams(new LinearLayout.LayoutParams(scroller.getWidth(), scroller.getHeight()));
            }
        });

        setTitle("");

        if (Utils.openbc)
        {
            Utils.openbc = false;
            Intent newsIntent = new Intent(me, ActivityNews.class);
            newsIntent.putExtra("cat", "today");
            newsIntent.putExtra("catname", "Malang Menyapa Today");
            startActivity(newsIntent);
        }
    }

    private LinearLayout setupBtn(int image, String title) {
        LinearLayout lyr = new LinearLayout(me);

        ImageButton btn = new ImageButton(me);
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btn.setImageDrawable(ContextCompat.getDrawable(me, image));
        btn.setBackground(null);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.iconsize), (int)getResources().getDimension(R.dimen.iconsize));
        tvParams.setMargins(0, 0, 0, 0);
        btn.setPadding(0, 0, 0, 0);
        btn.setLayoutParams(tvParams);
        btn.setClickable(false);

        LinearLayout.LayoutParams lvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lvParams.setMargins(0, 0, 0, 0);
        lyr.setOrientation(LinearLayout.VERTICAL);
        lyr.setLayoutParams(lvParams);
        lyr.setGravity(Gravity.CENTER);

        TextView text = new TextView(me);
        text.setText(title);
        text.setGravity(Gravity.CENTER);
        text.setLayoutParams(lvParams);
        text.setTextColor(Color.WHITE);
        text.setClickable(false);
        //text.setBackgroundColor(Color.parseColor("#99000000"));

        lyr.addView(btn);
        lyr.addView(text);

        lyr.setClickable(true);
        return lyr;
    }

    private void searchByGPS() {
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByLatLon(getApplicationContext(), Utils.ARG_DEFAULT_LATITUDE, Utils.ARG_DEFAULT_LONGITUDE, this);
    }

    public void getData() {
        List<Device> devices = new ArrayList<>();
        /*
        for (Device dev : Utils.venues) {
            if (!dev.getImage().isEmpty())// && (dev.getCatid().equals("6")||dev.getCatid().equals("8")||dev.getCatid().equals("49")))
                devices.add(dev);
        }
        for (Device dev : Utils.acco) {
            if (!dev.getImage().isEmpty())// && (dev.getCatid().equals("6")||dev.getCatid().equals("8")||dev.getCatid().equals("49")))
                devices.add(dev);
        }
        */
        for (Device dev : Utils.acco) {
            if (dev.getToday().equals("1"))
                devices.add(dev);
        }

        slides = devices;
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (circleMenu.isOpen()) {
            circleMenu.close(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        menunum = item.getItemId();

        switch (menunum) {
            case R.id.nav_news:
                Intent newsIntent = new Intent(me, ActivityNews.class);
                newsIntent.putExtra("cat", "15");
                newsIntent.putExtra("catname", "News");
                startActivity(newsIntent);
                break;
            case R.id.nav_macito:
                Intent macito = new Intent(me, ActivityNews.class);
                macito.putExtra("cat", "7");
                macito.putExtra("catname", "Macito");
                startActivity(macito);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                break;
            case R.id.nav_publicinfo:
                Intent publicinfo = new Intent(me, ActivityNews.class);
                publicinfo.putExtra("cat", "44");
                publicinfo.putExtra("catname", "Public Info");
                startActivity(publicinfo);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                break;
            case R.id.nav_moment:
                Intent intent = new Intent(me, ActivityList.class);
                intent.putExtra("cat", "19");
                intent.putExtra("catname", "Moments");
                intent.putExtra("src", "gallery");
                startActivity(intent);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                break;
            case R.id.nav_maps:
                Intent intent2 = new Intent(me, ActivityMap.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                break;
            case R.id.nav_member:
                String api_key4 = Utils.getConfig(me, "api_key4");
                if (api_key4.isEmpty()) {
                    Intent memberIntent = new Intent(me, ActivityRegister.class);
                    startActivity(memberIntent);
                } else {
                    Intent memberIntent = new Intent(me, ActivityMember.class);
                    startActivity(memberIntent);
                }
                break;
            case R.id.nav_promotor:
                String api_key = Utils.getConfig(me, "api_key2");
                if (api_key.isEmpty()) {
                    Intent memberIntent = new Intent(me, LoginActivity.class);
                    startActivity(memberIntent);
                } else {
                    String priv = Utils.getConfig(me, "priv");
                    Intent memberIntent = priv.equals("2") ? new Intent(me, VenueActivity.class) : new Intent(me, AdminActivity.class);
                    //Intent loginIntent = new Intent(me, AdminActivity.class);
                    startActivity(memberIntent);
                }
                break;
            case R.id.nav_about:
                Intent aboutIntent = new Intent(me, ActivityAbout.class);
                startActivity(aboutIntent);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            int num = getArguments().getInt(ARG_SECTION_NUMBER);

            rootView = inflater.inflate(R.layout.fragment_slide, container, false);
            ImageView img = (ImageView) rootView.findViewById(R.id.imginslide);
            TextView txt = (TextView) rootView.findViewById(R.id.txtinslide);
            TextView tgl = (TextView) rootView.findViewById(R.id.txttgl);

            final Device dev = me.slides.get(num);

            rootView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent loginIntent = new Intent(me, MainActivity.class);
                    loginIntent.putExtra("newsid", ""+dev.getId());
                    loginIntent.putExtra("src", "acco");
                    loginIntent.putExtra("catname", dev.getCatname());
                    startActivity(loginIntent);
                }
            });

            txt.setText(dev.getNama());
            //tgl.setText(Utils.showEventDate(dev.getStart(), dev.getEnd()));
            tgl.setText(dev.getKeterangan());
            mImageLoader.get(dev.getImage(),
                    com.android.volley.toolbox.ImageLoader.getImageListener(img,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));

            return rootView;
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return slides.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position >= slides.size()) {
                Device dev = slides.get(position);
                return dev.getNama();
            }
            return null;
        }
    }

    @Override
    public void gotWeatherInfo(final WeatherInfo weatherInfo) {
        me.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (weatherInfo != null) {
                    char deg = '\u00B0';
                    String temp = weatherInfo.getCurrentTemp() + "" + deg;
                    txtweather.setText(temp);
            /*
            mTvWeather0.setText("====== CURRENT ======" + "\n" +
                    "date: " + weatherInfo.getCurrentConditionDate() + "\n" +
                    "weather: " + weatherInfo.getCurrentText() + "\n" +
                    "temperature in ºC: " + weatherInfo.getCurrentTemp() + "\n" +
                    "wind chill: " + weatherInfo.getWindChill() + "\n" +
                    "wind direction: " + weatherInfo.getWindDirection() + "\n" +
                    "wind speed: " + weatherInfo.getWindSpeed() + "\n" +
                    "Humidity: " + weatherInfo.getAtmosphereHumidity() + "\n" +
                    "Pressure: " + weatherInfo.getAtmospherePressure() + "\n" +
                    "Visibility: " + weatherInfo.getAtmosphereVisibility()
            );
            */
                    if (weatherInfo.getCurrentConditionIcon() != null) {
                        imgweather.setImageBitmap(weatherInfo.getCurrentConditionIcon());
                        //imgweather.setVisibility(View.VISIBLE);
                    }

                } else {
                    txtweather.setText("-");
                    imgweather.setVisibility(View.GONE);
                }
            }
        });
    }


    @Override
    public void onFailConnection(final Exception e) {
        me.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtweather.setText("-");
                imgweather.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onFailParsing(final Exception e) {
        me.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtweather.setText("-");
                imgweather.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onFailFindLocation(final Exception e) {
        me.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtweather.setText("-");
                imgweather.setVisibility(View.GONE);
            }
        });
    }


}
