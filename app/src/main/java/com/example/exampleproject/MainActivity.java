package com.example.exampleproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ArrayList<VideoModel> list;
    private VideoAdapter adapter;
    String[] links ={"https://web.law.duke.edu/cspd/contest/videos/Framed-Contest_Documentaries-and-You.mp4","https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        list = new ArrayList<>();
        for(int i = 0;i<5;i++){
            list.add(new VideoModel(links[0],"title"+i, ""));
        }
        Log.d("LIST", "onCreate: "+list.get(0).getUrl());
        adapter = new VideoAdapter(list);
        viewPager.setAdapter(adapter);
    }
}