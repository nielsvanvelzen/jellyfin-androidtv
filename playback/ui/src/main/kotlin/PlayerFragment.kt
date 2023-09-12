package org.jellyfin.playback.ui

import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.core.plugin.PlayerService
import org.jellyfin.playback.ui.databinding.FragmentPlayerBinding
import org.koin.android.ext.android.inject

class PlayerFragment : Fragment() {
	private lateinit var binding: FragmentPlayerBinding
	private val playbackManager: PlaybackManager by inject()

	private val plugin = object : PlayerService() {
		override suspend fun onInitialize() {
			lifecycleScope.launch {
				state.playState.collect { playState ->
					val drawable = when (playState) {
						PlayState.PLAYING -> R.drawable.ic_pause
						PlayState.STOPPED,
						PlayState.PAUSED,
						PlayState.ERROR -> R.drawable.ic_play
					}
					binding.buttonPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(), drawable))
				}
			}

			lifecycleScope.launch {
				state.videoSize.collect {
					if (activity?.isInPictureInPictureMode == true) {
						activity?.setPictureInPictureParams(getPictureInPictureParams())
					}
				}
			}
		}

		fun getPictureInPictureParams(): PictureInPictureParams = PictureInPictureParams.Builder().apply {
			// Only apply aspect ratio if in range
			val (width, height) = state.videoSize.value
			val rational = Rational(width, height)
			if (rational.toFloat() in 0.41841003f..2.39f) setAspectRatio(rational)
		}.build()
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		playbackManager.addService(plugin)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		playbackManager.removeService(plugin)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentPlayerBinding.inflate(layoutInflater, container, false)
		binding.playerSurface.playbackManager = playbackManager
		binding.timeBar.playbackManager = playbackManager

		// Buttons
		binding.buttonExit.setOnClickListener { activity?.finishAfterTransition() }
		binding.buttonPlayPause.setOnClickListener { if (plugin.state.playState.value == PlayState.PLAYING) plugin.state.pause() else plugin.state.unpause() }

		binding.buttonPictureInPicture.apply {
			isVisible = context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

			setOnClickListener {
				val params = plugin.getPictureInPictureParams()
				activity?.enterPictureInPictureMode(params)
			}
		}

		binding.buttonNext.setOnClickListener {
			lifecycleScope.launch {
				playbackManager.state.queue.next()
			}
		}

		return binding.root
	}

	override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode)

		// Hide overlays when PiP is active
		binding.overlay.isVisible = !isInPictureInPictureMode
	}
}
