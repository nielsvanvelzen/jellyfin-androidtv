package org.jellyfin.androidtv

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_playback_test.*
import org.jellyfin.playback.JellyfinPlayback
import org.jellyfin.playback.queue.SingleQueue
import org.jellyfin.playback.queue.UserQueueItem

class PlaybackTestActivity : Activity() {
	private val jellyfinPlayback by lazy {
		JellyfinPlayback().apply {
			val url = "http://jell.yfish.us/media/jellyfish-3-mbps-hd-h264.mkv"
			val queue = SingleQueue(UserQueueItem(url))
			playQueue(queue)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val view = layoutInflater.inflate(R.layout.activity_playback_test, null, false)
		setContentView(view)

		activity_playback_test_player_view.jellyfinPlayback = jellyfinPlayback
	}
}
