package org.jellyfin.androidtv.ui.playback.overlay

import android.content.Context
import android.util.AttributeSet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import kotlinx.coroutines.flow.MutableStateFlow
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.playback.segment.MediaSegmentRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SkipOverlayComposable(
	visible: Boolean,
) {
	Box(
		contentAlignment = Alignment.BottomEnd,
		modifier = Modifier
			.padding(48.dp, 48.dp)
	) {
		AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
			Row(
				modifier = Modifier
					.clip(RoundedCornerShape(6.dp))
					.background(colorResource(R.color.popup_menu_background).copy(alpha = 0.6f))
					.padding(10.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Image(
					painter = painterResource(R.drawable.ic_control_select),
					contentDescription = null,
				)

				Text(
					text = stringResource(R.string.segment_action_skip),
					color = colorResource(R.color.button_default_normal_text),
					fontSize = 18.sp,
				)
			}
		}
	}
}

class SkipOverlayView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : AbstractComposeView(context, attrs, defStyle) {
	private val _currentPosition = MutableStateFlow(Duration.ZERO)
	private val _targetPosition = MutableStateFlow<Duration?>(null)

	fun setCurrentPositionMs(time: Long) {
		_currentPosition.value = time.milliseconds
	}

	var targetPosition: Duration?
		get() = _targetPosition.value
		set(value) {
			_targetPosition.value = value
		}

	fun getTargetPositionMs() = _targetPosition.value?.inWholeMilliseconds

	val visible: Boolean
		get() {
			val targetPosition = _targetPosition.value
			val currentPosition = _currentPosition.value

			return targetPosition != null && currentPosition <= (targetPosition - MediaSegmentRepository.SkipMinDuration)
		}

	fun hide() {
		_targetPosition.value = null
	}

	@Composable
	override fun Content() {
		val currentPosition by _currentPosition.collectAsState()
		val targetPosition by _targetPosition.collectAsState()

		val visible by remember(currentPosition, targetPosition) {
			derivedStateOf { visible }
		}

		SkipOverlayComposable(visible)
	}
}
