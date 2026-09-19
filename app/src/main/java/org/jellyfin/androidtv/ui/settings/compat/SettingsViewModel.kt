package org.jellyfin.androidtv.ui.settings.compat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel : ViewModel() {
	private val _visible = MutableStateFlow(false)
	val visible get() = _visible.asStateFlow()

	fun show() {
		_visible.value = true
	}

	fun hide() {
		_visible.value = false
	}
}
