package org.jellyfin.androidtv.data.repository.home

import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.model.home.HomeRowContext
import org.jellyfin.androidtv.data.model.home.HomeRowPresentation
import org.jellyfin.androidtv.data.model.home.HomeRowProvider
import org.jellyfin.androidtv.data.model.home.HomeRowTitle

class LibraryHomeRowProvider : HomeRowProvider {
	override val sections = setOf(HomeSectionType.LIBRARY_TILES_SMALL, HomeSectionType.LIBRARY_BUTTONS)

	override fun createRows(context: HomeRowContext) = listOf(
		HomeRow.media(
			key = context.key,
			title = HomeRowTitle(R.string.home_section_library),
			presentation = HomeRowPresentation(buttons = context.section == HomeSectionType.LIBRARY_BUTTONS),
		) {
			context.libraries.filterNot { it.id in context.user.configuration?.myMediaExcludes.orEmpty() }
		}
	)
}
