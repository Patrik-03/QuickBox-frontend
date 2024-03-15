package com.example.quickbox_front;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class Home_Handler extends AppCompatActivity {
    static Home_Handler homeHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        homeHandler = this;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);
        ViewPager viewPager = findViewById(R.id.viewPager);
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.map);
        images.add(R.drawable.map);
        images.add(R.drawable.map);
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(this, images);
        viewPager.setAdapter(myPagerAdapter);
    }
    public static Home_Handler getInstance(){
        return homeHandler;
    }
}
