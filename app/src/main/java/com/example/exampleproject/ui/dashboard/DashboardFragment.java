package com.example.exampleproject.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.exampleproject.R;
import com.example.exampleproject.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    private ViewPager2 viewPager;
    String[] links ={"https://web.law.duke.edu/cspd/contest/videos/Framed-Contest_Documentaries-and-You.mp4","https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4"};
    private Context context;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = this.getContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("Uri", Context.MODE_PRIVATE);
        String[] urlList = sharedPreferences.getString("urlList","").split(",");
        Log.d("UrlList", "onCreate: "+ Arrays.toString(urlList));

        if(urlList.length>1) links = urlList;
        Log.d("LINKS", "onCreate: "+ Arrays.toString(links));

        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        viewPager = root.findViewById(R.id.viewPager);

        ArrayList<VideoModel> list = new ArrayList<>();
        for(int i = 0;i<links.length;i++){
            list.add(new VideoModel(links[i],"title"+i, "Description"+i));
        }

        VideoAdapter adapter = new VideoAdapter(list);
        viewPager.setAdapter(adapter);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}