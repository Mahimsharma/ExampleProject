package com.example.exampleproject.ui.Camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.exampleproject.MainActivity;
import com.example.exampleproject.databinding.FragmentHomeBinding;

public class CameraFragment extends Fragment {

    private CameraViewModel cameraViewModel;
    private FragmentHomeBinding binding;
    private Button button;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cameraViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        button = binding.button1;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               ((MainActivity)getActivity()).onButtonClick();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}