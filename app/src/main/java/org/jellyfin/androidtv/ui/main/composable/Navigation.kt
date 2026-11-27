package org.jellyfin.androidtv.ui.main.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncherHelper
import org.jellyfin.androidtv.ui.main.NavigationViewModel
import org.jellyfin.androidtv.ui.main.Routes
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.navigation.focus.focusKey
import org.jellyfin.androidtv.ui.settings.compat.SettingsViewModel
import org.jellyfin.sdk.model.api.CollectionType
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
fun Navigation(initialFocus: Boolean = false) {
	val viewModel = koinViewModel<NavigationViewModel>()
	val userViews by viewModel.userViews.collectAsStateWithLifecycle()
	val router = LocalRouter.current
	val settingsViewModel = koinActivityViewModel<SettingsViewModel>()
	val context = LocalContext.current

	Image(
		painter = painterResource(R.drawable.ic_jellyfin),
		contentDescription = null,
		modifier = Modifier
			.size(64.dp)
			.padding(8.dp)
	)

	Column(
		verticalArrangement = Arrangement.spacedBy(4.dp),
		modifier = Modifier
			.padding(4.dp)
			.focusKey("navigation")
			.focusRestorer()
			.verticalScroll(rememberScrollState())
	) {
		Button(
			onClick = { router.push(Routes.SEARCH) },
			colors = ButtonDefaults.colors(containerColor = Color.Transparent),
			modifier = Modifier
				.fillMaxWidth()
				.focusKey("search")
		) {
			Icon(
				painter = painterResource(R.drawable.ic_search),
				contentDescription = stringResource(R.string.lbl_search),
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(text = stringResource(R.string.lbl_search), softWrap = false, overflow = TextOverflow.Ellipsis)
		}

		Button(
			onClick = { router.push(Routes.MAIN) },
			colors = ButtonDefaults.colors(containerColor = Color.Transparent),
			modifier = Modifier
				.fillMaxWidth()
				.focusKey("home", initialFocus = initialFocus)
		) {
			Icon(
				painter = painterResource(R.drawable.ic_house),
				contentDescription = stringResource(R.string.lbl_home),
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(text = stringResource(R.string.lbl_home), softWrap = false, overflow = TextOverflow.Ellipsis)
		}

		userViews.forEach { userView ->
			Button(
				onClick = { ItemLauncherHelper.launchUserView(context, userView) },
				colors = ButtonDefaults.colors(containerColor = Color.Transparent),
				modifier = Modifier
					.fillMaxWidth()
					.focusKey("library_${userView.id}"),
			) {
				Icon(
					painter = painterResource(userView.collectionType.libraryIcon),
					contentDescription = userView.name,
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = userView.name.orEmpty(),
					softWrap = false,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}

		Spacer(Modifier.weight(1f))

		Button(
			onClick = { settingsViewModel.show() },
			colors = ButtonDefaults.colors(containerColor = Color.Transparent),
			modifier = Modifier
				.fillMaxWidth()
				.focusKey("settings")
		) {
			Icon(
				painter = painterResource(R.drawable.ic_settings),
				contentDescription = stringResource(R.string.settings),
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(text = stringResource(R.string.settings), softWrap = false, overflow = TextOverflow.Ellipsis)
		}
	}
}

internal val CollectionType?.libraryIcon: Int
	@DrawableRes get() = when (this) {
		CollectionType.MOVIES -> R.drawable.ic_movie
		CollectionType.MUSIC -> R.drawable.ic_music_note
		CollectionType.HOMEVIDEOS,
		CollectionType.PHOTOS -> R.drawable.ic_photo

		CollectionType.LIVETV -> R.drawable.ic_tv_play
		CollectionType.TVSHOWS -> R.drawable.ic_tv
		CollectionType.TRAILERS -> R.drawable.ic_trailer
		CollectionType.MUSICVIDEOS -> R.drawable.ic_music_video
		CollectionType.BOXSETS -> R.drawable.ic_video_library
		CollectionType.PLAYLISTS -> R.drawable.ic_queue

		null,
		CollectionType.UNKNOWN -> R.drawable.ic_help

		else -> R.drawable.ic_folder
	}
