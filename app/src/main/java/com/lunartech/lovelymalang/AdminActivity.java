package com.lunartech.lovelymalang;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.toolbox.ImageLoader;
import com.github.mrengineer13.snackbar.SnackBar;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements OnDismissCallback {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private static final int INITIAL_DELAY_MILLIS = 300;

    static AdminActivity me;
    public static ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotor);

        me = this;

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        setTitle("Administration");

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(me, ActivityEditDest.class);
                loginIntent.putExtra("newsid", 0);
                switch (tabLayout.getSelectedTabPosition())
                {
                    case 0:
                        loginIntent.putExtra("src", "dest");
                        loginIntent.putExtra("catname", "Destination");
                        break;
                    case 1:
                        loginIntent.putExtra("src", "acco");
                        loginIntent.putExtra("catname", "Accomodation");
                        break;
                    case 2:
                        loginIntent.putExtra("src", "promo");
                        loginIntent.putExtra("catname", "Promotion");
                        break;
                }
                startActivity(loginIntent);
            }
        });

        mImageLoader = MySingleton.getInstance(this).getImageLoader();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_promotor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_logout) {

            new MaterialDialog.Builder(AdminActivity.this)
                    .content("Sure to Logout?")
                    .positiveText("Yes")
                    .negativeText("No")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Utils.setConfig(me, "api_key2", "");
                            finish();
                            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(loginIntent);
                        }
                    })
                    .show();

            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] ints) {

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

        private PlaceholderFragment mex;

        ListView listView;

        private LocalListAdapter adapter;
        List<Device> devices;

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
            mex = this;
            rootView = inflater.inflate(R.layout.fragment_list, container, false);
            listView = (ListView) rootView.findViewById(R.id.activity_listview);

            int num = getArguments().getInt(ARG_SECTION_NUMBER);
            adapter = new LocalListAdapter(mex.getContext(), num);

            devices = new ArrayList<>();

            if (num == 1) {

                for (Device dev : Utils.acco) {
                    devices.add(dev);
                    adapter.add(1);
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Device dev = devices.get(position);
                        Intent loginIntent = new Intent(me, ActivityEditDest.class);
                        loginIntent.putExtra("newsid", dev.getId());
                        loginIntent.putExtra("src", "dest");
                        loginIntent.putExtra("catname", "Destination");

                        startActivity(loginIntent);
                    }
                });
            } else if (num == 2) {
                for (Device dev : Utils.acco) {
                    devices.add(dev);
                    adapter.add(1);
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Device dev = devices.get(position);
                        Intent loginIntent = new Intent(me, ActivityEditDest.class);
                        loginIntent.putExtra("newsid", dev.getId());
                        loginIntent.putExtra("src", "acco");
                        loginIntent.putExtra("catname", "Accomodation");

                        startActivity(loginIntent);
                    }
                });

            } else {
                for (Device dev : Utils.promo) {
                    devices.add(dev);
                    adapter.add(1);
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Device dev = devices.get(position);
                        Intent loginIntent = new Intent(me, ActivityEditDest.class);
                        loginIntent.putExtra("newsid", dev.getId());
                        loginIntent.putExtra("src", "promo");
                        loginIntent.putExtra("catname", "Promotion");

                        startActivity(loginIntent);
                    }
                });
            }


            SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, me));
            swingBottomInAnimationAdapter.setAbsListView(listView);

            assert swingBottomInAnimationAdapter.getViewAnimator() != null;
            swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

            listView.setAdapter(swingBottomInAnimationAdapter);

            mImageLoader = MySingleton.getInstance(me).getImageLoader();

            return rootView;
        }

        class LocalListAdapter extends com.nhaarman.listviewanimations.ArrayAdapter<Integer> {

            private final Context mContext;
            int tab;

            LocalListAdapter(final Context context, int tab) {
                mContext = context;
                this.tab = tab;
            }

            @Override
            public View getView(final int position, final View convertView, final ViewGroup parent) {
                LocalListAdapter.ViewHolder viewHolder;
                View view = convertView;
                if (view == null) {
                    view = LayoutInflater.from(mContext).inflate(R.layout.list_news, parent, false);
                    viewHolder = new LocalListAdapter.ViewHolder();
                    viewHolder.imgview = (ImageView) view.findViewById(R.id.imgview);
                    viewHolder.title = (TextView) view.findViewById(R.id.txttitle);
                    view.setTag(viewHolder);
                    viewHolder.tanggal = (TextView) view.findViewById(R.id.txttanggal);
                } else {
                    viewHolder = (LocalListAdapter.ViewHolder) view.getTag();
                }

                Device dev = devices.get(position);

                viewHolder.title.setText(dev.getNama());
                viewHolder.tanggal.setText(Utils.changeDate(dev.getTanggal()));

                if (tab == 3)
                    mImageLoader.get(dev.getBackground(),
                            com.android.volley.toolbox.ImageLoader.getImageListener(viewHolder.imgview,
                                    R.mipmap.empty_photo, R.mipmap.empty_photo));
                else
                    mImageLoader.get(dev.getImage2(),
                        com.android.volley.toolbox.ImageLoader.getImageListener(viewHolder.imgview,
                                R.mipmap.empty_photo, R.mipmap.empty_photo));

                return view;
            }

            @SuppressWarnings({"PackageVisibleField", "InstanceVariableNamingConvention"})
            private class ViewHolder {
                TextView title, tanggal;
                ImageView imgview;
            }
        }

        public void showSnackbar(String message) {
            new SnackBar.Builder(me)
                    .withMessage(message)
                    .show();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Destination";
                case 1:
                    return "Accomodation";
                case 2:
                    return "Promotion";
                case 3:
                    return "QRCode";
                case 4:
                    return "Verify";
            }
            return null;
        }
    }
}
