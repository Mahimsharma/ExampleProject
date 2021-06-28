package com.example.exampleproject.ui.uploads;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.exampleproject.R;

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
    private int progress=0;
    private NotificationManagerCompat notificationManager;
    private Intent actionIntent;
    private boolean hasUploadFailed = false;

    public UploadTask(UploadService activity, TusClient client, TusUpload upload, NotificationCompat.Builder builder) {
        this.activity = activity;
        this.client = client;
        this.upload = upload;
        this.notificationBuilder = builder;
        notificationManager = NotificationManagerCompat.from(activity);
        actionIntent = new Intent(activity,UploadService.class);
    }

    @Override
    protected void onPreExecute() {
        activity.setStatus("Upload selected...");
        activity.setPauseButtonEnabled(true);
        activity.setUploadProgress(0);

        actionIntent.putExtra("action","pause");
        PendingIntent cancelPendingIntent = PendingIntent.getService(activity,3,actionIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        notificationBuilder.setProgress(100,0,false)
                .setContentText("Uploading")
                .clearActions()
                .addAction(R.mipmap.ic_launcher,"Cancel",cancelPendingIntent);
        notificationManager.notify(1, notificationBuilder.build());
    }

    @Override
    protected void onPostExecute(URL uploadURL) {
        activity.setStatus("Upload finished!\n" + uploadURL.toString());
        activity.setPauseButtonEnabled(false);
        activity.setResumeButtonEnabled(false);
        notificationBuilder.setProgress(100,100,false)
                .clearActions()
                .setContentText("Uploading finished")
                .setOngoing(false);
        notificationManager.notify(1, notificationBuilder.build());
        activity.stopForeground(false);
        }

    @Override
    protected void onCancelled() {
        if(exception != null && activity != null) {
            activity.showError(exception);
            activity.setPauseButtonEnabled(false);
        }
        actionIntent.putExtra("action","retry");
        PendingIntent pendingIntent = PendingIntent.getService(activity,4,actionIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent actionIntent2 = actionIntent;
        actionIntent2.putExtra("action","later");
        PendingIntent pendingIntent2 = PendingIntent.getService(activity,3,actionIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        notificationBuilder.clearActions()
                .addAction(R.mipmap.ic_launcher,"RETRY",pendingIntent)
                .addAction(R.mipmap.ic_launcher,"LATER",pendingIntent2);
        if(hasUploadFailed) notificationBuilder.setContentText("Upload Failed");
        else notificationBuilder.setContentText("Upload Cancelled");
        notificationManager.notify(1, notificationBuilder.build());
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
            notificationBuilder.setOngoing(false)
                    .setContentText("Something went wrong");
            notificationManager.notify(1, notificationBuilder.build());
            cancel(true);
        }
        return null;
    }
}
