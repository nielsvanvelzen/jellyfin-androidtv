package org.jellyfin.androidtv.ui.player.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.composable.rememberQueueEntry
import org.jellyfin.androidtv.ui.player.base.PlayerOverlayLayout
import org.jellyfin.androidtv.ui.player.base.rememberPlayerOverlayVisibility
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.jellyfin.queue.baseItem
import org.jellyfin.playback.jellyfin.queue.baseItemFlow
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VideoPlayerOverlay(
	modifier: Modifier = Modifier,
	playbackManager: PlaybackManager = koinInject(),
) {
	val visibilityState = rememberPlayerOverlayVisibility()

	val entry by rememberQueueEntry(playbackManager)
	val item = entry?.run { baseItemFlow.collectAsState(baseItem) }?.value

	PlayerOverlayLayout(
		visibilityState = visibilityState,
		modifier = modifier,
		header = {
			VideoPlayerHeader(
				item = item,
			)
		},
		controls = {
			VideoPlayerControls(
				playbackManager = playbackManager,
			)
		},
	)

	MediaActionOverlay()
}

sealed interface OverlayActionIndicator {
	data object Play : OverlayActionIndicator
	data object Pause : OverlayActionIndicator

	data object Mute : OverlayActionIndicator
	data object Unmute : OverlayActionIndicator

	data class Volume(val value: Float) : OverlayActionIndicator
}

@Composable
fun rememberOverlayActionIndicator(
	playbackManager: PlaybackManager,
): State<OverlayActionIndicator?> {
	val coroutineScope = rememberCoroutineScope()
	val flow = remember { MutableStateFlow<OverlayActionIndicator?>(null) }
	var unsetJob by remember { mutableStateOf<Job?>(null) }

	fun setIndicator(value: OverlayActionIndicator) {
		if (flow.value == value) return

		flow.value = value

		unsetJob?.cancel()
		unsetJob = coroutineScope.launch {
			delay(700.milliseconds)
			flow.value = null
		}
	}

	LaunchedEffect(playbackManager) {
		playbackManager.state.playState
			.onEach { playState ->
				when (playState) {
					PlayState.PLAYING -> setIndicator(OverlayActionIndicator.Play)
					PlayState.PAUSED -> setIndicator(OverlayActionIndicator.Pause)
					else -> Unit
				}
			}
			.launchIn(coroutineScope)
	}

	return flow.collectAsState(null)
}

@Composable
fun MediaActionOverlay(
	playbackManager: PlaybackManager = koinInject(),
) {
	val action by rememberOverlayActionIndicator(playbackManager)

	MediaActionOverlay(
		visible = action != null,
		icon = {
			when (action) {
				OverlayActionIndicator.Mute -> Icon(
					imageVector = ImageVector.vectorResource(R.drawable.app_icon_foreground),
					contentDescription = null,
					modifier = Modifier.fillMaxSize()
				)

				OverlayActionIndicator.Pause -> Icon(
					imageVector = ImageVector.vectorResource(R.drawable.ic_pause),
					contentDescription = null,
					modifier = Modifier.fillMaxSize()
				)

				OverlayActionIndicator.Play -> Icon(
					imageVector = ImageVector.vectorResource(R.drawable.ic_play),
					contentDescription = null,
					modifier = Modifier.fillMaxSize()
				)

				OverlayActionIndicator.Unmute -> Icon(
					imageVector = ImageVector.vectorResource(R.drawable.app_icon_foreground),
					contentDescription = null,
					modifier = Modifier.fillMaxSize()
				)

				is OverlayActionIndicator.Volume -> Icon(
					imageVector = ImageVector.vectorResource(R.drawable.app_icon_foreground),
					contentDescription = null,
					modifier = Modifier.fillMaxSize()
				)

				null -> Unit
			}
		}
	)
}

@Composable
fun MediaActionOverlay(
	visible: Boolean,
	icon: @Composable () -> Unit,
	modifier: Modifier = Modifier,
	shape: Shape = CircleShape,
) {
	AnimatedVisibility(
		visible = visible,
		enter = fadeIn() + scaleIn(initialScale = 0.8f),
		exit = fadeOut() + scaleOut(targetScale = 1.2f),
		modifier = modifier
			.fillMaxSize()
			.wrapContentSize(Alignment.Center)
	) {
		Box(
			modifier = Modifier
				.size(96.dp)
				.background(
					color = Color.Black.copy(alpha = 0.5f),
					shape = shape
				)
				.padding(16.dp),
			contentAlignment = Alignment.Center,
		) {
			icon()
		}
	}
}
