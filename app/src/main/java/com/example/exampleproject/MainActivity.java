package com.example.exampleproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.banuba.example.integrationapp.videoeditor.IntegrationAppExportVideoContract;
import com.banuba.sdk.ve.flow.VideoCreationActivity;
import com.banuba.sdk.veui.ui.ExportResult;

public class MainActivity extends AppCompatActivity {
    public ActivityResultLauncher createVideoRequest;
    Button button;
    static IntegrationAppExportVideoContract integrationAppExportVideoContract = new IntegrationAppExportVideoContract();
    ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createVideoRequest =  this.registerForActivityResult(integrationAppExportVideoContract,this::onResult);
        button = findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick();
            }
        });
    }

    public void onButtonClick(){
        Log.d("CLICK", "onButtonClick: ");
        Intent videoCreationIntent =  VideoCreationActivity.buildIntent(this,null,null);
        createVideoRequest.launch(videoCreationIntent);
    }
    public void onResult(ExportResult exportResult){
        Log.d("video exported", "onResult: "+ exportResult.toString());
    }
}
