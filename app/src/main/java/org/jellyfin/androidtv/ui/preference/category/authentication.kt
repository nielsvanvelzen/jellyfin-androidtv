package org.jellyfin.androidtv.ui.preference.category

import org.jellyfin.androidtv.auth.AuthenticationRepository
import org.jellyfin.androidtv.preference.AuthenticationPreferences
import org.jellyfin.androidtv.ui.preference.dsl.OptionsScreen
import org.jellyfin.androidtv.ui.preference.dsl.userSelector

fun OptionsScreen.authenticationCategory(
	authenticationRepository: AuthenticationRepository,
	authenticationPreferences: AuthenticationPreferences
) = category {
	title = "Preferences"

	userSelector(authenticationRepository) {
		title = "Automatically login"
	}

	userSelector(authenticationRepository) {
		title = "Service user"
	}
}
