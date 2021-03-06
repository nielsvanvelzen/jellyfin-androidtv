package org.jellyfin.androidtv.ui.preference.screen

import androidx.core.os.bundleOf
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.AuthenticationRepository
import org.jellyfin.androidtv.preference.AuthenticationPreferences
import org.jellyfin.androidtv.ui.preference.category.authenticationCategory
import org.jellyfin.androidtv.ui.preference.dsl.OptionsFragment
import org.jellyfin.androidtv.ui.preference.dsl.lazyOptionsScreen
import org.jellyfin.androidtv.ui.preference.dsl.link
import org.jellyfin.androidtv.ui.startup.preference.EditServerScreen
import org.koin.android.ext.android.inject

class AuthPreferencesScreen : OptionsFragment() {
	private val authenticationRepository: AuthenticationRepository by inject()
	private val authenticationPreferences: AuthenticationPreferences by inject()

	override val screen by lazyOptionsScreen {
		setTitle(R.string.pref_authentication_cat)

		authenticationCategory(authenticationRepository, authenticationPreferences)

		category {
			setTitle(R.string.lbl_manage_servers)

			authenticationRepository.getServers().forEach { server ->
				link {
					title = server.name
					icon = R.drawable.ic_cloud
					content = server.address
					withFragment<EditServerScreen>(bundleOf(
						EditServerScreen.ARG_SERVER_UUID to server.id
					))
				}
			}
		}
	}
}
