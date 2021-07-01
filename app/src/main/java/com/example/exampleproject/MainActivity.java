package com.example.exampleproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.banuba.example.integrationapp.videoeditor.IntegrationAppExportVideoContract;
import com.banuba.sdk.ve.flow.VideoCreationActivity;
import com.banuba.sdk.veui.ui.ExportResult;
import com.example.exampleproject.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


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
        Log.d("video exported", "onResult: "+ exportResult.toString());
    }
}