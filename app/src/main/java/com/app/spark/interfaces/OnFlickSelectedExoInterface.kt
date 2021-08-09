package com.app.spark.interfaces

import com.app.spark.models.GetFlickResponse
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

interface OnFlickSelectedExoInterface {

    fun sendExoPlayer(exoPlayer: SimpleExoPlayer, position: Int, playerView: PlayerView)
}