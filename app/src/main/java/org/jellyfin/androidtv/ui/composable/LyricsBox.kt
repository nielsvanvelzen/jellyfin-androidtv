package org.jellyfin.androidtv.ui.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import org.jellyfin.androidtv.ui.composable.modifier.fadingEdges
import org.jellyfin.sdk.model.api.LyricDto
import org.jellyfin.sdk.model.api.LyricLine
import org.jellyfin.sdk.model.api.LyricMetadata
import org.jellyfin.sdk.model.extensions.ticks
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class TimedLine(val timestamp: Duration, val text: String)

@Composable
private fun LyricsBox(
	lines: List<TimedLine>,
	currentTimestamp: Duration,
) {
	val scrollState = rememberScrollState()
	val linePositions = remember { mutableStateMapOf<Int, Float>() }
	var currentLineIndex by remember { mutableIntStateOf(0) }
	val lineScrollAnimationDuration = 1.seconds

	// Update current line
	LaunchedEffect(lines, currentTimestamp) {
		currentLineIndex = lines
			.withIndex()
			.lastOrNull { line -> line.value.timestamp < currentTimestamp }
			?.index
			?: return@LaunchedEffect
	}

	// Scroll to current line
	LaunchedEffect(currentLineIndex) {
		val currentLinePosition = linePositions[currentLineIndex] ?: return@LaunchedEffect
		val position = currentLinePosition - scrollState.viewportSize / 2f
		scrollState.animateScrollTo(
			position.roundToInt(),
			tween(lineScrollAnimationDuration.inWholeMilliseconds.toInt())
		)
	}

	BoxWithConstraints {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(25.dp),
			modifier = Modifier
				.fillMaxSize()
				.fadingEdges(vertical = 150.dp)
				.verticalScroll(
					state = scrollState,
					enabled = false,
				),
		) {

			Spacer(
				modifier = Modifier
					.height(this@BoxWithConstraints.maxHeight)
			)

			for ((index, line) in lines.withIndex()) {
				val color by animateColorAsState(
					if (index == currentLineIndex) Color.Red else Color.White,
					tween(lineScrollAnimationDuration.inWholeMilliseconds.toInt())
				)

				Text(
					text = line.text,
					textAlign = TextAlign.Center,
					fontSize = 22.sp,
					color = color,
					minLines = 1,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.onGloballyPositioned {
						val top = it.positionInParent().y
						linePositions[index] = top + it.size.height / 2f
					}
				)
			}

			Spacer(
				modifier = Modifier
					.height(this@BoxWithConstraints.maxHeight)
			)
		}
	}
}

@Composable
fun TimedLyricsBox(
	lines: List<TimedLine>,
	currentTimestamp: Duration,
) = LyricsBox(lines, currentTimestamp)

// TODO Use a continuous scroll animation instead of the "fake timestamp" approach
@Composable
fun UntimedLyricsBox(
	lines: List<String>,
	currentTimestamp: Duration,
	duration: Duration,
) {
	val totalLines = lines.size.toDouble()

	LyricsBox(
		lines = lines.mapIndexed { index, text ->
			TimedLine(duration * (index / totalLines), text)
		},
		currentTimestamp = currentTimestamp,
	)
}

@Composable
fun LyricsDtoBox(
	lyricDto: LyricDto,
	currentTimestamp: Duration = Duration.ZERO,
	duration: Duration = Duration.ZERO,
) {
	val lyrics = lyricDto.lyrics
	val isTimed = remember(lyrics) { lyrics.firstOrNull()?.start != null }
	if (isTimed) {
		TimedLyricsBox(
			lines = lyrics.map {
				TimedLine(it.start?.ticks ?: Duration.ZERO, it.text)
			},
			currentTimestamp = currentTimestamp
		)
	} else {
		UntimedLyricsBox(
			lines = lyrics.map { it.text },
			currentTimestamp = currentTimestamp,
			duration = duration
		)
	}
}

@Preview
@Composable
fun LyricsBoxPreview() {
	Box(
		modifier = Modifier
			.background(Color.White)
			.size(500.dp)
	) {
		LyricsDtoBox(
			currentTimestamp = 0.ticks,
			duration = 1.minutes,
			lyricDto = LyricDto(
				metadata = LyricMetadata(),
				lyrics = (1..100).map { i ->
					LyricLine("Line $i")
				}
			)
		)
	}
}
