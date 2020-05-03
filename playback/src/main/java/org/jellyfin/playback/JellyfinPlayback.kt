package org.jellyfin.playback

import org.jellyfin.playback.queue.Queue
import org.jellyfin.playback.view.PlayerView

class JellyfinPlayback {
	private val _playerViews = mutableSetOf<PlayerView>()
	private var _queue: Queue? = null

	fun attachView(view: PlayerView) {
		_playerViews += view
	}

	fun detachView(view: PlayerView) {
		_playerViews -= view
	}

	fun playQueue(queue: Queue) {
		_queue = queue

		//TODO: Create player etc.
	}

	fun getQueue(): Queue? = _queue
}
