package com.example.exoplayer_android_demo

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.*

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: SimpleExoPlayer
    private lateinit var dataSourceFactory: DataSource.Factory
    private val videoUrl =
        "https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_400,640x360_700,640x360_1000,950x540_1500,.f4v.csmil/master.m3u8"
    private var progressBar: ProgressBar? = null
    private lateinit var playerView: PlayerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initializeComponent()
        initialize(videoUrl)
    }

    private fun initializeComponent() {
        progressBar = findViewById(R.id.progress_bar)
        playerView = findViewById(R.id.simpleExoPlayerView)
    }

    private fun initialize(videoUrl: String) {
        val mp4VideoUri = Uri.parse(Objects.requireNonNull(videoUrl))
        player = SimpleExoPlayer.Builder(this).build()
        playerView.useController = true
        playerView.requestFocus()
        playerView.player = player
        dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"))
        val contentMediaSource = buildMediaSource(mp4VideoUri)
        val loopingSource = LoopingMediaSource(contentMediaSource)
        player.prepare()
        player.setMediaSource(loopingSource)
        player.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    progressBar!!.visibility = View.VISIBLE
                } else {
                    progressBar!!.visibility = View.INVISIBLE
                }
                playerView.keepScreenOn =
                    playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED && playWhenReady
            }

            override fun onPlayerError(error: PlaybackException) {
                player.stop()
                player.prepare()
                player.setMediaSource(loopingSource)
                player.playWhenReady = true
            }
        })
        player.playWhenReady = true
        val layout = findViewById<RelativeLayout>(R.id.exoplayerLayout)
        val layoutDescription = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutDescription.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        layout.layoutParams = layoutDescription
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        @C.ContentType val type = Util.inferContentType(uri)
        return when (type) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }
}