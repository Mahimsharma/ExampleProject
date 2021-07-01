package com.example.exampleproject.ui.uploads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.exampleproject.databinding.FragmentUploadsBinding;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.startForegroundService;

public class UploadsFragment extends Fragment {

    private final int REQUEST_FILE_SELECT = 1;
    private final String guestToken = "f22578f0-d589-11eb-a0d3-1f8408c16c5f";
    private final String authToken= "634593d7-2bd5-463c-9789-a54e29eb1a58";
    public TextView status;
    public Button pauseButton;
    public Button resumeButton;
    public ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private UploadsViewModel uploadsViewModel;
    private FragmentUploadsBinding binding;
    private Intent readFileIntent;
    public static UploadsFragment uploadsFragment;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        uploadsFragment = this;
        uploadsViewModel =
                new ViewModelProvider(this).get(UploadsViewModel.class);

        binding = FragmentUploadsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button button = binding.button;
        status = binding.status;
        pauseButton = binding.pauseButton;
        resumeButton = binding.resumeButton;
        progressBar = binding.progressBar;
        sharedPreferences = getActivity().getSharedPreferences("Uri",MODE_PRIVATE);

        readFileIntent= new Intent();
        readFileIntent.setType("video/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            readFileIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            readFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            Log.d("FILE_PERMISSION", "Asking for persistable uri");
        }
        else readFileIntent.setAction(Intent.ACTION_GET_CONTENT);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intent.createChooser(readFileIntent, "Select file to upload"), REQUEST_FILE_SELECT);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UploadService.uploadService != null){
                    UploadService.uploadService.pauseUpload();
                }
            }
        });
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uriString = sharedPreferences.getString("fileUri","");
                if(uriString.equalsIgnoreCase("")) {
                    Toast.makeText(getContext(),"No path found please select a valid path",Toast.LENGTH_LONG).show();
                    return;
                }
                beginUpload(Uri.parse(uriString));
            }
        };

        resumeButton.setOnClickListener(onClickListener);

        return root;
    }

    private void beginUpload(Uri uri) {
        Intent uploadIntent = new Intent(getContext(),UploadService.class);
        uploadIntent.putExtra("action","upload");
        uploadIntent.putExtra("fileUri",uri.toString());
        startForegroundService(getContext(),uploadIntent);
        Log.d("MAIN_ACTIVITY", "Starting service.. ");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_FILE_SELECT) {
            Uri uri = data.getData();
            getActivity().getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
            saveUploadUri(uri.toString());
            beginUpload(uri);
        }
    }

    private void saveUploadUri(String uri){
        sharedPreferences.edit().putString("fileUri", uri).apply();
    }

    private void makePostMediaCall() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.foxy.in/api/v1/media")
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/json")
                .addHeader("x-auth-tokem", authToken)
                .addHeader("x-guest-token", guestToken)
                .build();

        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}