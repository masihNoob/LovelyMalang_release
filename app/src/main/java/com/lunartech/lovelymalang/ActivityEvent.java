package com.lunartech.lovelymalang;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.lunartech.lovelymalang.Utils.dateFormat;


public class ActivityEvent extends BaseActivity implements OnDismissCallback {

    private static final int INITIAL_DELAY_MILLIS = 300;

    private LocalListAdapter adapter;
    private static ImageLoader mImageLoader;
    List<Device> devices;

    ActivityEvent me;
    CompactCalendarView compactCalendarView;

    TextView txtMonth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_event);

        me = this;

        final ListView listView = (ListView) findViewById(R.id.activity_listview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ac = getSupportActionBar();
        if (ac != null)
        {
            ac.setDisplayHomeAsUpEnabled(true);
            ac.setDisplayShowTitleEnabled(true);
        }

        String cat = getIntent().getStringExtra("cat");
        String catname = getIntent().getStringExtra("catname");
        setTitle(catname);

        txtMonth = (TextView) findViewById(R.id.txtBulan);
        txtMonth.setText(Utils.dateMonth.format(new Date()));

        final String today = Utils.dateFormatShort.format(new Date());

        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar);
        compactCalendarView.setLocale(TimeZone.getDefault(), Locale.ENGLISH);
        compactCalendarView.setUseThreeLetterAbbreviation(true);
        compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        compactCalendarView.shouldSelectFirstDayOfMonthOnScroll(false);

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                adapter = new LocalListAdapter(getApplicationContext());
                devices = new ArrayList<>();
                String selected = Utils.dateFormatShort.format(dateClicked);

                for(Device dev : Utils.news) {
                    if (dev.getCatid().equals("16")) {
                        try {
                            Date date = dateFormat.parse(dev.getTanggal());
                            if (Utils.dateFormatShort.format(date).equals(selected))
                            {
                                devices.add(dev);
                                adapter.add(1);
                            }
                        }
                        catch(ParseException e) {}
                    }
                }
                SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, me));
                swingBottomInAnimationAdapter.setAbsListView(listView);

                assert swingBottomInAnimationAdapter.getViewAnimator() != null;
                swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

                listView.setAdapter(swingBottomInAnimationAdapter);

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                txtMonth.setText(Utils.dateMonth.format(firstDayOfNewMonth));
            }
        });

        adapter = new LocalListAdapter(getApplicationContext());
        devices = new ArrayList<>();

        for(Device dev : Utils.news) {
                if (dev.getCatid().equals("16")) {
                    try {
                        Date date = dateFormat.parse(dev.getTanggal());
                        Event ev1 = new Event(Color.RED, date.getTime(), dev.getId());
                        compactCalendarView.addEvent(ev1, false);
                        if (Utils.dateFormatShort.format(date).equals(today))
                        {
                            devices.add(dev);
                            adapter.add(1);
                        }
                    }
                    catch(ParseException e) {}
                }
        }
        compactCalendarView.invalidate();

        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, this));
        swingBottomInAnimationAdapter.setAbsListView(listView);

        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

        listView.setAdapter(swingBottomInAnimationAdapter);

        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device dev = devices.get(position);
                Intent loginIntent = new Intent(ActivityEvent.this, ActivityNewsContent.class);
                loginIntent.putExtra("newsid", dev.getId());
                loginIntent.putExtra("catname", "Event");

                startActivity(loginIntent);
            }
        });

    }

    class LocalListAdapter extends ArrayAdapter<Integer> {

        private final Context mContext;

        LocalListAdapter(final Context context) {
            mContext = context;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            ViewHolder viewHolder;
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_news, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.imgview = (ImageView) view.findViewById(R.id.imgview);
                viewHolder.title = (TextView) view.findViewById(R.id.txttitle);
                view.setTag(viewHolder);
                viewHolder.tanggal = (TextView) view.findViewById(R.id.txttanggal);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            Device dev = devices.get(position);

            viewHolder.title.setText(dev.getNama());
            viewHolder.tanggal.setText(Utils.changeDate(dev.getTanggal()));
            mImageLoader.get(dev.getImage2(),
                    ImageLoader.getImageListener(viewHolder.imgview,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));

            return view;
        }

        @SuppressWarnings({"PackageVisibleField", "InstanceVariableNamingConvention"})
        private class ViewHolder {
            TextView title, tanggal;
            ImageView imgview;
        }
    }


    @Override
    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            //adapter.remove(position);
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
