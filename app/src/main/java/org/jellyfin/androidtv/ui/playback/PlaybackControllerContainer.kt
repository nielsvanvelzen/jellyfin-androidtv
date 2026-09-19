package org.jellyfin.androidtv.ui.playback

import org.koin.core.annotation.Single

@Single
class PlaybackControllerContainer {
	var playbackController: PlaybackController? = null
}
