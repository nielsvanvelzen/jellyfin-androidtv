package org.jellyfin.androidtv.ui.main.screen

import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.model.home.HomeRowItem
import java.util.UUID

sealed interface HomeScreenUiState {
	data object Loading : HomeScreenUiState
	data object Error : HomeScreenUiState

	data class Content(
		val rows: List<HomeRowUiState>,
		val libraryIds: Set<UUID>,
	) : HomeScreenUiState {
		val visibleRows: List<HomeRowUiState>
			get() = rows.filterNot { it is HomeRowUiState.Content && it.items.isEmpty() }

		val isEmpty: Boolean
			get() = rows.all { it is HomeRowUiState.Content && it.items.isEmpty() }

		val isLoadingRows: Boolean
			get() = rows.any { it is HomeRowUiState.Loading }
	}
}

sealed interface HomeRowUiState {
	val row: HomeRow
	val key: String get() = row.key

	data class Loading(override val row: HomeRow) : HomeRowUiState
	data class Content(override val row: HomeRow, val items: List<HomeRowItem>) : HomeRowUiState
	data class Error(override val row: HomeRow) : HomeRowUiState
}
