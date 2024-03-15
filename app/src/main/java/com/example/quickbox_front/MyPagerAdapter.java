package com.example.quickbox_front;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;


import java.util.List;

public class MyPagerAdapter extends PagerAdapter {
    private Context mContext;
    private List<Integer> mImages;

    public MyPagerAdapter(Context context, List<Integer> images) {
        mContext = context;
        mImages = images;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.carousel, container, false);

        ImageView imageView = itemView.findViewById(R.id.image1);
        imageView.setImageResource(mImages.get(position));

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
