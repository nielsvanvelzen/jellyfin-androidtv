package org.jellyfin.androidtv.ui.playback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import composable.PlayerSurface
import org.jellyfin.androidtv.ui.ScreensaverViewModel
import org.jellyfin.playback.ui.composable.DebugData
import org.jellyfin.playback.ui.composable.DebugToolbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class VideoPlayerFragment : Fragment() {
	private val screensaverViewModel by activityViewModel<ScreensaverViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

		setContent {
			MaterialTheme(
				colors = darkColors(),
			) {
				Surface {
					Column {
						DebugToolbar()
						DebugData()
						PlayerSurface(
							modifier = Modifier.fillMaxHeight()
						)
					}
				}
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		screensaverViewModel.addLifecycleLock(lifecycle)
	}
}
