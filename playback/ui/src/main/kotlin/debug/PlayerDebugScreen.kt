package org.jellyfin.playback.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.mediasession.toMediaItem
import org.jellyfin.playback.core.model.PositionInfo
import org.koin.compose.rememberKoinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun PlayerDebugScreen() {
	val playbackManager = rememberKoinInject<PlaybackManager>()
	val coroutineScope = rememberCoroutineScope()

	val queue = playbackManager.state.queue
	val state = playbackManager.state
	val playState by state.playState.collectAsState()
	val currentEntry by queue.entry.collectAsState()
	var positionInfo by remember { mutableStateOf(PositionInfo.EMPTY) }
	val currentSpeed by state.speed.collectAsState()

	LaunchedEffect(playState, currentEntry) {
		while (true) {
			positionInfo = playbackManager.state.positionInfo
			delay(100.milliseconds)
		}
	}

	MaterialTheme(
		colors = darkColors(),
	) {
		Surface {
			Column {
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
	}
}
