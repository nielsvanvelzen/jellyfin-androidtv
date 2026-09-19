package org.jellyfin.androidtv.auth.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jellyfin.sdk.model.api.UserDto
import org.koin.core.annotation.Single

/**
 * Repository to get the current authenticated user.
 */
interface UserRepository {
	val currentUser: StateFlow<UserDto?>

	fun setCurrentUser(user: UserDto?)
}

@Single(binds = [UserRepository::class])
class UserRepositoryImpl : UserRepository {
	override val currentUser = MutableStateFlow<UserDto?>(null)

	override fun setCurrentUser(user: UserDto?) {
		currentUser.value = user
	}
}
