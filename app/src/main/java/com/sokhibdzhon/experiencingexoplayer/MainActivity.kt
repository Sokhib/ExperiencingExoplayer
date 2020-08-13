package com.sokhibdzhon.experiencingexoplayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

// http://cdn.odece.xyz/1.php // check this with every type...
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4


class MainActivity : AppCompatActivity() {

    companion object {
        private const val userAgent = "exoplayer-data-factory"
    }

    private lateinit var exoListener: Player.EventListener
    private var player: SimpleExoPlayer? = null
    private var playWhenReadyFlag = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        exoListener = object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                val state = when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        "ExoPlayer.STATE_BUFFERING"
                    }
                    Player.STATE_ENDED -> {
                        "ExoPlayer.STATE_ENDED"
                    }
                    Player.STATE_IDLE -> {
                        "ExoPlayer.STATE_IDLE"
                    }
                    Player.STATE_READY -> {
                        "ExoPlayer.STATE_READY"
                    }
                    else -> "ExoPlayer.STATE_UNKNOWN"
                }
                Log.d("MAIN", "onPlayerStateChanged: $state playWhenReady $playWhenReady ")
            }
        }
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
        player!!.addListener(exoListener)
        val uri =
            Uri.parse(getString(R.string.media_url_aegis))
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
        playerView.player = player
    }

    private fun buildMediaSource(uri: Uri, type: Int): ProgressiveMediaSource? {
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
        return ProgressiveMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
            .createMediaSource(uri)
    }

    private fun releasePlayer() {
        player?.let { it ->
            playWhenReadyFlag = it.playWhenReady
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            it.release()
            it.removeListener(exoListener)
            player = null
        }

    }
}