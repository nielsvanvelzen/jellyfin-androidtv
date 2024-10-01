package org.jellyfin.androidtv.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.data.repository.NotificationsRepository
import org.jellyfin.androidtv.databinding.FragmentHomeBinding
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.startup.StartupActivity
import org.jellyfin.androidtv.util.ImageHelper
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
	onSwitchUser: () -> Unit,
) {
	val navigationRepository = koinInject<NavigationRepository>()
	val contentFocusRequester = remember { FocusRequester() }

	MaterialTheme(
		colorScheme = darkColorScheme()
	) {
		Surface(
			modifier = Modifier.focusRestorer(contentFocusRequester),
		) {
			var selectedIndex by remember { mutableIntStateOf(0) }
			val items = listOf(
				"Home" to Icons.Default.Home,
				"Favorites" to Icons.Default.Favorite,
				"Search" to Icons.Default.Search,
				"Settings" to Icons.Default.Settings,
			)

			NavigationDrawer(
				drawerContent = {
					Column(
						Modifier
							.fillMaxHeight()
							.padding(12.dp)
							.selectableGroup(),
						horizontalAlignment = Alignment.Start,
						verticalArrangement = Arrangement.spacedBy(10.dp)
					) {
						items.forEachIndexed { index, item ->
							val (text, icon) = item
							NavigationDrawerItem(
								selected = selectedIndex == index,
								onClick = { selectedIndex = index },
								leadingContent = {
									Icon(
										imageVector = icon,
										contentDescription = null,
									)
								}
							) {
								Text(text)
							}
						}
					}
				},
				content = {
					Box(
						modifier = Modifier
							.focusGroup()
							.focusRequester(contentFocusRequester)
					) {
						AndroidFragment<HomeRowsFragment>()
					}
				}
			)
		}
	}
}

class HomeFragment : Fragment() {
	private val sessionRepository by inject<SessionRepository>()
	private val mediaManager by inject<MediaManager>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			HomeScreen(
				onSwitchUser = ::switchUser
			)
		}
	}

	private fun switchUser() {
		mediaManager.clearAudioQueue()
		sessionRepository.destroyCurrentSession()

		// Open login activity
		val selectUserIntent = Intent(activity, StartupActivity::class.java)
		selectUserIntent.putExtra(StartupActivity.EXTRA_HIDE_SPLASH, true)
		// Remove history to prevent user going back to current activity
		selectUserIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

		activity?.startActivity(selectUserIntent)
		activity?.finishAfterTransition()
	}
}

class HomeFragment2 : Fragment() {
	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!

	private val sessionRepository by inject<SessionRepository>()
	private val userRepository by inject<UserRepository>()
	private val serverRepository by inject<ServerRepository>()
	private val notificationRepository by inject<NotificationsRepository>()
	private val navigationRepository by inject<NavigationRepository>()
	private val mediaManager by inject<MediaManager>()
	private val imageHelper by inject<ImageHelper>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		_binding = FragmentHomeBinding.inflate(inflater, container, false)

		binding.settings.setOnClickListener {
			navigationRepository.navigate(Destinations.userPreferences)
		}

		binding.switchUsers.setOnClickListener {
			switchUser()
		}

		binding.search.setOnClickListener {
			navigationRepository.navigate(Destinations.search())
		}

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		userRepository.currentUser
			.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
			.onEach { user ->
				if (user != null) {
					binding.switchUsersImage.load(
						url = imageHelper.getPrimaryImageUrl(user),
						placeholder = ContextCompat.getDrawable(requireContext(), R.drawable.ic_user)
					)
				}
			}
			.launchIn(viewLifecycleOwner.lifecycleScope)

		sessionRepository.currentSession
			.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
			.map { session ->
				if (session == null) null
				else serverRepository.getServer(session.serverId)
			}
			.onEach { server ->
				notificationRepository.updateServerNotifications(server)
			}
			.launchIn(viewLifecycleOwner.lifecycleScope)
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}

	private fun switchUser() {
		mediaManager.clearAudioQueue()
		sessionRepository.destroyCurrentSession()

		// Open login activity
		val selectUserIntent = Intent(activity, StartupActivity::class.java)
		selectUserIntent.putExtra(StartupActivity.EXTRA_HIDE_SPLASH, true)
		// Remove history to prevent user going back to current activity
		selectUserIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

		activity?.startActivity(selectUserIntent)
		activity?.finishAfterTransition()
	}
}
