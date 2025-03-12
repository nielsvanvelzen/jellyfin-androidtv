package org.jellyfin.androidtv.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import coil3.compose.rememberAsyncImagePainter
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Badge
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.LocalColorScheme
import org.jellyfin.androidtv.ui.base.LocalShapes
import org.jellyfin.androidtv.ui.base.LocalTextStyle
import org.jellyfin.androidtv.ui.base.ProvideTextStyle
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.darkColorScheme
import org.jellyfin.androidtv.ui.base.form.RadioButton
import org.jellyfin.androidtv.ui.base.lightColorScheme
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.base.modifier.childFocusRestorer
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.shared.toolbar.HomeToolbar
import org.koin.compose.koinInject

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
//			.alpha(if (navigationFocused) 1f else 0.6f)
			.background(JellyfinTheme.colorScheme.background)
			.onFocusChanged { navigationFocused = it.hasFocus },
	) {
		ProvideTextStyle(LocalTextStyle.current.copy(color = JellyfinTheme.colorScheme.onBackground)) {
			navigation()
		}
	}

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
		var useDarkTheme by remember { mutableStateOf(false) }

		Column {
			HomeToolbar(
				openSearch = {},
				openSettings = {},
				switchUsers = {},
			)
			CompositionLocalProvider(
				LocalColorScheme provides if (useDarkTheme) darkColorScheme else lightColorScheme,
				LocalDensity provides Density(LocalDensity.current.density * 0.75f, 1f)
			) {
				Box(
					modifier = Modifier,
				) {
					DuoSettingsPanel(
						navigation = {
							Column(
								modifier = Modifier
									.padding(12.dp),
								verticalArrangement = Arrangement.spacedBy(6.dp),
							) {
								ListSection(
									overlineContent = { Text("Jellyfin") },
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
									onClick = {
										useDarkTheme = !useDarkTheme
									},
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
							DuoSettingsPanel(
								navigation = {
									Column(
										modifier = Modifier
											.background(Color(0xFFF3F3F3))
											.padding(12.dp),
										verticalArrangement = Arrangement.spacedBy(6.dp),
									) {
										ListSection(
											overlineContent = { Text("Settings".uppercase()) },
											headingContent = { Text("Playback") },
											captionContent = { Text("Video, subtitles, live tv, next up") },
										)

										ListButton(
											leadingContent = { Icon(painterResource(R.drawable.ic_logout), contentDescription = null) },
											headingContent = { Text("Video player") },
											captionContent = { Text("Jellyfin (default)") },
											onClick = { navigationRepository.navigate(Destinations.userPreferences) }
										)

										repeat(10) {
											ListButton(
												leadingContent = { Icon(painterResource(R.drawable.ic_masks), contentDescription = null) },
												headingContent = { Text("Another suboption") },
												captionContent = { Text("idk") },
												onClick = { navigationRepository.navigate(Destinations.userPreferences) }
											)
										}
									}
								},

								content = {
									Column(
										modifier = Modifier
											.background(Color(0xFFE7E7E7))
											.padding(12.dp)
											.fillMaxHeight()
											.widthIn(min = 400.dp),
										verticalArrangement = Arrangement.spacedBy(6.dp),
									) {
										val packageManager = LocalContext.current.packageManager
										val packages = setOf(
											"org.jellyfin.androidtv.debug",
											"org.videolan.vlc",
											"is.xyz.mpv",
											"com.brouken.player",
											"org.courville.nova",
//										"com.google.android.youtube.tv",
										)
										var selectedPackage by remember { mutableStateOf("org.jellyfin.androidtv.debug") }

										ListSection(
											overlineContent = { Text("Playback".uppercase()) },
											headingContent = { Text("Video player") },
											captionContent = { Text("Choose which video player to use for playing media") },
										)

										for (packageName in packages) {
											val icon = remember(packageManager, packageName) {
												runCatching {
													packageManager.getApplicationIcon(packageName)
												}.getOrElse {
													packageManager.defaultActivityIcon
												}
											}
											val displayName = remember(packageManager, packageName) {
												runCatching {
													val appInfo = packageManager.getApplicationInfo(packageName, 0)
													packageManager.getApplicationLabel(appInfo).toString()
												}.getOrNull() ?: packageName
											}

											ListButton(
												leadingContent = {
													Image(
														rememberAsyncImagePainter(icon),
														contentDescription = null,
														modifier = Modifier
															.size(32.dp)
															.clip(LocalShapes.current.small)
													)
												},
												headingContent = { Text(displayName) },
												trailingContent = {
													RadioButton(
														checked = packageName == selectedPackage
													)
												},
												captionContent = {
													if (packageName != "org.jellyfin.androidtv.debug") {
														Text("External apps may behave unexpected")
													} else {
														Text("Best experience")
													}
												},
												onClick = {
													selectedPackage = packageName
												}
											)
										}
									}
								}
							)
						}
					)
				}
			}
		}
	}
}
