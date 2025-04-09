package org.jellyfin.androidtv.ui.quickswitch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.model.ApiClientErrorLoginState
import org.jellyfin.androidtv.auth.model.AuthenticatedState
import org.jellyfin.androidtv.auth.model.AuthenticatingState
import org.jellyfin.androidtv.auth.model.AutomaticAuthenticateMethod
import org.jellyfin.androidtv.auth.model.PrivateUser
import org.jellyfin.androidtv.auth.model.RequireSignInState
import org.jellyfin.androidtv.auth.model.ServerUnavailableState
import org.jellyfin.androidtv.auth.model.ServerVersionNotSupported
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
				.collect { update() }
		}
	}

	private suspend fun update() {
		val session = sessionRepository.currentSession.value
		val server = session?.serverId?.let { serverId -> serverRepository.getServer(serverId) }
		requireNotNull(server)

		val recentUsers = serverUserRepository.getRecentlyUsedUsers()
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
			val server = serverRepository.getServer(user.serverId)
			requireNotNull(server)

			authenticationRepository.authenticate(server, AutomaticAuthenticateMethod(user))
				.collect { state ->
					when (state) {
						AuthenticatedState -> navigationRepository.reset(Destinations.home)
						is ApiClientErrorLoginState,
						AuthenticatingState,
						RequireSignInState,
						ServerUnavailableState,
						is ServerVersionNotSupported -> {
							// TODO: If we cannot directly authenticate we should redirect to startup activity
							// ideally without starting this entire flow too
						}
					}
				}
		}
	}
}
