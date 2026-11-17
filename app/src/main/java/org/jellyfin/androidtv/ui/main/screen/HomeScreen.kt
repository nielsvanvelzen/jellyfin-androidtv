package org.jellyfin.androidtv.ui.main.screen

import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.composable.item.ItemCard
import org.jellyfin.androidtv.ui.main.composable.Navigation
import org.jellyfin.androidtv.ui.main.composable.NavigationLayout
import org.jellyfin.androidtv.ui.shared.toolbar.ToolbarClock
import org.koin.compose.koinInject

@Composable
fun HomeScreen() {
	val serverRepository = koinInject<ServerRepository>()
	val backgroundService = koinInject<BackgroundService>()
	val currentServer by serverRepository.currentServer.collectAsState()

	LaunchedEffect(backgroundService, currentServer) {
		if (currentServer != null) backgroundService.setBackground(currentServer!!)
		else backgroundService.clearBackgrounds()
	}

	AppBackground()

	NavigationLayout(
		navigation = { Navigation(initialFocus = true) },
	) {
		Column(
			modifier = Modifier
				.focusRestorer()
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
			.focusRestorer()
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
