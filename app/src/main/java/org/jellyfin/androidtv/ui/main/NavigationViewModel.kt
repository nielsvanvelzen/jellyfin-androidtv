package org.jellyfin.androidtv.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.auth.repository.SessionRepositoryState
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationViewModel(
	userViewsRepository: UserViewsRepository,
	sessionRepository: SessionRepository,
) : ViewModel() {
	val userViews = combine(sessionRepository.currentSession, sessionRepository.state) { session, state ->
		session.takeIf { state == SessionRepositoryState.READY }
	}.flatMapLatest { session ->
		if (session == null) flowOf(emptyList())
		else userViewsRepository.views
			.map { it.toList() }
			.catch {
				Timber.e(it, "Unable to load navigation libraries")
				emit(emptyList())
			}
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
