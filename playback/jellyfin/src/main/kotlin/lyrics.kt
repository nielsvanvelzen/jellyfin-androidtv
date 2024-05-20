package org.jellyfin.playback.jellyfin

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jellyfin.playback.core.element.ElementKey
import org.jellyfin.playback.core.element.element
import org.jellyfin.playback.core.element.elementFlow
import org.jellyfin.playback.core.plugin.PlayerService
import org.jellyfin.playback.core.queue.QueueEntry
import org.jellyfin.playback.core.queue.queue
import org.jellyfin.playback.jellyfin.queue.baseItem
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.lyricsApi
import org.jellyfin.sdk.model.api.LyricDto
import org.jellyfin.sdk.model.api.LyricLine
import org.jellyfin.sdk.model.api.LyricMetadata
import org.jellyfin.sdk.model.extensions.inWholeTicks
import kotlin.time.Duration.Companion.seconds

private val lyricsKey = ElementKey<LyricDto>("LyricDto")

/**
 * Get or set the [LyricDto] for this [QueueEntry].
 */
var QueueEntry.lyrics by element(lyricsKey)
val QueueEntry.lyricsFlow by elementFlow(lyricsKey)

class LyricsPlayerService(
	private val api: ApiClient,
) : PlayerService() {
	override suspend fun onInitialize() {
		// TODO: Update playback architecture so we can add "systems" that automatically do magic like this
		//  function on certain events / items
		manager.queue.entry
//			.onEach { entry -> entry?.let { fetchLyrics(entry) } }
			.onEach { entry -> entry?.let { fetchFakeLyrics(entry) } }
			.launchIn(coroutineScope)
	}

	private suspend fun fetchFakeLyrics(entry: QueueEntry) {
		// Already has lyrics!
		if (entry.lyrics != null) return

		// Simulate slow HTTP response time
		delay(2.5.seconds)

		// Set lyrics
		entry.lyrics = LyricDto(
			metadata = LyricMetadata(),
			lyrics = listOf(
				LyricLine("Line at 00:00", 0),
				LyricLine("Line at 00:01", 1.seconds.inWholeTicks),
				LyricLine("Line at 00:05", 5.seconds.inWholeTicks),
				LyricLine("Line at 00:30", 30.seconds.inWholeTicks),
			)
		)
	}

	private suspend fun fetchLyrics(entry: QueueEntry) {
		// Already has lyrics!
		if (entry.lyrics != null) return

		// BaseItem doesn't exist or doesn't have lyrics
		val baseItem = entry.baseItem ?: return
		if (baseItem.hasLyrics != true) return

		// Get via API
		val lyrics by api.lyricsApi.getLyrics(baseItem.id)
		entry.lyrics = lyrics
	}
}
