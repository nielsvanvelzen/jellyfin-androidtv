package org.jellyfin.androidtv.ui.playback.rewrite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.fragment.app.Fragment
import composable.PlayerSurface
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.ui.ScreensaverViewModel
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.PlayerState
import org.jellyfin.playback.core.model.PositionInfo
import org.jellyfin.playback.ui.composable.DebugData
import org.jellyfin.playback.ui.composable.DebugToolbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
private fun rememberPositionInfo(playerState: PlayerState): PositionInfo {
	var positionInfo by remember { mutableStateOf(PositionInfo.EMPTY) }

	LaunchedEffect(playerState) {
		while (true) {
			positionInfo = playerState.positionInfo
			delay(100.milliseconds)
		}
	}

	return positionInfo
}

@Composable
fun PlayerControls() {
	val playbackManager = koinInject<PlaybackManager>()
	val positionInfo = rememberPositionInfo(playerState = playbackManager.state)

	Column {
		DebugData()
		DebugToolbar()

		LinearProgressIndicator(
			progress = (positionInfo.active / positionInfo.duration).toFloat(),
			color = Color.White,
			backgroundColor = Color.White.copy(alpha = 0.2f),
			strokeCap = StrokeCap.Round,
			modifier = Modifier.fillMaxWidth()
		)
	}
}

@Composable
fun VideoPlayerScreen() {
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
			content = { PlayerControls() }
		)
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
