package org.jellyfin.androidtv.ui.quickswitch

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.auth.model.PrivateUser
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.ServerUserRepository
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.LocalTextStyle
import org.jellyfin.androidtv.ui.base.ProvideTextStyle
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.navigation.ActivityDestinations
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActivebutton
import org.jellyfin.androidtv.ui.startup.StartupActivity
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.androidtv.util.getActivity
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.api.client.extensions.imageApi
import org.koin.compose.koinInject

@Composable
fun QuickSwitchMenu(
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val sessionRepository = koinInject<SessionRepository>()
	val mediaManager = koinInject<MediaManager>()

	Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Button(
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				context.startActivity(ActivityDestinations.userPreferences(context))
			}
		) {
			Text("App settings")
		}

		Button(
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				val activity = context.getActivity()

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
		) {
			Text("Switch server")
		}
	}
}

@Composable
fun ProfileButton(
	image: @Composable () -> Unit,
	name: @Composable () -> Unit,
	modifier: Modifier = Modifier,
	shape: Shape = CircleShape,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val focused by interactionSource.collectIsFocusedAsState()

	// Mix button background with input foreground because we display text beneath the profile picture on a transparent background similar
	// to the input text
	val color = when {
		focused -> JellyfinTheme.colorScheme.buttonFocused to JellyfinTheme.colorScheme.onInputFocused
		else -> JellyfinTheme.colorScheme.button to JellyfinTheme.colorScheme.onInput
	}

	// TODO Use ButtonBase?
	Column(
		modifier = modifier
			.focusable(interactionSource = interactionSource)
			.clickable(interactionSource = interactionSource, onClick = {}, indication = null)
	) {
		Box(
			modifier = Modifier
				.aspectRatio(1f)
				.clip(shape)
				.border(2.dp, color.first, shape)
		) {
			image()
		}

		Spacer(Modifier.height(8.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.basicMarquee(
					iterations = if (focused) Int.MAX_VALUE else 0,
					initialDelayMillis = 0,
				),
			contentAlignment = Alignment.TopCenter,
		) {
			ProvideTextStyle(
				LocalTextStyle.current.copy(
					color = color.second,
				)
			) {
				name()
			}
		}
	}
}

@Composable
fun QuickSwitchProfileRow(
	users: List<PrivateUser>,
) {
	val jellyfin = koinInject<Jellyfin>()
	val serverRepository = koinInject<ServerRepository>()

	Row(
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		modifier = Modifier
			.focusRestorer()
			.focusGroup()
	) {
		var selectedIndex by remember { mutableIntStateOf(-1) }
		users.forEachIndexed { index, user ->
			val scale by animateFloatAsState(if (selectedIndex == index) 1f else 0.9f, label = "ProfileScaleAnimation$index")
			ProfileButton(
				modifier = Modifier
					.onFocusChanged {
						if (it.isFocused) selectedIndex = index
						else if (selectedIndex == index) selectedIndex = -1
					}
					.scale(scale)
					.width(110.dp),
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
									tag = user.imageTag,
									maxHeight = ImageHelper.MAX_PRIMARY_IMAGE_HEIGHT
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
						Box(
							Modifier
								.background(Color.Red)
								.fillMaxSize()
						)
					}
				},

				name = {
					Text(user.name, maxLines = 1)
				},
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
		val backgroundService = koinInject<BackgroundService>()
		val sessionRepository = koinInject<SessionRepository>()
		val serverRepository = koinInject<ServerRepository>()
		val serverUserRepository = koinInject<ServerUserRepository>()

		val server by remember {
			sessionRepository.currentSession
				.map { it?.serverId }
				.distinctUntilChanged()
				.map { serverId -> if (serverId != null) serverRepository.getServer(serverId) else null }
		}.collectAsState(null)

		LaunchedEffect(backgroundService, server) {
			if (server == null) backgroundService.clearBackgrounds()
			else backgroundService.setBackground(server!!)

			// TODO decide whether to use server splash or not
			backgroundService.clearBackgrounds()
		}

		val recentUsers = remember(serverUserRepository) { serverUserRepository.getRecentlyUsedUsers().take(3) }

		Column {
			MainToolbar(MainToolbarActivebutton.USER)

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

						Box(modifier = Modifier.height(16.dp))
						QuickSwitchProfileRow(recentUsers)
						Box(modifier = Modifier.height(32.dp))
					}

					QuickSwitchMenu(
						modifier = Modifier
							.width(300.dp)
					)
				}
			}
		}
	}
}
