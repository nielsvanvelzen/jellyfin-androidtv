package org.jellyfin.androidtv.ui.main.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.auth.repository.SessionRepositoryState
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.model.home.HomeRows
import org.jellyfin.androidtv.data.repository.HomeRowsRepository
import org.jellyfin.sdk.model.api.UserDto
import timber.log.Timber

class HomeScreenViewModel(
	private val homeRowsRepository: HomeRowsRepository,
	private val userRepository: UserRepository,
	private val sessionRepository: SessionRepository,
) : ViewModel() {
	private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Loading)
	val uiState = _uiState.asStateFlow()

	private var loadJob: Job? = null
	private var rowLoader: RowLoader? = null

	init {
		viewModelScope.launch {
			combine(userRepository.currentUser, sessionRepository.currentSession, sessionRepository.state) { user, session, state ->
				user?.takeIf { state == SessionRepositoryState.READY && it.id == session?.userId }
			}.collect(::loadHome)
		}
	}

	fun retry() {
		val user = userRepository.currentUser.value?.takeIf {
			sessionRepository.state.value == SessionRepositoryState.READY && it.id == sessionRepository.currentSession.value?.userId
		}
		loadHome(user)
	}

	fun retryRow(key: String) {
		rowLoader?.refresh(key)
	}

	fun refreshRows() {
		rowLoader?.refreshAll()
	}

	private fun loadHome(user: UserDto?) {
		loadJob?.cancel()
		loadJob = null
		rowLoader = null
		_uiState.value = HomeScreenUiState.Loading
		if (user == null) return

		loadJob = viewModelScope.launch {
			try {
				val home = homeRowsRepository.load(user)
				currentCoroutineContext().ensureActive()
				coroutineScope {
					val loader = RowLoader(this, home)
					rowLoader = loader
					_uiState.value = HomeScreenUiState.Content(
						rows = home.rows.map(HomeRowUiState::Loading),
						libraryIds = home.libraries.mapTo(mutableSetOf()) { it.id },
					)
					loader.refreshAll()
					// Keep row refreshes attached to this load until retry, session change, or disposal.
					awaitCancellation()
				}
			} catch (err: CancellationException) {
				throw err
			} catch (err: Exception) {
				currentCoroutineContext().ensureActive()
				Timber.e(err, "Unable to load home rows")
				rowLoader = null
				_uiState.value = HomeScreenUiState.Error
			}
		}
	}

	private fun updateRow(updated: HomeRowUiState) {
		_uiState.update { current ->
			if (current !is HomeScreenUiState.Content) return@update current
			current.copy(rows = current.rows.map { row ->
				when {
					row.key != updated.key -> row
					// Keep existing cards in place while refreshing so focus and scroll do not jump.
					updated is HomeRowUiState.Loading && row is HomeRowUiState.Content -> row
					else -> updated
				}
			})
		}
	}

	private inner class RowLoader(
		private val scope: CoroutineScope,
		private val home: HomeRows,
	) {
		private val semaphore = Semaphore(MAX_CONCURRENT_ROW_REQUESTS)
		private val jobs = mutableMapOf<String, Job>()

		fun refreshAll() {
			home.rows.forEach { refresh(it.key) }
		}

		fun refresh(key: String) {
			val row = home.rows.find { it.key == key } ?: return
			jobs[key]?.cancel()
			jobs[key] = scope.launch { loadItems(row) }
		}

		private suspend fun loadItems(row: HomeRow) {
			updateRow(HomeRowUiState.Loading(row))
			val result = try {
				semaphore.withPermit {
					HomeRowUiState.Content(row, row.loadItems())
				}
			} catch (err: CancellationException) {
				throw err
			} catch (err: Exception) {
				Timber.e(err, "Unable to load home row %s", row.key)
				HomeRowUiState.Error(row)
			}
			currentCoroutineContext().ensureActive()
			updateRow(result)
		}
	}

	private companion object {
		const val MAX_CONCURRENT_ROW_REQUESTS = 4
	}
}
