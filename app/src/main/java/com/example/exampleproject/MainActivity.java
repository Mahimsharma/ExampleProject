package com.example.exampleproject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.banuba.example.integrationapp.videoeditor.IntegrationAppExportVideoContract;
import com.banuba.sdk.ve.flow.VideoCreationActivity;
import com.banuba.sdk.veui.ui.ExportResult;
import com.example.exampleproject.databinding.ActivityMainBinding;
import com.example.exampleproject.ui.uploads.UploadsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.FileOutputStream;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public ActivityResultLauncher createVideoRequest;
    static IntegrationAppExportVideoContract integrationAppExportVideoContract = new IntegrationAppExportVideoContract();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = this.getSharedPreferences("Uri",MODE_PRIVATE);
        sharedPreferences.edit().putString("urlList", "").apply();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        createVideoRequest =  registerForActivityResult(integrationAppExportVideoContract,this::onResult);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main2);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    public void onButtonClick(){
        Log.d("CLICK", "onButtonClick: ");
        Intent videoCreationIntent =  VideoCreationActivity.buildIntent(this,null,null);
        createVideoRequest.launch(videoCreationIntent);
    }
    private void onResult(ExportResult exportResult){
        String uriString = exportResult.toString();
        if(!uriString.contains("Success")) {
            return;
        }
        int start = uriString.indexOf("fileUri");
        int end = uriString.indexOf(',',start);
        String uri = uriString.substring(start,end).replace("fileUri=","file://");
        saveVideo(uri);
        Log.d("video exported", "onResult: "+ uriString);
    }

    public void uploadVideo(Uri uri){
        if(UploadsFragment.uploadsFragment == null) {
            Toast.makeText(this, "Null Upload Fragment", Toast.LENGTH_SHORT).show();
            return;
        }
        UploadsFragment.uploadsFragment.beginUpload(uri);
    }
    public void saveVideo(String uri){
        Toast.makeText(this,"saving video",Toast.LENGTH_SHORT).show();
        String videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        ContentValues valuesVideos;
        valuesVideos = new ContentValues();
        valuesVideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Foxy");
        valuesVideos.put(MediaStore.Video.Media.TITLE, videoFileName);
        valuesVideos.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName);
        valuesVideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        valuesVideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesVideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        valuesVideos.put(MediaStore.Video.Media.IS_PENDING, 1);
        ContentResolver resolver = this.getContentResolver();
        Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri uriSavedVideo = resolver.insert(collection, valuesVideos);

        ParcelFileDescriptor pfd;

        try {
            pfd = this.getContentResolver().openFileDescriptor(uriSavedVideo, "w");
            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
            InputStream in = getContentResolver().openInputStream(Uri.parse(uri));

            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {

                out.write(buf, 0, len);
            }

            in.close();
            out.close();
            pfd.close();

        } catch (Exception e) {
            Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show();
            Log.e("EXPORT", "saveVideo: ",e);
            e.printStackTrace();
        }
        Toast.makeText(this,"Completed process",Toast.LENGTH_SHORT).show();

        valuesVideos.clear();
        valuesVideos.put(MediaStore.Video.Media.IS_PENDING, 0);
        this.getContentResolver().update(uriSavedVideo, valuesVideos, null, null);
        uploadVideo(uriSavedVideo);
    }
}