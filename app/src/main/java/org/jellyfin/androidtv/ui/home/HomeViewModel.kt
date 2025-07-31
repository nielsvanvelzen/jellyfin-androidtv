package org.jellyfin.androidtv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.CollectionType

data class HomeRow(
	val title: String,
	val items: List<BaseItemDto>,
)

class HomeViewModel(
	val userRepository: UserRepository,
	val userViewsRepository: UserViewsRepository,
	val userSettingPreferences: UserSettingPreferences,
	val api: ApiClient,
) : ViewModel() {
	companion object {
		// Collections excluded from latest row based on app support and common sense
		private val EXCLUDED_COLLECTION_TYPES = arrayOf(
			CollectionType.PLAYLISTS,
			CollectionType.LIVETV,
			CollectionType.BOXSETS,
			CollectionType.BOOKS,
		)

		// Maximum amount of items loaded for a row
		private const val ITEM_LIMIT = 50
	}

	data class State(
		val rows: List<HomeRow> = emptyList(),
	)

	private var _state = MutableStateFlow(State())
	val state get() = _state.asStateFlow()

	init {
		update()
	}

	fun update() = viewModelScope.launch(Dispatchers.IO) {
		val rows = userSettingPreferences.activeHomesections
			.map { type -> async { loadSection(type) } }
			.awaitAll()
			.flatten()

		// TODO: Add client notifications row
		// TODO: Add client now playing row
		_state.value = State(
			rows = rows,
		)
	}

	private suspend fun loadSection(type: HomeSectionType): List<HomeRow> = when (type) {
		HomeSectionType.LATEST_MEDIA -> {
			val latestItemsExcludes = userRepository.currentUser.value?.configuration?.latestItemsExcludes.orEmpty()

			withContext(Dispatchers.IO) {
				userViewsRepository.views
					.first()
					.filterNot { item -> item.collectionType in EXCLUDED_COLLECTION_TYPES || item.id in latestItemsExcludes }
					.map { userView ->
						async {
							val result by api.userLibraryApi.getLatestMedia(
								limit = ITEM_LIMIT,
								groupItems = true,
								parentId = userView.id,
								fields = ItemRepository.itemFields,
							)

							HomeRow("Latest ${userView.name}", result)
						}
					}.awaitAll()
			}
		}
		// TODO
		HomeSectionType.LIBRARY_TILES_SMALL,
		HomeSectionType.LIBRARY_BUTTONS -> emptyList()
		// TODO
		HomeSectionType.RESUME,
		HomeSectionType.RESUME_AUDIO,
		HomeSectionType.RESUME_BOOK -> emptyList()
		// TODO
		HomeSectionType.ACTIVE_RECORDINGS -> emptyList()
		// TODO
		HomeSectionType.NEXT_UP -> {
			val result by api.tvShowsApi.getNextUp(
				limit = ITEM_LIMIT,
				enableResumable = false,
				fields = ItemRepository.itemFields
			)
			listOf(HomeRow("Next up", result.items))
		}
		// TODO
		HomeSectionType.LIVE_TV -> emptyList()
		HomeSectionType.NONE -> emptyList()
	}
}
