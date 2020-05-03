package org.jellyfin.playback.view

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import org.jellyfin.playback.JellyfinPlayback

class PlayerView(
	context: Context,
	attrs: AttributeSet?,
	defStyleAttr: Int
) : SurfaceView(context, attrs, defStyleAttr) {
	constructor(context: Context, attrs: AttributeSet? = null): this(context, attrs, 0)

	lateinit var jellyfinPlayback: JellyfinPlayback

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()

		jellyfinPlayback.attachView(this)
	}

	override fun onDetachedFromWindow() {
		jellyfinPlayback.detachView(this)

		super.onDetachedFromWindow()
	}
}

