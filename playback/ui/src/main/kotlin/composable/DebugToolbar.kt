package org.jellyfin.playback.ui.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.jellyfin.playback.core.PlaybackManager
import org.koin.compose.koinInject

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun DebugToolbar() {
	val playbackManager = koinInject<PlaybackManager>()
	val coroutineScope = rememberCoroutineScope()

	val queue = playbackManager.state.queue
	val state = playbackManager.state
	val currentSpeed by state.speed.collectAsState()

	Row {
		Button(onClick = { state.pause() }) { Text("Pause") }
		Button(onClick = { state.unpause() }) { Text("Unpause") }
		Button(onClick = { state.stop() }) { Text("Stop") }
		Button(onClick = { state.fastForward() }) { Text("Fast-Forward") }
		Button(onClick = { state.rewind() }) { Text("Rewind") }
		if (queue != null) {
			Button(onClick = { coroutineScope.launch { queue.next() } }) { Text("Next") }
			Button(onClick = { coroutineScope.launch { queue.previous() } }) { Text("Previous") }
		}
		Button(onClick = {
			if (currentSpeed < 1) state.setSpeed(1f)
			else if (currentSpeed < 2) state.setSpeed(2f)
			else state.setSpeed(0.5f)
		}) { Text("Speed (${currentSpeed})") }
	}
}
