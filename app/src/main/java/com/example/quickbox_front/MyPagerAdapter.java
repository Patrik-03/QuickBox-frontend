package com.example.quickbox_front;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class MyPagerAdapter extends PagerAdapter {
    private Context mContext;
    private List<CarouselItem> mItems;

    public MyPagerAdapter(Context context, List<CarouselItem> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.carousel, container, false);

        TextView id = itemView.findViewById(R.id.textViewID);
        TextView time = itemView.findViewById(R.id.textViewTime);
        TextView status = itemView.findViewById(R.id.textViewStatus);

        ProgressBar progressBar = itemView.findViewById(R.id.progressBar);

        // Set the ID, time, and status of the item
        id.setText("ID: " + mItems.get(position).getId());
        time.setText("Time: " + mItems.get(position).getTime());
        status.setText("Status: " + mItems.get(position).getStatus());

        if (mItems.get(position).getStatus().equals("sent")) {
            progressBar.setProgress(0);
        }
        else if (mItems.get(position).getStatus().equals("received by courier")) {
            progressBar.setProgress(25);
        }
        else if (mItems.get(position).getStatus().equals("on the go'")) {
            progressBar.setProgress(50);
        }
        else if (mItems.get(position).getStatus().equals("nearby")) {
            progressBar.setProgress(75);
        }
        else if (mItems.get(position).getStatus().equals("delivered")) {
            progressBar.setProgress(100);
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
