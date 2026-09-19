package org.jellyfin.androidtv.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jellyfin.androidtv.constant.CustomMessage
import org.koin.core.annotation.Single

interface CustomMessageRepository {
	val message: StateFlow<CustomMessage?>
	fun pushMessage(message: CustomMessage)
}

@Single(binds = [CustomMessageRepository::class])
class CustomMessageRepositoryImpl : CustomMessageRepository {
	private val _message = MutableStateFlow<CustomMessage?>(null)
	override val message get() = _message.asStateFlow()

	override fun pushMessage(message: CustomMessage) {
		// Make sure to re-emit the same message if requested
		if (_message.value == message) _message.value = null

		_message.value = message
	}
}
