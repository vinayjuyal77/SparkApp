package com.app.spark.fragment.newflick;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.spark.R;
import com.app.spark.models.GetFlickResponse;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FlickNewJavaAdapter  extends RecyclerView.Adapter<FlickNewJavaAdapter.ViewHolder> {


    Context context;
    ArrayList<GetFlickResponse.Result> list;
    int global_position = 0;
    SimpleExoPlayer exoPlayer;

    public FlickNewJavaAdapter( Context context , ArrayList<GetFlickResponse.Result> list) {
        this.context = context;
        this.list = list;

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flicks, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {

        exoPlayer.setPlayWhenReady(true);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getString(R.string.app_name))
        );


        // This is the MediaSource representing the media to be played.
        MediaSource videoSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory())
                        .createMediaSource(Uri.parse(list.get(position).getFlickMedia()));
        // Prepare the player with the source.
        exoPlayer.prepare(videoSource, true, false);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
         PlayerView playerView;

//        public MyViewHolder(View view) {
//            super(view);
//            title = (TextView) view.findViewById(R.id.title);
//            genre = (TextView) view.findViewById(R.id.genre);
//            year = (TextView) view.findViewById(R.id.year);
//        }

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            playerView = itemView.findViewById(R.id.pvVideo);

            if(exoPlayer!=null) {
              //  exoPlayer = new SimpleExoPlayer.Builder(context).build();
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                exoPlayer.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                playerView.setPlayer(exoPlayer);
                exoPlayer.stop();
                exoPlayer.release();
            }
            exoPlayer = new SimpleExoPlayer.Builder(context).build();
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            exoPlayer.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            playerView.setPlayer(exoPlayer);





        }
    }
}
