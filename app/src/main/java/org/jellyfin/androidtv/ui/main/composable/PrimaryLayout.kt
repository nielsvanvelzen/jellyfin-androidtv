package org.jellyfin.androidtv.ui.main.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import org.jellyfin.androidtv.ui.navigation.LocalRouterTransitionScope
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton

@Composable
fun PrimaryLayout(
	activeButton: MainToolbarActiveButton = MainToolbarActiveButton.None,
	content: @Composable BoxScope.() -> Unit,
) {
	val toolbar = remember {
		movableContentOf<MainToolbarActiveButton, Modifier> { activeButton, modifier ->
			MainToolbar(
				activeButton = activeButton,
				modifier = modifier
			)
		}
	}

	Column {
		val transitionScope = LocalRouterTransitionScope.current
		val animatedVisibilityScope = LocalNavAnimatedContentScope.current

		with(transitionScope) {
			toolbar(
				activeButton,
				Modifier.sharedElement(rememberSharedContentState("toolbar"), animatedVisibilityScope)
			)
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.weight(1f),
		) {
			content()
		}
	}
}
