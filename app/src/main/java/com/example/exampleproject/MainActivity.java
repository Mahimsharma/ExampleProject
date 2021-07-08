package com.example.exampleproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
       String indexUrl = "https://d1zep23pc3t5l4.cloudfront.net/videos/foxy_video/transcoded/99080/playlist-26454cb0-0dac-432a-aca9-b6dccd95162d.m3u8";

        HlsCacheDownloader downFileUtil = new HlsCacheDownloader();
        downFileUtil.initiateCaching(indexUrl);
    }

}