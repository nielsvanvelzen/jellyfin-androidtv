package org.jellyfin.androidtv.ui.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.main.Routes
import org.jellyfin.androidtv.ui.main.composable.PrimaryLayout
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton

@Composable
fun SearchScreen() = PrimaryLayout(
	activeButton = MainToolbarActiveButton.Search,
) {
	val router = LocalRouter.current

	Row(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Magenta)
	) {
		Text("Search page", color = Color.White)

		Button(
			onClick = {
				router.push(Routes.MAIN)
			}
		) {
			Text("Go to main page")
		}
	}
}
