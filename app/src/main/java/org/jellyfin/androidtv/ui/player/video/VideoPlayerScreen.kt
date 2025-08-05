package org.jellyfin.androidtv.ui.player.video

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.LocalTextStyle
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.base.button.IconButton
import org.jellyfin.androidtv.ui.composable.rememberQueueEntry
import org.jellyfin.androidtv.ui.player.base.PlayerOverlayLayout
import org.jellyfin.androidtv.ui.player.base.PlayerSeekbar
import org.jellyfin.androidtv.ui.player.base.PlayerSubtitles
import org.jellyfin.androidtv.ui.player.base.PlayerSurface
import org.jellyfin.androidtv.ui.shared.toolbar.ToolbarClock
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.jellyfin.queue.baseItem
import org.jellyfin.playback.jellyfin.queue.baseItemFlow
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds

private const val DefaultVideoAspectRatio = 16f / 9f

@Composable
fun VideoPlayerScreen() {
	val backgroundService = koinInject<BackgroundService>()
	LaunchedEffect(backgroundService) {
		backgroundService.clearBackgrounds()
	}

	val playbackManager = koinInject<PlaybackManager>()
	val videoSize by playbackManager.state.videoSize.collectAsState()
	val aspectRatio =
		videoSize.aspectRatio.takeIf { !it.isNaN() && it > 0f } ?: DefaultVideoAspectRatio

	Box(
		modifier = Modifier
			.background(Color.Black)
			.fillMaxSize()
	) {
		PlayerSurface(
			playbackManager = playbackManager,
			modifier = Modifier
				.aspectRatio(aspectRatio, videoSize.height < videoSize.width)
				.fillMaxSize()
				.align(Alignment.Center)
		)

		PlayerOverlay(
			playbackManager = playbackManager,
		)

		PlayerSubtitles(
			playbackManager = playbackManager,
			modifier = Modifier
				.aspectRatio(aspectRatio, videoSize.height < videoSize.width)
				.fillMaxSize()
				.align(Alignment.Center)
		)
	}
}

@Composable
private fun PlayerOverlay(
	modifier: Modifier = Modifier,
	playbackManager: PlaybackManager = koinInject(),
) {
	val scope = rememberCoroutineScope()
	val playStateButtonFocusRequester = remember { FocusRequester() }

	var visible by remember { mutableStateOf(false) }
	var visibleTimerJob by remember { mutableStateOf<Job?>(null) }

	fun setVisible() {
		visible = true
		visibleTimerJob?.cancel()
		visibleTimerJob = scope.launch {
			delay(5.seconds)
			visible = false
		}
	}

	val entry by rememberQueueEntry(playbackManager)
	val item = entry?.run { baseItemFlow.collectAsState(baseItem) }?.value

	PlayerOverlayLayout(
		modifier = modifier
			.focusable()
			.onPreviewKeyEvent {
				if (visible) setVisible()
				false
			}
			.onKeyEvent {
				if (it.key == Key.Back && visible) {
					visible = false
					true
				} else if (!it.nativeKeyEvent.isSystem && !visible) {
					setVisible()
					true
				} else {
					false
				}
			}
			.focusRestorer(playStateButtonFocusRequester),
		visible = visible,
		header = item?.let { { VideoHeader(item) } },
		controls = {
			VideoControls(
				playbackManager = playbackManager,
				playStateButtonFocusRequester = playStateButtonFocusRequester,
			)
		},
	)

	LaunchedEffect(visible) {
		if (visible) playStateButtonFocusRequester.requestFocus()
	}
}

@Composable
private fun BaseHeader(
	content: @Composable ColumnScope.() -> Unit,
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.Top,
	) {
		Column {
			content()
		}

		Spacer(Modifier.weight(1f))

		ToolbarClock(
			modifier = Modifier.wrapContentWidth(unbounded = true)
		)
	}
}

