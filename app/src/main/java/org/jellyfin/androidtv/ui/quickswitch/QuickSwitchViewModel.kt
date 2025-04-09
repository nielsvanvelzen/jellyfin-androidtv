package org.jellyfin.androidtv.ui.quickswitch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.model.PrivateUser
import org.jellyfin.androidtv.auth.repository.AuthenticationRepository
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.ServerUserRepository
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository

class QuickSwitchViewModel(
	private val authenticationRepository: AuthenticationRepository,
	private val sessionRepository: SessionRepository,
	private val serverRepository: ServerRepository,
	private val serverUserRepository: ServerUserRepository,
	private val navigationRepository: NavigationRepository,
) : ViewModel() {
	private val _state = MutableStateFlow<QuickSwitchState?>(null)
	val state = _state.asStateFlow()

	init {
		viewModelScope.launch {
			sessionRepository.currentSession
				.filterNotNull()
				.collect { update() }
		}
	}

	private suspend fun update() {
		val session = sessionRepository.currentSession.value
		val server = session?.serverId?.let { serverId -> serverRepository.getServer(serverId) }
		requireNotNull(server)

		val recentUsers = serverUserRepository.getRecentlyUsedUsers()
			.filter { user -> user.serverId != session.serverId || user.id != session.userId }
			.take(3)
			.takeIf { it.size > 1 }
			.orEmpty()

		_state.value = QuickSwitchState(
			recentUsers,
			server,
		)
	}

	fun switchUser(user: PrivateUser) {
		viewModelScope.launch {
			val canSwitch = sessionRepository.canSwitchCurrentSession(user.serverId, user.id)
			var switched = false
			if (canSwitch) switched = sessionRepository.switchCurrentSession(user.serverId, user.id)

			if (switched) navigationRepository.reset(Destinations.home)
			// else open login fragment?
		}
	}
}
