package org.jellyfin.playback.service

import androidx.media2.session.MediaSession
import androidx.media2.session.MediaSessionService

class PlaybackService : MediaSessionService() {
	// TODO create new session if none exist
	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
		return sessions.firstOrNull()
	}
}
