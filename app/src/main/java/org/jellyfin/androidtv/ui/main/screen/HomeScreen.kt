package org.jellyfin.androidtv.ui.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.main.Routes
import org.jellyfin.androidtv.ui.main.composable.PrimaryLayout
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.compat.SettingsViewModel
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
fun HomeScreen() = PrimaryLayout(
	activeButton = MainToolbarActiveButton.Home,
) {
	val router = LocalRouter.current
	val settingsViewModel = koinActivityViewModel<SettingsViewModel>()

	Column(
		verticalArrangement = Arrangement.spacedBy(8.dp),
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Gray)
			.padding(8.dp)
	) {
		Text("Home page", color = Color.White)

		Button(
			onClick = {
				router.push(Routes.SEARCH)
			}
		) {
			Text("Go to search page")
		}

		Button(
			onClick = {
				settingsViewModel.show()
			}
		) {
			Text("Open settings")
		}
	}
}
