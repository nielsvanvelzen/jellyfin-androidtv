package org.jellyfin.androidtv.ui.settings.screen.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.navigation.ActivityDestinations

@Composable
fun SettingsPlaybackMainScreen() {
	val context = LocalContext.current

	Column(
		modifier = Modifier
			.verticalScroll(rememberScrollState())
			.padding(6.dp)
			.fillMaxHeight(),
		verticalArrangement = Arrangement.spacedBy(4.dp),
	) {
		ListSection(
			overlineContent = { Text("Settings".uppercase()) },
			headingContent = { Text("Playback") },
			captionContent = { Text("Video, subtitles, live tv, next up") },
		)

		ListButton(
			leadingContent = {
				Icon(
					painterResource(R.drawable.ic_logout),
					contentDescription = null
				)
			},
			headingContent = { Text("Video player") },
			captionContent = { Text("Jellyfin (default)") },
			onClick = {
				context.startActivity(ActivityDestinations.userPreferences(context))
			}
		)

		repeat(10) {
			ListButton(
				leadingContent = {
					Icon(
						painterResource(R.drawable.ic_masks),
						contentDescription = null
					)
				},
				headingContent = { Text("Another suboption") },
				captionContent = { Text("idk") },
				onClick = {
					context.startActivity(ActivityDestinations.userPreferences(context))
				}
			)
		}
	}
}
