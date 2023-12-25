package org.jellyfin.playback.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.mediasession.toMediaItem
import org.jellyfin.playback.core.model.PositionInfo
import org.koin.compose.rememberKoinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun DebugData() {
	val playbackManager = rememberKoinInject<PlaybackManager>()

	val queue = playbackManager.state.queue
	val state = playbackManager.state
	val playState by state.playState.collectAsState()
	val currentEntry by queue.entry.collectAsState()
	var positionInfo by remember { mutableStateOf(PositionInfo.EMPTY) }

	LaunchedEffect(playState, currentEntry) {
		while (true) {
			positionInfo = playbackManager.state.positionInfo
			delay(100.milliseconds)
		}
	}

	Column {
		currentEntry?.metadata?.displayTitle?.let { title ->
			Text(title, fontSize = 28.sp)
		}

		val metadata = currentEntry?.metadata?.toMediaItem()?.mediaMetadata
		if (metadata != null) {
			val bundle = metadata.toBundle()
			for (key in bundle.keySet()) {
				Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
					Text(key)
					Text(bundle.get(key).toString())
				}
			}
		}
		Text("playState=$playState, positionInfo=$positionInfo")
	}
}