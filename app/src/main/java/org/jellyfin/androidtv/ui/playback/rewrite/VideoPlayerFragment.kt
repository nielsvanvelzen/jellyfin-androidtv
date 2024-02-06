package org.jellyfin.androidtv.ui.playback.rewrite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.ui.ScreensaverViewModel
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.PlayerState
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.core.model.PositionInfo
import org.jellyfin.playback.ui.composable.DebugData
import org.jellyfin.playback.ui.composable.DebugToolbar
import org.jellyfin.playback.ui.composable.PlayerSurface
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.compose.koinInject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

// Modulo operator (%) for Duration
private operator fun Duration.rem(other: Duration): Duration = (inWholeNanoseconds % other.inWholeNanoseconds).nanoseconds

@Composable
private fun rememberPositionInfo(
	playerState: PlayerState,
	frequency: Duration = 1.seconds,
): PositionInfo {
	var positionInfo by remember { mutableStateOf(PositionInfo.EMPTY) }
	val playState by playerState.playState.collectAsState()

	LaunchedEffect(playState, positionInfo.duration) {
		do {
			withContext(Dispatchers.Main) {
				positionInfo = playerState.positionInfo
			}

			delay(frequency - (positionInfo.active % frequency))
		} while (playState == PlayState.PLAYING)
	}

	return positionInfo
}

@Composable
fun PlayerControls(
	playerState: PlayerState,
) {
	val positionInfo = rememberPositionInfo(playerState)

	ProvideTextStyle(value = TextStyle.Default.copy(color = Color.White)) {
		Column(
			modifier = Modifier.background(Color.Black.copy(alpha = 0.2f))
		) {
			DebugData()
			DebugToolbar()

			Text(
				text = "${positionInfo.active} / ${positionInfo.duration}",
				color = Color.Red,
			)

			LinearProgressIndicator(
				progress = (positionInfo.active / positionInfo.duration).toFloat(),
				color = Color.White,
				backgroundColor = Color.White.copy(alpha = 0.2f),
				strokeCap = StrokeCap.Round,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}

@Composable
fun VideoPlayerScreen() {
	val playbackManager = koinInject<PlaybackManager>()
	val playerState = playbackManager.state

	Box(
		modifier = Modifier
			.background(Color.Black),
	) {
		// Video in the background
		PlayerSurface(
			modifier = Modifier
				.align(Alignment.Center)
		)

		// Controls on top of video
		Box(
			modifier = Modifier
				.align(Alignment.BottomCenter),
			content = { PlayerControls(playerState) }
		)
	}

	// Stop playback when screen closes
	DisposableEffect(playerState) {
		onDispose { playerState.stop() }
	}
}

class VideoPlayerFragment : Fragment() {
	private val screensaverViewModel by activityViewModel<ScreensaverViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

		setContent {
			VideoPlayerScreen()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		screensaverViewModel.addLifecycleLock(lifecycle)
	}
}
