package com.example.exampleproject.ui.uploads;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.exampleproject.R;
import com.example.exampleproject.utils.NetworkManager;

import java.net.URL;

import io.tus.android.client.TusAndroidUpload;
import io.tus.android.client.TusPreferencesURLStore;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUpload;

public class UploadService extends Service {
    public UploadsFragment activity;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private final String api = "https://tusd.tusdemo.net/files/";
    private TusClient client;
    private UploadTask uploadTask;
    private Uri fileUri;
    private NotificationCompat.Builder notificationBuilder;
    public static UploadService uploadService;
    private NotificationManager manager;
    private final int retryTimePeriod = 60; // in seconds
    private final int retryTimeInterval = 2; // in seconds
    public int countErrors = 0;
    private boolean isRetrying = false;
    public final String[] failureMessages= {"User cancelled","Something went wrong","Please try again with a better connection"};
    public int cancellationReason = 2;
    @Override
    public void onCreate() {
        super.onCreate();
        uploadService = this;
        activity = UploadsFragment.uploadsFragment;
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        initTusClient();
    }

    private void initTusClient(){
        try {
            SharedPreferences pref = getSharedPreferences("tus", 0);
            client = new TusClient();
            client.setUploadCreationURL(new URL(api));
            client.enableResuming(new TusPreferencesURLStore(pref));
        } catch(Exception e) {
            showError(e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle bundle = intent.getExtras();
        String action = bundle.getString("action");
        Log.d("UPLOAD_SERVICE", "onStartCommand: " +action);

        switch (action){
            case "cancel":
                cancellationReason = 0;
                cancelUpload();
                break;
            case "retry":
                SharedPreferences sharedPreferences = getSharedPreferences("Uri",MODE_PRIVATE);
                String string = sharedPreferences.getString("fileUri","");
                Log.d("UPLOAD_SERVICE", "RETRY "+string);
                cancellationReason = 2;
                isRetrying = true;
                resumeUpload(string);
                break;
            case "upload":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel();
                }
                String uriString = bundle.getString("fileUri");
                Intent notificationIntent = new Intent(this, UploadsFragment.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        0, notificationIntent, 0);
                notificationBuilder = createNotification();
                startForeground(1, notificationBuilder.build());
                resumeUpload(uriString);
                break;
            default:
                cancellationReason = 0;
                cancelUpload();
                return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(){
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Foreground Notification", NotificationManager.IMPORTANCE_NONE);
        manager.createNotificationChannel(channel);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private NotificationCompat.Builder createNotification() {
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setContentTitle("Starting upload...")
              .setOnlyAlertOnce(true)
              .setProgress(100, 0, true);
      return builder;
    }

    void showError(Exception e) {
        if(UploadsFragment.uploadsFragment.getContext() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UploadsFragment.uploadsFragment.getContext());
            builder.setTitle("Internal error");
            builder.setMessage(e.getMessage());
            AlertDialog dialog = builder.create();
            Log.d("ACTIVITY", "showError:"+activity.toString());
            dialog.show();
            e.printStackTrace();
        }
    }

    public void resumeUpload(String uriString) {
        Log.d("UPLOAD_SERVICE", "resumeUpload: " + uriString);
        if(uriString.equalsIgnoreCase("")){
            Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show();
        }
        fileUri = Uri.parse(uriString);
        try {
            if(client == null || notificationBuilder == null) {
                initTusClient();
                notificationBuilder = createNotification();
            }
            TusUpload upload = new TusAndroidUpload(fileUri, this);
            uploadTask = new UploadTask(this, client, upload, notificationBuilder);
            uploadTask.execute(new Void[0]);
        } catch (Exception e) {
           stopForeground(true);
           showError(e);
        }
    }

    void setStatus(String text) {
        if(activity != null) activity.status.setText(text);
    }

    void setUploadProgress(int progress) {
        if(activity != null) activity.progressBar.setProgress(progress);
    }

    public void setPauseButtonEnabled(boolean enabled) {
        if(activity != null) {
            activity.pauseButton.setEnabled(enabled);
            activity.resumeButton.setEnabled(!enabled);
        }
    }

    public void setResumeButtonEnabled(boolean enabled) {
        if(activity != null) {
            activity.resumeButton.setEnabled(enabled);
        }
    }
    public void pauseUpload() {
        notificationBuilder.setProgress(100,0,true);
        manager.notify(1,notificationBuilder.build());
        uploadTask.cancel(false);
    }
    public void cancelUpload() {
        uploadTask.cancel(true);
        stopForeground(true);
    }

    public void checkInternetPeriodically(){
        uploadTask.updateNotification("pause");
        isRetrying = false;
        int i = 0;
        while (i<retryTimePeriod && !isRetrying && cancellationReason != 0) {
            Log.d("NETWORK_MANAGER", "retryUpload: "+ NetworkManager.isOnline(this) + ","+NetworkManager.getUploadSpeed(this));
            i += retryTimeInterval;
            if(NetworkManager.isOnline(this) && NetworkManager.getUploadSpeed(this)>50) {
                Intent intent = new Intent(this,UploadService.class);
                intent.putExtra("action","retry");
                startService(intent);
                break;
            } else {
                try {
                    Thread.sleep(retryTimeInterval * 1000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                    continue;
                }
            }
        }
        if(i>=retryTimePeriod) {
            cancellationReason = 2;
            uploadTask.cancel(true);
        }
    }
}
