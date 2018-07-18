package com.lunartech.lovelymalang;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import java.util.ArrayList;
import java.util.List;


public class ActivityPromoBackground extends BaseActivity implements OnDismissCallback {

    private static final int INITIAL_DELAY_MILLIS = 300;

    private LocalListAdapter adapter;
    private static ImageLoader mImageLoader;
    List<Device> devices;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.activity_listview);

        setTitle("Choose Promo Banner");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        adapter = new LocalListAdapter(getApplicationContext());

        devices = new ArrayList<>();
        int i=0;
        for(Device dev : Utils.promobg) {
            devices.add(dev);
            adapter.add(i);
            i++;
        }

        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(adapter, this));
        swingBottomInAnimationAdapter.setAbsListView(listView);

        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

        listView.setAdapter(swingBottomInAnimationAdapter);

        mImageLoader = MySingleton.getInstance(this).getImageLoader();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent();
                //Device item = devices.get(position);
                returnIntent.putExtra("pos", position);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
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
                view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView) view.findViewById(R.id.card_textview);
                view.setTag(viewHolder);
                viewHolder.textJarak = (TextView) view.findViewById(R.id.card_category);
                viewHolder.txtAmount= (TextView) view.findViewById(R.id.txtamount);
                viewHolder.txtRemains = (TextView) view.findViewById(R.id.txtremains);

                viewHolder.imageView = (ImageView) view.findViewById(R.id.card_imageview);
                viewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            Device dev = devices.get(position);

            viewHolder.textView.setText(dev.getGuid());
            viewHolder.textJarak.setText("");

            mImageLoader.get(dev.getBackground(),
                    ImageLoader.getImageListener(viewHolder.imageView,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));

            return view;
        }

        @SuppressWarnings({"PackageVisibleField", "InstanceVariableNamingConvention"})
        private class ViewHolder {
            TextView textView, textJarak, txtAmount, txtRemains;
            ImageView imageView;
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

    @Override
    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            adapter.remove(position);
        }
    }
}
