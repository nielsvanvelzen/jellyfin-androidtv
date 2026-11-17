package org.jellyfin.androidtv.data.model.home

import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.UserDto

/**
 * Creates the rows for a configured home section, including their titles, presentation, and item loaders.
 * A section can expand into several rows, for example recently added media for each library.
 */
interface HomeRowProvider {
	val sections: Set<HomeSectionType>

	fun createRows(context: HomeRowContext): List<HomeRow>
}

data class HomeRowContext(
	val key: String,
	val section: HomeSectionType,
	val user: UserDto,
	val libraries: List<BaseItemDto>,
)
