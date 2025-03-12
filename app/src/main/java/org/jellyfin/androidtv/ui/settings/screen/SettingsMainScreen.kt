package org.jellyfin.androidtv.ui.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Badge
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.navigation.ActivityDestinations
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.koin.compose.koinInject

@Composable
fun SettingsMainScreen() {
	val context = LocalContext.current
	val navigationRepository = koinInject<NavigationRepository>()

	Column(
		modifier = Modifier
			.verticalScroll(rememberScrollState())
			.padding(6.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp),
	) {
		ListSection(
			modifier = Modifier,
			overlineContent = { Text(stringResource(R.string.app_name).uppercase()) },
			headingContent = { Text("Settings") },
			captionContent = { Text("Change the app to your own liking") },
		)

		ListButton(
			leadingContent = {
				Icon(
					painterResource(R.drawable.ic_users),
					contentDescription = null
				)
			},
			headingContent = { Text(stringResource(R.string.pref_login)) },
			captionContent = { Text(stringResource(R.string.pref_login_description)) },
			onClick = {
				context.startActivity(ActivityDestinations.userPreferences(context))
			}
		)

		ListButton(
			leadingContent = {
				Icon(
					painterResource(R.drawable.ic_adjust),
					contentDescription = null
				)
			},
			headingContent = { Text(stringResource(R.string.pref_customization)) },
			captionContent = { Text(stringResource(R.string.pref_customization_description)) },
			onClick = { navigationRepository.reset(Destinations.home) }
		)

		ListButton(
			leadingContent = {
				Icon(
					painterResource(R.drawable.ic_next),
					contentDescription = null
				)
			},
			headingContent = { Text(stringResource(R.string.pref_playback)) },
			captionContent = { Text(stringResource(R.string.pref_playback_description)) },
			onClick = { },
			trailingContent = {
				Badge {
					Text("99+")
				}
			}
		)

		ListButton(
			leadingContent = {
				Icon(
					painterResource(R.drawable.ic_error),
					contentDescription = null
				)
			},
			headingContent = { Text(stringResource(R.string.pref_telemetry_category)) },
			captionContent = { Text(stringResource(R.string.pref_telemetry_description)) },
			onClick = {}
		)

		ListButton(
			leadingContent = {
				Icon(
					painterResource(R.drawable.ic_flask),
					contentDescription = null
				)
			},
			headingContent = { Text(stringResource(R.string.pref_developer_link)) },
			captionContent = { Text(stringResource(R.string.pref_developer_link_description)) },
			onClick = {}
		)
	}
}
