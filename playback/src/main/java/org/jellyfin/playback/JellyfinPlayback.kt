package org.jellyfin.playback

import android.content.Context
import org.jellyfin.playback.backend.Backend
import org.jellyfin.playback.exoplayer.ExoPlayerBackend
import org.jellyfin.playback.queue.Queue
import org.jellyfin.playback.view.PlayerView

class JellyfinPlayback(context: Context) {
	private val _playerViews = mutableSetOf<PlayerView>()
	private var _queue: Queue? = null

	private val backend: Backend = ExoPlayerBackend(context)

	fun attachView(view: PlayerView) {
		_playerViews += view
		backend.setSurface(view)
	}

	fun detachView(view: PlayerView) {
		_playerViews -= view
	}

	fun playQueue(queue: Queue) {
		_queue = queue

		//TODO: Create player etc.
		val item = queue.firstOrNull()

		if (item != null)
			backend.playItem(item)
	}

	fun getQueue(): Queue? = _queue
}
