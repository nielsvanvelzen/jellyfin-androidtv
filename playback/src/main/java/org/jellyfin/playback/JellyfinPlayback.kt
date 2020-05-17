package org.jellyfin.playback

import android.content.Context
import org.jellyfin.playback.backend.Backend
import org.jellyfin.playback.exoplayer.ExoPlayerBackend
import org.jellyfin.playback.queue.Queue
import org.jellyfin.playback.remote.MediaSessionRemote
import org.jellyfin.playback.remote.Remote
import org.jellyfin.playback.view.PlayerView

class JellyfinPlayback(
	private val context: Context
) {
	private val _playerViews = mutableSetOf<PlayerView>()
	private var _queue: Queue? = null

	private val backend: Backend = ExoPlayerBackend(context)
	private val remotes = mutableSetOf<Remote>()

	init {
		addRemote(MediaSessionRemote(context))
	}

	fun addRemote(remote: Remote) {
		remotes += remote
	}

	fun removeRemote(remote: Remote) {
		remotes -= remote
	}

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

