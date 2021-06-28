package com.example.exampleproject.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private String mText;

    public HomeViewModel() {
        mText = "This is home fragment";
    }

    public String getText() {
        return mText;
    }
}