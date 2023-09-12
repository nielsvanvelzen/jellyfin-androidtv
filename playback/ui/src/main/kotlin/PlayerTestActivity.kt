package org.jellyfin.playback.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import org.jellyfin.playback.ui.databinding.ActivityPlayerTestBinding

class PlayerTestActivity : FragmentActivity() {
	private lateinit var binding: ActivityPlayerTestBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityPlayerTestBinding.inflate(layoutInflater)
		setContentView(binding.root)
	}
}
