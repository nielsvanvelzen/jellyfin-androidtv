package org.jellyfin.androidtv.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import timber.log.Timber

/**
 * Repository for app navigation. This manages the screens/pages for the app.
 */
interface NavigationRepository {
	val history: List<Destination>

	/**
	 * Navigate to [destination].
	 *
	 * @see Destinations
	 */
	fun navigate(destination: Destination) = navigate(destination, false)

	/**
	 * Navigate to [destination].
	 *
	 * @see Destinations
	 */
	fun navigate(destination: Destination, replace: Boolean)

	/**
	 * Whether the [goBack] function will succeed or not.
	 *
	 * @see [goBack]
	 */
	val canGoBack: Boolean

	/**
	 * Go back to the previous fragment. The back stack does not consider other destination types.
	 *
	 * @see [canGoBack]
	 */
	fun goBack(): Boolean

	/**
	 * Reset navigation to the initial destination or a specific [Destination].
	 *
	 * @param clearHistory Empty out the back stack
	 */
	fun reset(destination: Destination? = null, clearHistory: Boolean)

	/**
	 * Reset navigation to the initial destination or a specific [Destination] without clearing history.
	 */
	fun reset(destination: Destination? = null) = reset(destination, false)
}

class NavigationRepositoryImpl(
	private val defaultDestination: Destination,
) : NavigationRepository {
	private val _history = mutableStateListOf(defaultDestination)
	override val history = _history

	override fun navigate(destination: Destination, replace: Boolean) {
		Timber.i("Navigating to $destination (via navigate function)")

		if (replace && _history.isNotEmpty()) _history[_history.size - 1] = destination
		else _history += destination
	}

	override val canGoBack: Boolean get() = _history.isNotEmpty()

	override fun goBack(): Boolean {
		if (_history.isEmpty()) return false

		Timber.i("Navigating back")
		_history.removeAt(_history.size - 1)
		return true
	}

	override fun reset(destination: Destination?, clearHistory: Boolean) {
		val actualDestination = destination ?: defaultDestination
		Timber.i("Navigating to $actualDestination (via reset, clearHistory=$clearHistory)")
		_history.clear()
		_history += actualDestination
	}
}

