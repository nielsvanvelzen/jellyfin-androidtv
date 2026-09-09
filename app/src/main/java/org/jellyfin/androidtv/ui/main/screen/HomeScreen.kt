package org.jellyfin.androidtv.ui.main.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.composable.item.ItemCard
import org.jellyfin.androidtv.ui.main.Routes
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.compat.SettingsViewModel
import org.jellyfin.androidtv.ui.shared.toolbar.ToolbarClock
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
fun NavigationLayout(
	navigation: @Composable ColumnScope.() -> Unit,
	content: @Composable BoxScope.() -> Unit,
) {
	val navigationFocusRequester = remember { FocusRequester() }

	Row(
		modifier = Modifier
			.focusRestorer(navigationFocusRequester)
	) {
		var navigationFocused by remember { mutableStateOf(false) }
		val navigationWidth by animateDpAsState(if (navigationFocused) 200.dp else 64.dp)

		// Nav
		Column(
			modifier = Modifier
				.background(JellyfinTheme.colorScheme.surface)
				.fillMaxHeight()
				.width(navigationWidth)
				.focusGroup()
				.onFocusChanged {
					navigationFocused = it.hasFocus
				},
			content = navigation,
		)

		// Content
		Box(
			modifier = Modifier
				.focusGroup()
				.focusRequester(navigationFocusRequester),
			content = content,
		)
	}
}

@Composable
fun HomeScreen() {
	val router = LocalRouter.current
	val settingsViewModel = koinActivityViewModel<SettingsViewModel>()
	val serverRepository = koinInject<ServerRepository>()
	val backgroundService = koinInject<BackgroundService>()
	val currentServer by serverRepository.currentServer.collectAsState()

	LaunchedEffect(currentServer) {
		if (currentServer != null) backgroundService.setBackground(currentServer!!)
		else backgroundService.clearBackgrounds()
	}

	NavigationLayout(
		navigation = {
			Image(
				painter = painterResource(R.drawable.ic_jellyfin),
				contentDescription = null,
				modifier = Modifier
					.size(64.dp)
					.padding(8.dp)
			)

			Column(
				verticalArrangement = Arrangement.spacedBy(4.dp),
				modifier = Modifier
					.padding(4.dp)
			) {
				Button(
					onClick = { router.push(Routes.SEARCH) },
					colors = ButtonDefaults.colors(containerColor = Color.Transparent),
					modifier = Modifier.fillMaxWidth()
				) {
					Icon(
						painter = painterResource(R.drawable.ic_search),
						contentDescription = stringResource(R.string.lbl_search),
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(text = stringResource(R.string.lbl_search), softWrap = false, overflow = TextOverflow.Ellipsis)
				}

				Button(
					onClick = { router.push(Routes.MAIN) },
					colors = ButtonDefaults.colors(containerColor = Color.Transparent),
					modifier = Modifier.fillMaxWidth()
				) {
					Icon(
						painter = painterResource(R.drawable.ic_house),
						contentDescription = stringResource(R.string.lbl_home),
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(text = stringResource(R.string.lbl_home), softWrap = false, overflow = TextOverflow.Ellipsis)
				}

				Spacer(Modifier.weight(1f))

				Button(
					onClick = { settingsViewModel.show() },
					colors = ButtonDefaults.colors(containerColor = Color.Transparent),
					modifier = Modifier.fillMaxWidth()
				) {
					Icon(
						painter = painterResource(R.drawable.ic_settings),
						contentDescription = stringResource(R.string.settings),
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(text = stringResource(R.string.settings), softWrap = false, overflow = TextOverflow.Ellipsis)
				}
			}
		},
	) {
		AppBackground()

		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
				.padding(0.dp, 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			Row(modifier = Modifier.padding(16.dp, 0.dp)) {
				Text(
					text = "Welcome back, Niels",
					color = JellyfinTheme.colorScheme.listHeader,
					style = JellyfinTheme.typography.listHeadline
				)
				Spacer(Modifier.weight(1f))
				ToolbarClock()
			}

			repeat(3) {
				Text(
					text = "Libraries",
					color = JellyfinTheme.colorScheme.listHeader,
					style = JellyfinTheme.typography.listHeader,
					modifier = Modifier.padding(24.dp, 0.dp)
				)
				ItemCards()
			}
		}
	}
}

@Composable
fun ItemCards() {
	Row(
		modifier = Modifier
			.horizontalScroll(rememberScrollState())
			.padding(16.dp, 0.dp),
		horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		repeat(50) {
			var focused by remember { mutableStateOf(false) }

			ItemCard(
				modifier = Modifier
					.height(150.dp)
					.aspectRatio(16f / 9f)
					.onFocusChanged { focused = it.hasFocus }
					.focusable(),
				image = {
					if (focused) Text("Focused!!", fontSize = 26.sp, color = Color.White)
				},
			)
		}
	}
}
