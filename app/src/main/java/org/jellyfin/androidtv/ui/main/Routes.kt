package org.jellyfin.androidtv.ui.main

import org.jellyfin.androidtv.ui.main.screen.HomeScreen
import org.jellyfin.androidtv.ui.main.screen.SearchScreen
import org.jellyfin.androidtv.ui.navigation.RouteComposable

object Routes {
	const val MAIN = "/"
	const val SEARCH = "/search"
}


val routes = mapOf<String, RouteComposable>(
	Routes.MAIN to { HomeScreen() },
	Routes.SEARCH to { SearchScreen() },
)
