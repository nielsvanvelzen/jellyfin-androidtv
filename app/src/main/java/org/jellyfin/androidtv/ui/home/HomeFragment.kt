package org.jellyfin.androidtv.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.content
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.data.repository.NotificationsRepository
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.koin.compose.koinInject

@Composable
fun HomeScreen() {
	val rowsFocusRequester = remember { FocusRequester() }
	LaunchedEffect(rowsFocusRequester) { rowsFocusRequester.requestFocus() }

	val sessionRepository = koinInject<SessionRepository>()
	val serverRepository = koinInject<ServerRepository>()
	val notificationRepository = koinInject<NotificationsRepository>()
	val lifecycleOwner = LocalLifecycleOwner.current

	LaunchedEffect(Unit) {
		sessionRepository.currentSession
			.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
			.map { session ->
				if (session == null) null
				else serverRepository.getServer(session.serverId)
			}
			.onEach { server ->
				notificationRepository.updateServerNotifications(server)
			}
			.launchIn(lifecycleOwner.lifecycleScope)
	}

	Column {
		MainToolbar(MainToolbarActiveButton.Home)

		// The leanback code has its own awful focus handling that doesn't work properly with Compose view inteop to workaround this
		// issue we add custom behavior that only allows focus exit when the current selected row is the first one. Additionally when
		// we do switch the focus, we reset the leanback state so it won't cause weird behavior when focus is regained
		var rowsSupportFragment by remember { mutableStateOf<HomeRowsFragment?>(null) }
		AndroidFragment<HomeRowsFragment>(
			modifier = Modifier
				.focusGroup()
				.focusRequester(rowsFocusRequester)
				.focusProperties {
					onExit = {
						val isFirstRowSelected = rowsSupportFragment?.selectedPosition?.let { it <= 0 } ?: false
						if (requestedFocusDirection != FocusDirection.Up || !isFirstRowSelected) {
							cancelFocusChange()
						} else {
							rowsSupportFragment?.selectedPosition = 0
							rowsSupportFragment?.verticalGridView?.clearFocus()
						}
					}
				}
				.fillMaxSize(),
			onUpdate = { fragment ->
				rowsSupportFragment = fragment
			}
		)
	}
}

class HomeFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	) = content {
		HomeScreen()
	}
}
