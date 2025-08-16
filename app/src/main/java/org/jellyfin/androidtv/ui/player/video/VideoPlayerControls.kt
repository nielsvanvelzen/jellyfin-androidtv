package org.jellyfin.androidtv.ui.player.video

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.base.button.IconButton
import org.jellyfin.androidtv.ui.base.popover.Popover
import org.jellyfin.androidtv.ui.player.base.PlayerSeekbar
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.koin.compose.koinInject

@Composable
fun VideoPlayerControls(
	playbackManager: PlaybackManager = koinInject()
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
			PlayPauseButton(playbackManager, playState)
			RewindButton(playbackManager)
			FastForwardButton(playbackManager)
			SubtitlesButton()
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
private fun PlayPauseButton(
	playbackManager: PlaybackManager,
	playState: PlayState,
) {
	val focusRequester = remember { FocusRequester() }
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
			.focusRequester(focusRequester)
			.onVisibilityChanged {
				focusRequester.requestFocus()
			}
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
}

@Composable
private fun RewindButton(
	playbackManager: PlaybackManager,
) = IconButton(
	onClick = { playbackManager.state.rewind() },
) {
	Icon(
		imageVector = ImageVector.vectorResource(R.drawable.ic_rewind),
		contentDescription = stringResource(R.string.rewind),
	)
}

@Composable
private fun FastForwardButton(
	playbackManager: PlaybackManager,
) = IconButton(
	onClick = { playbackManager.state.fastForward() },
) {
	Icon(
		imageVector = ImageVector.vectorResource(R.drawable.ic_fast_forward),
		contentDescription = stringResource(R.string.fast_forward),
	)
}

@Composable
private fun SubtitlesButton() = Box {
	var expanded by remember { mutableStateOf(false) }
	IconButton(
		onClick = { expanded = true },
	) {
		Icon(
			imageVector = ImageVector.vectorResource(R.drawable.ic_select_subtitle),
			contentDescription = stringResource(R.string.lbl_subtitle_track),
		)
	}

	// TODO temporary hardcoded subtitle tracks for testing UI
	var subtitleTracks by remember {
		mutableStateOf(
			setOf(
				"English" to false,
				"Nederlands" to true,
				"方言" to false,
				"霊の言葉" to false,
				"Delvish" to false,
			)
		)
	}

	Popover(
		expanded = expanded,
		onDismissRequest = { expanded = false },
		alignment = Alignment.TopCenter,
		offset = DpOffset(0.dp, (-5).dp)
	) {
		Column(
			modifier = Modifier
				.padding(4.dp)
				.width(IntrinsicSize.Max)
		) {
			for ((name, active) in subtitleTracks) {
				Button(
					onClick = {
						subtitleTracks = subtitleTracks
							.map { it.first to if (it.first == name) !it.second else it.second }
							.toSet()
					},
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(3.dp),
					colors = ButtonDefaults.colors(containerColor = Color.Transparent),
				) {
					Box(
						modifier = Modifier
							.size(16.dp)
					) {
						this@Button.AnimatedVisibility(
							visible = active,
							enter = fadeIn(),
							exit = fadeOut(),
						) {
							Icon(
								imageVector = ImageVector.vectorResource(R.drawable.ic_check),
								// todo string resource
								contentDescription = "Active subtitle track",
								modifier = Modifier.fillMaxSize(),
							)
						}
					}

					Spacer(Modifier.width(8.dp))
					Text(name)
				}
			}
		}
	}
}
