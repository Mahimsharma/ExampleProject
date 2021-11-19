package com.example.exampleproject;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class CacheWorker extends Worker {

    private Context context;
    String indexUrl1="https://d1zep23pc3t5l4.cloudfront.net/videos/foxy_video/transcoded/100563/playlist-c21f6248-49da-4afc-aac1-5fb0e11a5826.m3u8";
    String indexUrl2="https://d1zep23pc3t5l4.cloudfront.net/videos/foxy_video/transcoded/100471/playlist-c78f27de-1d50-4d63-a16c-040da3134df9.m3u8";
    String indexUrl = "https://d1zep23pc3t5l4.cloudfront.net/videos/foxy_video/transcoded/99080/playlist-26454cb0-0dac-432a-aca9-b6dccd95162d.m3u8";
    String[] indexUrlsArray= {indexUrl,indexUrl1,indexUrl2};

    public CacheWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        for(String indexUrl: indexUrlsArray) {
            new HlsCacheDownloader().initiateCaching(context, indexUrl);
        }
        int k =0;
        while(k<20){
            k+=1;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Result.success();
    }

}
