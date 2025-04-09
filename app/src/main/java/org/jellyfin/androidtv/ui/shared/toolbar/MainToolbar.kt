package org.jellyfin.androidtv.ui.shared.toolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.filterNotNull
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.ui.NowPlayingComposable
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.ProvideTextStyle
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.base.button.IconButton
import org.jellyfin.androidtv.ui.base.button.IconButtonDefaults
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.primaryImage
import org.jellyfin.sdk.api.client.ApiClient
import org.koin.compose.koinInject

enum class MainToolbarActivebutton {
	USER,
	HOME,
	SEARCH,

	UNKNOWN,
}

@Composable
fun MainToolbar(
	activebutton: MainToolbarActivebutton = MainToolbarActivebutton.UNKNOWN,
) {
	val userRepository = koinInject<UserRepository>()
	val api = koinInject<ApiClient>()

	// Prevent user image to disappear when signing out by skipping null values
	val currentUser by remember { userRepository.currentUser.filterNotNull() }.collectAsState(null)
	val userImage = remember(currentUser) { currentUser?.primaryImage?.getUrl(api) }

	MainToolbar(
		userImage = userImage,
		activebutton = activebutton,
	)
}

@Composable
private fun MainToolbar(
	userImage: String? = null,
	activebutton: MainToolbarActivebutton,
) {
	val focusRequester = remember { FocusRequester() }
	val navigationRepository = koinInject<NavigationRepository>()
	val activeButtonColors = ButtonDefaults.colors(
		containerColor = JellyfinTheme.colorScheme.buttonActive,
		contentColor = JellyfinTheme.colorScheme.onButtonActive,
	)

	Toolbar(
		modifier = Modifier
			.focusRestorer(focusRequester)
			.focusGroup()
	) {
		ToolbarButtons(
			modifier = Modifier
				.align(Alignment.CenterStart)
		) {
			val userImagePainter = rememberAsyncImagePainter(userImage)
			val userImageState by userImagePainter.state.collectAsState()
			val userImageVisible = userImageState is AsyncImagePainter.State.Success

			IconButton(
				onClick = {
					if (activebutton != MainToolbarActivebutton.USER) {
						navigationRepository.navigate(Destinations.quickSwitch)
					}
				},
				colors = if (activebutton == MainToolbarActivebutton.USER) activeButtonColors else ButtonDefaults.colors(),
				contentPadding = if (userImageVisible) PaddingValues(3.dp) else IconButtonDefaults.ContentPadding,
			) {
				Image(
					painter = if (userImageVisible) userImagePainter else painterResource(R.drawable.ic_user),
					contentDescription = stringResource(R.string.lbl_switch_user),
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.aspectRatio(1f)
						.clip(IconButtonDefaults.Shape)
				)
			}
		}

		ToolbarButtons(
			modifier = Modifier
				.align(Alignment.Center)
				.focusRequester(focusRequester)
		) {
			ProvideTextStyle(JellyfinTheme.typography.default.copy(fontWeight = FontWeight.Bold)) {
				Button(
					onClick = {
						if (activebutton != MainToolbarActivebutton.HOME) {
							navigationRepository.navigate(
								Destinations.home,
								replace = true,
							)
						}
					},
					colors = if (activebutton == MainToolbarActivebutton.HOME) activeButtonColors else ButtonDefaults.colors(),
					// TODO stringResource
					content = { Text("Home") }
				)
				Button(
					onClick = {
						if (activebutton != MainToolbarActivebutton.SEARCH) {
							navigationRepository.navigate(
								Destinations.search(),
								replace = true,
							)
						}
					},
					colors = if (activebutton == MainToolbarActivebutton.SEARCH) activeButtonColors else ButtonDefaults.colors(),
					// TODO stringResource
					content = { Text("Search") }
				)
			}
		}

		NowPlayingComposable(
			modifier = Modifier
				.align(Alignment.CenterEnd),
			onFocusableChange = {},
		)
	}
}
