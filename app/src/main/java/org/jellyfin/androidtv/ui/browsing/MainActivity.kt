package org.jellyfin.androidtv.ui.browsing

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.FragmentState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.ui.ScreensaverViewModel
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.navigation.NavigationAction
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.screensaver.InAppScreensaver
import org.jellyfin.androidtv.ui.startup.StartupActivity
import org.jellyfin.androidtv.ui.startup.fragment.SplashScreen
import org.jellyfin.androidtv.util.applyTheme
import org.jellyfin.androidtv.util.isMediaSessionKeyEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject
import timber.log.Timber

@Composable
@Stable
private fun NavigationBackHandler() {
	val navigationRepository = koinInject<NavigationRepository>()
	var backHandlerEnabled by remember { mutableStateOf(navigationRepository.canGoBack) }

	LaunchedEffect(navigationRepository) {
		navigationRepository.currentAction.onEach {
			backHandlerEnabled = navigationRepository.canGoBack
		}.launchIn(this)
	}

	BackHandler(backHandlerEnabled) {
		if (navigationRepository.canGoBack) {
			navigationRepository.goBack()
		}
	}
}

@Composable
@Stable
private fun NavigationContent() {
	val navigationRepository = koinInject<NavigationRepository>()
	val statefulDestination by remember {
		navigationRepository.currentAction
			.filterIsInstance<NavigationAction.NavigateFragment>()
			.map { it.destination }
			.distinctUntilChanged()
			.map { it to FragmentState() }
	}.collectAsState(null)

	Text(statefulDestination.toString(), color = Color.Red)

	statefulDestination?.let { (destination, state) ->
		key(destination) {
			AndroidFragment(
				clazz = destination.fragment.java,
				arguments = destination.arguments,
				fragmentState = state,
			)
		}
	}
}

class MainActivity : FragmentActivity() {
	private val navigationRepository by inject<NavigationRepository>()
	private val sessionRepository by inject<SessionRepository>()
	private val userRepository by inject<UserRepository>()
	private val screensaverViewModel by viewModel<ScreensaverViewModel>()
	private val sessionActive = combine(
		sessionRepository.currentSession,
		userRepository.currentUser
	) { session, user ->
		session != null && user != null
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Screen lock for custom screensaver
		screensaverViewModel.keepScreenOn.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
			.onEach { keepScreenOn ->
				if (keepScreenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
				else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
			}.launchIn(lifecycleScope)

		// Navigation reset on app relaunch
		if (savedInstanceState == null && navigationRepository.canGoBack) navigationRepository.reset()

		setContent {
			NavigationBackHandler()

			// App content
			Box {
				AppBackground()

				val sessionActive by sessionActive.collectAsState(false)
				if (sessionActive) {
					NavigationContent()
					InAppScreensaver()
				} else {
					SplashScreen()
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()

		applyTheme()

		if (sessionRepository.currentSession.value == null || userRepository.currentUser.value == null) {
			Timber.w("Activity ${this::class.qualifiedName} started without a session, bouncing to StartupActivity")
			startActivity(Intent(this, StartupActivity::class.java))
			finish()
		}

		screensaverViewModel.activityPaused = false
	}

	// Forward key events to fragments
	private fun Fragment.onKeyEvent(keyCode: Int, event: KeyEvent?): Boolean {
		var result = childFragmentManager.fragments.any { it.onKeyEvent(keyCode, event) }
		if (!result && this is View.OnKeyListener) result = onKey(currentFocus, keyCode, event)
		return result
	}

	private fun onKeyEvent(keyCode: Int, event: KeyEvent?): Boolean {
		// Ignore the key event that closes the screensaver
		if (screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(canCancel = event?.action == KeyEvent.ACTION_UP)
			return true
		}

		return supportFragmentManager.fragments
			.any { it.onKeyEvent(keyCode, event) }
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
		onKeyEvent(keyCode, event) || super.onKeyDown(keyCode, event)

	override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean =
		onKeyEvent(keyCode, event) || super.onKeyUp(keyCode, event)

	override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean =
		onKeyEvent(keyCode, event) || super.onKeyUp(keyCode, event)

	override fun onUserInteraction() {
		super.onUserInteraction()

		screensaverViewModel.notifyInteraction(false)
	}

	@Suppress("RestrictedApi") // False positive
	override fun dispatchKeyEvent(event: KeyEvent): Boolean {
		// Ignore the key event that closes the screensaver
		if (!event.isMediaSessionKeyEvent() && screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(canCancel = event.action == KeyEvent.ACTION_UP)
			return true
		}

		@Suppress("RestrictedApi") // False positive
		return super.dispatchKeyEvent(event)
	}

	@Suppress("RestrictedApi") // False positive
	override fun dispatchKeyShortcutEvent(event: KeyEvent): Boolean {
		// Ignore the key event that closes the screensaver
		if (!event.isMediaSessionKeyEvent() && screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(canCancel = event.action == KeyEvent.ACTION_UP)
			return true
		}

		@Suppress("RestrictedApi") // False positive
		return super.dispatchKeyShortcutEvent(event)
	}

	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		// Ignore the touch event that closes the screensaver
		if (screensaverViewModel.visible.value) {
			screensaverViewModel.notifyInteraction(true)
			return true
		}

		return super.dispatchTouchEvent(ev)
	}
}
