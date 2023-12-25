package org.jellyfin.playback.core.view

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.VideoSize
import kotlin.math.abs

/**
 * A view that is used to display the video output of the playing media.
 * The [playbackManager] must be set when the view is initialized.
 */
class PlayerSurfaceView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
	defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
	lateinit var playbackManager: PlaybackManager

	val surface = SurfaceView(context, attrs).apply {
		addView(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		val videoSize = playbackManager.state.videoSize.value
		if (videoSize == VideoSize.EMPTY) return

		val viewAspectRatio = width.toFloat() / height.toFloat()
		val aspectDiff = videoSize.aspectRatio / viewAspectRatio - 1
		if (abs(aspectDiff) <= 0.1f) return

		var width = measuredWidth
		var height = measuredHeight
		if (aspectDiff > 0f) height = (width / videoSize.aspectRatio).toInt()
		else width = (height * videoSize.aspectRatio).toInt()

		super.onMeasure(
			MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
			MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
		)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()

		if (!isInEditMode) {
			playbackManager.backendService.attach(this)
		}
	}
}

