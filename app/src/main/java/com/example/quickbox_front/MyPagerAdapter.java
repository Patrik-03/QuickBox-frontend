package com.example.quickbox_front;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class MyPagerAdapter extends PagerAdapter {
    private Context mContext;
    Format_time format_time;
    private List<CarouselItem> mItems;

    public MyPagerAdapter(Context context, List<CarouselItem> items) {
        mContext = context;
        mItems = items;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.carousel, container, false);

        TextView id = itemView.findViewById(R.id.textViewID);
        TextView time = itemView.findViewById(R.id.textViewTime);
        TextView status = itemView.findViewById(R.id.textViewStatus);
        TextView noDeliveries = itemView.findViewById(R.id.textViewNoDelivery);
        ImageButton delivery = itemView.findViewById(R.id.delivery);

        ProgressBar progressBar = itemView.findViewById(R.id.progressBar);
        noDeliveries.setVisibility(View.GONE);
        id.setVisibility(View.VISIBLE);
        time.setVisibility(View.VISIBLE);
        status.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        delivery.setOnClickListener(v -> {
            if (noDeliveries.getVisibility() == View.GONE) {
                Intent intent = new Intent(MyPagerAdapter.this.mContext, Delivery_Handler.class);
                intent.putExtra("id", mItems.get(position).getId());
                intent.putExtra("type", "active");
                MyPagerAdapter.this.mContext.startActivity(intent);
                if (mContext instanceof Activity) {
                    ((Activity) mContext).overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
                }            }
        });

        format_time = new Format_time(mItems.get(position).getTime());

        switch (mItems.get(position).getStatus()) {
            case "Sent":
                id.setText("ID: " + mItems.get(position).getId().toString());
                time.setText(mContext.getString(R.string.expected) + " " + format_time.getFormattedTime());
                status.setText("Status: " + mContext.getString(R.string.sentS));
                progressBar.setProgress(0);
                break;
            case "Received by courier":
                id.setText("ID: " + mItems.get(position).getId().toString());
                time.setText(mContext.getString(R.string.expected) + " " + format_time.getFormattedTime());
                status.setText("Status: " + mContext.getString(R.string.received));
                progressBar.setProgress(25);
                break;
            case "On the go":
                id.setText("ID: " + mItems.get(position).getId().toString());
                time.setText(mContext.getString(R.string.expected) + " " + format_time.getFormattedTime());
                status.setText("Status: " + mContext.getString(R.string.ontheGo));
                progressBar.setProgress(50);
                break;
            case "Nearby":
                id.setText("ID: " + mItems.get(position).getId().toString());
                time.setText(mContext.getString(R.string.expected) + " " + format_time.getFormattedTime());
                status.setText("Status: " + mContext.getString(R.string.nearby));
                progressBar.setProgress(75);
                break;
            case "Delivered":
                id.setText("ID: " + mItems.get(position).getId().toString());
                time.setText(mContext.getString(R.string.expected) + " " + format_time.getFormattedTime());
                status.setText("Status: " + mContext.getString(R.string.delivered));
                progressBar.setProgress(100);
                break;
            case "No delivery":
                Log.d("WebSocket", "No delivery");
                id.setVisibility(View.GONE);
                time.setVisibility(View.GONE);
                status.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                noDeliveries.setVisibility(View.VISIBLE);
                break;
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
