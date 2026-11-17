package org.jellyfin.androidtv.data.model.home

import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.BaseItemDto

class HomeRow(
	val key: String,
	val title: HomeRowTitle,
	val presentation: HomeRowPresentation = HomeRowPresentation(),
	private val load: suspend () -> List<HomeRowItem>,
) {
	suspend fun loadItems(): List<HomeRowItem> = withContext(Dispatchers.IO) {
		load().distinctBy { it.key }
	}

	companion object {
		fun media(
			key: String,
			title: HomeRowTitle,
			presentation: HomeRowPresentation = HomeRowPresentation(),
			load: suspend () -> List<BaseItemDto>,
		) = HomeRow(key, title, presentation) { load().map(HomeRowItem::Media) }
	}
}

data class HomeRowTitle(@param:StringRes val resourceId: Int, val arguments: List<String> = emptyList())

data class HomeRowPresentation(
	val buttons: Boolean = false,
	val posters: Boolean = false,
	val preferSeriesPoster: Boolean = false,
)

data class HomeRows(val rows: List<HomeRow>, val libraries: List<BaseItemDto>)
