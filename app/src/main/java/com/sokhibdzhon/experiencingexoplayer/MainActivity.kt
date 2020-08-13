package com.sokhibdzhon.experiencingexoplayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

// http://cdn.odece.xyz/1.php
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4

//TODO: set playbackPosition and currentWindow to continue on Resume. get via savedInstance?

class MainActivity : AppCompatActivity() {
    private val userAgent = "exoplayer-data-factory"
    private var player: SimpleExoPlayer? = null
    private var playWhenReadyFlag = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24)
            initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }


    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player
        val uri =
            Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
        val type = Util.inferContentType(uri)
        Log.d("MAIN", "initializePlayer: $type ")
        val mediaSource = buildMediaSource(uri, type)
        player!!.apply {
            playWhenReady = playWhenReadyFlag
            seekTo(currentWindow, playbackPosition)
            if (mediaSource != null) {
                prepare(mediaSource, false, false)
            }
        }

    }

    private fun buildMediaSource(uri: Uri, type: Int) = when (type) {
        C.TYPE_DASH -> DashMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
            .createMediaSource(uri)
        C.TYPE_SS -> SsMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
            .createMediaSource(uri)
        C.TYPE_HLS -> HlsMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
            .createMediaSource(uri)
        C.TYPE_OTHER -> ProgressiveMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
            .createMediaSource(uri)
        else -> {
            throw  IllegalStateException("Unsupported type: $type")
        }
    }

    private fun releasePlayer() {
        player?.let { it ->
            playWhenReadyFlag = it.playWhenReady
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            it.release()
            player = null
        }

    }
}