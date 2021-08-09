package com.app.spark.interfaces

import com.app.spark.models.GetFlickResponse
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

interface OnFeedSelectedInterface {
    fun onPlayPause(isPlay:Int)
    fun showPostMenu(
        userId: String,
        postId: String,
        name: String,
        profilePic: String?,
        item: String
    )
    fun onLike(postId:String,isLike:Boolean)
    fun onFlickListFollow(item: GetFlickResponse.Result, data: String)

    fun sendExoPlayer(list : ArrayList<SimpleExoPlayer>  , exoPlayer: SimpleExoPlayer, position: Int, playerView: PlayerView)
}