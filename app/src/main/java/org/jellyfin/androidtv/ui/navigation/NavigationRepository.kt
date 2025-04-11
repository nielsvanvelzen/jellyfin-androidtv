package org.jellyfin.androidtv.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.Stack
import kotlin.reflect.KClass

interface RouteBuildTarget
interface Routes

interface RouteBuildContext : RouteBuildTarget {
	infix fun String.with(target: RouteBuildTarget)

	fun routes(context: RouteBuildContext.() -> Unit): RouteBuildContext
	fun composable(content: @Composable BoxScope.() -> Unit): RouteBuildTarget
	fun fragment(fragment: KClass<out Fragment>): RouteBuildTarget

	fun alias(path: String): RouteBuildTarget
}

inline fun <reified T : Fragment> RouteBuildContext.fragment() = fragment(T::class)

fun createRoutes(context: RouteBuildContext.() -> Unit) {}

val routes = createRoutes {
	"/startup" with routes {
		"/" with alias("/home")
	}

	"/home" with routes {
		"/" with alias("/overview")

		"/overview" with composable {}
		"/search/:query?" with composable {}
	}

	"/browse" with routes {}
}

class RouterHistoryDestination(
	val destination: Destination.Fragment,
	val state: SavedStateHandle = SavedStateHandle(),
)

class Router(
	val defaultDestination: Destination.Fragment,
) {
	private val _history = Stack<RouterHistoryDestination>()

	private val _canGoBack = MutableStateFlow(false)
	val canGoBack = _canGoBack.asStateFlow()

	private val _currentFragment = MutableStateFlow<Destination.Fragment?>(null)
	val currentFragment = _currentFragment.asStateFlow()

	private val _content = MutableStateFlow<RouterHistoryDestination?>(null)
	internal val content = _content.asStateFlow()

	init {
		navigate(defaultDestination, false)
	}

	private fun updateState() {
		_canGoBack.value = _history.isNotEmpty()
		_content.value = _history.lastOrNull()
		_currentFragment.value = _content.value?.destination
	}

	fun navigate(destination: Destination.Fragment, replace: Boolean = false) {
		val historyDestination = RouterHistoryDestination(destination)
		if (replace && _history.isNotEmpty()) _history[_history.lastIndex] = historyDestination
		else _history.push(historyDestination)

		updateState()
	}

	fun goBack(): Boolean {
		if (_history.empty()) return false

		Timber.d("Navigating back")
		_history.pop()
		updateState()
		return true
	}

	fun reset(destination: Destination.Fragment = defaultDestination) {
		_history.clear()
		navigate(destination)
	}
}

@Composable
@Stable
fun RouterBackHandler(router: Router) {
	val canGoBack by router.canGoBack.collectAsState()
	BackHandler(canGoBack, router::goBack)
}

@Composable
@Stable
fun RouterContent(router: Router) {
	val stateHolder = rememberSaveableStateHolder()
	val content by router.content.collectAsState()

	content?.let { content ->
		stateHolder.SaveableStateProvider(content.destination.fragment.qualifiedName.orEmpty()) {
			AndroidFragment(
				clazz = content.destination.fragment.java,
				arguments = content.destination.arguments,
			)
		}
	}
}

@Deprecated("Replaced with router")
/**
 * Repository for app navigation. This manages the screens/pages for the app.
 */
interface NavigationRepository {
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
	 * Reset navigation to the initial destination or a specific [Destination.Fragment].
	 *
	 * @param clearHistory Empty out the back stack
	 */
	fun reset(destination: Destination.Fragment? = null, clearHistory: Boolean)

	/**
	 * Reset navigation to the initial destination or a specific [Destination.Fragment] without clearing history.
	 */
	fun reset(destination: Destination.Fragment? = null) = reset(destination, false)
}

@Deprecated("Replaced with router")
class NavigationRepositoryImpl(
	private val router: Router,
) : NavigationRepository {
	override fun navigate(destination: Destination, replace: Boolean) {
		if (destination is Destination.Fragment) router.navigate(destination, replace)
	}

	override val canGoBack: Boolean get() = router.canGoBack.value
	override fun goBack(): Boolean = router.goBack()
	override fun reset(destination: Destination.Fragment?, clearHistory: Boolean) = router.reset(destination ?: router.defaultDestination)
}

