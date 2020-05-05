package org.jellyfin.playback.backend

import android.view.SurfaceView
import org.jellyfin.playback.queue.QueueItem

interface Backend {
	fun setSurface(surfaceView: SurfaceView)
	fun playItem(item: QueueItem)
}
