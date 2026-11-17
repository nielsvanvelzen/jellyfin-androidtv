package org.jellyfin.androidtv.data.repository

import kotlinx.coroutines.flow.first
import org.jellyfin.androidtv.data.model.home.HomeRowContext
import org.jellyfin.androidtv.data.model.home.HomeRowProvider
import org.jellyfin.androidtv.data.model.home.HomeRows
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.sdk.model.api.UserDto
import java.io.IOException

class HomeRowsRepository(
	private val userSettingPreferences: UserSettingPreferences,
	private val userViewsRepository: UserViewsRepository,
	providers: List<HomeRowProvider>,
) {
	private val providers = buildMap {
		providers.forEach { provider ->
			provider.sections.forEach { section ->
				check(put(section, provider) == null) { "Multiple home row providers for $section" }
			}
		}
	}

	suspend fun load(user: UserDto): HomeRows {
		if (!userSettingPreferences.update()) throw IOException("Unable to load home sections")
		val libraries = userViewsRepository.views.first().toList()
		val rows = userSettingPreferences.activeHomesections.flatMapIndexed { index, section ->
			val context = HomeRowContext("$index:${section.serializedName}", section, user, libraries)
			providers[section]?.createRows(context).orEmpty()
		}
		return HomeRows(rows, libraries)
	}
}
