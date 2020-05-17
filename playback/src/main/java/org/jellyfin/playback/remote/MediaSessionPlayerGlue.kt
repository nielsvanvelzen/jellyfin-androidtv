package org.jellyfin.playback.remote

import androidx.media.AudioAttributesCompat
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import com.google.common.util.concurrent.ListenableFuture

class MediaSessionPlayerGlue(
	private val remote: MediaSessionRemote
) : SessionPlayer() {
	override fun getCurrentMediaItem(): MediaItem? {
		val metadata = MediaMetadata.Builder().apply {
			// Modern
			putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, "METADATA_KEY_DISPLAY_TITLE")
			putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, "METADATA_KEY_DISPLAY_SUBTITLE")
			putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, "https://i.redd.it/uybguvnj1p821.png")

			// And at minimum the title and artist for legacy support
			putString(MediaMetadata.METADATA_KEY_TITLE, "METADATA_KEY_TITLE")
			putString(MediaMetadata.METADATA_KEY_ARTIST, "METADATA_KEY_ARTIST")
			// Recommended
//			.putBitmap(MediaMetadata.METADATA_KEY_ART, myData.artBitmap)
		}

		return MediaItem.Builder()
			.setMetadata(metadata.build())
			.build()
	}

	override fun getDuration(): Long = UNKNOWN_TIME

	override fun setMediaItem(item: MediaItem): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun updatePlaylistMetadata(metadata: MediaMetadata?): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun setPlaybackSpeed(playbackSpeed: Float): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun getCurrentPosition(): Long = UNKNOWN_TIME

	override fun getPlaylist(): MutableList<MediaItem> = mutableListOf(currentMediaItem!!)

	override fun play(): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun skipToPreviousPlaylistItem(): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun getShuffleMode(): Int = SHUFFLE_MODE_NONE

	override fun close() {
		TODO("Not yet implemented")
	}

	override fun getRepeatMode(): Int = REPEAT_MODE_NONE

	override fun getPlayerState(): Int = PLAYER_STATE_PLAYING

	override fun setPlaylist(list: MutableList<MediaItem>, metadata: MediaMetadata?): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun getPlaybackSpeed(): Float = 1f

	override fun setShuffleMode(shuffleMode: Int): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun skipToNextPlaylistItem(): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun getBufferedPosition(): Long = UNKNOWN_TIME

	override fun replacePlaylistItem(index: Int, item: MediaItem): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun getNextMediaItemIndex(): Int = 1

	override fun addPlaylistItem(index: Int, item: MediaItem): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun seekTo(position: Long): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun getBufferingState(): Int = BUFFERING_STATE_UNKNOWN

	override fun removePlaylistItem(index: Int): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun setRepeatMode(repeatMode: Int): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun skipToPlaylistItem(index: Int): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun prepare(): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun pause(): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun getPlaylistMetadata(): MediaMetadata? = MediaMetadata.Builder().build()

	override fun getPreviousMediaItemIndex(): Int = 0

	override fun setAudioAttributes(attributes: AudioAttributesCompat): ListenableFuture<PlayerResult> {
		TODO("Not yet implemented")
	}

	override fun getAudioAttributes(): AudioAttributesCompat? = AudioAttributesCompat.Builder().build()

	override fun getCurrentMediaItemIndex(): Int = 1
}
