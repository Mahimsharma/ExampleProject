package com.example.exampleproject.ui.uploads;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.exampleproject.R;
import com.example.exampleproject.utils.NetworkManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.tus.java.client.TusClient;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;

class UploadTask extends AsyncTask<Void, Long, URL> {
    private UploadService service;
    private TusClient client;
    private TusUpload upload;
    private Exception exception;
    private NotificationCompat.Builder notificationBuilder;
    private int progress = 0;
    private NotificationManagerCompat notificationManager;
    private Intent actionIntent;
    private boolean hasUploadFailed = false;


    public UploadTask(UploadService service, TusClient client, TusUpload upload, NotificationCompat.Builder builder) {
        this.service = service;
        this.client = client;
        this.upload = upload;
        this.notificationBuilder = builder;
        notificationManager = NotificationManagerCompat.from(service);
        actionIntent = new Intent(service,UploadService.class);
    }

    @Override
    protected void onPreExecute() {
        service.setStatus("Upload selected...");
        service.setPauseButtonEnabled(true);
        service.setUploadProgress(0);
        updateNotification("pre-execute");
    }

    @Override
    protected void onPostExecute(URL uploadURL) {
        if(uploadURL == null) return;
        service.setStatus("Upload finished!\n" + uploadURL.toString());
        service.setPauseButtonEnabled(false);
        service.setResumeButtonEnabled(false);
        updateNotification("post-execute");

        //saving/adding url to the saved list urls of uploaded videos
        SharedPreferences sharedPreferences = service.getSharedPreferences("Uri", Context.MODE_PRIVATE);
        String urls = sharedPreferences.getString("urlList","");
        sharedPreferences.edit().putString("urlList",uploadURL.toString()+","+urls).apply();
    }

    @Override
    protected void onCancelled() {
        if(exception != null && service != null) {
            service.showError(exception);
            service.setPauseButtonEnabled(false);
        }
        if(service.cancellationReason != 0) {
            updateNotification("fail");
        } else {
            notificationManager.cancelAll();
        }
    }

    @Override
    protected void onProgressUpdate(Long... updates) {
        long uploadedBytes = updates[0];
        long totalBytes = updates[1];
        service.setStatus(String.format("Uploaded %d/%d.", uploadedBytes, totalBytes));
        progress = (int) ((double) uploadedBytes / totalBytes * 100);
        service.setUploadProgress(progress);
        notificationBuilder.setProgress(100,progress,false);
        notificationManager.notify(1, notificationBuilder.build());
    }

    @Override
    protected URL doInBackground(Void... params) {
        try {
            if(!NetworkManager.isOnline(service)){
                service.checkInternetPeriodically();
            }

            TusUploader uploader = client.resumeOrCreateUpload(upload);
            long totalBytes = upload.getSize();
            long uploadedBytes = uploader.getOffset();

            // Upload file in 1MiB chunks
            uploader.setChunkSize(1024 * 1024);
            service.countErrors = 0;
            while(!isCancelled() && uploader.uploadChunk() > 0) {
                uploadedBytes = uploader.getOffset();
                publishProgress(uploadedBytes, totalBytes);
            }

            uploader.finish();
            return uploader.getUploadURL();

        } catch(Exception e) {
            exception = e;
            if(service.countErrors > 3) {
                service.cancellationReason = 1;
                cancel(true);
                return null;
            }
            service.countErrors+=1;
            service.checkInternetPeriodically();
        }
        return null;
    }

    public void updateNotification(String action) {
        switch (action) {
            case "pre-execute":
                actionIntent.putExtra("action","cancel");
                PendingIntent cancelPendingIntent = PendingIntent.getService(service,33,actionIntent,PendingIntent.FLAG_CANCEL_CURRENT);

                notificationBuilder.setProgress(100,0,false)
                        .setContentTitle("Uploading")
                        .setContentText("Estimated 2 minutes remaining")
                        .clearActions()
                        .addAction(R.mipmap.ic_launcher,"Cancel",cancelPendingIntent);
                notificationManager.notify(1, notificationBuilder.build());
                break;
            case "pause":
                List<PendingIntent> listPendInt = getPendingIntents();
                notificationBuilder.setContentTitle("Paused")
                        .setContentText("Waiting for a better connection")
                        .clearActions()
                        .addAction(R.mipmap.ic_launcher,"TRY NOW",listPendInt.get(0))
                        .addAction(R.mipmap.ic_launcher,"LATER",listPendInt.get(1))
                        .setProgress(100,progress,true);
                notificationManager.notify(1, notificationBuilder.build());
                break;
            case "fail":
                List<PendingIntent> listPenInt = getPendingIntents();
                notificationBuilder.setContentTitle("Uploading failed")
                        .setContentText(service.failureMessages[service.cancellationReason])
                        .clearActions()
                        .addAction(R.mipmap.ic_launcher,"RETRY",listPenInt.get(0))
                        .addAction(R.mipmap.ic_launcher,"LATER",listPenInt.get(1))
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
                service.stopForeground(false);
                break;
        }
    }
    
    private List<PendingIntent> getPendingIntents(){
        actionIntent.putExtra("action","retry");
        PendingIntent pendingIntent = PendingIntent.getService(service,44,actionIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent actionIntent2 = actionIntent;
        actionIntent2.putExtra("action","later");
        PendingIntent pendingIntent2 = PendingIntent.getService(service,55,actionIntent2,PendingIntent.FLAG_CANCEL_CURRENT);
        List<PendingIntent> list = new ArrayList<>();
        list.add(pendingIntent);
        list.add(pendingIntent2);
        return list;
    }
}
