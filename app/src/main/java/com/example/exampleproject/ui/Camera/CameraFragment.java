package com.example.exampleproject.ui.Camera;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.banuba.example.integrationapp.videoeditor.IntegrationAppExportVideoContract;
import com.banuba.sdk.ve.flow.VideoCreationActivity;
import com.banuba.sdk.veui.ui.ExportResult;
import com.example.exampleproject.MainActivity;
import com.example.exampleproject.databinding.FragmentHomeBinding;

public class CameraFragment extends Fragment {

    private CameraViewModel cameraViewModel;
    private FragmentHomeBinding binding;
    private Button button;
    public ActivityResultLauncher createVideoRequest;
    static IntegrationAppExportVideoContract integrationAppExportVideoContract = new IntegrationAppExportVideoContract();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cameraViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);
        createVideoRequest =  registerForActivityResult(integrationAppExportVideoContract,MainActivity::onResult);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        button = binding.button1;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    public void onButtonClick(){
        Log.d("CLICK", "onButtonClick: ");
        Intent videoCreationIntent =  VideoCreationActivity.buildIntent(getContext(),null,null);
        createVideoRequest.launch(videoCreationIntent);
    }
    public void onResult(ExportResult exportResult){
        Log.d("video exported", "onResult: "+ exportResult.toString());
    }
}