package composable

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.VideoSize
import org.jellyfin.playback.core.view.PlayerSurfaceView
import org.koin.compose.koinInject

@Composable
fun PlayerSurface(
	modifier: Modifier = Modifier,
) {
	val playbackManager = koinInject<PlaybackManager>()
	val videoSize by playbackManager.state.videoSize.collectAsState()

	AndroidView(
		factory = { context ->
			PlayerSurfaceView(context).also { playerView ->
				playerView.playbackManager = playbackManager
			}
		},
		modifier = Modifier
			.apply {
				if (videoSize != VideoSize.EMPTY) aspectRatio(videoSize.aspectRatio, true)
			}
			.then(modifier)
	)
}