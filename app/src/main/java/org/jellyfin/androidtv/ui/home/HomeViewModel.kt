package org.jellyfin.androidtv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.model.api.BaseItemDto

data class HomeRow(
	val items: List<BaseItemDto>,
)

class HomeViewModel(
	val userRepository: UserRepository,
	val userSettingPreferences: UserSettingPreferences,
	val api: ApiClient,
) : ViewModel() {
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
		// TODO
		HomeSectionType.LATEST_MEDIA -> emptyList()
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
				imageTypeLimit = 1,
				limit = 50,
				enableResumable = false,
				fields = ItemRepository.itemFields
			)
			listOf(HomeRow(result.items))
		}
		// TODO
		HomeSectionType.LIVE_TV -> emptyList()
		HomeSectionType.NONE -> emptyList()
	}
}
