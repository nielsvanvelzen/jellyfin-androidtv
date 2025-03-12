package org.jellyfin.androidtv.ui.settings.screen.playback

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import org.jellyfin.androidtv.ui.base.LocalShapes
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.form.RadioButton
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection

@Composable
fun SettingsPlaybackExternalAppScreen() {
	Column(
		modifier = Modifier
			.verticalScroll(rememberScrollState())
			.padding(6.dp)
			.fillMaxHeight()
			.widthIn(min = 400.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp),
	) {
		val packageManager = LocalContext.current.packageManager
		val packages = packageManager.getInstalledApplications(0)
			.map { it.packageName }
			.distinct()
//		val packages = setOf(
//			"org.jellyfin.androidtv.debug",
//			"org.videolan.vlc",
//			"is.xyz.mpv",
//			"com.brouken.player",
//			"org.courville.nova",
////			"com.google.android.youtube.tv",
//		)
		var selectedPackage by remember { mutableStateOf("org.jellyfin.androidtv.debug") }

		ListSection(
			overlineContent = { Text("Playback".uppercase()) },
			headingContent = { Text("Video player") },
			captionContent = { Text("Choose which video player to use for playing media") },
		)

		for (packageName in packages) {
			val icon = remember(packageManager, packageName) {
				runCatching { packageManager.getApplicationIcon(packageName) }
					.getOrElse { packageManager.defaultActivityIcon }
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
