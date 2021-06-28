package com.example.exampleproject.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel {

    private String mText;

    public DashboardViewModel() {
        mText = "This is dashboard fragment";
    }

    public String getText() {
        return mText;
    }
}