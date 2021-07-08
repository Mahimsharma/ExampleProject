package com.example.exampleproject;
import android.util.Log;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HlsCacheDownloader {

    private static String pathDir;
    private static String cacheFolderPath;
    private static String chunkPreUrl;
    private static String videoFolderPath;
    private List<String> chunksUrlList;
    private HashMap<Integer,String> keyFileMap;

    public void initiateCaching(String indexUrl) {
        pathDir = MainActivity.mainActivity.getExternalFilesDir(null)+"";
        chunkPreUrl = indexUrl.substring(0,indexUrl.lastIndexOf("/")+1);
        Log.d("chunkPreUrl", "main: "+chunkPreUrl);
        cacheFolderPath = pathDir+"/cache/";
        videoFolderPath = cacheFolderPath + indexUrl.substring(indexUrl.lastIndexOf("/")+1,indexUrl.lastIndexOf(".")) + "/"; 
        //videoFolderPath = cacheFolderPath + playlist-name from IndexUrl
        chunksUrlList = new ArrayList<String>();
        keyFileMap = new HashMap<Integer,String>();

        getIndexFile(indexUrl);
    }

    static String str="";
    static int iteration=0;
    //iteration 0: to extract subVersion urls for different video qualities; iteration 1: to extract the chunk urls of the video
    private void getIndexFile(String urlPath){
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(urlPath)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("RESPONSE_FAILURE", "onResponse: "+response.body().string());
                    } else {
                        str = response.body().string();
                        extractUrlsFromIndex(str, iteration < 1);
                    }
                }});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void extractUrlsFromIndex(String content, boolean containsQualitySubUrls){
        Pattern pattern = Pattern.compile(".*ts");
        if (containsQualitySubUrls) pattern = Pattern.compile(".*m3u8");
        Matcher ma = pattern.matcher(content);

        List<String> list = new ArrayList<String>();
        while(ma.find()){
            String s = ma.group();
            list.add(s);
            System.out.println(s);
        }
        if(iteration == 0) {
            getIndexFile(list.get(0));
            iteration += 1;
        } else if (iteration == 1){
            for (String chunkName: list){
                chunksUrlList.add(chunkPreUrl+chunkName);
            }
            Log.d("INDEX's", "main: "+chunksUrlList.toString());
            startDownloading();
        }
    }

    private void startDownloading() {
        for (String chunkUrl: chunksUrlList) {
            new DownloadNode(chunkUrl).start();
        }

        while (keyFileMap.size()<chunksUrlList.size()){
            try {                
                System.out.println("Sleeping cuz downloading");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        composeFile(keyFileMap);
    }

    private void composeFile(HashMap<Integer,String> keyFileMap){
        String name = "video.ts";
        try {
            File dir = new File(videoFolderPath);
            File cache = new File(videoFolderPath+name);

            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (cache.exists()) {
                if (cache.delete()) {
                    System.out.println("file Deleted :");
                } else {
                    System.out.println("file not Deleted :");
                }
            }
            cache.createNewFile();
            FileOutputStream out = new FileOutputStream(videoFolderPath+name);

            for(int k=0;k<keyFileMap.size();k++) {
                File chunk = new File(keyFileMap.get(k));
                FileInputStream input = new FileInputStream(chunk);
                byte[] buf = new byte[8192];
                int len;
                while ((len = input.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                input.close();
                chunk.delete();
            }
            out.close();
        System.out.println("Cache Created");
        }catch (Exception e){
            Log.e("CACHE_SYNTHESIS_ERROR", "composeFile: ", e);
        }
    }

    class DownloadNode extends  Thread{
        private String downloadUrl;

        public  DownloadNode(String downloadUrl){
            this.downloadUrl = downloadUrl;
        }

        @Override
        public void run(){
            try{
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(downloadUrl).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.e("CACHING_REQUEST_ERROR", "Request: "+request.toString(),e);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        saveVideo(response.body().bytes());
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void saveVideo(byte[] bytes){
            String videoFileName = downloadUrl.substring(downloadUrl.lastIndexOf("_")+1);
            String chunksPath = videoFolderPath;
            File dir = new File(chunksPath);
            File file = new File(chunksPath+videoFileName);
            try {
                if (!dir.exists()) {
                    dir.mkdir();
                }
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("file Deleted :");
                    } else {
                        System.out.println("file not Deleted :");
                    }
                }
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(chunksPath+videoFileName);
                InputStream input = new ByteArrayInputStream(bytes);

                byte[] buf = new byte[8192];
                int len;
                while ((len = input.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                input.close();
                out.close();
            } catch (Exception e) {
                Log.e("EXPORT", "saveVideo: ",e);
                e.printStackTrace();
                return;
            }
            int key = Integer.parseInt(videoFileName.substring(0,videoFileName.lastIndexOf(".")));
            keyFileMap.put(key,file.getPath());
        }
    }

}