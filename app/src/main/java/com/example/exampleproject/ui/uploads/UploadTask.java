package com.example.exampleproject.ui.uploads;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.exampleproject.R;
import com.example.exampleproject.utils.NetworkManager;

import java.net.URL;

import io.tus.java.client.TusClient;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;

class UploadTask extends AsyncTask<Void, Long, URL> {
    private UploadService activity;
    private TusClient client;
    private TusUpload upload;
    private Exception exception;
    private NotificationCompat.Builder notificationBuilder;
    private int progress = 0;
    private NotificationManagerCompat notificationManager;
    private Intent actionIntent;
    private boolean hasUploadFailed = false;
    private final int retryTimePeriod = 120; // in seconds
    private final int retryTimeInterval = 2; // in seconds
    private int countRetries = 0;


    public UploadTask(UploadService activity, TusClient client, TusUpload upload, NotificationCompat.Builder builder) {
        this.activity = activity;
        this.client = client;
        this.upload = upload;
        this.notificationBuilder = builder;
        notificationManager = NotificationManagerCompat.from(activity);
        actionIntent = new Intent(activity,UploadService.class);
        countRetries = 0;
    }

    @Override
    protected void onPreExecute() {
        activity.setStatus("Upload selected...");
        activity.setPauseButtonEnabled(true);
        activity.setUploadProgress(0);
        updateNotification("pre-execute");
    }

    @Override
    protected void onPostExecute(URL uploadURL) {
        activity.setStatus("Upload finished!\n" + uploadURL.toString());
        activity.setPauseButtonEnabled(false);
        activity.setResumeButtonEnabled(false);
        updateNotification("post-execute");

    }

    @Override
    protected void onCancelled() {
        if(exception != null && activity != null) {
            activity.showError(exception);
            activity.setPauseButtonEnabled(false);
        }
        updateNotification("fail");
    }

    @Override
    protected void onProgressUpdate(Long... updates) {
        long uploadedBytes = updates[0];
        long totalBytes = updates[1];
        activity.setStatus(String.format("Uploaded %d/%d.", uploadedBytes, totalBytes));
        progress = (int) ((double) uploadedBytes / totalBytes * 100);
        activity.setUploadProgress(progress);
        notificationBuilder.setProgress(100,progress,false);
        notificationManager.notify(1, notificationBuilder.build());
    }

    @Override
    protected URL doInBackground(Void... params) {
        try {
            if(!NetworkManager.isOnline(activity)){
                retryUpload();
            }
            TusUploader uploader = client.resumeOrCreateUpload(upload);
            long totalBytes = upload.getSize();
            long uploadedBytes = uploader.getOffset();

            // Upload file in 1MiB chunks
            uploader.setChunkSize(1024 * 1024);

            while(!isCancelled() && uploader.uploadChunk() > 0) {
                uploadedBytes = uploader.getOffset();
                publishProgress(uploadedBytes, totalBytes);
            }

            uploader.finish();
            return uploader.getUploadURL();

        } catch(Exception e) {
            exception = e;
            updateNotification("pause");
            if(countRetries < 3) {
                countRetries+=1;
                retryUpload();
            } else {
                cancel(true);
            }
        }
        return null;
    }

    private void updateNotification(String action) {
        switch (action) {
            case "pre-execute":
                actionIntent.putExtra("action","cancel");
                PendingIntent cancelPendingIntent = PendingIntent.getService(activity,33,actionIntent,PendingIntent.FLAG_CANCEL_CURRENT);

                notificationBuilder.setProgress(100,0,false)
                        .setContentText("Uploading")
                        .clearActions()
                        .addAction(R.mipmap.ic_launcher,"Cancel",cancelPendingIntent);
                notificationManager.notify(1, notificationBuilder.build());
                break;
            case "pause":
                actionIntent.putExtra("action","retry");
                PendingIntent pendingIntent = PendingIntent.getService(activity,44,actionIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                Intent actionIntent2 = actionIntent;
                actionIntent2.putExtra("action","later");
                PendingIntent pendingIntent2 = PendingIntent.getService(activity,55,actionIntent,PendingIntent.FLAG_CANCEL_CURRENT);
                notificationBuilder.setContentTitle("Paused")
                        .setContentText("Waiting for a better connection")
                        .clearActions()
                        .addAction(R.mipmap.ic_launcher,"RETRY",pendingIntent)
                        .addAction(R.mipmap.ic_launcher,"LATER",pendingIntent2)
                        .setProgress(100,progress,true);
                notificationManager.notify(1, notificationBuilder.build());
                break;
            case "fail":
                notificationBuilder.setContentTitle("Uploading failed")
                        .setContentText("Please try again with good network")
                        .setProgress(0,0,false);
                notificationManager.notify(1, notificationBuilder.build());
                break;
            case "post-execute":
                notificationBuilder.setProgress(0,0,false)
                        .clearActions()
                        .setContentTitle("Success")
                        .setContentText("Your video was successfully uploaded!")
                        .setOngoing(false);
                notificationManager.notify(1, notificationBuilder.build());
                activity.stopForeground(false);
                break;
        }
    }

    private void retryUpload(){
        for(int i = 0; i <= retryTimePeriod; i+= retryTimeInterval){
            try {
                Log.d("NETWORK_MANAGER", "retryUpload: "+NetworkManager.isOnline(activity) + ","+NetworkManager.getUploadSpeed(activity) );
                if(NetworkManager.isOnline(activity) && NetworkManager.getUploadSpeed(activity)>50) {
                    doInBackground();
                    break;
                } else {
                    Thread.sleep(retryTimeInterval * 1000);
                }
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
        cancel(true);
    }
}
