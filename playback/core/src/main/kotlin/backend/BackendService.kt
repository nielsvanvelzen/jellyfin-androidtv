package org.jellyfin.playback.core.backend

import androidx.core.view.doOnDetach
import org.jellyfin.playback.core.mediastream.PlayableMediaStream
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.core.view.PlayerSurfaceView

/**
 * Service keeping track of the current playback backend and surface view.
 */
class BackendService {
	private var _backend: PlayerBackend? = null
	val backend get() = _backend

	private var listeners = mutableListOf<PlayerBackendEventListener>()

	private var _surfaceView: PlayerSurfaceView? = null
	val surfaceView get() = _surfaceView

	fun switchBackend(backend: PlayerBackend) {
		_backend?.stop()
		_backend?.setListener(null)
		_backend?.setSurface(null)

		_backend = backend.apply {
			surfaceView?.surface?.let(::setSurface)
			setListener(BackendEventListener())
		}
	}

	fun addListener(listener: PlayerBackendEventListener) {
		listeners.add(listener)
	}

	fun removeListener(listener: PlayerBackendEventListener) {
		listeners.remove(listener)
	}

	inner class BackendEventListener : PlayerBackendEventListener {
		private fun <T> callListeners(
			body: PlayerBackendEventListener.() -> T
		): List<T> = listeners.map { listener -> listener.body() }

		override fun onPlayStateChange(state: PlayState) {
			callListeners { onPlayStateChange(state) }
		}

		override fun onVideoSizeChange(width: Int, height: Int) {
			callListeners { onVideoSizeChange(width, height) }
		}

		override fun onMediaStreamEnd(mediaStream: PlayableMediaStream) {
			callListeners { onMediaStreamEnd(mediaStream) }
		}
	}

	fun attach(surfaceView: PlayerSurfaceView) {
		if (_surfaceView != null) throw IllegalStateException("A surface is already attached!")

		_surfaceView = surfaceView.apply {
			_backend?.setSurface(surface)

			// Automatically detach
			doOnDetach {
				detach(surfaceView)
			}
		}
	}

	private fun detach(surfaceView: PlayerSurfaceView) {
		if (surfaceView != _surfaceView) return

		_surfaceView = null
		_backend?.setSurface(null)
	}
}
