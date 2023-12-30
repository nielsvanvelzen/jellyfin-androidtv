package org.jellyfin.playback.jellyfin

import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.jellyfin.queue.AudioAlbumQueue
import org.jellyfin.playback.jellyfin.queue.AudioInstantMixQueue
import org.jellyfin.playback.jellyfin.queue.AudioTrackQueue
import org.jellyfin.playback.jellyfin.queue.EpisodeQueue
import org.jellyfin.playback.jellyfin.queue.MovieQueue
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind

class BaseItemQueueManager(
	private val api: ApiClient,
	private val playbackManager: PlaybackManager,
) {
	fun canPlayItem(item: BaseItemDto) = canPlayItem(item.type)
	fun canPlayItem(type: BaseItemKind) = type in arrayOf(
		BaseItemKind.MUSIC_ALBUM,
		BaseItemKind.MUSIC_ARTIST,
		BaseItemKind.AUDIO,
	)

	suspend fun play(item: BaseItemDto): Boolean {
		val queue = when (item.type) {
			BaseItemKind.MUSIC_ALBUM -> AudioAlbumQueue(item, api)
			BaseItemKind.MUSIC_ARTIST -> AudioInstantMixQueue(item, api)
			BaseItemKind.AUDIO -> {
				if (item.albumId != null) {
					val album by api.userLibraryApi.getItem(itemId = item.albumId!!)
					if ((album.childCount ?: 0) >= 1) return play(album)
				}

				AudioTrackQueue(item, api)
			}

			BaseItemKind.SERIES -> null
			BaseItemKind.SEASON -> null
			BaseItemKind.MOVIE -> MovieQueue(item, api)
			BaseItemKind.EPISODE -> EpisodeQueue(item, api)

			else -> null
		} ?: return false

		playbackManager.state.play(queue)
		return true
	}
}
