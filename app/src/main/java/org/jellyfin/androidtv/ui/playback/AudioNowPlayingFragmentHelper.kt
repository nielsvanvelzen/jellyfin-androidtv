package org.jellyfin.androidtv.ui.playback

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.ui.AsyncImageView
import org.jellyfin.androidtv.ui.composable.LyricsDtoBox
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.jellyfin.lyrics
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun initializeLyricsView(
	coverView: AsyncImageView,
	lyricsView: ComposeView,
	playbackManager: PlaybackManager,
) {
	lyricsView.setContent {
		val entry by playbackManager.state.queue.entry.collectAsState()
		val lyrics = entry?.lyrics

		var playbackPosition by remember { mutableStateOf(Duration.ZERO) }
		var playbackDuration by remember { mutableStateOf(Duration.ZERO) }

		LaunchedEffect(lyrics) {
			while (true) {
				if (lyrics == null) break

				val positionInfo = playbackManager.state.positionInfo
				playbackPosition = positionInfo.active
				playbackDuration = positionInfo.duration

				delay(1.seconds)
			}
		}

		val coverViewAlpha by animateFloatAsState(if (lyrics == null) 1f else 0.2f)
		LaunchedEffect(coverViewAlpha) { coverView.alpha = coverViewAlpha }

		if (lyrics != null) {
			// TODO: Smaller font size
			LyricsDtoBox(
				lyricDto = lyrics,
				currentTimestamp = playbackPosition,
				duration = playbackDuration,
				fontSize = 12.sp,
			)
		}
	}
}
