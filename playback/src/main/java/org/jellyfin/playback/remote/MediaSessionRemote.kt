package org.jellyfin.playback.remote

import android.content.Context
import androidx.media2.session.MediaSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class MediaSessionRemote(
	private val context: Context
) : Remote {
	private val session = MediaSession.Builder(context, MediaSessionPlayerGlue(this))
		.setSessionCallback(Dispatchers.Default.asExecutor(), object : MediaSession.SessionCallback() {})
		.setId("MediaSessionRemote")
		.build()
}
