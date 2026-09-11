package org.jellyfin.androidtv.ui.main.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.main.Routes
import org.jellyfin.androidtv.ui.navigation.LocalRouter

@Composable
fun SearchScreen() {
	val router = LocalRouter.current

	NavigationLayout(
		navigation = { Navigation() },
	) {
		Row {
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
}
