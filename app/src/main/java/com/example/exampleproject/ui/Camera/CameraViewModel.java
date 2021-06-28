package com.example.exampleproject.ui.Camera;

import androidx.lifecycle.ViewModel;

public class CameraViewModel extends ViewModel {

    private String mText;

    public CameraViewModel() {
        mText = "This is home fragment";
    }

    public String getText() {
        return mText;
    }
}