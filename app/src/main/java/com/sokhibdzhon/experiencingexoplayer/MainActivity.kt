package com.sokhibdzhon.experiencingexoplayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.BaseMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

// http://cdn.odece.xyz/1.php
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4

//TODO: set playbackPosition and currentWindow to continue on Resume. get via savedInstance?

class MainActivity : AppCompatActivity() {

    companion object {
        private const val userAgent = "exoplayer-data-factory"
        private const val videoURL =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        private const val dashVideoURL =
            "<![CDATA[https://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0]]>"
    }

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
        if (player == null) {
            /*
            * which is responsible for choosing tracks in the media source.
            * Then, tell your trackSelector to only pick tracks of standard definition
            * or lower—a good way of saving your user's data at the expense of quality
            * */
            val trackSelector = DefaultTrackSelector(this)
            trackSelector.parameters =
                trackSelector.buildUponParameters().setMaxVideoSizeSd().build()
            player = SimpleExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build()
        }
        playerView.player = player
        val uri =
            Uri.parse(dashVideoURL)
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

    private fun buildMediaSource(uri: Uri, type: Int): BaseMediaSource? {
        val mediaSource = when (type) {
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
        return mediaSource
        //return ConcatenatingMediaSource(mediaSource, mediaSource)
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