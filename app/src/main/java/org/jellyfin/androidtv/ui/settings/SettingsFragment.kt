package org.jellyfin.androidtv.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Badge
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.base.modifier.childFocusRestorer
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.koin.compose.koinInject

@Composable
fun SettingsHeading(
	text: @Composable () -> Unit,
) {
	text()
}

/**
 * Settings panel that has navigation on one side and content on the other. Can be nested for sub-navigation.
 */
@Composable
fun DuoSettingsPanel(
	navigation: @Composable BoxScope.() -> Unit,
	content: @Composable BoxScope.() -> Unit
) = Row {
	var navigationFocused by remember { mutableStateOf(false) }

	Box(
		modifier = Modifier
			.width(350.dp)
			.fillMaxHeight()
			.verticalScroll(rememberScrollState())
			.childFocusRestorer()
//			.background(Color.Black.copy(alpha = if (navigationFocused) 0.6f else 0.2f)),
			.background(Color(0xFFFCFCFD))
			.onFocusChanged { navigationFocused = it.hasFocus },
		content = navigation,
	)

	Box(
		modifier = Modifier.weight(1f),
		content = content,
	)
}


class SettingsFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	) = content {
		val navigationRepository = koinInject<NavigationRepository>()

		Box(
			modifier = Modifier
		) {
			DuoSettingsPanel(
				navigation = {
					Column(
						modifier = Modifier
							.padding(12.dp),
						verticalArrangement = Arrangement.spacedBy(6.dp),
					) {
						ListSection(
							overlineContent = { Text("Overline".uppercase()) },
							headingContent = { Text("Settings") },
							captionContent = { Text("Change the app to your own liking") },
						)

						ListButton(
							leadingContent = { Icon(painterResource(R.drawable.ic_users), contentDescription = null) },
							headingContent = { Text(stringResource(R.string.pref_login)) },
							captionContent = { Text(stringResource(R.string.pref_login_description)) },
							onClick = { navigationRepository.navigate(Destinations.userPreferences) }
						)

						ListButton(
							leadingContent = { Icon(painterResource(R.drawable.ic_adjust), contentDescription = null) },
							headingContent = { Text(stringResource(R.string.pref_customization)) },
							captionContent = { Text(stringResource(R.string.pref_customization_description)) },
							onClick = { navigationRepository.reset(Destinations.home) }
						)

						ListButton(
							leadingContent = { Icon(painterResource(R.drawable.ic_next), contentDescription = null) },
							headingContent = { Text(stringResource(R.string.pref_playback)) },
							captionContent = { Text(stringResource(R.string.pref_playback_description)) },
							onClick = {},
							trailingContent = {
								Badge {
									Text("99+")
								}
							}
						)

						ListButton(
							leadingContent = { Icon(painterResource(R.drawable.ic_error), contentDescription = null) },
							headingContent = { Text(stringResource(R.string.pref_telemetry_category)) },
							captionContent = { Text(stringResource(R.string.pref_telemetry_description)) },
							onClick = {}
						)

						ListButton(
							leadingContent = { Icon(painterResource(R.drawable.ic_flask), contentDescription = null) },
							headingContent = { Text(stringResource(R.string.pref_developer_link)) },
							captionContent = { Text(stringResource(R.string.pref_developer_link_description)) },
							onClick = {}
						)

//						ListItem(
//							headingContent = { Text("Open the old menu") },
//							onClick = { navigationRepository.navigate(Destinations.userPreferences) }
//						)
					}
				},

				content = {
					Text("Test")
				}
			)
		}
	}
}