@Composable
@Stable
private fun VideoHeader(
	item: BaseItemDto,
) {
	BaseHeader {
		Text(
			text = item.name.orEmpty(),
			overflow = TextOverflow.Ellipsis,
			maxLines = 1,
			style = LocalTextStyle.current.copy(
				color = Color.White,
				fontSize = 22.sp
			)
		)

		Text(
			text = item.seriesName.orEmpty(),
			overflow = TextOverflow.Ellipsis,
			maxLines = 1,
			style = LocalTextStyle.current.copy(
				color = Color.White,
				fontSize = 18.sp
			)
		)
	}
}

@Composable
private fun VideoControls(
	playbackManager: PlaybackManager = koinInject(),
	playStateButtonFocusRequester: FocusRequester
) {
	val playState by playbackManager.state.playState.collectAsState()

	Column(
		verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier
				.focusRestorer()
				.focusGroup()
		) {
			IconButton(
				onClick = {
					when (playState) {
						PlayState.STOPPED,
						PlayState.ERROR -> playbackManager.state.play()

						PlayState.PLAYING -> playbackManager.state.pause()
						PlayState.PAUSED -> playbackManager.state.unpause()
					}
				},
				modifier = Modifier
					.focusRequester(playStateButtonFocusRequester)
			) {
				AnimatedContent(playState) { playState ->
					when (playState) {
						PlayState.PLAYING -> {
							Icon(
								imageVector = ImageVector.vectorResource(R.drawable.ic_pause),
								contentDescription = stringResource(R.string.lbl_pause),
							)
						}

						PlayState.STOPPED,
						PlayState.PAUSED,
						PlayState.ERROR -> {
							Icon(
								imageVector = ImageVector.vectorResource(R.drawable.ic_play),
								contentDescription = stringResource(R.string.lbl_play),
							)
						}
					}
				}
			}

			IconButton(
				onClick = { playbackManager.state.rewind() },
			) {
				Icon(
					imageVector = ImageVector.vectorResource(R.drawable.ic_rewind),
					contentDescription = stringResource(R.string.rewind),
				)
			}

			IconButton(
				onClick = { playbackManager.state.fastForward() },
			) {
				Icon(
					imageVector = ImageVector.vectorResource(R.drawable.ic_fast_forward),
					contentDescription = stringResource(R.string.fast_forward),
				)
			}

			Box {
				var expanded by remember { mutableStateOf(false) }
				IconButton(
					onClick = { expanded = true },
				) {
					Icon(
						imageVector = ImageVector.vectorResource(R.drawable.ic_select_subtitle),
						contentDescription = stringResource(R.string.lbl_subtitle_track),
					)
				}

				DropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false },
					alignment = Alignment.BottomCenter,
				) {
					Column {
						Button(onClick = {}) { Text("English") }
						Button(onClick = {}) { Text("Nederlands") }
						Button(onClick = {}) { Text("方言") }
						Button(onClick = {}) { Text("霊の言葉") }
						Button(onClick = {}) { Text("Delvish") }
					}
				}
			}
		}

		PlayerSeekbar(
			playbackManager = playbackManager,
			modifier = Modifier
				.fillMaxWidth()
				.height(4.dp)
		)
	}
}

@Composable
fun DropdownMenu(
	expanded: Boolean,
	onDismissRequest: () -> Unit,
	alignment: Alignment = Alignment.TopStart,
	offset: IntOffset = IntOffset(0, 0),
	modifier: Modifier = Modifier,
	content: @Composable BoxScope.() -> Unit,
) {
	val focusRequester = remember { FocusRequester() }

	if (expanded) {
		Popup(
			onDismissRequest = onDismissRequest,
			properties = PopupProperties(
				focusable = true,
				dismissOnBackPress = true,
				dismissOnClickOutside = true,
			),
			alignment = alignment,
			offset = offset,
		) {
			// TODO: Hardcoded design for testing
			Box(
				modifier = modifier
					.shadow(4.dp, RoundedCornerShape(8.dp))
					.background(Color.White, RoundedCornerShape(8.dp))
					.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
					.padding(vertical = 4.dp)
					.wrapContentSize()
					.focusRequester(focusRequester)
					.focusGroup()
			) {
				content()
			}
		}

		LaunchedEffect(focusRequester) {
			focusRequester.requestFocus()
		}
	}
}
