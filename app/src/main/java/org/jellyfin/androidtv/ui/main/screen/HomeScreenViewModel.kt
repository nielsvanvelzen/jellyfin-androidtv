package org.jellyfin.androidtv.ui.main.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jellyfin.androidtv.data.repository.UserViewsRepository

class HomeScreenViewModel(
	userViewsRepository: UserViewsRepository,
) : ViewModel() {
	val userViews = userViewsRepository.views
		.map { it.toList() }
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5_000),
			emptyList(),
		)
}
