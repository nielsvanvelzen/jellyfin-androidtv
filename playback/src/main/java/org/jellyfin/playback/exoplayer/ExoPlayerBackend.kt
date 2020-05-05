package org.jellyfin.playback.exoplayer

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import org.jellyfin.playback.backend.Backend
import org.jellyfin.playback.queue.QueueItem


class ExoPlayerBackend(
	private val context: Context
) : Backend {
	private val exoPlayer by lazy {
		SimpleExoPlayer.Builder(context)
			.build()
	}

	override fun setSurface(surfaceView: SurfaceView) {
		exoPlayer.videoComponent?.setVideoSurfaceView(surfaceView)
	}

	override fun playItem(item: QueueItem) {
		val dataSourceFactory = DefaultDataSourceFactory(context, "Jellyfin")
		val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
			.createMediaSource(Uri.parse(item.url))

		exoPlayer.playWhenReady = true
		exoPlayer.prepare(videoSource)
	}
}
