package org.jellyfin.androidtv.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.composable.ButtonBase
import org.jellyfin.androidtv.ui.composable.rememberMediaItem
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.util.ImageUtils
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.sdk.model.api.ImageType
import org.koin.androidx.compose.get

@Composable
fun NowPlayingComposable(
	modifier: Modifier = Modifier,
) {
	val mediaManager = get<MediaManager>()
	val navigationRepository = get<NavigationRepository>()

	val item by rememberMediaItem(mediaManager)
	// TODO: position doesn't update live
	val position by remember { mutableStateOf(mediaManager.currentAudioPosition) }

	AnimatedVisibility(
		modifier = modifier,
		visible = item != null,
		enter = fadeIn(),
		exit = fadeOut(),
	) {
		ButtonBase(
			modifier = Modifier
				.widthIn(0.dp, 250.dp),
			onClick = {
				navigationRepository.navigate(Destinations.nowPlaying)
			},
		) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.padding(5.dp)
			) {
				val name = item?.albumArtist ?: item?.name

				AsyncImage(
					url = item?.let { ImageUtils.getPrimaryImageUrl(it) },
					blurHash = item?.imageBlurHashes?.get(ImageType.PRIMARY)?.get(item?.imageTags?.get(ImageType.PRIMARY)),
					placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.ic_album),
					aspectRatio = item?.primaryImageAspectRatio ?: 1.0,
					modifier = Modifier
						.size(35.dp)
						.clip(RoundedCornerShape(4.dp)),
				)

				Column(
					verticalArrangement = Arrangement.SpaceAround,
				) {
					// Name
					Text(text = name.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)

					// Position & Duration
					val positionMillis = TimeUtils.formatMillis(position)
					val durationMillis = TimeUtils.formatMillis(item?.runTimeTicks?.div(10_000)
						?: 0)

					Text(stringResource(R.string.lbl_status, positionMillis, durationMillis), maxLines = 1)
				}
			}
		}
	}
}

class NowPlayingView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : AbstractComposeView(context, attrs, defStyle) {

	private val focusRequester = FocusRequester()

	@Composable
	override fun Content() = NowPlayingComposable(
		modifier = Modifier
			.focusRequester(focusRequester),
	)

	override fun onViewAdded(child: View?) {
		super.onViewAdded(child)
		// There should be only one child of type AndroidComposeView. When this child view
		// gains focus, we manually request focus on the content as a workaround for
		// currently unimplemented logic in FocusOwnerImpl.takeFocus().
		child?.setOnFocusChangeListener { _, hasFocus ->
			if (hasFocus) {
				focusRequester.requestFocus()
			}
		}
	}
}
