package org.jellyfin.androidtv.ui.quickswitch

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.model.PrivateUser
import org.jellyfin.androidtv.auth.model.Server
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.ServerButton
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.LocalTextStyle
import org.jellyfin.androidtv.ui.base.ProvideTextStyle
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.card.UserCard
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.settings.compat.SettingsViewModel
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.jellyfin.androidtv.ui.startup.StartupActivity
import org.jellyfin.androidtv.util.getActivity
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.api.client.extensions.imageApi
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
@Stable
fun QuickSwitchMenu(
	currentServer: Server,
	onOpenSettings: () -> Unit,
	onOpenServer: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Button(
			modifier = Modifier.fillMaxWidth(),
			onClick = onOpenSettings,
		) {
			Icon(
				painter = painterResource(R.drawable.ic_settings),
				contentDescription = null,
				modifier = Modifier.size(20.dp),
			)
			Spacer(Modifier.size(8.dp))
			Text("App settings")
		}

		ServerButton(
			icon = {
				Icon(
					painter = painterResource(R.drawable.ic_house_edit),
					contentDescription = null,
					modifier = Modifier.size(20.dp),
				)
			},
			name = { Text(currentServer.name) },
			address = { Text(currentServer.address) },
			version = { Text(currentServer.version.orEmpty()) },
			modifier = Modifier
				.fillMaxWidth()
				.height(51.dp),
			onClick = onOpenServer,
		)
	}
}

@Composable
fun QuickSwitchProfile(
	user: PrivateUser,
	onOpen: () -> Unit,
) {
	// TODO remove these next two dependencies
	val jellyfin = koinInject<Jellyfin>()
	val serverRepository = koinInject<ServerRepository>()

	val interactionSource = remember { MutableInteractionSource() }
	val focused by interactionSource.collectIsFocusedAsState()
	val scale by animateFloatAsState(if (focused) 1f else 0.9f, label = "QuickSwitchProfileScale")

	UserCard(
		interactionSource = interactionSource,
		image = {
			var imageUrl by remember { mutableStateOf<String?>(null) }
			LaunchedEffect(user) {
				imageUrl = if (user.imageTag == null) {
					null
				} else {
					val server = withContext(Dispatchers.IO) {
						serverRepository.getServer(user.serverId)
					}
					if (server == null) {
						null
					} else {
						jellyfin.createApi(server.address).imageApi.getUserImageUrl(
							userId = user.id,
							tag = user.imageTag
						)
					}
				}
			}
			if (imageUrl != null) {
				AsyncImage(
					modifier = Modifier.fillMaxSize(),
					url = imageUrl
				)
			} else {
				Box(modifier = Modifier.fillMaxSize()) {
					Image(
						painter = painterResource(R.drawable.ic_user),
						contentDescription = user.name,
						modifier = Modifier
							.size(48.dp)
							.align(Alignment.Center)
					)
				}
			}
		},
		name = {
			Text(user.name, maxLines = 1)
		},
		onClick = onOpen,
		modifier = Modifier
			.scale(scale)
			.width(110.dp),
	)
}

@Composable
@Stable
fun QuickSwitchProfileRow(
	users: List<PrivateUser>,
	onOpen: (user: PrivateUser) -> Unit,
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		modifier = Modifier
			.focusRestorer()
			.focusGroup()
	) {
		for (user in users) {
			QuickSwitchProfile(
				user = user,
				onOpen = { onOpen(user) }
			)
		}
	}
}

class QuickSwitchFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	) = content {
		val viewModel = koinViewModel<QuickSwitchViewModel>()
		val sessionRepository = koinInject<SessionRepository>()
		val settingsViewModel = koinActivityViewModel<SettingsViewModel>()
		val state by viewModel.state.collectAsState()
		val currentServer = state?.currentServer
		val recentUsers = state?.recentUsers.orEmpty()

		val backgroundService = koinInject<BackgroundService>()
		LaunchedEffect(backgroundService) { backgroundService.clearBackgrounds() }

		val context = LocalContext.current

		Column {
			MainToolbar(MainToolbarActiveButton.User)

			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(top = 27.dp)
					.padding(horizontal = 48.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				ProvideTextStyle(LocalTextStyle.current.copy(color = Color.White)) {
					if (recentUsers.size > 1) {
						Text("Quick switch", fontSize = 20.sp)
						Text("Showing the ${recentUsers.size} most recently used profiles", fontSize = 14.sp)

						Spacer(modifier = Modifier.height(16.dp))
						QuickSwitchProfileRow(
							users = recentUsers,
							onOpen = { user -> viewModel.switchUser(user) }
						)
						Spacer(modifier = Modifier.height(32.dp))
					}

					if (currentServer != null) {
						QuickSwitchMenu(
							currentServer = currentServer,
							onOpenSettings = { settingsViewModel.show() },
							onOpenServer = {
								val activity = context.getActivity()

								// TODO
//								mediaManager.clearAudioQueue()
								sessionRepository.destroyCurrentSession()

								// Open login activity
								val selectUserIntent = Intent(activity, StartupActivity::class.java)
								selectUserIntent.putExtra(StartupActivity.EXTRA_HIDE_SPLASH, true)
								// Remove history to prevent user going back to current activity
								selectUserIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

								activity?.startActivity(selectUserIntent)
								activity?.finishAfterTransition()
							},
							modifier = Modifier
								.width(300.dp),
						)
					}
				}
			}
		}
	}
}
