package org.jellyfin.playback.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import kotlin.time.Duration.Companion.seconds

class TimeBar @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
) : AppCompatSeekBar(context, attrs), LifecycleObserver {
	var playbackManager: PlaybackManager? = null

	var ticker: Job? = null

	init {
		setWillNotDraw(false)

		setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			private var playingBeforeSeek = false

			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				if (fromUser) playbackManager?.backendService?.backend?.seekTo(progress.seconds)
			}

			override fun onStartTrackingTouch(seekBar: SeekBar?) {
				playingBeforeSeek = playbackManager?.state?.playState?.value == PlayState.PLAYING
				if (playingBeforeSeek) playbackManager?.backendService?.backend?.pause()
			}

			override fun onStopTrackingTouch(seekBar: SeekBar?) {
				if (playingBeforeSeek) playbackManager?.backendService?.backend?.play()
			}
		})
	}

	override fun onVisibilityChanged(changedView: View, visibility: Int) {
		super.onVisibilityChanged(changedView, visibility)

		if (visibility == View.VISIBLE) {
			val scope = (findViewTreeLifecycleOwner() ?: ProcessLifecycleOwner.get()).lifecycleScope
			ticker = scope.launch {
				val positionInfo = playbackManager?.state?.positionInfo ?: return@launch
				while (isActive) {
					withContext(Dispatchers.Main) {
						max = positionInfo.duration.inWholeSeconds.toInt()
						progress = positionInfo.active.inWholeSeconds.toInt()
						secondaryProgress = positionInfo.buffer.inWholeSeconds.toInt()
					}
					delay(1000)
				}
			}
		} else {
			ticker?.cancel()
		}
	}
}
