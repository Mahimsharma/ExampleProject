package com.example.exampleproject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    ArrayList<VideoModel> listVideos;
    public VideoAdapter(ArrayList<VideoModel> listVideos){
        this.listVideos = listVideos;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_card,parent,false);
        return new VideoViewHolder(parent.getContext(),view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.VideoViewHolder holder, int position) {
        holder.setView(listVideos.get(position));
    }

    @Override
    public int getItemCount() {
        return listVideos.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder{
        TextView titleText;
        TextView descText;
        ProgressBar progressBar;
        SimpleExoPlayer exoPlayer;
        Context context;
        PlayerView exoPlayerView;

        public VideoViewHolder(Context context,@NonNull View itemView) {
            super(itemView);
            exoPlayerView = itemView.findViewById(R.id.videoView);
            titleText = itemView.findViewById(R.id.titleText);
            descText = itemView.findViewById(R.id.descText);
            progressBar = itemView.findViewById(R.id.progressBar);
            this.context = context;
        }

        void setView(VideoModel model){
            titleText.setText(model.getTitle());
            descText.setText(model.getDesc());

            try {
                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
                Uri videoUri = Uri.parse(model.getUrl());
                String userAgent = Util.getUserAgent(context, "ExampleProject");
                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,userAgent);
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri);
                exoPlayerView.setPlayer(exoPlayer);
                exoPlayer.prepare(mediaSource);
                exoPlayer.setPlayWhenReady(true);
            } catch (Exception e) {
                Log.e("TAG", "Error : " + e.toString());
            }
        }
    }
}
