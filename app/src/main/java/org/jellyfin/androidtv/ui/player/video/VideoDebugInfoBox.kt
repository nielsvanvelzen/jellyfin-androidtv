package org.jellyfin.androidtv.ui.player.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.composable.rememberQueueEntry
import org.jellyfin.design.token.ColorTokens
import org.jellyfin.design.token.SpaceTokens
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.mediastream.mediaStream
import org.jellyfin.playback.core.mediastream.mediaStreamFlow
import org.koin.compose.koinInject

@Composable
fun VideoDebugInfoBox(
	modifier: Modifier = Modifier,
	playbackManager: PlaybackManager = koinInject(),
) {
	val entry by rememberQueueEntry(playbackManager)
	val mediaStream = entry?.run { mediaStreamFlow.collectAsState(mediaStream) }?.value

	Box(
		modifier = modifier
			.background(ColorTokens.colorBluegrey900.copy(alpha = 0.8f), JellyfinTheme.shapes.medium)
			.padding(SpaceTokens.spaceMd)
	) {
		Column(
			verticalArrangement = Arrangement.spacedBy(SpaceTokens.spaceSm)
		) {
			InfoCategory(
				name = "Player"
			) {
				InfoLine("Backend", playbackManager.backend.javaClass.simpleName)
			}

			InfoCategory(
				name = "Media"
			) {
				InfoLine("Conversion", mediaStream?.conversionMethod?.toString() ?: "?")
				InfoLine("Container", mediaStream?.container?.format ?: "?")
			}
		}
	}
}

@Composable
private fun InfoCategory(
	name: String,
	content: @Composable () -> Unit
) {
	Text(
		text = name,
		fontWeight = FontWeight.Bold,
		color = ColorTokens.colorWhite
	)

	Column(
		modifier = Modifier
			.padding(start = SpaceTokens.spaceSm)
	) {
		content()
	}
}

@Composable
private fun InfoLine(name: String, value: String) {
	Text(
		text = buildAnnotatedString {
			withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
				append(name)
				append(" ")
			}

			append(value)
		},
		color = ColorTokens.colorWhite
	)
}
